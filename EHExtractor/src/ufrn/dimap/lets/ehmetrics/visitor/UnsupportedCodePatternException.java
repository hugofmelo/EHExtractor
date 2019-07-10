package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.Node;

public class UnsupportedCodePatternException extends VisitorException 
{
	private static final long serialVersionUID = 1L;
	
	public UnsupportedCodePatternException (String message, Node node)
	{
		super (message, node);
	}
	
	public UnsupportedCodePatternException (String message, Node node, Throwable exception)
	{
		super (message, node, exception);
	}
}
