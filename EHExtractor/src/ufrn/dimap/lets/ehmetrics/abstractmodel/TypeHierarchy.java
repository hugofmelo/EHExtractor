package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import ufrn.dimap.lets.ehmetrics.ThinkLaterException;
import ufrn.dimap.lets.ehmetrics.visitor.UnresolvedTypeException;

/**
 * Uma hierarquia de classes que é gerada duranter o parser de arquivos Java de um projeto.
 * Tipos que não conseguem ser resolvidos pelo SymbolSolver entram na hierarquia como subtipos
 * de java.lang.Object caso allowUnresolved é true. Nesse caso, os tipos apresentam informações de Origin e ClassType incompletas.
 * 
 * A ordem em que os arquivos são parseados influencia a hierarquia. Se o source de uma exceção que herda de
 * uma exceção não resolvida for parseado antes de qualquer referencia a ela (em um throw ou catch), o tipo não
 * será reconhecido como uma exceção. Se mais tarde esse tipo for usado em um throw ou catch, a hierarquia pode ser
 * externamente atualizada.
 * */
public class TypeHierarchy implements Iterable<Type> {

	private Type typeRoot;
	private boolean allowUnresolvedTypes;

	public TypeHierarchy (boolean allowUnresolved)
	{
		// São iniciados os tipos Object e Throwable. Object para ser a raiz da hierarquia. E Throwable para tentar corrigir um bug que faz com que algumas libs o considerem como Origin.Library.
		Type object = new Type();
		object.setQualifiedName(Object.class.getCanonicalName());
		object.setClassType (ClassType.NO_EXCEPTION);
		object.setOrigin(TypeOrigin.JAVA);

		Type throwable = new Type();
		throwable.setQualifiedName(Throwable.class.getCanonicalName());
		throwable.setClassType (ClassType.CHECKED_EXCEPTION);
		throwable.setOrigin(TypeOrigin.JAVA);

		object.getSubTypes().add(throwable);
		throwable.setSuperType(object);

		this.typeRoot = object;
		this.allowUnresolvedTypes = allowUnresolved;
	}

	/**
	 * Find or create a type in the hierarchy.
	 * 
	 * If the ClassOrInterfaceType can be resolved, create a resolved (with declaration) Type.
	 * If the ClassOrInterfaceType cant be resolved, and if unresolved types are allowed, create a unresolved Type.
	 * 
	 * @throws UnresolvedTypeException if the declaration was not resolved and allowUnresolved is false
	 * */
	public Type findOrCreateType(ClassOrInterfaceType classOrInterfaceType)
	{
		Type type = null;

		try
		{
			type = this.findOrCreateResolvedType(classOrInterfaceType.resolve().getTypeDeclaration().asClass());
		}
		catch ( UnsolvedSymbolException | UnsupportedOperationException e )
		{
			if ( allowUnresolvedTypes )
			{
				type = this.findOrCreateUnresolvedType (classOrInterfaceType);
			}
			else
			{
				throw new UnresolvedTypeException ("Referencia a um tipo que não pôde ser resolvido: " + classOrInterfaceType + ".", classOrInterfaceType, e);
			}
		}
		
		return type;
	}

	/**
	 * Find or create a type in this hierarchy from its declaration.
	 * */
	public Type findOrCreateResolvedType(ResolvedClassDeclaration classDeclaration)
	{
		return findTypeByName (classDeclaration.getQualifiedName())
				.orElseGet(() -> createResolvedType(classDeclaration, null));
	}

	/**
	 * Find or create a type in this hierarchy from its ClassOrInterfaceType.
	 * */
	private Type findOrCreateUnresolvedType(ClassOrInterfaceType classOrInterfaceType)
	{
		return findTypeByName (classOrInterfaceType.getNameAsString())
				.orElseGet(() -> createUnresolvedType(classOrInterfaceType));
	}

	/**
	 * Create a resolved type in this hierarchy.
	 * 
	 * If the ancestors of this type could not be resolved, and if allowedUnresolved, the new type is
	 * created as subtype of Object with a UNRESOLVED ClassType.
	 * 
	 * @throws UnresolvedTypeException if the ancestors could not be resolved and AllowUnresolved is false
	 * */
	private Type createResolvedType(ResolvedClassDeclaration classDeclaration, Type parent)
	{
		Type newType;

		newType = initResolvedType(classDeclaration);

		if ( parent != null )
		{
			newType.setClassType (ClassType.resolveClassType (newType.getQualifiedName(), parent));
			parent.getSubTypes().add(newType);
			newType.setSuperType(parent);
		}
		else
		{
			List<ResolvedReferenceType> classAncestors;
			try
			{
				classAncestors = classDeclaration.getAllSuperClasses();
				newType.setClassType (ClassType.resolveClassType (newType.getQualifiedName(), classAncestors));
				this.setTypePositionInHierarchy(newType, classAncestors);
			}
			catch (UnsolvedSymbolException e)
			{
				if ( allowUnresolvedTypes )
				{
					newType.setClassType(ClassType.UNRESOLVED);
					this.setTypePositionInHierarchy(newType, this.typeRoot);
				}
				else
				{
					throw new ThinkLaterException("Ancestrais de um tipo não encontrados.", e);
				}
			}
		}

		return newType;
	}

	/**
	 * Create an unresolved type. The ancestors of this type, the qualified name and origin can not
	 * be resolved. The new type is put as subtype of Object and has a UNRESOLVED_EXCEPTION ClassType
	 * because this type was referenced in a throw or catch (create a type from its declaration always
	 * result in a resolved type).
	 * */
	private Type createUnresolvedType(ClassOrInterfaceType classOrInterfaceType)
	{
		Type newType;

		newType = initUnresolvedType(classOrInterfaceType);

		this.setTypePositionInHierarchy(newType, this.typeRoot);

		return newType;
	}

	/**
	 * Init a Type which node was resolved. A resolved node is guaranteed to have qualified name and origin.
	 * */
	private Type initResolvedType(ResolvedClassDeclaration classDeclaration)
	{		
		Type newType = new Type ();
	
		newType.setQualifiedName (classDeclaration.getQualifiedName());
		newType.setOrigin (TypeOrigin.resolveTypeOrigin(classDeclaration));
	
		return newType;
	}

	/**
	 * Init a unresolved type.
	 * */
	private Type initUnresolvedType(ClassOrInterfaceType classOrInterfaceType)
	{
		Type newType = new Type ();

		newType.setQualifiedName (classOrInterfaceType.getNameAsString());
		newType.setOrigin (TypeOrigin.UNRESOLVED);
		newType.setClassType(ClassType.UNRESOLVED_EXCEPTION);

		return newType;
	}

	/**
	 * Find a type in the hierarchy in DFS.
	 * */
	public Optional<Type> findTypeByName(String typeName)
	{
		for ( Type type : this )
		{
			if ( type.getQualifiedName().equals(typeName))
			{
				return Optional.of(type);
			}
		}
		
		return Optional.empty();
	}

	/**
	 * Set a new Type in the hierarchy in the right position.
	 * 
	 * To navigate to there, the ancestors must be resolved.
	 * */
	private void setTypePositionInHierarchy (Type newType, List<ResolvedReferenceType> ancestors)
	{
		Type superType;
		Type subtype;

		ResolvedReferenceType ancestor;

		// O ultimo type da list é Object, então o processamento deve ocorrer do penultimo elemento até o primeiro.
		superType = this.typeRoot;
		for ( int i = ancestors.size() - 2 ; i >= 0 ; i-- )
		{
			ancestor = ancestors.get(i);
			subtype = superType.getSubTypeWithName (ancestor.getQualifiedName());

			if ( subtype == null )
			{
				subtype = createResolvedType(ancestor.getTypeDeclaration().asClass(), superType);
			}

			superType = subtype;
		}

		superType.getSubTypes().add(newType);
		newType.setSuperType(superType);
	}

	/**
	 * Set a new Type in the hierarchy from knowing his direct parent Type.
	 * */
	private void setTypePositionInHierarchy (Type newType, Type parent)
	{
		parent.getSubTypes().add(newType);
		newType.setSuperType(parent);
	}

	/**
	 * List of all Types in this hierarchy in FDS order.
	 * */
	public List<Type> listTypes ()
	{
		List<Type> types = new ArrayList<>();
		
		this.iterator().forEachRemaining(types::add);

		return types;
	}
	
	/**
	 * Return all Types in DFS order and with identation.
	 * */
	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder();

		toString(this.typeRoot, "", output);

		return output.toString();
	}

	private void toString ( Type type, String level, StringBuilder stringBuilder )
	{
		stringBuilder.append(level+type.toString()+"\n");

		for (Type subtype : type.getSubTypes())
		{
			toString (subtype, level+"\t", stringBuilder);
		}
	}

	@Override
	public Iterator<Type> iterator()
	{
		return new TypeHierarchyIterator();
	}
	
	/**
	 * Navigates in the TypeHierarchy in DFS order.
	 * */
	private class TypeHierarchyIterator implements Iterator<Type>
	{
		private Deque <Type> typesStack;
		
		public TypeHierarchyIterator()
		{
			this.typesStack = new ArrayDeque<>();
			
			this.typesStack.push(TypeHierarchy.this.typeRoot);
		}
		
		@Override
		public boolean hasNext()
		{
			return !typesStack.isEmpty();
		}

		@Override
		public Type next()
		{
			if ( this.hasNext() )
			{
				Type next = this.typesStack.pop();
				
				this.typesStack.addAll(next.getSubTypes());
				
				return next;
			}
			
			throw new NoSuchElementException();
		}

	}
}


