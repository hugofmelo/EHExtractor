package ufrn.dimap.lets.ehmetrics.analyzer;

import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class UnresolvedTypeException extends EHMetricsException
{
	private ClassOrInterfaceType unknownType;
	
	public UnresolvedTypeException(String message, ClassOrInterfaceType unknownType)
	{
		super (message);
		
		this.unknownType = unknownType;
	}
	
	public UnresolvedTypeException(String message, ClassOrInterfaceType unknownType, Throwable e)
	{
		super (message, e);
		
		this.unknownType = unknownType;
	}
	
	public ClassOrInterfaceType getUnknownType() {
		return unknownType;
	}
	
}
