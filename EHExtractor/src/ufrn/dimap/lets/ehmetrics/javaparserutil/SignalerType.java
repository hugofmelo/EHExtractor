package ufrn.dimap.lets.ehmetrics.javaparserutil;

public enum SignalerType
{
	/*
	SIMPLE_NAME ("Simple name"),
	OBJECT_CREATION ("Object creation"),
	GET_CAUSE ("Get cause"),
	CAST ("Cast"),
	*/
	
	SIMPLE_THROW ("Simple throw"),
	RETHROW ("Rethrow"),
	WRAPPING ("Wrapping"),
	UNWRAPPING ("Unwrapping"),
	DESTRUCTIVE_SIMPLE_THROW ("Destructive simple throw"),
	
	/*
	WRAPPING_CHECKED_CHECKED ("Wrapping checked-checked"),
	WRAPPING_CHECKED_UNCHECKED ("Wrapping checked-unchecked"),
	WRAPPING_CHECKED_ERROR ("Wrapping checked-error"),
	WRAPPING_UNCHECKED_CHECKED ("Wrapping unchecked-checked"),
	WRAPPING_UNCHECKED_UNCHECKED ("Wrapping unchecked-unchecked"),
	WRAPPING_UNCHECKED_ERROR ("Wrapping unchecked-error"),
	WRAPPING_ERROR_CHECKED ("Wrapping error-checked"),
	WRAPPING_ERROR_UNCHECKED ("Wrapping error-unchecked"),
	WRAPPING_ERROR_ERROR ("Wrapping error-error"),
	WRAPPING_UNKNOWN ("Wrapping unknown"),
	*/
	/*
	SIMPLE ("Simple"),
	RETHROW_LAST ("Rethrow last"),
	RETHROW_INNER ("Rethrow inner"),
	THROW_NEW ("Suppressing throw"),
	WRAPPING_SAME ("Wrapping same"),
	WRAP_CHECKED_CHECKED ("Wrapping checked-checked"),
	WRAP_CHECKED_UNCHECKED ("Wrapping checked-unchecked"),
	WRAP_CHECKED_ERROR ("Wrapping checked-error"),
	WRAP_UNCHECKED_CHECKED ("Wrapping unchecked-checked"),
	WRAP_UNCHECKED_UNCHECKED ("Wrapping unchecked-unchecked"),
	WRAP_UNCHECKED_ERROR ("Wrapping unchecked-error"),
	WRAP_ERROR_CHECKED ("Wrapping error-checked"),
	WRAP_ERROR_UNCHECKED ("Wrapping error-unchecked"),
	WRAP_ERROR_ERROR ("Wrapping error-error"),
	UNWRAP ("Unwrap"), 
	 */
	UNKNOWN ("Unknown"),
	;

	private final String text;
	private SignalerType (final String text)
	{
		this.text = text;
	}

	@Override
	public String toString ()
	{
		return this.text;
	}
}
