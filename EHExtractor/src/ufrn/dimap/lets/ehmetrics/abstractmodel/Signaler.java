package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.stmt.ThrowStmt;

public class Signaler extends AbstractEHModelElement
{
	private Type thrownType;
	//private SignalerType signalerType;
	private boolean isRethrow;
	private boolean isWrapping;
	//private boolean isUnwrapping;
	private Handler resignaledHandler; // In case of rethrow or wrapping, the associated handler 
	public Signaler ()
	{
		super();
		this.thrownType = null;
		this.isRethrow = false;
		this.isWrapping = false;
		this.resignaledHandler = null;
	}

	public Type getThrownType()
	{
		return this.thrownType;
	}
	
	/*
	public SignalerType getSignalerType ()
	{
		return this.signalerType;
	}
	*/

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
	
	public String toString()
	{
		return this.thrownType.getQualifiedName();
	}

	public void setThrownType(Type thrownType)
	{
		this.thrownType = thrownType;
	}

	public Handler getResignaledHandler() {
		return resignaledHandler;
	}

	public void setResignaledHandler(Handler resignaledHandler) {
		this.resignaledHandler = resignaledHandler;
	}
}
