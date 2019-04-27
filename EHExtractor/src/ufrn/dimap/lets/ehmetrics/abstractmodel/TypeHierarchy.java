package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.io.File;
import java.nio.channels.NetworkChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.gradle.tooling.exceptions.UnsupportedOperationConfigurationException;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
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

	/**
	 * Find or create a type in the hierarchy.
	 * 
	 * If the ClassOrInterfaceType can be resolved, create a resolved (with declaration) type.
	 * */
	public Type findOrCreateType(ClassOrInterfaceType classOrInterfaceType)
	{
		Type type = null;

		try
		{
			type = this.findOrCreateResolvedType(classOrInterfaceType.resolve().getTypeDeclaration().asClass());
		}
		catch ( UnsolvedSymbolException e )
		{
			type = this.findOrCreateUnresolvedType (classOrInterfaceType);
		}
		
		return type;
	}

	/**
	 * Find or create a type in this hierarchy from its declaration.
	 * */
	public Type findOrCreateResolvedType(ResolvedClassDeclaration classDeclaration)
	{
		Type type = this.findTypeByName (classDeclaration.getQualifiedName());

		if (type == null)
		{
			type = this.createResolvedType(classDeclaration, null);
		}

		return type;
	}

	/**
	 * Create a resolved type in this hierarchy. If the ancestors of this type could not be 
	 * resolved, the new type is created as subtype of Object with a UNRESOLVED ClassType.
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
				newType.setClassType(ClassType.UNRESOLVED);
				this.setTypePositionInHierarchy(newType, this.typeRoot);			
			}
		}

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
	 * Find or create a type in this hierarchy from its ClassOrInterfaceType.
	 * */
	private Type findOrCreateUnresolvedType(ClassOrInterfaceType classOrInterfaceType)
	{
		Type type = this.findTypeByName (classOrInterfaceType.getNameAsString());

		if (type == null)
		{
			type = this.createUnresolvedType(classOrInterfaceType);
		}

		return type;
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
	private Type findTypeByName(String qualifiedName)
	{
		Stack <Type> types = new Stack<>();
		Type auxType;

		types.push(this.typeRoot);

		while ( !types.isEmpty() )
		{
			auxType = types.pop();

			if ( auxType.getQualifiedName().equals(qualifiedName) )
				return auxType;
			else
			{
				for ( Type t : auxType.getSubTypes() )
				{
					types.push(t);
				}
			}
		}

		return null;
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
				subtype = createResolvedType(ancestor.getTypeDeclaration().asClass(), superType);
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
