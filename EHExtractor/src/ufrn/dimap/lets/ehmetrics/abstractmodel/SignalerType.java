package ufrn.dimap.lets.ehmetrics.abstractmodel;

public enum SignalerType
{
	SIMPLE,
	RETHROW,
	WRAPPING,
	UNKNOWN
	;
	
	public String toString ()
	{
		if ( this == SIMPLE )
			return "Simple";
		else if ( this == RETHROW )
			return "Rethrow";
		else if ( this == WRAPPING )
			return "Wrapping";
		else // if ( this == UNKNOWN )
			return "Unknown";
	}
}
