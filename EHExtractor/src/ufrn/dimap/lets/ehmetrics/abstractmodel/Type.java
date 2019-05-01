package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;

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
	 * Create a new empty type.
	 * */
	public Type ()
	{
		super();

		this.subtypes = new ArrayList<>();
		this.superType = null;

		this.signalers = new ArrayList<>();
		this.handlers = new ArrayList<>();
	}

	
	
	
	
	// UTIL METHODS **********************************

	/**
	 * Returns all subtypes of this in DFS order.
	 * */
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

	/**
	 * Returns the direct subtype with specified qualified name. Or null if don't exist.
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

	/**
	 * Returns all ancestors of this. Direct super type first.
	 * */
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

	/**
	 * Check if this is a system defined exception type.
	 * 
	 * Util method.
	 * */
	public boolean isSystemExceptionType ()
	{
		return this.origin == TypeOrigin.SYSTEM &&
				(this.classType == ClassType.CHECKED_EXCEPTION ||
				this.classType == ClassType.UNCHECKED_EXCEPTION ||
				this.classType == ClassType.ERROR_EXCEPTION ||
				this.classType == ClassType.UNRESOLVED_EXCEPTION);
	}

	/**
	 * Returns the more distant super type which are still a system type.
	 * 
	 * Return null if the type already is the root system type.
	 * 
	 * @throws IllegalArgumentException if the type is not a exception or is not a system type.
	 * */
	public Type getSystemExceptionRootType()
	{
		if ( this.isSystemExceptionType() )
		{
			Type auxType = this;

			while ( auxType.getSuperType().isSystemExceptionType() )
			{
				auxType = auxType.getSuperType();
			}

			if ( auxType != this )
				return auxType;
			else
				return null;
		}
		else
		{
			throw new IllegalArgumentException ("Type is not a system exception.");
		}
	}

	
	
	
	// GETTERS AND SETTERS ******************************************
	
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;	
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

	/**
	 * Alias no getSignalers.add()
	 * */
	public void addSignaler(Signaler signaler)
	{
		this.signalers.add(signaler);
	}

	/**
	 * Alias no getHandlers.add()
	 * */
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

	public Type getSuperType()
	{
		return this.superType;
	}



	@Override
	public String toString()
	{
		return this.qualifiedName+"::"+ this.classType +"::"+this.origin;
	}


}

