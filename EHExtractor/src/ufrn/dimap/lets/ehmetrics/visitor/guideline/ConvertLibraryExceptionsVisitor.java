package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;

/**
 * Visitor para verificar o guideline "Convert library exceptions".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * De todas as capturas de exceções externas em que aquele tratador re-signaliza
 * uma exceção, a re-sinalização não é de uma exceção externa em pelo menos 95% dos tratadores.
 * */
public class ConvertLibraryExceptionsVisitor extends GuidelineCheckerVisitor
{
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
	 * Returns the guideline columns names
	 * */
	@Override
	public String getGuidelineHeader ()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("# handlers");
		builder.append("\t");
		builder.append("# resignalers handlers");
		builder.append("\t");
		builder.append("# resignaler handlers of external exceptions");
		builder.append("\t");
		builder.append("# resignaler handlers of external exceptions which signal project exceptions");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		List<Handler> resignalerHandlers = this.handlersOfProject.stream()
				.filter( handler -> !handler.isFinalHandler() )
				.collect (Collectors.toList());
		
		Predicate <Handler> handleExternalExceptions = handler -> handler.getExceptions().stream()
				.anyMatch(type -> type.getOrigin() == TypeOrigin.LIBRARY || type.getOrigin() == TypeOrigin.UNRESOLVED);
		
		List<Handler> resignalerHandlersOfExternalExceptions = resignalerHandlers.stream()
				.filter ( handleExternalExceptions )
				.collect (Collectors.toList());
		
		Predicate <Signaler> throwExternalException = signaler -> 
			signaler.getThrownTypes().stream()
				.anyMatch(type -> type.getOrigin() == TypeOrigin.LIBRARY || type.getOrigin() == TypeOrigin.UNRESOLVED);
		
		List<Handler> resignalerHandlersOfExternalExceptionsWhichResignalNonExternalExceptions = resignalerHandlersOfExternalExceptions.stream()
				.filter ( handler -> handler.getEscapingSignalers().stream()
						.anyMatch(throwExternalException.negate() ))
				.collect (Collectors.toList());
				
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.handlersOfProject.size());
		builder.append("\t");
		builder.append(resignalerHandlers.size());
		builder.append("\t");
		builder.append(resignalerHandlersOfExternalExceptions.size());
		builder.append("\t");
		builder.append(resignalerHandlersOfExternalExceptionsWhichResignalNonExternalExceptions.size());
		builder.append("\t");
		
		return builder.toString();
	}
}