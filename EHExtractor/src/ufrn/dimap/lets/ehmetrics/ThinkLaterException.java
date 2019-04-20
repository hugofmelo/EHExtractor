package ufrn.dimap.lets.ehmetrics;

public class ThinkLaterException extends RuntimeException {
	
	public ThinkLaterException (Throwable e)
	{
		super (e);
	}

	public ThinkLaterException(String message)
	{
		super (message);
	}

}
