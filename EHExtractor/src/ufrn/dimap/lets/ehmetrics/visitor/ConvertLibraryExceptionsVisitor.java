package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;

/**
 * Visitor para verificar o guideline "Convert library exceptions".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * De todas as capturas de exceções externas em que aquele tratador re-signaliza
 * uma exceção, a re-sinalização não é de uma exceção externa em pelo menos 95% dos tratadores.
 * */
public class ConvertLibraryExceptionsVisitor extends GuidelineCheckerVisitor {

	private Optional<Handler> handlerInScopeOptional;

	public ConvertLibraryExceptionsVisitor (boolean allowUnresolved)
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

		// All handlers in context have this signaler as escaping exception
		if (handlerInScopeOptional.isPresent())
		{
			handlerInScopeOptional.get().getAllHandlersInContext()
				.forEach(handler -> handler.getEscapingSignalers().add(newSignaler));
		}
		
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
		List<Handler> handlersOfExternalExceptions = this.handlersOfProject.stream()
			.filter ( handler -> // check if there are external exception being handled
				handler.getExceptions().stream()
					.anyMatch(type ->
						type.getOrigin() == TypeOrigin.UNRESOLVED ||
						type.getOrigin() == TypeOrigin.LIBRARY))
			.collect (Collectors.toList());
		
		int numberOfHandlersOfExternalExceptions = handlersOfExternalExceptions.size();
		System.out.println("Number of handlers of external exceptions: " + numberOfHandlersOfExternalExceptions);
		
		
		List<Handler> handlersOfExternalExceptionsWhichResignalSomething = handlersOfExternalExceptions.stream()
				.filter ( handler -> !handler.getEscapingSignalers().isEmpty()) // check if there are resignaled
				.collect (Collectors.toList());

		int numberOfHandlersOfExternalExceptionsWhichResignalSomething = handlersOfExternalExceptionsWhichResignalSomething.size();
		System.out.println("Number of handlers of external exceptions which resignal somethings: " + numberOfHandlersOfExternalExceptionsWhichResignalSomething);

		
		List<Handler> handlersOfExternalExceptionsWhichResignalExternalExceptions = handlersOfExternalExceptionsWhichResignalSomething.stream()
				.filter ( handler -> 
					handler.getEscapingSignalers().stream()
						.anyMatch(signaler -> 
							signaler.getThrownType().getOrigin() == TypeOrigin.UNRESOLVED ||
							signaler.getThrownType().getOrigin() == TypeOrigin.LIBRARY))
				.collect(Collectors.toList());

		int numberOfHandlersOfExternalExceptionsWhichResignalExternalExceptions = handlersOfExternalExceptionsWhichResignalExternalExceptions.size();
		System.out.println("Number of handlers of external exceptions which resignal external exceptions: " + numberOfHandlersOfExternalExceptionsWhichResignalExternalExceptions);

		
		System.out.println("'Convert library exceptions' conformance: " + (1.0-(1.0*numberOfHandlersOfExternalExceptionsWhichResignalExternalExceptions/numberOfHandlersOfExternalExceptionsWhichResignalSomething)));
	}
}