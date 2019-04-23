package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistClassDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;

public enum TypeOrigin
{
	JAVA ("Java"),
	SYSTEM ("System"),
	LIBRARY ("Library"),
	ANDROID ("Android"),
	UNRESOLVED ("Unresolved")
	;
	
	private final String text;
	private TypeOrigin (final String text)
	{
		this.text = text;
	}
	
	@Override
	public String toString ()
	{
		return this.text;
	}
	
	public static TypeOrigin resolveTypeOrigin(ResolvedClassDeclaration classDeclaration)
	{
		// A ordem de verificação é importante.
		if ( classDeclaration instanceof ReflectionClassDeclaration )
		{
			return TypeOrigin.JAVA;
		}
		else if ( classDeclaration instanceof JavaParserClassDeclaration )
		{
			return TypeOrigin.SYSTEM;
		}
		else if ( classDeclaration.getQualifiedName().startsWith("android.") )
		{
			return TypeOrigin.ANDROID;
		}
		else if ( classDeclaration instanceof JavassistClassDeclaration )
		{
			return TypeOrigin.LIBRARY;
		}
		else
		{
			throw new IllegalStateException("Um tipo indefinido encontrado.");
		}
	}
	
	
}
