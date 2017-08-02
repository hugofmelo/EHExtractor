package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.ast.Node;

public class Type extends AbstractEHModelElement
{
	private String qualifiedName;
	
	private Type superType;
	private List<Type> subtypes;
	
	// Tipo da exce��o: NO_EXCEPTION, CHECKED_EXCEPTION, UNCHECKED_EXCEPTION, ERROR_EXCEPTION
	private TypeOrigin origin;
	private ExceptionType exceptionType;

	// Uma referencia direta aos signalers e handlers deste type
	private List<Signaler> signalers;
	private List<Handler> handlers;
	
	
	public Type (Node node, String typeName, ExceptionType exceptionType, TypeOrigin origin)
	{
		super(node);
		this.qualifiedName = typeName;
		this.exceptionType = exceptionType;
		this.origin = origin;
		
		this.subtypes = new ArrayList<Type>();
		this.superType = null;
		
		this.signalers = new ArrayList<Signaler>();
		this.handlers = new ArrayList<Handler>();
	}
	
	
	
	public List<Type> getAllSubTypes()
	{
		List <Type> types = new ArrayList<Type>();
		Stack <Type> stack = new Stack<Type> ();
		Type auxType;
		
		for ( Type t : this.subtypes )
		{
			stack.push(t);
		}
		
		
		while ( stack.isEmpty() == false )
		{
			auxType = stack.pop();
			types.add(auxType);
			
			for ( Type t : auxType.subtypes )
			{
				stack.push(t);
			} 
		}
		
		return types;
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
	
	public ExceptionType getExceptionType ()
	{
		return this.exceptionType;
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
		return this.qualifiedName+"::"+ this.exceptionType +"::"+this.origin;
	}

}

