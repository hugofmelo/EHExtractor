package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.CatchClause;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.javaparserutil.JavaParserUtil;

/**
 * List of auxiliar methods used by visitors.
 * */
public class VisitorsUtil {

	private VisitorsUtil ()	{}
	
	
	
	/**
	 * Extract CatchClauses from given Handler e from nested handlers.
	 * */
	public static List<CatchClause> getCatchClausesFromHandler( Optional<Handler> handlerMaybe )
	{
		List <CatchClause> catchClauses = new ArrayList<>();
		Optional <Handler> auxHandlerMaybe = handlerMaybe;
		
		while ( auxHandlerMaybe.isPresent() )
		{
			catchClauses.add((CatchClause) handlerMaybe.get().getNode());
			auxHandlerMaybe = auxHandlerMaybe.get().getParentHandler();
		}
		
		return catchClauses;
	}
	
	/**
	 * Check if a handler in the context has caught an exception which name is in the list of simpleNames.
	 * */
	public static Optional<Handler> findHandler (Handler handler, List<SimpleName> simpleNames)
	{
		for ( SimpleName simpleName : simpleNames )
		{
			Optional<Handler> handlerOptional = findHandler(handler, simpleName);
			
			if ( handlerOptional.isPresent() )
			{
				return handlerOptional;
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Finds the handler which caught the given SimpleName. Or Optional.Empty if there is none.
	 * */
	public static Optional<Handler> findHandler (Handler handler, SimpleName exceptionName)
	{
		Optional<Handler> handlerOptional = Optional.of(handler);
		
		while ( handlerOptional.isPresent() )
		{
			CatchClause catchClause = (CatchClause) handlerOptional.get().getNode();
			
			if ( catchClause.getParameter().getName().equals(exceptionName) )
			{
				return Optional.of(handler);
			}
			else
			{
				handlerOptional = handlerOptional.get().getParentHandler();
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Create a new HandlingAction to the Handler which exceptions was logged and returns true.
	 * 
	 * Returns false if there is no a LoggingAction.
	 * */
	public static boolean checkForLogAction (MethodCallExpr callExpression, Handler handlerInContext)
	{
		Optional<Handler> loggedHandlerOptional;

		loggedHandlerOptional = checkForPrintStackTrace(callExpression, handlerInContext);
		if ( loggedHandlerOptional.isPresent() )
		{
			loggedHandlerOptional.get().createLogHandlerAction ( callExpression );
			return true;
		}


		loggedHandlerOptional = checkForPrintLn(callExpression, handlerInContext);
		if ( loggedHandlerOptional.isPresent() )
		{
			loggedHandlerOptional.get().createLogHandlerAction ( callExpression );
			return true;
		}

		loggedHandlerOptional = checkForJavaLogger(callExpression, handlerInContext);
		if ( loggedHandlerOptional.isPresent() )
		{
			loggedHandlerOptional.get().createLogHandlerAction ( callExpression );
			return true;
		}

		loggedHandlerOptional = checkForExternalLogger(callExpression, handlerInContext);
		if ( loggedHandlerOptional.isPresent() )
		{
			loggedHandlerOptional.get().createLogHandlerAction ( callExpression );
			return true;
		}

		loggedHandlerOptional = checkForGenericLogAction(callExpression, handlerInContext);
		if ( loggedHandlerOptional.isPresent() )
		{
			loggedHandlerOptional.get().createLogHandlerAction ( callExpression );
			return true;
		}
		
		return false;
	}

	/**
	 * Check if the called method is printStackTrace and returns the handler which caught the printed exception.
	 * 
	 * Return Optional.empty otherwise.
	 * */
	private static Optional<Handler> checkForPrintStackTrace (MethodCallExpr callExpression, Handler handlerInContext)
	{	
		if (callExpression.getNameAsString().equals("printStackTrace"))
		{
			if ( callExpression.getArguments().isEmpty() )
			{
				Optional<SimpleName> scopeSimpleNameOptional = callExpression.getScope()
						.filter ( Expression::isNameExpr )
						.map( expression -> expression.asNameExpr().getName());

				if ( scopeSimpleNameOptional.isPresent() )
				{
					return VisitorsUtil.findHandler(handlerInContext, scopeSimpleNameOptional.get());
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Check if the called method is System.[out|err].print[Ln] and returns the handler
	 * which is printing the caught exception.
	 * 
	 * Return Optional.empty otherwise.
	 * */
	private static Optional<Handler> checkForPrintLn (MethodCallExpr callExpression, Handler handlerInContext)
	{
		if (callExpression.getNameAsString().equals("print") ||
			callExpression.getNameAsString().equals("println"))
		{
			Optional<Expression> printlnScopeOptional = callExpression.getScope();
			
			if ( printlnScopeOptional.isPresent() )
			{
				if ( printlnScopeOptional.get().toString().equals("System.out") ||
						printlnScopeOptional.get().toString().equals("System.err") )
				{
					return VisitorsUtil.findHandler(handlerInContext, JavaParserUtil.filterSimpleNames(callExpression));
				}
			}
		}
		

		return Optional.empty();
	}

	/**
	 * Check if the called method is from a JavaLogger and returns the handler
	 * which exception is being logged.
	 * 
	 * Return Optional.empty otherwise.
	 * */
	private static Optional<Handler> checkForJavaLogger (MethodCallExpr callExpression, Handler handlerInContext)
	{
		if ( callExpression.getNameAsString().equals("log") )
		{
			try
			{
				if ( callExpression.getScope()
						.filter(expression -> expression.calculateResolvedType().asReferenceType().getQualifiedName().equals(Logger.class.getCanonicalName()))
						.isPresent() )
				{
					return VisitorsUtil.findHandler(handlerInContext, JavaParserUtil.filterSimpleNames(callExpression));
				}
			}
			catch (Exception e)
			{
				// Empty because the method will return Optional.empty. 					
			}
		}
	
		return Optional.empty();
	}

	/**
	 * Check if the called method is from an external Logger and returns the handler
	 * which exception is being logged.
	 * 
	 * Supported loggers frameworks: Log4J, Apache Commons Logging, SLF4J, tinylog, LogBack
	 * 
	 * Return Optional.empty otherwise.
	 * */
	private static Optional<Handler> checkForExternalLogger (MethodCallExpr callExpression, Handler handlerInContext)
	{
		String methodName = callExpression.getNameAsString();

		if ( 	methodName.equals("fatal") ||
				methodName.equals("error") ||
				methodName.equals("warn") ||
				methodName.equals("warning") ||
				methodName.equals("info") ||
				methodName.equals("debug") ||
				methodName.equals("trace") )
		{
			return VisitorsUtil.findHandler(handlerInContext, JavaParserUtil.filterSimpleNames(callExpression));
		}

		return Optional.empty();
	}


	/**
	 * Check if the called method is from a generic log call and returns the handler
	 * which exception is being logged.
	 * 
	 * Return Optional.empty otherwise.
	 * */
	private static Optional<Handler> checkForGenericLogAction (MethodCallExpr callExpression, Handler handlerInContext)
	{
		if ( callExpression.getNameAsString().contains("log") )
		{
			return VisitorsUtil.findHandler(handlerInContext, JavaParserUtil.filterSimpleNames(callExpression));
		}
		return Optional.empty();
	}
}
