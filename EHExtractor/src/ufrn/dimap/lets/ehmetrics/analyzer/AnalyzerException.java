package ufrn.dimap.lets.ehmetrics.analyzer;

public abstract class AnalyzerException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public AnalyzerException(String msg)
	{
		super (msg);
	}
	
	public AnalyzerException (String msg, Throwable cause)
	{
		super(msg, cause);
	}

	
}
