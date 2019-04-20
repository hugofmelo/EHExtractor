package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistClassDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;

import ufrn.dimap.lets.ehmetrics.ThinkLaterException;
import ufrn.dimap.lets.ehmetrics.analyzer.Util;

/**
 * Uma hierarquia de classes que é gerada duranter o parser de arquivos Java de um projeto.
 * Tipos que não conseguem ser resolvidos pelo SymbolSolver entram na hierarquia como subtipos
 * de java.lang.Object e apresentam informações de Origin e ClassType incompletas.
 * 
 * A ordem em que os arquivos são parseados influencia a hierarquia. Se o source de uma exceção que herda de
 * uma exceção não resolvida for parseado antes de qualquer referencia a ela (em um throw ou catch), o tipo não
 * será reconhecido como uma exceção.
 * */
public class TypeHierarchy {

	private Type typeRoot;

	public TypeHierarchy ()
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
	}

	public Type getRoot()
	{
		return this.typeRoot;
	}

	/**
	 * Create and return a new type in this hierarchy. If parent is not provided, the ancestors of referenceType must be 
	 * resolved. If the resolution is not possible, the new type is created as Object subtype.
	 * */
	private Type createType(ResolvedReferenceType referenceType, Type parent)
	{
		Type newType;

		if (referenceType.getTypeDeclaration() != null)
		{
			newType = createOrUpdateTypeFromTypeDeclaration(referenceType.getTypeDeclaration(), parent);
		}
		else
		{
			throw new ThinkLaterException("Is this even possible?");
			/*
			newType = createUnresolvedType ();
			this.setTypePositionInHierarchy(newType, this.typeRoot);
			 */
		}

		return newType;
	}

	/**
	 * Create or update a type in this hierarchy. If parent is not provided, the ancestors of referenceType must be 
	 * resolved. If the resolution is not possible, the new type is created as subtype of Object.
	 * */
	public Type createOrUpdateTypeFromTypeDeclaration(ResolvedReferenceTypeDeclaration referenceTypeDeclaration, Type parent)
	{
		/*
		TODO
		PROCURA O TIPO NA HIERARQUIA
		SE ACHAR, UPDATE FILE, NODE, CLASSTYPE E ORIGIN?
		SENÃO ACHAR, CRIA
		 */


		Type newType;

		ResolvedClassDeclaration classDeclaration = referenceTypeDeclaration.asClass();

		newType = initTypeFromClassDeclaration(classDeclaration);

		List<ResolvedReferenceType> classAncestors;
		try
		{
			classAncestors = classDeclaration.getAllSuperClasses();
			newType.setClassType (resolveClassType (newType.getQualifiedName(), classAncestors));
			this.setTypePositionInHierarchy(newType, classAncestors);
		}
		catch (UnsolvedSymbolException e)
		{
			newType.setClassType (ClassType.UNRESOLVED);
			this.setTypePositionInHierarchy(newType, this.typeRoot);			
		}

		return newType;
	}

	/**
	 * Adding a type from class declaration is guaranteed to have node and origin.
	 * */
	private Type initTypeFromClassDeclaration(ResolvedClassDeclaration classDeclaration)
	{		
		Type newType = new Type ();

		newType.setQualifiedName (classDeclaration.getQualifiedName());
		newType.setOrigin (resolveTypeOrigin(classDeclaration));

		return newType;
	}

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
				subtype = createType(ancestor, superType);
			}

			superType = subtype;
		}

		superType.getSubTypes().add(newType);
		newType.setSuperType(superType);
	}

	private void setTypePositionInHierarchy (Type newType, Type parent)
	{
		parent.getSubTypes().add(newType);
		newType.setSuperType(parent);
	}

	public List<Type> listTypes ()
	{
		List<Type> types = new ArrayList<>();

		types.add(this.typeRoot);
		types.addAll(this.typeRoot.getAllSubTypes());

		return types;
	}

	private static ClassType resolveClassType (String className)
	{
		if ( className.equals(Throwable.class.getCanonicalName()) ||
				className.equals(Exception.class.getCanonicalName())	)
		{
			return ClassType.CHECKED_EXCEPTION;
		}
		else if ( className.equals(RuntimeException.class.getCanonicalName()) )
		{
			return ClassType.UNCHECKED_EXCEPTION;
		}
		else if ( className.equals(Error.class.getCanonicalName()) )
		{
			return ClassType.ERROR_EXCEPTION;
		}
		else
		{
			return ClassType.UNRESOLVED;
		}
	}

	private static ClassType resolveClassType (String className, Type parent)
	{
		ClassType classType = resolveClassType (className);

		if ( classType != ClassType.UNRESOLVED )
		{
			return classType;
		}
		else
		{
			return resolveClassType (parent.getQualifiedName());
		}
	}

	private static ClassType resolveClassType(String classQualifiedName, List<ResolvedReferenceType> ancestors)
	{
		ClassType classType = resolveClassType (classQualifiedName);

		if ( classType != ClassType.UNRESOLVED )
		{
			return classType;
		}
		else
		{
			// The last position has Object type
			for ( int i = 0 ; i < ancestors.size() - 1 ; i++)
			{
				classType = resolveClassType (ancestors.get(i).getQualifiedName());
				if ( classType != ClassType.UNRESOLVED )
				{
					return classType;
				}
			}
		}
		
		return ClassType.NO_EXCEPTION;
	}


	private static TypeOrigin resolveTypeOrigin(ResolvedClassDeclaration declaration)
	{
		String className = declaration.getQualifiedName();

		// A ordem de verificação é importante.
		if ( declaration instanceof ReflectionClassDeclaration )
		{
			return TypeOrigin.JAVA;
		}
		else if ( declaration instanceof JavaParserClassDeclaration )
		{
			return TypeOrigin.SYSTEM;
		}
		else if ( className.startsWith("android.") )
		{
			return TypeOrigin.ANDROID;
		}
		else if ( declaration instanceof JavassistClassDeclaration )
		{
			return TypeOrigin.LIBRARY;
		}
		else
		{
			throw new IllegalStateException("Um tipo indefinido encontrado.");
		}
	}

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
}
