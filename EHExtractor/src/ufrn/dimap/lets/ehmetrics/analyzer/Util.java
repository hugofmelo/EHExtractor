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
					Logger.getInstance().writeError("Util#getClassAncestorsNames (ReferenceTypeDeclaration) --> Um dos ancestrais (incluindo interfaces) de '" + typeDeclaration.getClassName() + "' não foi encontrado.\n");
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
					throw new UnknownAncestralException("Os ancestrais da classe '" + typeDeclaration.getQualifiedName() + "' não foram encontrados.", e);
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
