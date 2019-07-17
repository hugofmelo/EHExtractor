package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;

/**
 * Visitor para verificar o guideline "Catch in a specific layer".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????????????????
 * */
public class CatchInSpecificLayerVisitor extends AbstractGuidelineVisitor
{
	private String packageDeclarationName;
	private Map<String, List<Handler>> handlersPerPackage;
	
	
	public CatchInSpecificLayerVisitor (BaseGuidelineVisitor baseGuidelineVisitor, boolean allowUnresolved)
	{
		super (baseGuidelineVisitor, allowUnresolved);
		this.handlersPerPackage = new HashMap<>();
	}
	
	@Override
	public void visit (PackageDeclaration packageDeclaration, Void arg)
	{
		this.packageDeclarationName = packageDeclaration.getNameAsString();
		
		this.handlersPerPackage.computeIfAbsent(packageDeclarationName, k -> new ArrayList<>());
		
        super.visit(packageDeclaration, arg);
    }
	
	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = this.baseVisitor.findHandler (catchClause);

		this.handlersPerPackage.get(packageDeclarationName).add(newHandler);

		// VISIT CHILDREN
		super.visit(catchClause, arg);
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
		builder.append("# handlers finais");
		builder.append("\t");
		builder.append("# pacotes");
		builder.append("\t");
		builder.append("# final handlers por pacotes");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		Map <String, List<Handler>> copyMap = new HashMap<>(this.handlersPerPackage);
		
		copyMap.entrySet().stream()
			.forEach( entry -> entry.getValue()
					.removeIf(handler -> !handler.isFinalHandler()));
		
		Comparator <Map.Entry<String, List<Handler>>> comparator = (t1, t2) -> t2.getValue().size() - t1.getValue().size();
				
		// Deus sabe que tentei usar um map que ordena com custom comparator sem ser somente pela key, mas desisti
		List<Map.Entry<String, List<Handler>>> sortedPackages = copyMap.entrySet().stream()
				.sorted(comparator)
				.collect(Collectors.toList());
		
		long totalFinalHandlers = copyMap.values().stream()
				.flatMap (List::stream)
				.count();
		
		StringBuilder builder = new StringBuilder();
		
		String finalHandlersPerPackageCount = sortedPackages.stream()
			.map(entry -> entry.getValue().size())
			.map(Object::toString)
			.collect(Collectors.joining(" "));
		
		if (finalHandlersPerPackageCount.equals(""))
		{
			finalHandlersPerPackageCount = "0";
		}
		
		builder.append(this.baseVisitor.getHandlers().size());
		builder.append("\t");
		builder.append(totalFinalHandlers);
		builder.append("\t");
		builder.append(this.handlersPerPackage.keySet().size());
		builder.append("\t");
		builder.append(finalHandlersPerPackageCount);
		builder.append("\t");
		
		return builder.toString();
	}
}