package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;

/**
 * Visitor para verificar o guideline "Define a single exception".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todas as sinalizações de exceções da aplicação são de uma mesma exceção
 * */
public class DefineSingleExceptionVisitor extends GuidelineCheckerVisitor
{
	public DefineSingleExceptionVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
	}

	@Override
	public void visit (ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Void arg)
	{		
		this.createTypeFromClassDeclaration(classOrInterfaceDeclaration);
		
		//VISIT CHILDREN
		super.visit(classOrInterfaceDeclaration, arg);
	}

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		this.createHandler(catchClause);
		
		// VISIT CHILDREN
		super.visit(catchClause, arg);
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		this.createSignaler(throwStatement);
		
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
		
		builder.append("# project exceptions");
		builder.append("\t");
		builder.append("# signalers of project exception");
		builder.append("\t");
		builder.append("# exceptions which signal 90% of signalers");
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
		
		List<Type> systemExceptions = this.typeHierarchy.listTypes().stream()
				.filter(type -> type.getOrigin() == TypeOrigin.SYSTEM)
				.filter(Type::isException)
				.sorted(comparatorOfTypes_signalersSizeDesc)
				.collect(Collectors.toList());
		
		long totalSignalersOfSystemExceptions = systemExceptions.stream()
				.map(type -> type.getSignalers().size())
				.mapToInt(Integer::intValue)
				.sum();
				
		double threshold = totalSignalersOfSystemExceptions * 0.9;
		double signalersSum = 0;
		int typesCount = 0;
		
		while ( signalersSum < threshold )
		{
			signalersSum += systemExceptions.get(typesCount).getSignalers().size();
			typesCount++;
		}
		
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(systemExceptions.size());
		builder.append("\t");
		builder.append(totalSignalersOfSystemExceptions);
		builder.append("\t");
		builder.append(typesCount);
		builder.append("\t");
		
		return builder.toString();
	}
}