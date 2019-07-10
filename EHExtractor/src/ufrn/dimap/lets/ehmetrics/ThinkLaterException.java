package ufrn.dimap.lets.ehmetrics;

public class ThinkLaterException extends EHMetricsException {
	
	public ThinkLaterException (String message, Throwable e)
	{
		super (message, e);
	}

	public ThinkLaterException(String message)
	{
		super (message);
	}

}
