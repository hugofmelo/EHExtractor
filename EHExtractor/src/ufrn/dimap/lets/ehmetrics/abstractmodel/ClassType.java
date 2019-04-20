package ufrn.dimap.lets.ehmetrics.abstractmodel;

public enum ClassType
{
	NO_EXCEPTION ("No exception"),
	CHECKED_EXCEPTION ("Checked exception"),
	UNCHECKED_EXCEPTION ("Unchecked exception"),
	ERROR_EXCEPTION ("Error"),
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
}
