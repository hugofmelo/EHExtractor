package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.logging.Logger;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;

/**
 * Visitor para verificar o guideline "Use Java Built-in exceptions".
 * 
 * No mind map esse guideline � dividido em 2:
 * Reuse Java exceptions. Para reuso direto, como IllegalArgumentException; e
 * Extend Java exceptions. Quando no projeto s�o criados tipos que herdam de exce��es n�o-gen�ricas Java, como MySQLException extends SQLException.
 * <p>
 * 
 * Ent�o para confirmar o guideline a seguinte heur�stica � usada:
 * Quantidade de sinalizadores de exce��es Java
 * Quantidade de sinalizadores de exce��es que herdam de uma exce��o n�o-gen�rica Java
 * Total de sinalizadores
 * */
public class UseJavaBuiltinExceptionsVisitor extends GuidelineCheckerVisitor
{
	private static final Logger GUIDELINE_LOGGER = LoggerFacade.getGuidelinesLogger(UseJavaBuiltinExceptionsVisitor.class);

	public UseJavaBuiltinExceptionsVisitor (boolean allowUnresolved)
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
	 * Para entender as condi��es do guideline, ver Javadoc da classe
	 * */
	 // Quantidade de sinalizadores de exce��es n�o-gen�ricas Java
	 // Quantidade de sinalizadores de exce��es que herdam (direta ou indiretamente) de uma exce��o n�o-gen�rica Java
	 // Total de sinalizadores
	@Override
	public void checkGuidelineConformance ()
	{	
		int numberOfSignalers = this.signalersOfProject.size();
		GUIDELINE_LOGGER.info("Total of signalers: " + numberOfSignalers);
		
		long numberOfSignalersOfNonGenericJavaExceptions = this.signalersOfProject.stream()
				.filter(signaler -> isNonGenericJavaException(signaler.getThrownType()))
				.count();
		GUIDELINE_LOGGER.info("Total of signalers of non generic Java exceptions: " + numberOfSignalersOfNonGenericJavaExceptions);
		
		long numberOfSignalersOfSubtypesOfNonGenericJavaExceptions = this.signalersOfProject.stream()
				.filter(signaler -> isSubTypeOfNonGenericJavaException(signaler.getThrownType()))
				.count();
		GUIDELINE_LOGGER.info("Total of signalers of subtypes of non generic Java exceptions: " + numberOfSignalersOfSubtypesOfNonGenericJavaExceptions);
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
	
	private static boolean isSubTypeOfNonGenericJavaException (Type type)
	{
		if ( type.getOrigin() == TypeOrigin.SYSTEM )
		{
			if ( isNonGenericJavaException(type.getSuperType()) )
			{
				return true;
			}
			else
			{
				return isSubTypeOfNonGenericJavaException(type.getSuperType());
			}
		}
		
		return false;
	}
}