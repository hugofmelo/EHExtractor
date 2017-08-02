package ufrn.dimap.lets.ehmetrics;

import java.util.List;
import java.util.Stack;

import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistClassDeclaration;
import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;

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
			return parent.getExceptionType();
		}
	}
	
	public static ExceptionType resolveExceptionType(ReferenceTypeDeclaration referenceTypeDeclaration)
	{
		Stack<ReferenceTypeDeclaration> classesStack = Util.getClassAncestorsNames(referenceTypeDeclaration);
		
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
	
	public static TypeOrigin resolveTypeOrigin(ReferenceTypeDeclaration declaration)
	{
		if ( declaration instanceof JavaParserClassDeclaration )
		{
			return TypeOrigin.SYSTEM;
		}
		else if ( declaration instanceof ReflectionClassDeclaration )
		{
			return TypeOrigin.JAVA;
		}
		else if ( declaration instanceof JavassistClassDeclaration )
		{
			return TypeOrigin.LIBRARY;
		}
		else
		{
			throw new IllegalStateException("Um tipo indefinido de exceção encontrado.");
		}
	}
	
	public static Stack<ReferenceTypeDeclaration> getClassAncestorsNames (ReferenceType type)
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
	
	public static Stack<ReferenceTypeDeclaration> getClassAncestorsNames (ReferenceTypeDeclaration typeDeclaration)
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
					Logger.getInstance().writeError("Util#getClassAncestorsNames (ReferenceTypeDeclaration) --> Um dos ancestrais (incluindo interfaces) de '" + typeDeclaration.getClassName() + "' não foi encontrado.\n");
					throw new IgnoreFileException(e);
				}
				else
				{
					throw e;
				}
			}
			*/
			
			Stack<ReferenceTypeDeclaration> types = new Stack<ReferenceTypeDeclaration>();
			
			types.push(typeDeclaration);
			
			try
			{
				for ( ReferenceType t : typeDeclaration.getAllAncestors())
				{
					if (t.getTypeDeclaration().isClass())
					{
						types.push(t.getTypeDeclaration());
					}
				}
				
				//return Util.reverseStack(typesNames);
				return types;
			}
			catch (RuntimeException e)
			{
				if ( e.getCause() instanceof NotFoundException )
				{
					throw new AnalyzerException("Os ancestrais da classe '" + typeDeclaration.getQualifiedName() + "' não foram encontrados.", e);
				}
				else
				{
					throw e;
				}
			}
		}
	}
	
	private static Stack<ReferenceTypeDeclaration> reverseStack( Stack<ReferenceTypeDeclaration> input)
	{
		Stack<ReferenceTypeDeclaration> output = new Stack<ReferenceTypeDeclaration>();
		
		while ( input.isEmpty() == false )
		{
			output.push(input.pop());
		}
		
		return output;
	}

	
	
}
