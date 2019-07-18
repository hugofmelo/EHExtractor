package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.io.File;

import com.github.javaparser.ast.Node;

public abstract class AbstractEHModelElement
{
	private File file;
	//private Node node;
	
	public AbstractEHModelElement ()
	{
		this.file = null;
		//this.node = null;
	}

	/*
	public Node getNode()
	{
		return this.node;
	}

	public void setNode (Node node)
	{
		this.node = node;
	}
	*/

	public void setFile (File javaFile)
	{
		this.file = javaFile;
	}
	
	public File getFile ()
	{
		return this.file;
	}

	/*
	public int getInitFileNumber ()
	{
		if ( this.node.getBegin().isPresent() )
		{
			return this.node.getBegin().get().line;
		}
		else
		{
			throw new IllegalStateException ();
		}
	}

	public String toString ()
	{
		return this.node.toString();
	}
	*/
}
