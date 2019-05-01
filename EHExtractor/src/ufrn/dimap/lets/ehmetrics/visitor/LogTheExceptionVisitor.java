package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.Comparator;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.javaparserutil.JavaParserUtil;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerParser;

/**
 * Visitor para verificar o guideline "Log the exception".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????
 * */
public class LogTheExceptionVisitor extends GuidelineCheckerVisitor
{
	private Stack <Handler> handlersInContext;
	
	public LogTheExceptionVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
	}

	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{
		// Forces the stack to reset. Sometimes um error when parsing precious java files could finish the visitor without reseting the stack.
		this.handlersInContext = new Stack<>(); 
		
        super.visit(compilationUnit, arg);
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
		Handler newHandler = this.createHandler(catchClause);
		
		this.handlersInContext.push(newHandler);
		
		// VISIT CHILDREN
		super.visit(catchClause, arg);
		
		this.handlersInContext.pop();
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		this.createSignaler(throwStatement);
		
		SignalerParser signalerParser = new SignalerParser(throwStatement, VisitorsUtil.getCatchClausesFromHandlers(handlersInContext));
		// TODO continuar daqui
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
		System.out.println("Number of system signalers: " + sumOfSystemExceptionSignalersOccurrences);
		
		Long mostSignaledSystemExceptionOccurrences = systemExceptionSignalersToOccurrences.values().stream()
				.max(Comparator.naturalOrder())
				.get();
		System.out.println("Number of signalers of the most signaled exception: " + mostSignaledSystemExceptionOccurrences);
		
		
		System.out.println("'Define a single exception' conformance: " + 1.0*mostSignaledSystemExceptionOccurrences/sumOfSystemExceptionSignalersOccurrences);
	}	
}