package ufrn.dimap.lets.ehmetrics;

/**
 * Root of all project exceptions.
 * */
public abstract class EHMetricsException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public EHMetricsException(String msg)
	{
		super (msg);
	}
	
	public EHMetricsException (String msg, Throwable cause)
	{
		super(msg, cause);
	}

	
}
