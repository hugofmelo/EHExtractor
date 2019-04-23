package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.ast.stmt.ThrowStmt;

public class Signaler extends AbstractEHModelElement
{
	private Type thrownType;
	//private Type caughtType;
	private SignalerType signalerType;
	
	public Signaler ()
	{
		super();
	}

	public Type getThrownType()
	{
		return this.thrownType;
	}
	
	public Type getCaughtException()
	{
		throw new UnsupportedOperationException();
	}
	
	public SignalerType getSignalerType ()
	{
		return this.signalerType;
	}

	public String toString()
	{
		return this.thrownType.getQualifiedName();
	}

	public void setThrownType(Type thrownType)
	{
		this.thrownType = thrownType;
	}
}
