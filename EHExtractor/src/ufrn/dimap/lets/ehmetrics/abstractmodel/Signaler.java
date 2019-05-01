package ufrn.dimap.lets.ehmetrics.abstractmodel;

public class Signaler extends AbstractEHModelElement
{
	private Type thrownType;

	private boolean isRethrow;
	private boolean isWrapping;
	private boolean isUnwrapping;
	private Handler relatedHandler; // In case of rethrow, wrapping or unwrapping, the associated handler 
	public Signaler ()
	{
		super();
		this.thrownType = null;
		this.isRethrow = false;
		this.isWrapping = false;
		this.relatedHandler = null;
	}

	public Type getThrownType()
	{
		return this.thrownType;
	}

	public void setThrownType(Type thrownType)
	{
		this.thrownType = thrownType;
	}
	
	public boolean isRethrow() {
		return isRethrow;
	}

	public void setRethrow(boolean isRethrow) {
		this.isRethrow = isRethrow;
	}

	public boolean isWrapping() {
		return isWrapping;
	}

	public void setWrapping(boolean isWrapping) {
		this.isWrapping = isWrapping;
	}
	
	public boolean isUnwrapping() {
		return isUnwrapping;
	}

	public void setUnwrapping(boolean isUnwrapping) {
		this.isUnwrapping = isUnwrapping;
	}
	
	public Handler getResignaledHandler() {
		return relatedHandler;
	}

	public void setResignaledHandler(Handler resignaledHandler) {
		this.relatedHandler = resignaledHandler;
	}
	
	@Override
	public String toString()
	{
		return this.thrownType.getQualifiedName();
	}
}
