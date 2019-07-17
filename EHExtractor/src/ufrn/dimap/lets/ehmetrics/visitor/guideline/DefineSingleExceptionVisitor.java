package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;

/**
 * Visitor para verificar o guideline "Define a single exception".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todas as sinalizações de exceções da aplicação são de uma mesma exceção
 * */
public class DefineSingleExceptionVisitor extends AbstractGuidelineVisitor
{
	public DefineSingleExceptionVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
	{
		super(baseVisitor, allowUnresolved);
	}
	
	/**
	 * Returns the guideline columns names
	 * */
	@Override
	public String getGuidelineHeader ()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("# project exceptions");
		builder.append("\t");
		builder.append("# signalers of project exceptions");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		Comparator <Type> comparatorOfTypes_signalersSizeDesc = (t1, t2) -> t2.getSignalers().size() - t1.getSignalers().size();
		
		List<Type> systemExceptions = this.baseVisitor.getTypes().stream()
				.filter(type -> type.getOrigin() == TypeOrigin.SYSTEM)
				.filter(Type::isException)
				.sorted(comparatorOfTypes_signalersSizeDesc)
				.collect(Collectors.toList());
		
		long totalSystemSignalers = systemExceptions.stream()
				.flatMap( type -> type.getSignalers().stream() )
				.count();
		
		String exceptionsPerSignalersCount = systemExceptions.stream()
				.map(type -> type.getSignalers().size())
				.map(Object::toString)
				.collect(Collectors.joining(" "));
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(systemExceptions.size());
		builder.append("\t");
		builder.append(exceptionsPerSignalersCount);
		builder.append("\t");
		
		return builder.toString();
	}
}