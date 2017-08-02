package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.ast.Node;

public abstract class AbstractEHModelElement
{
	// TODO remover a referencia a Node para ter um modelo mais leve e desacoplado
	private 	Node				node;
	
	public AbstractEHModelElement ( Node node )
	{
		this.node = node;
	}
	
	public Node getNode()
	{
		return this.node;
	}
	
	public void setNode (Node node)
	{
		this.node = node;
	}
	
	public String toString ()
	{
		return this.node.toString();
	}
}
