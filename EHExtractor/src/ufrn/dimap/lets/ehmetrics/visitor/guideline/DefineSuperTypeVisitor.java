package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;

/**
 * Visitor para verificar o guideline "Define a super type".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todas as exceções definidas pela aplicação possuem um mesmo supertipo
 * */
public class DefineSuperTypeVisitor extends AbstractGuidelineVisitor
{
	public DefineSuperTypeVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
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
		builder.append("# subtypes of project exceptions");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		Comparator <Type> typesPerSubtypes = (t1, t2) -> t2.getAllSubTypes().size() - t1.getAllSubTypes().size();
		
		List<Type> projectTypes = this.baseVisitor.getTypes().stream()
				.filter(Type::isSystemExceptionType)
				.sorted(typesPerSubtypes)
				.collect(Collectors.toList());
		
		StringBuilder builder = new StringBuilder();
		
		String typesPerSubtypesCount = projectTypes.stream()
				.map(type -> type.getAllSubTypes().size())
				.map(Object::toString)
				.collect(Collectors.joining(" "));
		
		if (typesPerSubtypesCount.equals(""))
		{
			typesPerSubtypesCount = "0";
		}
		
		builder.append(projectTypes.size());
		builder.append("\t");
		builder.append(typesPerSubtypesCount);
		builder.append("\t");	
		
		return builder.toString();
	}
}