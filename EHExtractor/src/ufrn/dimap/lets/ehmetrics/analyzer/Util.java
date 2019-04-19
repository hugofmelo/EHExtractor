package ufrn.dimap.lets.ehmetrics.analyzer;

import java.util.Stack;

import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistClassDeclaration;

import javassist.NotFoundException;
import ufrn.dimap.lets.ehmetrics.abstractmodel.ExceptionType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;

public class Util
{	
	
	
	public static ExceptionType resolveExceptionType (String className, Type parent)
	{
		if ( className.equals(Throwable.class.getCanonicalName()) ||
			 className.equals(Exception.class.getCanonicalName())	)
		{
			return ExceptionType.CHECKED_EXCEPTION;
		}
		else if ( className.equals(RuntimeException.class.getCanonicalName()) )
		{
			return ExceptionType.UNCHECKED_EXCEPTION;
		}
		else if ( className.equals(Error.class.getCanonicalName()) )
		{
			return ExceptionType.ERROR_EXCEPTION;
		}
		else
		{
			return parent.getType();
		}
	}
	
	public static ExceptionType resolveExceptionType(ResolvedTypeDeclaration referenceTypeDeclaration)
	{
		Stack<ResolvedTypeDeclaration> classesStack = Util.getClassAncestorsNames(referenceTypeDeclaration);
		
		if ( classesStack.peek().getQualifiedName().equals(Object.class.getCanonicalName()) )
		{
			classesStack = Util.reverseStack(classesStack);
		}
		
		String className;
		
		while ( classesStack.peek().getQualifiedName().equals(Object.class.getCanonicalName()) == false )
		{
			className = classesStack.pop().getQualifiedName();
			
			if ( className.equals(Throwable.class.getCanonicalName()) ||
				 className.equals(Exception.class.getCanonicalName()))
			{
				return ExceptionType.CHECKED_EXCEPTION;
			}
			else if ( className.equals(RuntimeException.class.getCanonicalName()) )
			{
				return ExceptionType.UNCHECKED_EXCEPTION;
			}
			else if ( className.equals(Error.class.getCanonicalName()) )
			{
				return ExceptionType.ERROR_EXCEPTION;
			}
		}
		
		return ExceptionType.NO_EXCEPTION;
	}
	
	public static TypeOrigin resolveTypeOrigin(ResolvedTypeDeclaration declaration)
	{
		String className = declaration.getQualifiedName();
		
		// A ordem de verifica��o � importante.
		if ( className.startsWith("java.") || className.startsWith("javax.") )
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
			throw new IllegalStateException("Um tipo indefinido de exce��o encontrado.");
		}
	}
	
	public static Stack<ResolvedTypeDeclaration> getClassAncestorsNames (ResolvedReferenceType type)
	{	
		return Util.getClassAncestorsNames(type.getTypeDeclaration());
		/*
		if ( type.getTypeDeclaration().isClass() == false )
		{
			throw new IllegalArgumentException ("Expected: ReferenceType de uma classe. Given: "+ type.toString());
		}
		else
		{
			Stack<String> typesNames = new Stack<String>();
			
			typesNames.push(type.getQualifiedName());
			
			for ( ReferenceType t : type.getAllClassesAncestors())
			{
				typesNames.push(t.getQualifiedName());
			}
			
			//return Util.reverseStack(typesNames);
			return typesNames;
		}
		*/
	}
	
	public static Stack<ResolvedTypeDeclaration> getClassAncestorsNames (ResolvedTypeDeclaration typeDeclaration)
	{
		if ( typeDeclaration.isClass() == false )
		{
			throw new IllegalArgumentException ("Expected: ReferenceTypeDeclaration de uma classe. Given: "+ typeDeclaration.toString());
		}
		else
		{
			/*
			try
			{
				List<ReferenceType> ancestors = typeDeclaration.getAllAncestors();
			}
			catch (RuntimeException e)
			{
				if (e.getCause() instanceof NotFoundException)
				{
					Logger.getInstance().writeError("Util#getClassAncestorsNames (ReferenceTypeDeclaration) --> Um dos ancestrais (incluindo interfaces) de '" + typeDeclaration.getClassName() + "' n�o foi encontrado.\n");
					throw new IgnoreFileException(e);
				}
				else
				{
					throw e;
				}
			}
			*/
			
			Stack<ResolvedTypeDeclaration> types = new Stack<ResolvedTypeDeclaration>();
			
			types.push(typeDeclaration);
			
			try
			{
				for ( ResolvedReferenceType t : typeDeclaration.containerType().get().getAllAncestors())
				{
					if (t.getTypeDeclaration().isClass())
					{
						types.push(t.getTypeDeclaration());
					}
				}
				
				return types;
			}
			catch (RuntimeException e)
			{
				if ( e.getCause() instanceof NotFoundException )
				{
					throw new UnknownAncestralException("Os ancestrais da classe '" + typeDeclaration.getQualifiedName() + "' n�o foram encontrados.", e);
				}
				else
				{
					throw e;
				}
			}
		}
	}
	
	private static Stack<ResolvedTypeDeclaration> reverseStack( Stack<ResolvedTypeDeclaration> input)
	{
		Stack<ResolvedTypeDeclaration> output = new Stack<>();
		
		while ( input.isEmpty() == false )
		{
			output.push(input.pop());
		}
		
		return output;
	}

	
	
}
