package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.List;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;

public class NamedHandler
{
	private List<Type> types;
	private String name;
	
	public NamedHandler (List<Type> types, String name)
	{
		this.types = types;
		this.name = name;
	}

	public List<Type> getTypes() {
		return types;
	}

	public String getName() {
		return name;
	}	
}
