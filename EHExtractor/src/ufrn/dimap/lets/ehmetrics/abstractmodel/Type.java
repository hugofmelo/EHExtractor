package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;

public class Type extends AbstractEHModelElement
{
	private String qualifiedName;
	
	private Type superType;
	private List<Type> subtypes;
	
	private TypeOrigin origin;
	private ClassType classType;

	// Uma referencia direta aos signalers e handlers deste type
	private List<Signaler> signalers;
	private List<Handler> handlers;
	
	/**
	 * Create a new type empty. The attributes must be set.
	 * */
	public Type ()
	{
		super();
		
		this.subtypes = new ArrayList<>();
		this.superType = null;
		
		this.signalers = new ArrayList<>();
		this.handlers = new ArrayList<>();
	}
	
	/**
	 * Returns the subtype with specified qualified name. Or null if don't exist.
	 * */
	public Type getSubTypeWithName(String qualifiedName)
	{
		for ( Type type : this.subtypes )
		{
			if ( type.getQualifiedName().equals(qualifiedName) )
			{
				return type;
			}
		}
		
		return null;
	}
	
	// Retorna todos os subtipos de this, em DFS order
	public List<Type> getAllSubTypes()
	{
		List<Type> result = new ArrayList<>();
		
		for ( Type type : this.subtypes )
		{
			result.add(type);
			result.addAll(type.getAllSubTypes());
		}
		
		return result;
	}
	
	public List<Type> getAllAncestors()
	{
		List <Type> types = new ArrayList<>();
		Type aux = this;
		
		while ( aux.superType != null )
		{
			types.add(aux.superType);
			aux = aux.superType;
		}
			
		return types;
	}
	
	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;	
	}
	
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	public ClassType getClassType ()
	{
		return this.classType;
	}
	
	public void setClassType( ClassType classType )
	{
		this.classType = classType;
	}
	
	public TypeOrigin getOrigin ()
	{
		return this.origin;
	}
	
	public void setOrigin(TypeOrigin origin) {
		this.origin = origin;	
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
		return this.qualifiedName+"::"+ this.classType +"::"+this.origin;
	}


}

