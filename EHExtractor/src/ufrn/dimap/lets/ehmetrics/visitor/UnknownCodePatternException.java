package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

public abstract class UnknownCodePatternException extends RuntimeException
{
	private Node node;
	
	public UnknownCodePatternException (String msg, Node node)
	{
		super (msg);
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
