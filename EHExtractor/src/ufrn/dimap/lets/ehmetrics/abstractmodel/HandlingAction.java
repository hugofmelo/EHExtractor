package ufrn.dimap.lets.ehmetrics.abstractmodel;

public class HandlingAction extends AbstractEHModelElement
{
	private String methodName;
	
	private Handler enclosingHandler;
	
	private boolean isLoggingAction;
	
	public HandlingAction()
	{
		super();
		
		this.methodName = null;
		
		this.enclosingHandler = null;
		
		this.isLoggingAction = false;
	}
	
	public String getMethodName ()
	{
		return this.methodName;
	}
	
	public void setMethodName (String methodName)
	{
		this.methodName = methodName;
	}
	
	public Handler getHandler ()
	{
		return this.enclosingHandler;
	}
	
	public void setHandler (Handler handler)
	{
		this.enclosingHandler = handler;
	}
	
	public boolean isLoggingAction ()
	{
		return isLoggingAction;
	}
	
	public void setIsLoggingAction (boolean isLoggingAction)
	{
		this.isLoggingAction = isLoggingAction;
	}
}
