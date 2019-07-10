package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerType;

public class Signaler extends AbstractEHModelElement
{
	private List<Type> thrownTypes;

	private SignalerType signalerType;
	private Handler relatedHandler; // In case of rethrow, wrapping or unwrapping, the associated handler
	
	public Signaler ()
	{
		super();
		this.thrownTypes = new ArrayList<>();
		this.signalerType = null;
		
		this.relatedHandler = null;
	}

	public List<Type> getThrownTypes()
	{
		return this.thrownTypes;
	}

	public void setThrownTypes(List<Type> thrownTypes)
	{
		this.thrownTypes = thrownTypes;
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
		return this.thrownTypes.stream()
			.map(Type::getQualifiedName)
			.collect(Collectors.joining("|"));
	}
}
