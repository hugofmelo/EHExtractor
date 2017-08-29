package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.ast.stmt.ThrowStmt;

public class Signaler extends AbstractEHModelElement
{
	private Type thrownException;
	private Type catchedException;
	private SignalerType type;
	
	public Signaler (String filePath, ThrowStmt throwStatement, Type thrownException, Type catchedException, SignalerType type)
	{
		super(filePath, throwStatement);
		this.thrownException = thrownException;
		this.catchedException = catchedException;
		this.type = type;
		thrownException.addSignaler (this);
	}

	public Type getThrownException()
	{
		return this.thrownException;
	}
	
	public Type getCatchedException()
	{
		return this.catchedException;
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
