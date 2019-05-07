package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;

public class Handler extends AbstractEHModelElement
{
	private List<Type> exceptions;
	
	private List<Signaler> escapingSignalers; // Every signaler in the context of this handler
	
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
	
	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		String separator = "";
		
		for ( Type t : this.exceptions)
		{
			result.append(separator + t.getQualifiedName());
			separator = " | ";
		}
		
		return result.toString();
	}

	public List<Signaler> getEscapingSignalers() {
		return escapingSignalers;
	}

	public void setEscapingSignalers(List<Signaler> signalers) {
		this.escapingSignalers = signalers;
	}
}
