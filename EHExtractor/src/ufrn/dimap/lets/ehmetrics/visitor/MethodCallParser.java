package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.Optional;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;

public class MethodCallParser
{
	private MethodCallExpr methodCallExpression;
	
	// List of supported methods
	private boolean getCause;
	private SimpleName getCauseScope;
	
	public MethodCallParser ( MethodCallExpr callExpression )
	{
		this.methodCallExpression = callExpression;
		
		getCause = false;
		getCauseScope = null;
	}
	
	public void parse ()
	{
		SimpleName methodName = this.methodCallExpression.getName();
		
		// Tests to "e.getCause()". Arguments must be empty. Scope must be a SimpleName.
		if ( methodName.asString().equals("getCause") )
		{
			Optional<Expression> getCauseScopeOptional = this.methodCallExpression.getScope();
			
			if ( getCauseScopeOptional.isPresent() && this.methodCallExpression.getArguments().isEmpty() )
			{
				Expression getCauseScopeExpression = getCauseScopeOptional.get();
				
				if ( getCauseScopeExpression.isNameExpr())
				{
					this.getCauseScope = getCauseScopeExpression.asNameExpr().getName();
					this.getCause = true;
				}
			}
		}
		else if ( methodName.asString().equals("println") )
		{
			
		}
	}
	
	public boolean isGetCause()
	{
		return getCause;
	}
	
	public SimpleName getGetCauseScope()
	{
		return this.getCauseScope;
	}
}
