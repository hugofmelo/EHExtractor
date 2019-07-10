package ufrn.dimap.lets.ehmetrics.projectresolver;

import java.io.File;

import ufrn.dimap.lets.ehmetrics.EHMetricsException;

public class DependencyResolverException extends EHMetricsException
{
	private static final long serialVersionUID = 1L;
	
	private final File file;
	
	public DependencyResolverException (String message, File file)
	{
		super (message);
		this.file = file;
	}
	
	public DependencyResolverException(String message, File file, Throwable e)
	{
		super (message, e);
		this.file = file;
	}

	public File getFile ()
	{
		return this.file;
	}
}
