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
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;

/**
 * Visitor para verificar o guideline "Convert to runtime exceptions".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * De todas as exceções que são lançadas no contexto de um handler, 95% são não-checadas.
 * */
public class ConvertToRuntimeExceptionsVisitor extends GuidelineCheckerVisitor
{
	private Optional<Handler> handlerInScopeOptional;

	public ConvertToRuntimeExceptionsVisitor (boolean allowUnresolved)
	{
		super (allowUnresolved);
	}

	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{
		// Forces the stack to reset. Sometimes um error when parsing previous java files could finish the visitor without reseting the stack.
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

		// TODO colocar essa lógica no visitor base? Pensar se é possível para os subvisitors herdarem esse comportamenteo e ainda assim executarem o que tem q executar
		// All handlers in context have this signaler as escaping exception
		if (handlerInScopeOptional.isPresent())
		{
			handlerInScopeOptional.get().getAllHandlersInContext()
			.forEach(handler -> handler.getEscapingSignalers().add(newSignaler));

			// TODO novidade.. precisa?
			newSignaler.setRelatedHandler(handlerInScopeOptional.get());
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
		
		builder.append("# signalers");
		builder.append("\t");
		builder.append("# resignalers");
		builder.append("\t");
		builder.append("# resignalers of unchecked exceptions");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		// Todos os handlers aninhados podem possui o mesmo escapingSignaler, por isso podem haver duplicatas
		List<Signaler> resignalers = this.handlersOfProject.stream()
				.flatMap ( handler -> handler.getEscapingSignalers().stream() )
				.distinct()
				.collect (Collectors.toList());

		
		Predicate <Signaler> throwUncheckedException = signaler -> 
			signaler.getThrownTypes().stream()
				.anyMatch(type -> type.getClassType() == ClassType.UNCHECKED_EXCEPTION);
		
		List<Signaler> resignalersWhichThrowRuntimeExceptions = resignalers.stream()
				.filter(throwUncheckedException)
				.collect(Collectors.toList());

		
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.signalersOfProject.size());
		builder.append("\t");
		builder.append(resignalers.size());
		builder.append("\t");
		builder.append(resignalersWhichThrowRuntimeExceptions.size());
		builder.append("\t");
		
		return builder.toString();
	}
}