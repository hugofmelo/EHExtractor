package ufrn.dimap.lets.ehmetrics.abstractmodel;

import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerType;

public class Signaler extends AbstractEHModelElement
{
	private Type thrownType;

	private SignalerType signalerType;
	private Handler relatedHandler; // In case of rethrow, wrapping or unwrapping, the associated handler
	
	public Signaler ()
	{
		super();
		this.thrownType = null;
		this.signalerType = null;
		
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
	
	public SignalerType getSignalerType()
	{
		return this.signalerType;
	}
	
	public void setSignalerType(SignalerType signalerType)
	{
		this.signalerType = signalerType;
	}
	
	public Handler getRelatedHandler() {
		return relatedHandler;
	}

	public void setRelatedHandler(Handler relatedHandler) {
		this.relatedHandler = relatedHandler;
	}
	
	@Override
	public String toString()
	{
		return this.thrownType.getQualifiedName();
	}

	
}
