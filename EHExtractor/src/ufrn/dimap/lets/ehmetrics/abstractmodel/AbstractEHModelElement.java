package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.ast.Node;

public abstract class AbstractEHModelElement
{
	// TODO remover a referencia a Node para ter um modelo mais leve e desacoplado
	private 	Node				node;
	private String filePath;
	private int initLineNumber;

	public AbstractEHModelElement ( String filePath, Node node )
	{
		//this.node = node;

		this.filePath = filePath;

		if ( node != null )
		{
			this.initLineNumber = node.getBegin().get().line;
		}
	}

	public Node getNode()
	{
		return this.node;
	}

	public void setNode (Node node)
	{
		this.node = node;
	}

	public String getFilePath ()
	{
		return this.filePath;
	}

	public int getInitFileNumber ()
	{
		return this.initLineNumber;
	}

	public String toString ()
	{
		return this.node.toString();
	}
}
