package ufrn.dimap.lets.ehmetrics.visitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.SignalerType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeHierarchy;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.analyzer.UnknownSignalerException;

/**
 * Visitor para verificar o guideline "Convert library exceptions".
 * 
 * Para confirmar o guideline a seguinte heur�stica � usada:
 * 95% de todas as exce��es externas que s�o re-sinalizadas sofrem uma convers�o antes.
 * */
public class ConvertLibraryExceptionsVisitor extends VoidVisitorAdapter<Void> {

	public TypeHierarchy typeHierarchy;
	private File javaFile; // Java file being parsed
	private Stack <SimpleName> handledExceptionsNames;
	
	public ConvertLibraryExceptionsVisitor ()
	{
		this.typeHierarchy = new TypeHierarchy();
		this.javaFile = null;
		this.handledExceptionsNames = new Stack<>();
	}

	/*
	@Override
	public void visit (ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Void arg)
	{		
		ResolvedReferenceTypeDeclaration referenceTypeDeclaration = classOrInterfaceDeclaration.resolve();

		if ( referenceTypeDeclaration.isClass() )
		{	
			Type type = this.typeHierarchy.findOrCreateResolvedType(referenceTypeDeclaration.asClass());
			type.setFile(javaFile);
			type.setNode(classOrInterfaceDeclaration);
		}


		//VISIT CHILDREN
		super.visit(classOrInterfaceDeclaration, arg);
	}
	*/

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = new Handler();
		newHandler.setFile(javaFile);
		newHandler.setNode(catchClause);

		List<ClassOrInterfaceType> types = VisitorUtil.getHandledTypes(catchClause);

		processCaughtTypes (types, newHandler);
		
		this.handledExceptionsNames.push(catchClause.getParameter().getName());

		// VISIT CHILDREN
		super.visit(catchClause, arg);
		
		this.handledExceptionsNames.pop();
	}

	@Override
	public void visit ( SimpleName name, Void arg )
	{
		if ( this.handledExceptionsNames.contains(name) )
		
		super.visit(name, arg);
	}
	
	private void processCaughtTypes(List<ClassOrInterfaceType> types, Handler handler)
	{
		Type caughtType;

		for ( ClassOrInterfaceType t : types )
		{
			try
			{
				caughtType = this.typeHierarchy.findOrCreateResolvedType(t.resolve().getTypeDeclaration().asClass());

				/* If this type already exists in the hierarchy, was create by its declaration and
				 * dont had they ancestors resolved, it has the ClassType.UNRESOLVED. For being used
				 * in a throw or catch, we know this type is a UNRESOLVED_EXCEPTION.
				 */
				if ( caughtType.getClassType() == ClassType.UNRESOLVED )
				{
					caughtType.setClassType(ClassType.UNRESOLVED_EXCEPTION);
				}

				handler.getExceptions().add( caughtType );
				caughtType.addHandler(handler);

			}
			catch ( UnsolvedSymbolException e )
			{
				caughtType = this.typeHierarchy.findOrCreateUnresolvedType (t);

				handler.getExceptions().add( caughtType );
				caughtType.addHandler(handler);
			}
		}
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		Expression throwExpression = throwStatement.getExpression();

		Signaler newSignaler = new Signaler();
		newSignaler.setFile(javaFile);
		newSignaler.setNode(throwStatement);

		Type thrownType = null;

		try
		{
			thrownType = this.typeHierarchy.findOrCreateResolvedType(throwExpression.calculateResolvedType().asReferenceType().getTypeDeclaration().asClass());

			/* If this type already exists in the hierarchy, and it was create by its declaration, and it
			 * dont had they ancestors resolved, it has the ClassType.UNRESOLVED. For being used
			 * in a throw or catch, we know this type is a UNRESOLVED_EXCEPTION.
			 */
			if ( thrownType.getClassType() == ClassType.UNRESOLVED )
			{
				thrownType.setClassType(ClassType.UNRESOLVED_EXCEPTION);
			}

			newSignaler.setThrownType( thrownType );
			thrownType.addSignaler(newSignaler);

		}
		catch ( UnsolvedSymbolException e )
		{
			ClassOrInterfaceType classOrInterfaceType = parseThrow (throwExpression);

			thrownType = this.typeHierarchy.findOrCreateUnresolvedType (classOrInterfaceType);

			newSignaler.setThrownType( thrownType );
			thrownType.addSignaler(newSignaler);
		}

		// VISIT CHILDREN
		super.visit(throwStatement, arg);
	}

	/**
	 * Tenta parsear o c�digo de um "throw" para identificar o tipo sinalizado.
	 * 
	 * Este m�todo � usado quando o tipo do throw n�o � resolvido pelo JavaParser.
	 * */
	private ClassOrInterfaceType parseThrow (Expression throwExpression)
	{
		// o statement � um "throw e". Provavelmente � um rethrow, mas � poss�vel que a exce��o tenha sido instanciada previamente e agora est� sendo lan�ada.
		if ( throwExpression instanceof NameExpr )
		{
			try
			{
				ResolvedValueDeclaration declaration = throwExpression.asNameExpr().resolve();

				return findVariableType (declaration);
			}
			catch (UnsolvedSymbolException e)
			{
				throw new UnknownSignalerException ("Sinaliza��o de NameExpr cuja declara��o n�o foi encontrada. Sinaliza��o:\n\n'" + throwExpression + "'.\n\n", e);
			}
		}
		// O statement � um "throw new...".
		else if ( throwExpression instanceof ObjectCreationExpr )
		{
			ObjectCreationExpr objectCreationExp = throwExpression.asObjectCreationExpr();

			return objectCreationExp.getType();
		}
		else if (throwExpression instanceof CastExpr)
		{
			CastExpr castExpression = throwExpression.asCastExpr();

			if ( castExpression.getType() instanceof ClassOrInterfaceType )
			{
				return castExpression.getType().asClassOrInterfaceType();
			}
			else
			{
				throw new UnknownSignalerException("Sinaliza��o com cast n�o suportada. Sinaliza��o:\n\n'" + throwExpression + "'.\n\n");
			}
		}
		else
		{
			throw new UnknownSignalerException ("A sinaliza��o n�o � de um dos tipos suportados. Sinaliza��o: '" + throwExpression + "'.");
		}
	}

	/**
	 * Dada uma vari�vel no c�digo, tenta resolver o seu tipo.
	 * 
	 * Suporta vari�veis de m�todos, atributos de classes, parametros de m�todos e parametros de catch block.
	 * */
	private ClassOrInterfaceType findVariableType (ResolvedValueDeclaration resolvedValueDeclaration)
	{		
		// Uma vari�vel declarada no corpo de um m�todo
		if ( resolvedValueDeclaration instanceof JavaParserSymbolDeclaration )
		{
			VariableDeclarator declarator = (VariableDeclarator) ((JavaParserSymbolDeclaration)resolvedValueDeclaration).getWrappedNode();

			return declarator.getType().asClassOrInterfaceType();
		}
		// Uma vari�vel que � parametro de um m�todo ou em um catch block
		else if ( resolvedValueDeclaration instanceof JavaParserParameterDeclaration )
		{
			Parameter parameter = ((JavaParserParameterDeclaration)resolvedValueDeclaration).getWrappedNode();

			return parameter.getType().asClassOrInterfaceType();
		}
		// Uma vari�vel que � atributo da classe
		else if ( resolvedValueDeclaration instanceof JavaParserFieldDeclaration )
		{
			FieldDeclaration fieldDeclaration = ((JavaParserFieldDeclaration)resolvedValueDeclaration).getWrappedNode();

			return fieldDeclaration.getVariable(0).getType().asClassOrInterfaceType();
		}
		else
		{
			throw new UnsupportedOperationException("Formato n�o suportado - v� o stack trace ai");
		}
	}

	public void setJavaFile (File javaFile)
	{
		this.javaFile = javaFile;
	}

	
	/**
	 * Verifica se o projeto adota o guideline referenciado neste visitor.
	 * 
	 * Para entender as condi��es do guideline, ver Javadoc da classe
	 * */
	public void checkGuidelineConformance ()
	{	
		long numberOfSystemExceptionTypes = this.typeHierarchy.listTypes().stream()
				.filter(Type::isSystemExceptionType)
				.count();
		System.out.println("Number of custom exceptions: " + numberOfSystemExceptionTypes);
		
		
		Optional<Long> mostSubtypedSystemException = this.typeHierarchy.listTypes().stream()
			.filter(Type::isSystemExceptionType)
			.map(Type::getSystemExceptionRootType)
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
			.values()
			.stream()
			.max(Comparator.naturalOrder());
		
		
		if ( numberOfSystemExceptionTypes!= 0 && mostSubtypedSystemException.isPresent() )
		{
			System.out.println("'Define a super type' conformance: " + 1.0*mostSubtypedSystemException.get()/numberOfSystemExceptionTypes);
		}
		else
		{
			System.out.println("'Define a super type' conformance: 0.0");
		}	
	}
}