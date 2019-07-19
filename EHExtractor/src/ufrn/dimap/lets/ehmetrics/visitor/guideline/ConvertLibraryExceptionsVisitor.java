package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
		
		builder.append("# handlers");
		builder.append("\t");
		builder.append("# resignalers handlers");
		builder.append("\t");
		builder.append("# resignaler handlers of external exceptions");
		builder.append("\t");
		builder.append("# resignaler handlers of external exceptions which signal project exceptions");
		builder.append("\t");
		builder.append("# final handlers");
		builder.append("\t");
		builder.append("# final handlers of external exceptions");
		builder.append("\t");
		
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		Predicate <Handler> handleExternalExceptions = handler ->
			handler.getExceptions().stream()
				.anyMatch(type -> type.getOrigin() == TypeOrigin.LIBRARY || type.getOrigin() == TypeOrigin.UNRESOLVED);
		
		Predicate <Signaler> throwNonExternalException = signaler -> 
			signaler.getThrownTypes().stream()
				.anyMatch(type -> type.getOrigin() == TypeOrigin.SYSTEM || type.getOrigin() == TypeOrigin.JAVA);
		
		List<Handler> resignalerHandlers = this.baseVisitor.getHandlers().stream()
				.filter( handler -> !handler.isFinalHandler() )
				.collect (Collectors.toList());
		
		List<Handler> resignalerHandlersOfExternalExceptions = resignalerHandlers.stream()
				.filter ( handleExternalExceptions )
				.collect (Collectors.toList());
		
		List<Handler> resignalerHandlersOfExternalExceptionsWhichResignalNonExternalExceptions = resignalerHandlersOfExternalExceptions.stream()
				.filter ( handler -> handler.getEscapingSignalers().stream()
						.anyMatch(throwNonExternalException ))
				.collect (Collectors.toList());
		
		List<Handler> finalHandlers = this.baseVisitor.getHandlers().stream()
				.filter( Handler::isFinalHandler )
				.collect (Collectors.toList());
		
		List<Handler> finalHandlersOfExternalExceptions = finalHandlers.stream()
				.filter ( handleExternalExceptions )
				.collect (Collectors.toList());
		
		List<Handler> finalHandlersOfExternalExceptionsWhichResignalNonExternalExceptions = finalHandlersOfExternalExceptions.stream()
				.filter ( handler -> handler.getEscapingSignalers().stream()
						.anyMatch(throwNonExternalException ))
				.collect (Collectors.toList());
		
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.baseVisitor.getHandlers().size());
		builder.append("\t");
		builder.append(resignalerHandlers.size());
		builder.append("\t");
		builder.append(resignalerHandlersOfExternalExceptions.size());
		builder.append("\t");
		builder.append(resignalerHandlersOfExternalExceptionsWhichResignalNonExternalExceptions.size());
		builder.append("\t");
		builder.append(finalHandlers.size());
		builder.append("\t");
		builder.append(finalHandlersOfExternalExceptions.size());
		builder.append("\t");
			
		return builder.toString();
	}
}