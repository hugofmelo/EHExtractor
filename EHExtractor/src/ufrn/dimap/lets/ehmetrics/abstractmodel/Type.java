package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;

public class Type extends AbstractEHModelElement
{
	private String qualifiedName;
	
	private Type superType;
	private List<Type> subtypes;
	
	// Tipo da exceção: NO_EXCEPTION, CHECKED_EXCEPTION, UNCHECKED_EXCEPTION, ERROR_EXCEPTION
	private TypeOrigin origin;
	private ExceptionType type;

	// Uma referencia direta aos signalers e handlers deste type
	private List<Signaler> signalers;
	private List<Handler> handlers;
	
	
	public Type (String filePath, Node node, String typeName, ExceptionType type, TypeOrigin origin)
	{
		super(filePath, node);
		this.qualifiedName = typeName;
		this.type = type;
		this.origin = origin;
		
		this.subtypes = new ArrayList<Type>();
		this.superType = null;
		
		this.signalers = new ArrayList<Signaler>();
		this.handlers = new ArrayList<Handler>();
	}
	
	// Retorna todos os subtipos de this, em DFS order
	public List<Type> getAllSubTypes()
	{
		List<Type> result = new ArrayList<Type>();
		
		for ( Type type : this.subtypes )
		{
			result.add(type);
			result.addAll(type.getAllSubTypes());
		}
		
		return result;
	}
	
	public List<Type> getAllAncestors()
	{
		List <Type> types = new ArrayList<Type>();
		Type aux = this;
		
		while ( aux.superType != null )
		{
			types.add(aux.superType);
			aux = aux.superType;
		}
			
		return types;
	}
	
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	public ExceptionType getType ()
	{
		return this.type;
	}
	
	public TypeOrigin getOrigin ()
	{
		return this.origin;
	}
	
	public List<Type> getSubTypes()
	{
		return this.subtypes;
	}

	public void setSuperType(Type superType)
	{
		this.superType = superType;
	}
	
	public void addSignaler(Signaler signaler)
	{
		this.signalers.add(signaler);
	}
	
	public void addHandler(Handler handler)
	{
		this.handlers.add(handler);
	}
	
	public List<Signaler> getSignalers()
	{
		return this.signalers;
	}
	
	public List<Handler> getHandlers()
	{
		return this.handlers;
	}
	
	public String toString()
	{
		return this.qualifiedName+"::"+ this.type +"::"+this.origin;
	}

}

