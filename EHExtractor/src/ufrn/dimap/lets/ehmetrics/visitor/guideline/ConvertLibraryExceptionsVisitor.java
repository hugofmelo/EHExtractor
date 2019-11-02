package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;

/**
 * Visitor para verificar o guideline "Convert library exceptions".
 * */
public class ConvertLibraryExceptionsVisitor extends AbstractGuidelineVisitor
{
	public ConvertLibraryExceptionsVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
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

		builder.append("# final handlers of external");
		builder.append("\t");
		builder.append("# resignaler handlers external -> any");
		builder.append("\t");
		builder.append("# resignaler handlers external -> project");
		builder.append("\t");

		return builder.toString();
	}

	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		Predicate <Handler> catchExternalException = handler -> 
			handler.getExceptions().stream()
				.anyMatch(type -> type.getOrigin() == TypeOrigin.LIBRARY || type.getOrigin() == TypeOrigin.UNRESOLVED);
	
		Predicate <Handler> finalHandler = Handler::isFinalHandler;
		
		Predicate <Handler> resignalProject = handler -> 
			handler.getEscapingSignalers().stream()
				.flatMap(signaler -> signaler.getThrownTypes().stream())
				.anyMatch(type -> type.getOrigin() == TypeOrigin.SYSTEM);
		
		List<Handler> finalHandlersOfExternalExceptions = this.baseVisitor.getHandlers().stream()
			.filter( finalHandler.and(catchExternalException) )
			.collect(Collectors.toList());
			
		List<Handler> resignalersHandlersOfExternalWhichResignalAny = this.baseVisitor.getHandlers().stream()
				.filter( finalHandler.negate().and(catchExternalException) )
				.collect(Collectors.toList());
		
		List<Handler> resignalersHandlersOfCheckedWhichResignalProject = resignalersHandlersOfExternalWhichResignalAny.stream()
				.filter(resignalProject)
				.collect(Collectors.toList());
	
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(finalHandlersOfExternalExceptions.size());
		builder.append("\t");
		builder.append(resignalersHandlersOfExternalWhichResignalAny.size());
		builder.append("\t");
		builder.append(resignalersHandlersOfCheckedWhichResignalProject.size());
		builder.append("\t");
		
		return builder.toString();
	}
}