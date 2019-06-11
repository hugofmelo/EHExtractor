package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;

/**
 * Visitor para verificar o guideline "Throw specific exceptions".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 90% de todas as sinalizações são feitas com um tipo folha na hierarquia.
 * */
public class ThrowSpecificExceptionsVisitor extends GuidelineCheckerVisitor
{

	public ThrowSpecificExceptionsVisitor (boolean allowUnresolved)
	{
		super (allowUnresolved);
	}
	
	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{
        super.visit(compilationUnit, arg);
    }

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		createSignaler(throwStatement);
		
		// VISIT CHILDREN
		super.visit(throwStatement, arg);
	}	

	/**
	 * Verifica se o projeto adota o guideline referenciado neste visitor.
	 * 
	 * Para entender as condições do guideline, ver Javadoc da classe
	 * */
	public void checkGuidelineConformance ()
	{	
		int numberOfSignalers = this.signalersOfProject.size();
		System.out.println("Total of signalers: " + numberOfSignalers);
		
		long numberOfSignalersOfSpecificExceptions = this.signalersOfProject.stream()
				.filter(signaler -> isSpecificException(signaler.getThrownType()))
				.count();
		System.out.println("Total of signalers of specific exceptions: " + numberOfSignalersOfSpecificExceptions);
		
		System.out.println("'Throw specific exceptions' conformance: " + 1.0*numberOfSignalersOfSpecificExceptions/numberOfSignalers);
	}
	
	private static boolean isSpecificException (Type type)
	{
		return isNonGenericJavaException(type) && type.getSubTypes().isEmpty();
	}
	
	private static boolean isNonGenericJavaException (Type type)
	{
		if ( type.getOrigin() == TypeOrigin.JAVA )
		{
			if ( !type.getQualifiedName().equals(Throwable.class.getCanonicalName()) &&
				 !type.getQualifiedName().equals(Exception.class.getCanonicalName()) &&
				 !type.getQualifiedName().equals(RuntimeException.class.getCanonicalName()) &&
				 !type.getQualifiedName().equals(Error.class.getCanonicalName()) )
			{
				return true;
			}
		}
		
		return false;
	}
}