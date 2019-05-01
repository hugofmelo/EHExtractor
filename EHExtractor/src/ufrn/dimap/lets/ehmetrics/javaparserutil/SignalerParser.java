package ufrn.dimap.lets.ehmetrics.javaparserutil;

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

import ufrn.dimap.lets.ehmetrics.analyzer.UnknownSignalerException;


/**
 * Classe utilit�ria para processar e resolver um ThrowStmt.
 * 
 * This class stores relevant data for consumption e define methods to check rethrown,
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
		relatedCatchClause = null;
	}
	
	/**
	 * Parsear o c�digo de um sinalizar e salva, no objeto, informa��es �teis para consumo.
	 * */
	// TODO unir parse com resolve e resolver logo a sinaliza��o sem ter q testar 2x as mesmas coisas..
	private void parse ()
	{
		Expression throwExpression = this.throwStatement.getExpression();
		
		/* o statement � um "throw e". Provavelmente � um rethrow, mas � poss�vel que a exce��o tenha
		 * sido instanciada previamente e agora est� sendo lan�ada. Isso n�o faz diferen�a neste momento
		 */
		if ( throwExpression instanceof NameExpr )
		{
			this.simpleName = throwExpression.asNameExpr().getName();
			
			//ResolvedValueDeclaration declaration = throwExpression.asNameExpr().resolve();
			//this.thrownClassType = findVariableType (declaration);
		}
		// O statement � um "throw new...".
		else if ( throwExpression instanceof ObjectCreationExpr )
		{
			ObjectCreationExpr objectCreationExp = throwExpression.asObjectCreationExpr();
			
			this.argumentsInObjectCreation = objectCreationExp.getArguments();
		}
		// O statement � uma chamada de m�todo.
		else if (throwExpression instanceof MethodCallExpr)
		{
			this.methodCallExpression = throwExpression.asMethodCallExpr();
			
			// if e.getCause
			// this.type = SignalerType.GET_CAUSE
			// sen�o, throw exce��o
		}
		// O statement � um (Exception) e
		else if (throwExpression instanceof CastExpr)
		{
			CastExpr castExpression = throwExpression.asCastExpr();

			if ( castExpression.getType() instanceof ClassOrInterfaceType )
			{
				this.castedType = castExpression.getType().asClassOrInterfaceType();
			}
			else
			{
				throw new UnknownSignalerException("Sinaliza��o com cast n�o suportada.", throwStatement);
			}
		}
		else
		{
			throw new UnknownSignalerException ("A sinaliza��o n�o � de um dos tipos suportados.", throwStatement);
		}
		
		parsed = true;
	}

	/**
	 * Tenta identificar que tipo de sinaliza��o est� ocorrendo. Os tipos suportados s�o os da Enum SignalerType.
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
			
			// O SimpleName � de uma exce��o capturada no contexto do sinalizar. Houve um rethrow.
			if (this.relatedCatchClause.isPresent())
			{
				this.signalerType = SignalerType.RETHROW;
			}
			else
			{
				throw new UnknownSignalerException ("Sinalizado um SimpleName que n�o � rethrow.", this.throwStatement);
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
					this.signalerType = SignalerType.WRAPPING;
				}
				else
				{
					this.signalerType = SignalerType.DESTRUCTIVE_SIMPLE_THROW;
				}
			}
			else
			{
				this.signalerType = SignalerType.SIMPLE_THROW; 
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
					this.signalerType = SignalerType.UNWRAPPING;
				}
				else
				{
					throw new UnknownSignalerException ("Sinalizado um 'e.getCause()' cujo contexto n�o foi resolvido.", this.throwStatement);
				}
			}
			else
			{
				throw new UnknownSignalerException ("Sinalizado uma chamada de m�todo que n�o � reconhecida.", this.throwStatement);
			}
		}
		else
		{
			throw new UnknownSignalerException ("A sinaliza��o � de um padr�o desconhecido.", this.throwStatement);
		}
	}
	
	public SignalerType getType() {
		if ( !parsed )
			throw new IllegalStateException("Call 'parse' method first.");
		else
			return signalerType;
	}
}
