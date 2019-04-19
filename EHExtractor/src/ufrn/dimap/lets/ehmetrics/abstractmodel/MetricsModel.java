package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import ufrn.dimap.lets.ehmetrics.analyzer.Util;

public class MetricsModel
{
	private Type typeRoot;
	private List<Signaler> signalers;
	private List<Handler> handlers;
	
	public int totalCatches;
	public int emptyHandlers;
	public int genericCatch;

	public int totalThrows;
	public int genericThrows;
	
	public MetricsModel ()
	{	
		signalers = new ArrayList<> ();
		handlers = new ArrayList<> ();

		// São iniciados os tipos Object e Throwable. Object para ser a raiz da hierarquia. E Throable para tentar corrigir um bug que faz com que algumas libs o considerem como Origin.Library.
		Type object = new Type(null, null, Object.class.getCanonicalName(), ExceptionType.NO_EXCEPTION, TypeOrigin.JAVA);
		Type throwable = new Type(null, null, Throwable.class.getCanonicalName(), ExceptionType.CHECKED_EXCEPTION, TypeOrigin.JAVA);
		
		object.getSubTypes().add(throwable);
		throwable.setSuperType(object);
		
		this.typeRoot = object;
	}
	
	// Os tipos são armazenados em um estrutura hierarquica, tendo java.lang.Object como raiz.
	public Type getRoot()
	{
		return this.typeRoot;
	}	
	
	public Type findOrCreate(Node node, ResolvedReferenceType referenceType)
	{
		return this.findOrCreate(node, referenceType.getTypeDeclaration());
	}
	
	public Type findOrCreate(Node node, ResolvedTypeDeclaration referenceTypeDeclaration)
	{
		Stack<ResolvedTypeDeclaration> classesStack = Util.getClassAncestorsNames(referenceTypeDeclaration);
		
		return this.findOrCreate(node, classesStack);
	}
	
	private Type findOrCreate(Node node, Stack<ResolvedTypeDeclaration> classesStack)
	{	
		Type auxType;
		String className;
		boolean found;
		
		
		// O primeiro type da pilha é sempre Object. Como o Model já é instanciado com Object, ele é retirado da pilha.
		classesStack.pop();
		
		auxType = this.typeRoot;
		while ( !classesStack.isEmpty() )
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
			
			// O type não existe na hierarquia. Criar e descer um nivel.
			if ( !found )
			{
				ExceptionType exceptionType = Util.resolveExceptionType(className, auxType);
				TypeOrigin typeOrigin = Util.resolveTypeOrigin(classesStack.peek());
				
				Type newType = new Type(null, null, className, exceptionType, typeOrigin);
				
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
	
	public Signaler addSignaler(String filePath, ThrowStmt throwNode, Type thrownType, Type catchedType, SignalerType type)
	{
		Signaler signaler = new Signaler (filePath, throwNode, thrownType, catchedType, type);
		
		this.signalers.add(signaler);
		
		return signaler;
	}
	
	public Handler addHandler(String filePath, CatchClause catchNode, List<Type> types)
	{
		Handler handler = new Handler (filePath, catchNode, types);
		
		this.handlers.add(handler);
		
		return handler;
	}
	
	
	
	public List<Type> listTypes ()
	{
		List<Type> types = new ArrayList<>();
		
		types.add(this.typeRoot);
		types.addAll(this.typeRoot.getAllSubTypes());
		
		return types;
	}
	
	public List<Signaler> getSignalers()
	{
		return this.signalers;
	}

	public List<Handler> getHandlers()
	{
		return this.handlers;
	}
}
