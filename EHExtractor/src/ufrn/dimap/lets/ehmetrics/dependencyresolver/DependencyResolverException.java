package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;

public class DependencyResolverException extends Exception
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
