package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;

/**
 * Visitor para verificar o guideline "Log the exception".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????
 * */
public class LogTheExceptionVisitor extends GuidelineCheckerVisitor implements GuidelineMetrics
{	
	private Optional<Handler> handlerInScopeOptional;

	public LogTheExceptionVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
	}

	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{		
		handlerInScopeOptional = Optional.empty();

		super.visit(compilationUnit, arg);
	}	

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = createHandler(catchClause);

		this.handlerInScopeOptional.ifPresent(handler ->
		{
			handler.getNestedHandlers().add(newHandler);
			newHandler.setParentHandler(handler);
		});

		this.handlerInScopeOptional = Optional.of(newHandler);


		// VISIT CHILDREN
		super.visit(catchClause, arg);

		this.handlerInScopeOptional = this.handlerInScopeOptional.get().getParentHandler();
	}	

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		Signaler newSignaler = createSignaler(throwStatement);

		// All handlers in context have this signaler as escaping exception
		if (handlerInScopeOptional.isPresent())
		{
			handlerInScopeOptional.get().getAllHandlersInContext()
				.forEach(handler -> handler.getEscapingSignalers().add(newSignaler));
		}

		// VISIT CHILDREN
		super.visit(throwStatement, arg);
	}

	@Override
	public void visit ( MethodCallExpr methodCallExpression, Void arg )
	{
		if ( this.handlerInScopeOptional.isPresent() )
		{
			VisitorsUtil.checkForLogAction (methodCallExpression, this.handlerInScopeOptional.get());
		}

		// VISIT CHILDREN
		super.visit(methodCallExpression, arg);
	}

	/**
	 * Returns the guideline columns names
	 * */
	@Override
	public String getGuidelineHeader ()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("# handlers");
		builder.append("\t");
		builder.append("# final handlers");
		builder.append("\t");
		builder.append("# final handlers with logging actions");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{
		List<Handler> finalHandlers = this.handlersOfProject.stream()
				.filter(Handler::isFinalHandler)
				.collect(Collectors.toList());

		List<Handler> finalHandlersWithLogHandlingActions = this.handlersOfProject.stream()
				.filter(Handler::isFinalHandler)
				.filter(Handler::hasLoggingActions)
				.collect(Collectors.toList());

		/*
		GUIDELINE_LOGGER.info("Number of handlers: " + this.handlersOfProject.size());

		GUIDELINE_LOGGER.info("Number of final handlers: " + finalHandlers.size());

		GUIDELINE_LOGGER.info("Number of final handlers which has logging actions: " + finalHandlersWithLogHandlingActions.size());
		*/

		StringBuilder builder = new StringBuilder();
		
		builder.append(this.handlersOfProject.size());
		builder.append("\t");
		builder.append(finalHandlers.size());
		builder.append("\t");
		builder.append(finalHandlersWithLogHandlingActions.size());
		builder.append("\t");
		
		return builder.toString();
	}

}