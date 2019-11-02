package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;

/**
 * Visitor para verificar o guideline "Convert to runtime exceptions".
 * */
public class ConvertToRuntimeExceptionsVisitor extends AbstractGuidelineVisitor
{
	public ConvertToRuntimeExceptionsVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
	{
		super (baseVisitor, allowUnresolved);
	}

	/**
	 * Returns the guideline columns names
	 * */
	@Override
	public String getGuidelineHeader ()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("# final handlers of checked");
		builder.append("\t");
		builder.append("# resignaler handlers checked -> any");
		builder.append("\t");
		builder.append("# resignaler handlers checked -> unchecked");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		Predicate <Handler> catchCheckedException = handler -> 
			handler.getExceptions().stream()
				.anyMatch(type -> type.getClassType() == ClassType.CHECKED_EXCEPTION);
		
		Predicate <Handler> finalHandler = Handler::isFinalHandler;
		
		Predicate <Handler> resignalUnchecked = handler -> 
			handler.getEscapingSignalers().stream()
				.flatMap(signaler -> signaler.getThrownTypes().stream())
				.anyMatch(type -> type.getClassType() == ClassType.UNCHECKED_EXCEPTION);
		
		List<Handler> finalHandlersOfCheckedExceptions = this.baseVisitor.getHandlers().stream()
			.filter( finalHandler.and(catchCheckedException) )
			.collect(Collectors.toList());
			
		List<Handler> resignalersHandlersOfCheckedWhichResignalAny = this.baseVisitor.getHandlers().stream()
				.filter( finalHandler.negate().and(catchCheckedException) )
				.collect(Collectors.toList());
		
		List<Handler> resignalersHandlersOfCheckedWhichResignalUnchecked = resignalersHandlersOfCheckedWhichResignalAny.stream()
				.filter(resignalUnchecked)
				.collect(Collectors.toList());

		
		StringBuilder builder = new StringBuilder();
		
		builder.append(finalHandlersOfCheckedExceptions.size());
		builder.append("\t");
		builder.append(resignalersHandlersOfCheckedWhichResignalAny.size());
		builder.append("\t");
		builder.append(resignalersHandlersOfCheckedWhichResignalUnchecked.size());
		builder.append("\t");
		
		return builder.toString();
	}
}