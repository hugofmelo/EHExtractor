package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;

import org.apache.maven.shared.invoker.MavenInvocationException;

public class DependencyResolverException extends Exception
{
	private File file;
	
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
