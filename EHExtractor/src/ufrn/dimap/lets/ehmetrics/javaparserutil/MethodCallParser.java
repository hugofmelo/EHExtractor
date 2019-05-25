package ufrn.dimap.lets.ehmetrics.javaparserutil;

import java.util.Optional;
import java.util.logging.Logger;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;

/**
 * This class stores relevant data for consumption e define methods to check which
 * method was call.
 * 
 * Must call "parse" method before accessing any data.
 * 
 * Supported methods:
 * e.getCause
 * */
public class MethodCallParser
{
	private MethodCallExpr methodCallExpression;

	// List of supported methods
	private boolean isGetCause;
	private SimpleName getCauseScope;

	private boolean isPrintStackTrace;
	private SimpleName printStackTraceScope;
	
	private boolean isPrintln;

	private boolean isJavaLogger;
	private boolean isExternalLogger;
	private Object loggerScope;

	private boolean isGenericLog;

	public MethodCallParser ( MethodCallExpr callExpression )
	{
		this.methodCallExpression = callExpression;

		isGetCause = false;
		getCauseScope = null;

		isPrintStackTrace = false;
		printStackTraceScope = null;
		
		isPrintln = false;

		isJavaLogger = false;		
		isExternalLogger = false;
		loggerScope = null;

		isGenericLog = false;
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
					this.isGetCause = true;
				}
			}
		}
		else if ( methodName.asString().equals("printStackTrace") )
		{
			Optional<Expression> printStackTraceScopeOptional = this.methodCallExpression.getScope();

			if ( printStackTraceScopeOptional.isPresent() && this.methodCallExpression.getArguments().isEmpty() )
			{
				Expression getPrintStackTraceScopeExpression = printStackTraceScopeOptional.get();

				if ( getPrintStackTraceScopeExpression.isNameExpr())
				{
					this.printStackTraceScope = getPrintStackTraceScopeExpression.asNameExpr().getName();
					this.isPrintStackTrace = true;
				}
			}
		}
		else if ( methodName.asString().equals("println") || methodName.asString().equals("print") )
		{
			Optional<Expression> printlnScopeOptional = this.methodCallExpression.getScope();

			if ( printlnScopeOptional.isPresent() )
			{
				if ( printlnScopeOptional.get().toString().equals("System.out") ||
					 printlnScopeOptional.get().toString().equals("System.err") )
				{
					this.isPrintln = true;
				}
			}
		}
		else if ( 	methodName.asString().equals("fatal") ||
					methodName.asString().equals("error") ||
					methodName.asString().equals("warn") ||
					methodName.asString().equals("debug") ||
					methodName.asString().equals("trace") )
		{
			this.isExternalLogger = true;
			this.loggerScope = this.methodCallExpression.getScope();
		}	
		else if ( methodName.asString().equals("log") )
		{
			Optional<Expression> logScopeOptional = this.methodCallExpression.getScope();

			// Test for java Logger usage - LOGGER.log(...)
			if ( logScopeOptional.isPresent() )
			{
				Expression logScopeExpression = logScopeOptional.get();

				try
				{
					if (logScopeExpression.calculateResolvedType().asReferenceType().getQualifiedName().equals(Logger.class.getCanonicalName()))
					{
						this.isJavaLogger = true;
						this.loggerScope = logScopeExpression;
					}
					else
					{
						this.isGenericLog = true;
					}
				}
				catch (Exception e)
				{
					this.isGenericLog = true;					
				}
			}
			else
			{
				this.isGenericLog = true;
			}
		}
		else if ( methodName.asString().toLowerCase().contains("log") )
		{			
			this.isGenericLog = true;
		}
	}

	public boolean isGetCause() {
		return isGetCause;
	}

	public SimpleName getGetCauseScope() {
		return this.getCauseScope;
	}

	public boolean isPrintln() {
		return this.isPrintln;
	}
	
	public boolean isPrintStackTrace() {
		return this.isPrintStackTrace;
	}

	public SimpleName getPrintStackTraceScope() {
		return this.printStackTraceScope;
	}

	public boolean isJavaLogger() {
		return this.isJavaLogger;
	}
	
	public boolean isExternalLogger() {
		return this.isExternalLogger;
	}

	public Object getLoggerScope ()	{
		return this.loggerScope;
	}

	public boolean isGenericLog() {
		// TODO Auto-generated method stub
		return false;
	}
}
