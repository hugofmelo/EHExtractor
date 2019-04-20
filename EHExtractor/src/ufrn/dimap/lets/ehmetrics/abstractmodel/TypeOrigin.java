package ufrn.dimap.lets.ehmetrics.abstractmodel;

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
	
	public String toString ()
	{
		return this.text;
	}
}
