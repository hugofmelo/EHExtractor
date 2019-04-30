package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.stmt.CatchClause;

public class Handler extends AbstractEHModelElement
{
	private List<Type> exceptions;
	
	private List<Signaler> escapingSignalers; // Every signaler in the context of this handler
	
	private boolean isAutoComplete;
	private boolean isEmpty;
	
	public Handler()
	{
		super();
		
		this.exceptions = new ArrayList<>();
		
		this.escapingSignalers = new ArrayList<>();
	}
	
	public List<Type> getExceptions()
	{
		return this.exceptions;
	}
	
	/**
	 * Convenient method to filter exceptions
	 * */
	public List<Type> getExternalExceptions()
	{
		return this.exceptions.stream()
				.filter(t -> t.getOrigin() == TypeOrigin.LIBRARY ||
							 t.getOrigin() == TypeOrigin.UNRESOLVED)
				.collect(Collectors.toList());
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

	public List<Signaler> getEscapingSignalers() {
		return escapingSignalers;
	}

	public void setEscapingSignalers(List<Signaler> signalers) {
		this.escapingSignalers = signalers;
	}
}
