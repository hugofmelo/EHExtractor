package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;

/**
 * Visitor para verificar o guideline "Define a single exception".
 * */
public class DefineSingleExceptionVisitor extends AbstractGuidelineVisitor
{
	private int totalTypes;
	
	public DefineSingleExceptionVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
	{
		super(baseVisitor, allowUnresolved);
		this.totalTypes = 0;
	}
	
	@Override
	public void visit (ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Void arg)
	{		
		//VISIT CHILDREN
		super.visit(classOrInterfaceDeclaration, arg);
		
		if ( classOrInterfaceDeclaration.resolve().isClass() )
		{
			totalTypes++;
		}
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
		builder.append("# project types");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		/*
		Comparator <Type> comparatorOfTypes_signalersSizeDesc = (t1, t2) -> t2.getSignalers().size() - t1.getSignalers().size();
		
		List<Type> systemExceptions = this.baseVisitor.getTypes().stream()
				.filter(type -> type.getOrigin() == TypeOrigin.SYSTEM)
				.filter(Type::isException)
				.sorted(comparatorOfTypes_signalersSizeDesc)
				.collect(Collectors.toList());
		
		String exceptionsPerSignalersCount = systemExceptions.stream()
				.map(type -> type.getSignalers().size())
				.map(Object::toString)
				.collect(Collectors.joining(" "));
				
		if (exceptionsPerSignalersCount.equals(""))
		{
			exceptionsPerSignalersCount = "0";
		}
		*/
		
		List<Type> projectExceptions = this.baseVisitor.getTypes().stream()
				.filter(type -> type.getOrigin() == TypeOrigin.SYSTEM)
				.filter(Type::isException)
				.collect(Collectors.toList());
		
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(projectExceptions.size());
		builder.append("\t");
		builder.append(this.totalTypes);
		builder.append("\t");
		
		return builder.toString();
	}
}