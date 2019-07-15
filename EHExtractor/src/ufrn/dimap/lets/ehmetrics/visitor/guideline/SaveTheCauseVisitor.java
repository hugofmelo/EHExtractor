package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerParser;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerType;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.VisitorsUtil;

/**
 * Visitor para verificar o guideline "Save the cause".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todos os signalers que lançam uma exceção no contexto de algum bloco catch
 * são do tipo wrapping.
 * */
public class SaveTheCauseVisitor extends GuidelineCheckerVisitor
{
	private static final Logger GUIDELINE_LOGGER = LoggerFacade.getGuidelinesLogger(SaveTheCauseVisitor.class);
	
	private Optional<Handler> handlerInScopeOptional;

	public SaveTheCauseVisitor (boolean allowUnresolved)
	{
		super (allowUnresolved);
	}
	
	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{
		// Forces the stack to reset. Sometimes um error when parsing precious java files could finish the visitor without reseting the stack.
		handlerInScopeOptional = Optional.empty(); 
		
        super.visit(compilationUnit, arg);
    }

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = createHandler(catchClause);
		
		this.handlerInScopeOptional.ifPresent(handler ->
		{
			handler.getNestedHandlers().add(newHandler);
			newHandler.setParentHandler(handler);
		});

		this.handlerInScopeOptional = Optional.of(newHandler);


		// VISIT CHILDREN
		super.visit(catchClause, arg);

		this.handlerInScopeOptional = this.handlerInScopeOptional.get().getParentHandler();
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		Signaler newSignaler = createSignaler(throwStatement);

		SignalerParser signalerParser = new SignalerParser(throwStatement, VisitorsUtil.getCatchClausesFromHandler(handlerInScopeOptional));
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
	@Override
	public void checkGuidelineConformance ()
	{	
		List<Signaler> wrappersSignalers = this.signalersOfProject.stream()
			.filter ( signaler -> signaler.getSignalerType() == SignalerType.WRAPPING)
			.collect (Collectors.toList());
		
		List<Signaler> destructiveSignalers = this.signalersOfProject.stream()
				.filter ( signaler -> signaler.getSignalerType() == SignalerType.DESTRUCTIVE_SIMPLE_THROW)
				.collect (Collectors.toList());
		
		GUIDELINE_LOGGER.info("Number of re-signalers of wrapping type: " + wrappersSignalers.size());
		GUIDELINE_LOGGER.info("Number of destructive re-signalers: " + destructiveSignalers.size());
		
		GUIDELINE_LOGGER.info("'Save the cause' conformance: " + 1.0*wrappersSignalers.size() / (wrappersSignalers.size()+destructiveSignalers.size()));
	}
}