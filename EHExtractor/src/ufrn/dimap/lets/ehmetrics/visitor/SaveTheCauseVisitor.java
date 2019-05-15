package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerParser;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerType;

/**
 * Visitor para verificar o guideline "Save the cause".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todos os signalers que lançam uma exceção no contexto de algum bloco catch
 * são do tipo wrapping.
 * */
public class SaveTheCauseVisitor extends GuidelineCheckerVisitor {

	private Stack <Handler> handlersInContext;

	public SaveTheCauseVisitor (boolean allowUnresolved)
	{
		super (allowUnresolved);
	}
	
	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{
		// Forces the stack to reset. Sometimes um error when parsing precious java files could finish the visitor without reseting the stack.
		this.handlersInContext = new Stack<>(); 
		
        super.visit(compilationUnit, arg);
    }

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = createHandler(catchClause);	
		
		this.handlersInContext.push(newHandler);

		// VISIT CHILDREN
		super.visit(catchClause, arg);

		this.handlersInContext.pop();
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		Signaler newSignaler = createSignaler(throwStatement);

		SignalerParser signalerParser = new SignalerParser(throwStatement, VisitorsUtil.getCatchClausesFromHandlers(handlersInContext));
		signalerParser.parse();
		
		newSignaler.setSignalerType (signalerParser.getType());
		
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
		List<Signaler> wrappersSignalers = this.signalersOfProject.stream()
			.filter ( signaler -> signaler.getSignalerType() == SignalerType.WRAPPING)
			.collect (Collectors.toList());
		
		List<Signaler> destructiveSignalers = this.signalersOfProject.stream()
				.filter ( signaler -> signaler.getSignalerType() == SignalerType.DESTRUCTIVE_SIMPLE_THROW)
				.collect (Collectors.toList());
		
		System.out.println("Number of re-signalers of wrapping type: " + wrappersSignalers.size());
		System.out.println("Number of destructive re-signalers: " + destructiveSignalers.size());
		
		System.out.println("'Save the cause' conformance: " + 1.0*wrappersSignalers.size() / (wrappersSignalers.size()+destructiveSignalers.size()));
	}
}