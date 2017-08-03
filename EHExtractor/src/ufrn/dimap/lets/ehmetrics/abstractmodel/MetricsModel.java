package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;

import ufrn.dimap.lets.ehmetrics.Util;

public class MetricsModel
{
	//private static MetricsModel instance = null;
	
	private Type typeRoot;
	private List<Signaler> signalers;
	//private List<TryEntry> tries;
	private List<Handler> handlers;
	//private List<FinallyEntry> finallies;
	//private int rethrows;
	//private int wrappings;
	
	public MetricsModel ()
	{
		this.typeRoot = new Type(null, Object.class.getCanonicalName(), ExceptionType.NO_EXCEPTION, TypeOrigin.JAVA);
		
		signalers = new ArrayList<Signaler> ();
		//tries = new ArrayList<TryEntry> ();
		handlers = new ArrayList<Handler> ();
		//finallies = new ArrayList<FinallyEntry> ();

		
		//rethrows = 0;
		//wrappings = 0;
		
	}
	/*
	public static MetricsModel getInstance ()
	{
		if ( instance == null )
		{
			instance = new MetricsModel();
		}
		
		return instance;
	}
	*/
	/*
	public static void clearInstance ()
	{
		instance = new MetricsModel(); 
	}
	*/
	// Os tipos são armazenados em um estrutura hierarquica, tendo java.lang.Object como raiz.
	public Type getRoot()
	{
		return this.typeRoot;
	}	
	
	public Type findOrCreate(Node node, ReferenceType referenceType)
	{
		return this.findOrCreate(node, referenceType.getTypeDeclaration());
	}
	
	public Type findOrCreate(Node node, ReferenceTypeDeclaration referenceTypeDeclaration)
	{
		Stack<ReferenceTypeDeclaration> classesStack = Util.getClassAncestorsNames(referenceTypeDeclaration);
		
		return this.findOrCreate(node, classesStack);
	}
	
	private Type findOrCreate(Node node, Stack<ReferenceTypeDeclaration> classesStack)
	{	
		Type auxType;
		String className;
		boolean found;
		
		
		// O primeiro type da pilha é sempre Object. Como o Model já é instanciado com Object, ele é retirado da pilha.
		classesStack.pop();
		
		auxType = this.typeRoot;
		while ( classesStack.isEmpty() == false )
		{
			className = classesStack.peek().getQualifiedName();
			
			found = false;
			for ( Type child : auxType.getSubTypes() )
			{
				if ( child.getQualifiedName().equals(className) )
				{
					auxType = child;
					found = true;
					break;
				}
			}
			
			// O type ja existe na hierarquia. Descer um nivel.
			if ( !found )
			{
				ExceptionType exceptionType = Util.resolveExceptionType(className, auxType);
				TypeOrigin typeOrigin = Util.resolveTypeOrigin(classesStack.peek());
				
				Type newType = new Type(null, className, exceptionType, typeOrigin);
				
				auxType.getSubTypes().add(newType);
				newType.setSuperType(auxType);
				auxType = newType;
			}
			
			classesStack.pop();
		}
		
		if ( auxType.getNode() == null )
		{
			auxType.setNode(node);
		}
			
		
		return auxType;
	}
	
	/*
	public void findOrCreateR(Node node, ReferenceType referenceType)
	{
		List<ReferenceType> ancestors = referenceType.getAllClassesAncestors();
		Stack<ReferenceType> typesStack = new Stack<ReferenceType>();
		
		// Os types estão em ordem contrária. Usar a pilha para inverter.
		typesStack.push(referenceType);
		for ( ReferenceType t : ancestors )
		{
			typesStack.push(t);
		}
		
		addTypeRR (node, this.typeRoot, typesStack);
	}
	
	private void addTypeRR(Node node, Type type, Stack<ReferenceType> typesStack)
	{
		ReferenceType peek;
				
		// O primeiro type da pilha é sempre Object. Ele é processado somente se root for null.
		if ( typesStack.isEmpty() == false )
		{
			peek = typesStack.pop(); 
			
			if ( this.typeRoot == null)
			{
				this.typeRoot = new Type ( null, peek.getQualifiedName(), Util.getExceptionType(peek), null);
				addTypeRR(node, this.typeRoot, typesStack);
			}
			else
			{
				boolean found = false;
				for ( Type child : type.getSubTypes() )
				{
					if ( child.getQualifiedName().equals(peek.getQualifiedName()) )
					{
						type = child;
						found = true;
						break;
					}
				}
				
				// O type ja existe na hierarquia. Descer um nivel.
				if ( !found )
				{
					Type newType = new Type(null, peek.getQualifiedName(), Util.getExceptionType(peek), null);
					type.getSubTypes().add(newType);
					newType.setSuperType(type);
					type = newType;
				}
				
				addTypeRR(node, type, typesStack);
			}
		}
		else
		{
			type.setNode(node);
		}
	}
	*/
	
	/*public SignalerEntry addSignaler (ThrowStatement throwNode)
	{
		SignalerEntry signaler = new SignalerEntry (throwNode);
		this.signalers.add(signaler);
		
		// Associação as entidades entre si
		//TypeEntry signaledType = this.findOrCreateType(signaler.signaledException);
		//signaledType.signalers.add(signaler);
		
		return signaler;
	}

	public TryEntry addTry(TryStatement tryNode)
	{
		TryEntry tryEntry = new TryEntry(tryNode);
		this.tries.add(tryEntry);
		return tryEntry;
	}
	*/
	
	public Signaler addSignaler(ThrowStmt throwStatement, Type thrownType, SignalerType type)
	{
		Signaler signaler = new Signaler (throwStatement, thrownType, type);
		
		this.signalers.add(signaler);
		
		return signaler;
	}
	
	public Handler addHandler(CatchClause catchNode, List<Type> types)
	{
		Handler handler = new Handler (catchNode, types);
		
		this.handlers.add(handler);
		
		return handler;
	}
	
	
	
	public List<Type> listTypes ()
	{
		List<Type> types = new ArrayList<Type>();
		
		types.add(this.typeRoot);
		types.addAll(this.typeRoot.getAllSubTypes());
		
		return types;
	}

	/*
	public FinallyEntry addFinally(Block finallyBlock)
	{
		FinallyEntry finallyEntry = new FinallyEntry (finallyBlock);
		this.finallies.add(finallyEntry);
		return finallyEntry;
	}
	*/
	
	public List<Signaler> getSignalers()
	{
		return this.signalers;
	}
	/*
	public List<TryEntry> getTries()
	{
		return this.tries;
	}
	*/
	public List<Handler> getHandlers()
	{
		return this.handlers;
	}
	/*
	public List<FinallyEntry> getFinallies()
	{
		return this.finallies;
	}

	public int getRethrows ()
	{
		return this.rethrows;
	}
	
	public void incrementRethrows ()
	{
		this.rethrows++;
	}

	public int getWrappings ()
	{
		return this.wrappings;
	}
	
	public void incrementWrappings ()
	{
		this.wrappings++;
	}
	

	// Esse método lança IllegalArgumentException se type não for uma classe.
	private static Stack<ITypeBinding> createTypeHierarchy (ITypeBinding type)
	{		
		if ( type.isClass() )
		{
			Stack<ITypeBinding> typesStack = new Stack <ITypeBinding> ();
			
			while ( !type.getQualifiedName().equals("java.lang.Object") )
			{
				typesStack.push(type);
				type = type.getSuperclass();
			}
			typesStack.push(type);
			
			return typesStack;
		}
		else
		{
			throw new IllegalArgumentException ("É esperada uma classe.\n\nARGUMENT: type\nVALUE: " + type.toString());
		}
	}
	*/

	public int computeHandledTypes()
	{
		Set <Type> handledTypes = new HashSet <Type> ();
		
		for ( Handler h : this.handlers )
		{
			for ( Type t : h.getExceptions() )
			{
				handledTypes.add(t);
			}
		}
		
		return handledTypes.size();
	}
	
	public int computeTotalAutoCompleteHandlers()
	{
		int totalAutoCompleteHandlers = 0;
		
		for ( Handler h : this.handlers )
		{
			if (h.isAutoComplete())
			{
				totalAutoCompleteHandlers++;
			}
		}
		
		return totalAutoCompleteHandlers;
	}
}
