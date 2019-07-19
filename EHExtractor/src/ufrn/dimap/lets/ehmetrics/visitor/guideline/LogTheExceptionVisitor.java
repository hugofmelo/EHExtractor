package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.VisitorsUtil;

/**
 * Visitor para verificar o guideline "Log the exception".
 * */
public class LogTheExceptionVisitor extends AbstractGuidelineVisitor
{	
	private Optional<Handler> handlerInScopeOptional;

	public LogTheExceptionVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
	{
		super(baseVisitor, allowUnresolved);
		this.handlerInScopeOptional = Optional.empty();
	}

	/*
	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{		
		handlerInScopeOptional = Optional.empty();

		super.visit(compilationUnit, arg);
	}
	*/
	
	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = this.baseVisitor.findHandler (catchClause);

		this.handlerInScopeOptional = Optional.of(newHandler);

		// VISIT CHILDREN
		super.visit(catchClause, arg);

		this.handlerInScopeOptional = this.handlerInScopeOptional.get().getParentHandler();
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
		builder.append("# resignaler handlers");
		builder.append("\t");
		builder.append("# resignaler handlers with logging actions");
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
		List<Handler> resignalerHandlers = this.baseVisitor.getHandlers().stream()
				.filter(handler -> !handler.isFinalHandler())
				.collect(Collectors.toList());
		
		List<Handler> resignalerHandlersWithLogHandlingActions = resignalerHandlers.stream()
				.filter(Handler::hasLoggingActions)
				.collect(Collectors.toList());
		
		List<Handler> finalHandlers = this.baseVisitor.getHandlers().stream()
				.filter(Handler::isFinalHandler)
				.collect(Collectors.toList());
		
		List<Handler> finalHandlersWithLogHandlingActions = finalHandlers.stream()
				.filter(Handler::hasLoggingActions)
				.collect(Collectors.toList());


		StringBuilder builder = new StringBuilder();
		
		builder.append(this.baseVisitor.getHandlers().size());
		builder.append("\t");
		builder.append(resignalerHandlers.size());
		builder.append("\t");
		builder.append(resignalerHandlersWithLogHandlingActions.size());
		builder.append("\t");
		builder.append(finalHandlers.size());
		builder.append("\t");
		builder.append(finalHandlersWithLogHandlingActions.size());
		builder.append("\t");
		
		return builder.toString();
	}
}