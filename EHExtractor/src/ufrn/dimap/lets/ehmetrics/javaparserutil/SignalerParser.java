package ufrn.dimap.lets.ehmetrics.javaparserutil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import ufrn.dimap.lets.ehmetrics.visitor.UnsupportedSignalerException;


/**
 * Classe utilit�ria para processar e resolver um ThrowStmt.
 * 
 * This class stores relevant data for consumption and define methods to check rethrown,
 * wrapping and unwrapping.
 * 
 * Must call "parse" method before accessing any data.
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
	
	private SignalerType signalerType;

	// Throw types. If the type can be resolved, the optional is present
	//private Optional<ResolvedClassDeclaration> thrownClassDeclaration;
	//private ClassOrInterfaceType thrownClassType;
	
	// For "throw e" signalers
	private SimpleName simpleName;
	
	// For "throw new Exception (arguments)" signalers
	private NodeList<Expression> argumentsInObjectCreation;
	
	// For "e.getCause" and other calls
	private MethodCallExpr methodCallExpression;
	
	// For "(Exception) e"
	private ClassOrInterfaceType castedType;
	
	private Optional<CatchClause> relatedCatchClause; // is present in a rethrow, wrapping or unwrapping
	
	public SignalerParser(ThrowStmt throwStatement, List<CatchClause> catchesInContext)
	{
		this.throwStatement = throwStatement;
		this.catchesInContext = catchesInContext;
		
		this.parsed = false;
		
		signalerType = null;
		//thrownClassDeclaration = null;
		//thrownClassType = null;
		simpleName = null;
		argumentsInObjectCreation = null;
		methodCallExpression = null;
		castedType = null;
		relatedCatchClause = Optional.empty();
	}
	
	/**
	 * Parsear o c�digo de um sinalizar e salva, no objeto, informa��es �teis para consumo.
	 * */
	public void parse ()
	{
		Expression throwExpression = this.throwStatement.getExpression();
		
		/* o statement � um "throw e". Provavelmente � um rethrow, mas � poss�vel que a exce��o tenha
		 * sido instanciada previamente e agora est� sendo lan�ada. Isso n�o faz diferen�a neste momento
		 */
		if ( throwExpression.isNameExpr() )
		{
			this.simpleName = throwExpression.asNameExpr().getName();
			
			this.testForRethrow();
		}
		// O statement � um "throw new...".
		else if ( throwExpression.isObjectCreationExpr() )
		{
			ObjectCreationExpr objectCreationExp = throwExpression.asObjectCreationExpr();
			
			this.argumentsInObjectCreation = objectCreationExp.getArguments();
			
			this.testForWrappingOrSimpleThrow();
		}
		// O statement � uma chamada de m�todo.
		else if (throwExpression.isMethodCallExpr() )
		{
			this.methodCallExpression = throwExpression.asMethodCallExpr();
			
			MethodCallParser methodCallParser = new MethodCallParser(methodCallExpression);
			
			if ( methodCallParser.isGetCause() )
			{
				this.testForUnwrapping(methodCallParser);
			}
			else
			{
				throw new UnsupportedSignalerException ("A sinaliza��o contem uma chamada de metodo n�o suportada.", throwStatement);
			}
		}
		// O statement � um (Exception) e
		else if (throwExpression.isCastExpr())
		{
			throw new UnsupportedSignalerException("Sinaliza��o com cast n�o suportada.", throwStatement);
			/*
			CastExpr castExpression = throwExpression.asCastExpr();

			if ( castExpression.getType() instanceof ClassOrInterfaceType )
			{
				this.castedType = castExpression.getType().asClassOrInterfaceType();
			}
			else
			{
				throw new UnknownSignalerException("Sinaliza��o com cast n�o suportada.", throwStatement);
			}
			*/
		}
		else
		{
			throw new UnsupportedSignalerException ("A sinaliza��o n�o � de um dos tipos suportados.", throwStatement);
		}
		
		parsed = true;
	}

	// METHODS TO CHECK EACH SIGNALER TYPE
	
	private void testForRethrow()
	{
		this.relatedCatchClause = this.catchesInContext.stream()
			.filter(catchClause -> catchClause.getParameter().getName().equals(this.simpleName))
			.findAny();
			
		// O SimpleName � de uma exce��o capturada no contexto do sinalizar. Houve um rethrow.
		if (this.relatedCatchClause.isPresent())
		{
			this.signalerType = SignalerType.RETHROW;
		}
		else
		{
			throw new UnsupportedSignalerException ("Sinalizado um SimpleName que n�o � rethrow.", this.throwStatement);
		}
	}
	
	private void testForWrappingOrSimpleThrow ()
	{
		if ( this.argumentsInObjectCreation.isEmpty() )
		{
			this.signalerType = SignalerType.SIMPLE_THROW;
		}
		else
		{
			if ( catchesInContext.isEmpty() )
			{
				this.signalerType = SignalerType.SIMPLE_THROW; 
			}
			else
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
					this.signalerType = SignalerType.WRAPPING;
				}
				else
				{
					this.signalerType = SignalerType.DESTRUCTIVE_SIMPLE_THROW;
				}
			}
		}
	}
	
	private void testForUnwrapping (MethodCallParser methodCallParser)
	{
		// e.getCause
		if ( methodCallParser.isGetCause() )
		{
			this.relatedCatchClause = this.catchesInContext.stream()
					.filter(catchClause -> catchClause.getParameter().getName().equals(methodCallParser.getGetCauseScope()))
					.findAny();
			
			if ( this.relatedCatchClause.isPresent() )
			{
				this.signalerType = SignalerType.UNWRAPPING;
			}
			else
			{
				throw new UnsupportedSignalerException ("Sinalizado um 'e.getCause()' cujo contexto n�o foi resolvido.", this.throwStatement);
			}
		}
		else
		{
			throw new IllegalStateException ("Call to '" + this.getClass().getName() + "#" + this.getClass().getEnclosingMethod().getName() + "', but the call is not to getCause method.");
		}
	}
	
	public SignalerType getType() {
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		else
			return signalerType;
	}
	
	public Optional<CatchClause> getRelatedCatchClause ()
	{
		return this.relatedCatchClause;
	}
}
