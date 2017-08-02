package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.ast.stmt.ThrowStmt;

public class Signaler extends AbstractEHModelElement
{
	private Type thrownException;
	private SignalerType type;
	
	public Signaler (ThrowStmt throwStatement, Type thrownException, SignalerType type)
	{
		super(throwStatement);
		this.thrownException = thrownException;
		this.type = type;
		thrownException.addSignaler (this);
	}

	public Type getException()
	{
		return this.thrownException;
	}
	
	public SignalerType getType ()
	{
		return this.type;
	}

	public String toString()
	{
		return this.thrownException.getQualifiedName();
	}
}
