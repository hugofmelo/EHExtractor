package ufrn.dimap.lets.ehmetrics.ops;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.apache.maven.shared.invoker.MavenInvocationException;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

public class CallGraphTest {

	private Logger logger = Logger.getLogger("sdfdsfdsf");
	
	public static void main (String args[])
	{
		CallGraphTest cgt = new CallGraphTest();
		
		
		
		
	}
	
	private void method1 () throws Throwable
	{
		B a = new B();
		try
		{
			System.out.println("");
		}
		catch (Exception eee)
		{
			JavaSymbolSolver jss = new JavaSymbolSolver(null); 
			method(eee, jss);
		}
	}
	
	
	
	private void method(Exception eee, JavaSymbolSolver javaSymbolSolver) {
		// TODO Auto-generated method stub
		
	}

	private void resolveException2(UnsolvedSymbolException eee) {
		
	}

	private void resolveException(Exception e2) {
		
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

class Mariama extends SQLException {}
class Mariama2 extends Mariama {}