package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;

public class UnresolvedTypeException extends VisitorException
{	
	public UnresolvedTypeException(String message, Type unknownType)
	{
		super (message, unknownType);
	}
	
	public UnresolvedTypeException(String message, Type unknownType, Throwable e)
	{
		super (message, unknownType, e);
	}
}
