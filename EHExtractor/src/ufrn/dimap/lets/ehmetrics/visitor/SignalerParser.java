package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;

import ufrn.dimap.lets.ehmetrics.abstractmodel.SignalerType;
import ufrn.dimap.lets.ehmetrics.analyzer.UnknownSignalerException;

/**
 * Parses a ThrowStmt and stores relevant data for consumption.
 * 
 * Supported signalers patterns:
 * throw e
 * throw new <ExceptionType> ( <args> )
 * throw e.getCause()
 * 
 * */
public class SignalerParser
{
	private ThrowStmt throwStatement;
	
	private boolean parsed;
	
	private SignalerType type;

	// Throw types. If the type can be resolved, the optional is present
	private Optional<ResolvedClassDeclaration> thrownClassDeclaration;
	private ClassOrInterfaceType thrownClassType;
	
	// For "throw e" signalers
	private SimpleName simpleName;
	
	// For "throw new Exception (arguments)" signalers
	private NodeList<Expression> argumentsInObjectCreation;
	
	public SignalerParser(ThrowStmt throwStatement)
	{
		this.throwStatement = throwStatement;
		this.parsed = false;
	}
	
	/**
	 * Parsear o c�digo de um sinalizar e salva, no objeto, informa��es �teis para consumo.
	 * */
	public void parse ()
	{
		init();
		
		this.resolveClassDeclaration();
		
		Expression throwExpression = this.throwStatement.getExpression();
		
		/* o statement � um "throw e". Provavelmente � um rethrow, mas � poss�vel que a exce��o tenha
		 * sido instanciada previamente e agora est� sendo lan�ada. Isso n�o faz diferen�a neste momento
		 */
		if ( throwExpression instanceof NameExpr )
		{
			this.type = SignalerType.SIMPLE_NAME;
			this.simpleName = throwExpression.asNameExpr().getName();
			
			//ResolvedValueDeclaration declaration = throwExpression.asNameExpr().resolve();
			//this.thrownClassType = findVariableType (declaration);
		}
		// O statement � um "throw new...".
		else if ( throwExpression instanceof ObjectCreationExpr )
		{
			ObjectCreationExpr objectCreationExp = throwExpression.asObjectCreationExpr();
			this.type = SignalerType.OBJECT_CREATION;
			
			this.argumentsInObjectCreation = objectCreationExp.getArguments();
		}
		// O statement � uma chamada de m�todo. Verificar se � um e.getCause()
		else if (throwExpression instanceof MethodCallExpr)
		{
			MethodCallExpr methodCallExpression = throwExpression.asMethodCallExpr();
			
			System.out.println(methodCallExpression);
			
			// if e.getCause
			// this.type = SignalerType.GET_CAUSE
			// sen�o, throw exce��o
		}
		// O statement � um (Exception) e
		else if (throwExpression instanceof CastExpr)
		{
			this.type = SignalerType.CAST;
			
			CastExpr castExpression = throwExpression.asCastExpr();

			if ( castExpression.getType() instanceof ClassOrInterfaceType )
			{
				this.thrownClassType = castExpression.getType().asClassOrInterfaceType();
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
	
	private void init()
	{
		type = null;

		thrownClassDeclaration = null;
		thrownClassType = null;
		
		simpleName = null;
		
		argumentsInObjectCreation = null;
	}

	/**
	 * Tries to resolve the thrown class declaration.
	 * 
	 * This resolution is optional, so the failure can be ignored because the ReferenceType will be used instead.
	 * */
	private void resolveClassDeclaration ()
	{
		try
		{
			this.thrownClassDeclaration = Optional.of(throwStatement.getExpression().calculateResolvedType().asReferenceType().getTypeDeclaration().asClass());
		}
		catch ( UnsolvedSymbolException e )
		{
			this.thrownClassDeclaration = Optional.empty();
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
	
	public SignalerType getType() {
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		else
			return type;
	}

	public Optional<ResolvedClassDeclaration> getThrownClassDeclaration()
	{
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		else
			return this.thrownClassDeclaration;
	}

	public ClassOrInterfaceType getThrownClassType() {
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		else if ( this.thrownClassDeclaration != null )
			throw new IllegalStateException("The thrown type was resolved. Use 'getThrownClassDeclaration' method instead.");
		else
			return this.thrownClassType;
	}

	public NodeList<Expression> getArgumentsInObjectCreation()
	{
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		if ( this.type != SignalerType.OBJECT_CREATION )
			throw new IllegalStateException("Throw is not a object creation expression.");
		else
			return argumentsInObjectCreation;
	}

	public SimpleName getSimpleName() {
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		if ( this.type != SignalerType.SIMPLE_NAME )
			throw new IllegalStateException("Throw is not a simple name.");
		return this.simpleName;
	}

	
}
