package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.HandlingAction;

/**
 * Visitor para verificar o guideline "Send to a global or default handler".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????
 * */
public class SendToGlobalOrDefaultVisitor extends GuidelineCheckerVisitor
{
	private Optional<Handler> handlerInScopeOptional;
	
	public SendToGlobalOrDefaultVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
	}

	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{				
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
	public void visit (MethodCallExpr methodCallExpression, Void arg)
	{
		if ( this.handlerInScopeOptional.isPresent() )
		{
			if ( !VisitorsUtil.checkForLogAction (methodCallExpression, this.handlerInScopeOptional.get()) )
			{
				this.handlerInScopeOptional.get().createHandlerAction(methodCallExpression);
			}
		}
		
		super.visit(methodCallExpression, arg);
	}
	
	/**
	 * Verifica se o projeto adota o guideline referenciado neste visitor.
	 * 
	 * Para entender as condições do guideline, ver Javadoc da classe
	 * */
	public void checkGuidelineConformance ()
	{
		Map<String, Long> methodsOccurrences = this.handlersOfProject.stream()
				.filter(handler -> handler.getEscapingSignalers().isEmpty())
				.filter(handler -> !handler.hasLoggingActions())
				.flatMap(handler -> handler.getHandlingActions().stream())
				.collect(Collectors.groupingBy(HandlingAction::getMethodName, Collectors.counting()));
		
		System.out.println("Methods calls in handlers:");
		methodsOccurrences.forEach((name, occurrences) -> System.out.println (name + " : " + occurrences));
	}	
}