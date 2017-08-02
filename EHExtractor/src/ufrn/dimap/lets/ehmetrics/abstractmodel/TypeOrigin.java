package ufrn.dimap.lets.ehmetrics.abstractmodel;

public enum TypeOrigin
{
	JAVA,
	SYSTEM,
	LIBRARY
	;
	
	public String toString ()
	{
		if ( this == JAVA )
			return "Java";
		else if ( this == SYSTEM )
			return "System";
		else //if ( this == LIBRARY )
			return "Library";
	}
}
