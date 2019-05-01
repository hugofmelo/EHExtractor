package ufrn.dimap.lets.ehmetrics.ops;

import org.apache.maven.shared.invoker.MavenInvocationException;

import com.github.javaparser.resolution.UnsolvedSymbolException;

public class CallGraphTest {

	public static void main (String args[])
	{
		CallGraphTest cgt = new CallGraphTest();
		
		
		
		
	}
	
	private void method1 () throws Throwable
	{
		B a = new B();
		try
		{
			a.m(new UnsolvedSymbolException("sdfsdf"));
		}
		catch (UnsolvedSymbolException e1)
		{
			try
			{
				a.m();
			}
			catch (Exception e2)
			{
				throw e2.getCause();
			}
		}
	}
	
	private void method2 () throws MavenInvocationException
	{
		B a = new B();
		try
		{
			a.m(new UnsolvedSymbolException("sdfsdf"));
		}
		catch (UnsolvedSymbolException e1)
		{
			try
			{
				a.m();
			}
			catch (Exception e2)
			{
				throw new MavenInvocationException("dsf");
			}
		}
	}
	
	private void method3 () throws MavenInvocationException
	{
		B a = new B();
		try
		{
			a.m(new UnsolvedSymbolException("sdfsdf"));
		}
		catch (UnsolvedSymbolException e1)
		{
			try
			{
				a.m();
			}
			catch (Exception e2)
			{
				//MavenInvocationException e3 = new MavenInvocationException("",e1);  
				
				//throw e3;
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
