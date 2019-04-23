package ufrn.dimap.lets.ehmetrics.ops;

import com.github.javaparser.resolution.UnsolvedSymbolException;

import ufrn.dimap.lets.ehmetrics.ThinkLaterException;

public class CallGraphTest {

	public static void main (String args[])
	{
		A a = new B();
		try
		{
			a.m(new UnsolvedSymbolException("sdfsdf"));
		}
		catch (ThinkLaterException e)
		{
			throw e;
		}
		
		
	}

	/*
	private static UnsolvedSymbolException createException() {
		return new UnsolvedSymbolException("sdfdsf");
	}
	*/
	private static RuntimeException createException() {
		return new UnsolvedSymbolException("sdfdsf");
	}
	
}
