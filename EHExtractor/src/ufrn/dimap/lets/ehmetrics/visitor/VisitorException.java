package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.Node;

import ufrn.dimap.lets.ehmetrics.EHMetricsException;

/**
 * Type root of exceptions which happens during visiting.
 * */
public abstract class VisitorException extends EHMetricsException
{
	private static final long serialVersionUID = 1L;
	
	protected Node node;
	
	public VisitorException (String msg, Node node)
	{
		super (msg);
		this.node = node;
	}
	
	public VisitorException (String msg, Node node, Throwable e)
	{
		super (msg, e);
		this.node = node;
	}
	
	public String getCodeSnippet ()
	{
		return this.node.toString();
	}
	
	public int getInitLineNumber ()
	{
		return this.node.getBegin().get().line;
	}
	
	public int getEndLineNumber ()
	{
		return this.node.getEnd().get().line;
	}
}
