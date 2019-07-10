package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.logging.Logger;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;

/**
 * Visitor para verificar o guideline "Throw specific exceptions".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 90% de todas as sinalizações são feitas com um tipo folha na hierarquia.
 * */
public class ThrowSpecificExceptionsVisitor extends GuidelineCheckerVisitor
{
	private static final Logger GUIDELINE_LOGGER = LoggerFacade.getGuidelinesLogger(ThrowSpecificExceptionsVisitor.class);

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
	@Override
	public void checkGuidelineConformance ()
	{	
		int numberOfSignalers = this.signalersOfProject.size();
		GUIDELINE_LOGGER.info("Total of signalers: " + numberOfSignalers);
		
		long numberOfSignalersOfSpecificExceptions = this.signalersOfProject.stream()
				.filter(signaler -> isSpecificException(signaler.getThrownType()))
				.count();
		GUIDELINE_LOGGER.info("Total of signalers of specific exceptions: " + numberOfSignalersOfSpecificExceptions);
		
		GUIDELINE_LOGGER.info("'Throw specific exceptions' conformance: " + 1.0*numberOfSignalersOfSpecificExceptions/numberOfSignalers);
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