package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.Comparator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;

/**
 * Visitor para verificar o guideline "Define a single exception".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todas as sinalizações de exceções da aplicação são de uma mesma exceção
 * */
public class DefineSingleExceptionVisitor extends GuidelineCheckerVisitor
{
	private static final Logger GUIDELINE_LOGGER = LoggerFacade.getGuidelinesLogger(DefineSingleExceptionVisitor.class);
	
	public DefineSingleExceptionVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
	}

	@Override
	public void visit (ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Void arg)
	{		
		this.createTypeFromClassDeclaration(classOrInterfaceDeclaration);
		
		//VISIT CHILDREN
		super.visit(classOrInterfaceDeclaration, arg);
	}

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		this.createHandler(catchClause);
		
		// VISIT CHILDREN
		super.visit(catchClause, arg);
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		this.createSignaler(throwStatement);
		
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
		Map <Type, Long> systemExceptionSignalersToOccurrences = this.signalersOfProject.stream()
				.filter(s -> s.getThrownType().isSystemExceptionType())
				.collect (Collectors.groupingBy(Signaler::getThrownType, Collectors.counting()));
		
		
		Long sumOfSystemExceptionSignalersOccurrences = systemExceptionSignalersToOccurrences.values().stream()
				.mapToLong(Long::longValue)
				.sum();
		GUIDELINE_LOGGER.info("Number of system signalers: " + sumOfSystemExceptionSignalersOccurrences);
		
		Long mostSignaledSystemExceptionOccurrences = systemExceptionSignalersToOccurrences.values().stream()
				.max(Comparator.naturalOrder())
				.get();
		GUIDELINE_LOGGER.info("Number of signalers of the most signaled exception: " + mostSignaledSystemExceptionOccurrences);
		
		
		GUIDELINE_LOGGER.info("'Define a single exception' conformance: " + 1.0*mostSignaledSystemExceptionOccurrences/sumOfSystemExceptionSignalersOccurrences);
	}	
}