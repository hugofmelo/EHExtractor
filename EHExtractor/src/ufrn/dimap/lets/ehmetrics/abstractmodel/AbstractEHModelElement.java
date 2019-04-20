package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.io.File;

import com.github.javaparser.ast.Node;

public abstract class AbstractEHModelElement
{
	private File file;
	private Node node;
	
	public AbstractEHModelElement ()
	{
		this.file = null;
		this.node = null;
	}
	
	/**
	 * Por causa da ordem em que os arquivos java s�o parseados, um elemento pode n�o ter sido ainda resolvido. A resolu��o ocorre somente quando o source code daquele elemento � parseado, o que seta o arquivo e node para not null.
	 * */
	public boolean isFullyResolved()
	{
		return this.file != null && this.node != null;
	}

	public Node getNode()
	{
		return this.node;
	}

	public void setNode (Node node)
	{
		this.node = node;
	}

	public void setFile (File javaFile)
	{
		this.file = javaFile;
	}
	
	public File getFile ()
	{
		return this.file;
	}

	public int getInitFileNumber ()
	{
		return this.node.getBegin().get().line;
	}

	public String toString ()
	{
		return this.node.toString();
	}
}
