package ufrn.dimap.lets.ehmetrics.analyzer;

import com.github.javaparser.resolution.UnsolvedSymbolException;

public class UnknownSignalerException extends AnalyzerException
{
	private static final long serialVersionUID = 1L;

	public UnknownSignalerException(String message)
	{
		super(message);
	}

	public UnknownSignalerException(String message, UnsolvedSymbolException e)
	{
		super ( message, e );
	}
	
}
