package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.List;

import com.github.javaparser.ast.stmt.CatchClause;

public class Handler extends AbstractEHModelElement
{
	private List<Type> exceptions;
	
	private boolean isAutoComplete;
	private boolean isEmpty;
	
	public Handler( String filePath, CatchClause node, List<Type> handledExceptions )
	{
		super(filePath, node);
		this.exceptions = handledExceptions;
		
		for ( Type type : handledExceptions )
		{
			type.addHandler (this);
		}
		
		this.isAutoComplete = false;
	}
	
	public List<Type> getExceptions()
	{
		return this.exceptions;
	}
	
	public String toString()
	{
		String result = "";
		String separator = "";
		
		for ( Type t : this.exceptions)
		{
			result += separator + t.getQualifiedName();
			separator = " | ";
		}
		
		return result;
	}
	
	public void setAutoComplete(boolean isAutoComplete)
	{
		this.isAutoComplete = isAutoComplete;
	}
	
	public boolean isAutoComplete()
	{
		return this.isAutoComplete;
	}
	
	public void setEmpty(boolean isEmpty)
	{
		this.isEmpty = isEmpty;
	}
	
	public boolean isEmpty()
	{
		return this.isEmpty;
	}
}
