package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;

/**
 * Visitor para verificar o guideline "Convert to runtime exceptions".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * De todas as exceções que são lançadas no contexto de um handler, 95% são não-checadas.
 * */
public class ConvertToRuntimeExceptionsVisitor extends AbstractGuidelineVisitor
{
	public ConvertToRuntimeExceptionsVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
	{
		super (baseVisitor, allowUnresolved);
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
		builder.append("# resignalers checked -> unchecked");
		builder.append("\t");
		builder.append("# resignalers unchecked -> unchecked");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		Predicate <Signaler> throwUncheckedException = signaler -> 
			signaler.getThrownTypes().stream()
				.anyMatch(type -> type.getClassType() == ClassType.UNCHECKED_EXCEPTION);
		
		List<Signaler> resignalers = this.baseVisitor.getSignalers().stream()
			.filter(throwUncheckedException)
			.filter(signaler -> signaler.getRelatedHandler().isPresent())
			.collect(Collectors.toList());
		
		Predicate <Handler> catchCheckedException = handler -> 
			handler.getExceptions().stream()
				.anyMatch(type -> type.getClassType() == ClassType.CHECKED_EXCEPTION);
		
		Predicate <Handler> catchUncheckedException = handler -> 
			handler.getExceptions().stream()
				.anyMatch(type -> type.getClassType() == ClassType.UNCHECKED_EXCEPTION);
			
		List<Signaler> checked2UncheckedResignalers = resignalers.stream()
				.filter(signaler -> catchCheckedException.test(signaler.getRelatedHandler().get()))
				.collect(Collectors.toList());
		
		List<Signaler> unchecked2UncheckedResignalers = resignalers.stream()
				.filter(signaler -> catchUncheckedException.test(signaler.getRelatedHandler().get()))
				.collect(Collectors.toList());

		
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.baseVisitor.getSignalers().size());
		builder.append("\t");
		builder.append(resignalers.size());
		builder.append("\t");
		builder.append(checked2UncheckedResignalers.size());
		builder.append("\t");
		builder.append(unchecked2UncheckedResignalers.size());
		builder.append("\t");
		
		return builder.toString();
	}
}