package ufrn.dimap.lets.ehmetrics.analyzer;

import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;

public class UnresolvedAncestorsException extends EHMetricsException
{
	private static final long serialVersionUID = 1L;
	
	private final ResolvedClassDeclaration classDeclaration;
	
	public UnresolvedAncestorsException(String message, ResolvedClassDeclaration classDeclaration)
	{
		super(message);
	
		this.classDeclaration = classDeclaration;
	}

	public UnresolvedAncestorsException(String message, ResolvedClassDeclaration classDeclaration,
			Throwable e)
	{
		super ( message, e );
		
		this.classDeclaration = classDeclaration;
	}

	public ResolvedClassDeclaration getClassDeclaration() {
		return classDeclaration;
	}
}
