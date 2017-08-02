package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.Node;

public class UnknownSignalerPatternException extends UnknownCodePatternException
{
	
	public UnknownSignalerPatternException(String msg, Node node)
	{
		super(msg, node);
	}

}
