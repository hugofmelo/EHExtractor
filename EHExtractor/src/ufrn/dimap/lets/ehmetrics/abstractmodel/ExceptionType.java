package ufrn.dimap.lets.ehmetrics.abstractmodel;

public enum ExceptionType
{
	NO_EXCEPTION,
	CHECKED_EXCEPTION,
	UNCHECKED_EXCEPTION,
	ERROR_EXCEPTION
	;
	
	public String toString ()
	{
		if ( this == NO_EXCEPTION )
			return "No exception";
		else if ( this == CHECKED_EXCEPTION )
			return "Checked";
		else if ( this == UNCHECKED_EXCEPTION )
			return "Unchecked";
		else // if ( this == ERROR_EXCEPTION )
			return "Error";
	}
}
