package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;

/**
 * Visitor para verificar o guideline "Catch in a specific layer".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????????????????
 * */
public class CatchInSpecificLayerVisitor extends GuidelineCheckerVisitor
{
	private Optional<Handler> handlerInScopeOptional;
	
	private String packageDeclarationName;
	private Map<String, List<Handler>> handlersPerPackage;
	
	
	public CatchInSpecificLayerVisitor (boolean allowUnresolved)
	{
		super (allowUnresolved);
		clear();
	}
	
	@Override
	public void visit (PackageDeclaration packageDeclaration, Void arg)
	{
		this.packageDeclarationName = packageDeclaration.getNameAsString();
		
		this.handlersPerPackage.computeIfAbsent(packageDeclarationName, k -> new ArrayList<>());
		
        super.visit(packageDeclaration, arg);
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

		this.handlersPerPackage.get(packageDeclarationName).add(newHandler);
		
		
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
		builder.append("# handlers finais");
		builder.append("\t");
		builder.append("# pacotes");
		builder.append("\t");
		builder.append("# pacotes que correspondem a 90% of handlers finais");
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
			.forEach( entry -> entry.getValue().removeIf(handler -> 
				!handler.isFinalHandler()));
		
		Comparator <Map.Entry<String, List<Handler>>> comparator = (t1, t2) -> t2.getValue().size() - t1.getValue().size();
				
		// Deus sabe que tentei usar um map que ordena com custom comparator sem ser somente pela key, mas desisti
		List<Map.Entry<String, List<Handler>>> sortedPackages = copyMap.entrySet().stream()
				.sorted(comparator)
				.collect(Collectors.toList());
		
		long totalFinalHandlers = this.handlersOfProject.stream()
				.filter(Handler::isFinalHandler)
				.count();
				
		double threshold = totalFinalHandlers * 0.9;
		double packagesSum = 0;
		int packageCount = 0;
		
		Iterator<Map.Entry<String, List<Handler>>> entriesIterator = sortedPackages.iterator();
		
		while ( packagesSum < threshold )
		{
			packagesSum += entriesIterator.next().getValue().size();
			packageCount++;
		}
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.handlersOfProject.size());
		builder.append("\t");
		builder.append(totalFinalHandlers);
		builder.append("\t");
		builder.append(this.handlersPerPackage.keySet().size());
		builder.append("\t");
		builder.append(packageCount);
		builder.append("\t");
		
		return builder.toString();
	}
	
	@Override
	protected void clear ()
	{
		super.clear();
		this.handlersPerPackage = new HashMap<>();
	}
}