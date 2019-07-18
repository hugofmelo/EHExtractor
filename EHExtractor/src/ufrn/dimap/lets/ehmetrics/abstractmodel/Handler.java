package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;

public class Handler extends AbstractEHModelElement
{
	private List<Type> exceptions;
	private String variableName;
	private List<HandlingAction> handlingActions;
	
	
	private List<Handler> nestedHandlers;
	private Optional<Handler> parentHandler;
	
	private List<Signaler> escapingSignalers; // Every signaler in the context of this handler
	
	public Handler()
	{
		super();
		
		this.exceptions = new ArrayList<>();
		this.variableName = null;
		this.handlingActions = new ArrayList<>();
		
		this.nestedHandlers = new ArrayList<>();
		this.parentHandler = Optional.empty();
		
		this.escapingSignalers = new ArrayList<>();
	}
	
	public String getVariableName ()
	{
		return this.variableName;
	}
	
	public void setVariableName (String variableName)
	{
		this.variableName = variableName;
	}
	
	public List<Type> getExceptions()
	{
		return this.exceptions;
	}
	
	public List<HandlingAction> getHandlingActions()
	{
		return this.handlingActions;
	}
	
	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		String separator = "";
		
		for ( Type t : this.exceptions)
		{
			result.append(separator + t.getQualifiedName());
			separator = " | ";
		}
		
		return result.toString();
	}

	public List<Signaler> getEscapingSignalers() {
		return escapingSignalers;
	}

	public List<Handler> getNestedHandlers()
	{
		return this.nestedHandlers;
	}
	
	public Optional<Handler> getParentHandler ()
	{
		return this.parentHandler;
	}
	
	public void setParentHandler (Handler handler)
	{
		this.parentHandler = Optional.of(handler);
	}
	
	public void setEscapingSignalers(List<Signaler> signalers) {
		this.escapingSignalers = signalers;
	}

	public List<Handler> getAllHandlersInContext()
	{
		List<Handler> handlersInContext = new ArrayList<>();
		
		handlersInContext.add(this);
		
		Optional<Handler> parentMaybe = this.parentHandler;
		
		while ( parentMaybe.isPresent() )
		{
			handlersInContext.add(parentMaybe.get());
			parentMaybe = parentMaybe.get().parentHandler;
		}
		
		return handlersInContext;
	}

	public HandlingAction createHandlerAction(MethodCallExpr callExpression)
	{
		HandlingAction newHandlingAction = new HandlingAction();
		
		//newHandlingAction.setNode(callExpression);
		newHandlingAction.setHandler(this);
		
		this.getHandlingActions().add(newHandlingAction);
		
		try
		{
			newHandlingAction.setMethodName( callExpression.resolve().getQualifiedName() );
		}
		catch (UnsolvedSymbolException e)
		{
			newHandlingAction.setMethodName( callExpression.getNameAsString() );
		}
		catch (RuntimeException e)
		{
			newHandlingAction.setMethodName( callExpression.getNameAsString() );
		}
		
		
		return newHandlingAction;
	}
	
	
	public HandlingAction createLogHandlerAction(MethodCallExpr callExpression)
	{
		HandlingAction newHandlingAction = createHandlerAction(callExpression);
		
		newHandlingAction.setIsLoggingAction(true);
		
		return newHandlingAction;
	}
	
	/**
	 * Utility method which inspects this handler HandlingActions.
	 * */
	public boolean hasLoggingActions ()
	{
		return this.handlingActions.stream()
			.filter(HandlingAction::isLoggingAction)
			.findAny()
			.isPresent();
	}
	
	/**
	 * A final handler is a handler which has not escaping signalers.
	 * 
	 * Utility method.
	 * */
	public boolean isFinalHandler ()
	{
		return this.escapingSignalers.isEmpty();
	}
}
