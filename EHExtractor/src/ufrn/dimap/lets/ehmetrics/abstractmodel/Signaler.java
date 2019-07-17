package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerType;

public class Signaler extends AbstractEHModelElement
{
	private List<Type> thrownTypes;

	private SignalerType signalerType;
	private Optional<Handler> relatedHandler; // In case this signaler occurs in the context of a handler
	
	public Signaler ()
	{
		super();
		this.thrownTypes = new ArrayList<>();
		this.signalerType = null;
		
		this.relatedHandler = Optional.empty();
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
	
	public Optional<Handler> getRelatedHandler() {
		return relatedHandler;
	}

	public void setRelatedHandler(Optional<Handler> relatedHandler) {
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
