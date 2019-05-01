package ufrn.dimap.lets.ehmetrics.analyzer;

import com.github.javaparser.ast.Node;

public abstract class UnknownCodePatternException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
	private Node node;
	
	public UnknownCodePatternException (String msg, Node node)
	{
		super (msg);
		this.node = node;
	}
	
	public UnknownCodePatternException (String msg, Node node, Throwable e)
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
