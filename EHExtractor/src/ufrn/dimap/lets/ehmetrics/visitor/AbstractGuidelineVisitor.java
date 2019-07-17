package ufrn.dimap.lets.ehmetrics.visitor;

import java.io.File;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Generic visitor to parse exceptional types, signalers and handlers.
 * */
public abstract class AbstractGuidelineVisitor extends VoidVisitorAdapter<Void> implements GuidelineMetrics
{
	protected File javaFile; // Java file being parsed
	protected boolean allowUnresolved;
	protected BaseGuidelineVisitor baseVisitor;

	public AbstractGuidelineVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
	{
		this.baseVisitor = baseVisitor;
		this.allowUnresolved = allowUnresolved;
	}
	
	// GETTERS AND SETTERS **************************************
	
	public void setJavaFile (File javaFile)
	{
		this.javaFile = javaFile;
	}
}
