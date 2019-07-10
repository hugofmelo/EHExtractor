package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.stmt.ThrowStmt;

public class UnsupportedSignalerException extends UnsupportedCodePatternException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedSignalerException(String message, ThrowStmt throwStatement)
	{
		super(message, throwStatement);
	}

	public UnsupportedSignalerException(String message, ThrowStmt throwStatement, Throwable e)
	{
		super ( message, throwStatement, e );
	}
	
}
