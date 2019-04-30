package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.SignalerType;
import ufrn.dimap.lets.ehmetrics.analyzer.UnknownSignalerException;

/**
 * Parses a ThrowStmt, stores relevant data for consumption e define methods to check rethrown,
 * wrapping and unwrapping. Must call "parse" method before accessing any data.
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
	private List<CatchClause> catchesInContext;
	
	private boolean parsed;
	
	private SignalerType type;

	// Throw types. If the type can be resolved, the optional is present
	private Optional<ResolvedClassDeclaration> thrownClassDeclaration;
	private ClassOrInterfaceType thrownClassType;
	
	// For "throw e" signalers
	private SimpleName simpleName;
	
	// For "throw new Exception (arguments)" signalers
	private NodeList<Expression> argumentsInObjectCreation;
	
	// For "e.getCause" and other calls
	private MethodCallExpr methodCallExpression;
	
	private Optional<CatchClause> relatedCatchClause; // is present in a rethrow, wrapping or unwrapping
	
	public SignalerParser(ThrowStmt throwStatement, List<CatchClause> catchesInContext)
	{
		this.throwStatement = throwStatement;
		this.catchesInContext = catchesInContext;
		
		this.parsed = false;
		
		type = null;
		thrownClassDeclaration = null;
		thrownClassType = null;
		simpleName = null;
		argumentsInObjectCreation = null;
		methodCallExpression = null;
		relatedCatchClause = null;
	}
	
	/**
	 * Parsear o código de um sinalizar e salva, no objeto, informações úteis para consumo.
	 * */
	private void parse ()
	{
		this.resolveClassDeclaration();
		
		Expression throwExpression = this.throwStatement.getExpression();
		
		/* o statement é um "throw e". Provavelmente é um rethrow, mas é possível que a exceção tenha
		 * sido instanciada previamente e agora está sendo lançada. Isso não faz diferença neste momento
		 */
		if ( throwExpression instanceof NameExpr )
		{
			this.simpleName = throwExpression.asNameExpr().getName();
			
			//ResolvedValueDeclaration declaration = throwExpression.asNameExpr().resolve();
			//this.thrownClassType = findVariableType (declaration);
		}
		// O statement é um "throw new...".
		else if ( throwExpression instanceof ObjectCreationExpr )
		{
			ObjectCreationExpr objectCreationExp = throwExpression.asObjectCreationExpr();
			
			this.argumentsInObjectCreation = objectCreationExp.getArguments();
		}
		// O statement é uma chamada de método.
		else if (throwExpression instanceof MethodCallExpr)
		{
			this.methodCallExpression = throwExpression.asMethodCallExpr();
			
			// if e.getCause
			// this.type = SignalerType.GET_CAUSE
			// senão, throw exceção
		}
		// O statement é um (Exception) e
		else if (throwExpression instanceof CastExpr)
		{
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
		
		parsed = true;
	}

	/**
	 * Tenta identificar que tipo de sinalização está ocorrendo. Os tipos suportados são os da Enum SignalerType.
	 * */
	public void resolve()
	{
		parse();
		
		// throw e
		if ( this.simpleName != null )
		{
			this.relatedCatchClause = this.catchesInContext.stream()
				.filter(catchClause -> catchClause.getParameter().getName().equals(this.simpleName))
				.findAny();
			
			// O SimpleName é de uma exceção capturada no contexto do sinalizar. Houve um rethrow.
			if (this.relatedCatchClause.isPresent())
			{
				this.type = SignalerType.RETHROW;
			}
			else
			{
				throw new UnknownSignalerException ("Sinalizado um SimpleName que não é rethrow. Sinalização: '" + this.throwStatement + "'.");
			}
		}
		// throw new <Exception> (args)
		else if ( this.argumentsInObjectCreation != null )
		{
			// Check if wrapping
			if ( !catchesInContext.isEmpty() )
			{
				List<SimpleName> simpleNamesInObjectCreation = argumentsInObjectCreation.stream()
						.filter(Expression::isNameExpr)
						.map(expression -> expression.asNameExpr().getName())
						.collect(Collectors.toList());
				
				this.relatedCatchClause = catchesInContext.stream()
						.filter (catchClause -> simpleNamesInObjectCreation.contains((catchClause.getParameter().getName())))
						.findAny();
	
				if ( this.relatedCatchClause.isPresent() )
				{
					this.type = SignalerType.WRAPPING;
				}
				else
				{
					this.type = SignalerType.DESTRUCTIVE_SIMPLE_THROW;
				}
			}
			else
			{
				this.type = SignalerType.SIMPLE_THROW; 
			}
		}
		// e.getCause, 
		else if ( this.methodCallExpression != null )
		{
			MethodCallParser methodCallParser = new MethodCallParser(this.methodCallExpression);
			methodCallParser.parse();
			
			// e.getCause
			if ( methodCallParser.isGetCause() )
			{
				this.relatedCatchClause = this.catchesInContext.stream()
						.filter(catchClause -> catchClause.getParameter().getName().equals(methodCallParser.getGetCauseScope()))
						.findAny();
				
				if ( this.relatedCatchClause.isPresent() )
				{
					this.type = SignalerType.UNWRAPPING;
				}
				else
				{
					throw new UnknownSignalerException ("Sinalizado um 'e.getCause()' cujo contexto não foi resolvido. Sinalização: '" + this.throwStatement + "'.");
				}
			}
			else
			{
				throw new UnknownSignalerException ("Sinalizado uma chamada de método que não é reconhecida. Sinalização: '" + this.throwStatement + "'.");
			}
		}
		else
		{
			throw new UnknownSignalerException ("Sinalizado uma chamada de método que não é reconhecida. Sinalização: '" + this.throwStatement + "'.");
		}
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

	private ClassOrInterfaceType getThrownClassType() {
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		else if ( this.thrownClassDeclaration != null )
			throw new IllegalStateException("The thrown type was resolved. Use 'getThrownClassDeclaration' method instead.");
		else
			return this.thrownClassType;
	}

	private NodeList<Expression> getArgumentsInObjectCreation()
	{
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		else
			return argumentsInObjectCreation;
	}

	private SimpleName getSimpleName() {
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		return this.simpleName;
	}
}
