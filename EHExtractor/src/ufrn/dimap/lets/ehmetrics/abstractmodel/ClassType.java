package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.List;

import com.github.javaparser.resolution.types.ResolvedReferenceType;

public enum ClassType
{
	NO_EXCEPTION ("No exception"),
	CHECKED_EXCEPTION ("Checked exception"),
	UNCHECKED_EXCEPTION ("Unchecked exception"),
	ERROR_EXCEPTION ("Error"),
	UNRESOLVED_EXCEPTION ("Unresolved exception"), // The hierarchy could not be resolved, but it was used in a throw statement, in a catch block or in a class declaration which ends with "Exception".
	UNRESOLVED ("Unresolved")
	;
	
	private final String text;
	private ClassType (final String text)
	{
		this.text = text;
	}
	
	public String toString ()
	{
		return this.text;
	}
	
	public static ClassType resolveClassType(String classQualifiedName, List<ResolvedReferenceType> ancestors)
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
	
	public static ClassType resolveClassType(String qualifiedName, Type parent)
	{
		ClassType classType = resolveClassType (qualifiedName);
	
		if ( classType != ClassType.UNRESOLVED )
		{
			return classType;
		}
		else
		{
			return parent.getClassType();
		}
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
}
