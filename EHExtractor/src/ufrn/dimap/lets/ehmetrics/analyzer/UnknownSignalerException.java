package ufrn.dimap.lets.ehmetrics.analyzer;

import com.github.javaparser.ast.stmt.ThrowStmt;

public class UnknownSignalerException extends UnknownCodePatternException
{
	private static final long serialVersionUID = 1L;

	public UnknownSignalerException(String message, ThrowStmt throwStatement)
	{
		super(message, throwStatement);
	}

	public UnknownSignalerException(String message, ThrowStmt throwStatement, Throwable e)
	{
		super ( message, throwStatement, e );
	}
	
}
