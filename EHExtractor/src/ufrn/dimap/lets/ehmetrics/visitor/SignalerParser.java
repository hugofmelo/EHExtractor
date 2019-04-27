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
	 * Parsear o código de um sinalizar e salva, no objeto, informações úteis para consumo.
	 * */
	public void parse ()
	{
		init();
		
		this.resolveClassDeclaration();
		
		Expression throwExpression = this.throwStatement.getExpression();
		
		/* o statement é um "throw e". Provavelmente é um rethrow, mas é possível que a exceção tenha
		 * sido instanciada previamente e agora está sendo lançada. Isso não faz diferença neste momento
		 */
		if ( throwExpression instanceof NameExpr )
		{
			this.type = SignalerType.SIMPLE_NAME;
			this.simpleName = throwExpression.asNameExpr().getName();
			
			//ResolvedValueDeclaration declaration = throwExpression.asNameExpr().resolve();
			//this.thrownClassType = findVariableType (declaration);
		}
		// O statement é um "throw new...".
		else if ( throwExpression instanceof ObjectCreationExpr )
		{
			ObjectCreationExpr objectCreationExp = throwExpression.asObjectCreationExpr();
			this.type = SignalerType.OBJECT_CREATION;
			
			this.argumentsInObjectCreation = objectCreationExp.getArguments();
		}
		// O statement é uma chamada de método. Verificar se é um e.getCause()
		else if (throwExpression instanceof MethodCallExpr)
		{
			MethodCallExpr methodCallExpression = throwExpression.asMethodCallExpr();
			
			System.out.println(methodCallExpression);
			
			// if e.getCause
			// this.type = SignalerType.GET_CAUSE
			// senão, throw exceção
		}
		// O statement é um (Exception) e
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
				throw new UnknownSignalerException("Sinalização com cast não suportada. Sinalização:\n\n'" + throwExpression + "'.\n\n");
			}
		}
		else
		{
			throw new UnknownSignalerException ("A sinalização não é de um dos tipos suportados. Sinalização: '" + throwExpression + "'.");
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
	 * Dada uma variável no código, tenta resolver o seu tipo.
	 * 
	 * Suporta variáveis de métodos, atributos de classes, parametros de métodos e parametros de catch block.
	 * */
	private ClassOrInterfaceType findVariableType (ResolvedValueDeclaration resolvedValueDeclaration)
	{		
		// Uma variável declarada no corpo de um método
		if ( resolvedValueDeclaration instanceof JavaParserSymbolDeclaration )
		{
			VariableDeclarator declarator = (VariableDeclarator) ((JavaParserSymbolDeclaration)resolvedValueDeclaration).getWrappedNode();

			return declarator.getType().asClassOrInterfaceType();
		}
		// Uma variável que é parametro de um método ou em um catch block
		else if ( resolvedValueDeclaration instanceof JavaParserParameterDeclaration )
		{
			Parameter parameter = ((JavaParserParameterDeclaration)resolvedValueDeclaration).getWrappedNode();

			return parameter.getType().asClassOrInterfaceType();
		}
		// Uma variável que é atributo da classe
		else if ( resolvedValueDeclaration instanceof JavaParserFieldDeclaration )
		{
			FieldDeclaration fieldDeclaration = ((JavaParserFieldDeclaration)resolvedValueDeclaration).getWrappedNode();

			return fieldDeclaration.getVariable(0).getType().asClassOrInterfaceType();
		}
		else
		{
			throw new UnsupportedOperationException("Formato não suportado - vê o stack trace ai");
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
