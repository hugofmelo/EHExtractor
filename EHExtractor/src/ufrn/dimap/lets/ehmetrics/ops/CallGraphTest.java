package ufrn.dimap.lets.ehmetrics.ops;

import com.github.javaparser.resolution.UnsolvedSymbolException;

import ufrn.dimap.lets.ehmetrics.ThinkLaterException;

public class CallGraphTest {

	public static void main (String args[])
	{
		CallGraphTest cgt = new CallGraphTest();
		
		
		
		
	}
	
	private void method1 ()
	{
		B a = new B();
		try
		{
			a.m(new UnsolvedSymbolException("sdfsdf"));
		}
		catch (ThinkLaterException e1)
		{
			try
			{
				a.m();
			}
			catch (Exception e2)
			{
				throw e1;
			}
		}
	}
	
	private void method2 ()
	{
		B a = new B();
		try
		{
			a.m(new UnsolvedSymbolException("sdfsdf"));
		}
		catch (ThinkLaterException e1)
		{
			try
			{
				a.m();
			}
			catch (Exception e2)
			{
				throw new IllegalArgumentException (e1);
			}
		}
	}
	
	private void method3 ()
	{
		B a = new B();
		try
		{
			a.m(new UnsolvedSymbolException("sdfsdf"));
		}
		catch (ThinkLaterException e1)
		{
			try
			{
				a.m();
			}
			catch (Exception e2)
			{
				IllegalArgumentException e3 = new IllegalArgumentException(e1);  
				
				throw e3;
			}
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
