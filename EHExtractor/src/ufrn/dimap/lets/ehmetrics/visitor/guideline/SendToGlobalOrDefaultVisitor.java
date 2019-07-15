package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.HandlingAction;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.javaparserutil.JavaParserUtil;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.VisitorsUtil;

/**
 * Visitor para verificar o guideline "Send to a global or default handler".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????
 * */
public class SendToGlobalOrDefaultVisitor extends GuidelineCheckerVisitor
{
	private Optional<Handler> handlerInScopeOptional;
	
	private List<DedicatedHandler> dedicatedHandlers;
	
	public SendToGlobalOrDefaultVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
		clear();
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
			checkForDedicatedHandlerCall (methodCallExpression);
		}
		
		super.visit(methodCallExpression, arg);
	}
	
	private void checkForDedicatedHandlerCall (MethodCallExpr methodCallExpression)
	{
		Optional<Handler> handlerMaybe = VisitorsUtil.findHandler(
					this.handlerInScopeOptional.get(),
					JavaParserUtil.filterSimpleNames(methodCallExpression));
		if ( handlerMaybe.isPresent() )
		{
			try
			{
				ResolvedMethodDeclaration methodDeclaration = methodCallExpression.resolve();	
				ResolvedReferenceTypeDeclaration typeDeclaration = methodDeclaration.declaringType();
								
				if ( typeDeclaration.isClass() )
				{
					Type type = this.typeHierarchy.findOrCreateResolvedType(typeDeclaration.asClass());
					
					if ( type.getOrigin() == TypeOrigin.SYSTEM )
					{
						addDedicatedHandler(type, handlerMaybe.get(), methodCallExpression, dedicatedHandlers);
					}
				}
			}
			catch (UnsolvedSymbolException e)
			{
				System.out.print("Não resolveu: " + methodCallExpression);
				System.out.println();
				// O contexto não foi resolvido, logo não é de uma exceção do projeto, logo não é um dedicated handler
			}
			catch (RuntimeException e)
			{
				// Acontece quando um dos argumentos do método não foi resolvido, embora a classe que possui o método possa ser qualquer uma, inclusive do projeto
				// TODO é uma limitação real. Reportar.
				System.out.print("Não resolveu: " + methodCallExpression);
				System.out.println();
			}
		}		
	}
	
	private static void addDedicatedHandler (Type type, Handler handler, MethodCallExpr callExpression, List<DedicatedHandler> dedicatedHandlers)
	{
		boolean found = false;
		DedicatedHandler dedicatedHandler = null;
		
		for ( DedicatedHandler dh : dedicatedHandlers )
		{
			if ( dh.getType().equals(type) )
			{
				found = true;
				dedicatedHandler = dh;
			}
		}
		
		if ( !found )
		{
			dedicatedHandler = new DedicatedHandler(type);
			dedicatedHandlers.add(dedicatedHandler);
		}
		
		if ( !dedicatedHandler.getCallsPerHandler().containsKey(handler) )
		{
			dedicatedHandler.getCallsPerHandler().put(handler, new ArrayList<MethodCallExpr>());
		}
		
		dedicatedHandler.getCallsPerHandler().get(handler).add(callExpression);
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
		builder.append("# handlers with calls to dedicated handlers");
		builder.append("\t");
		builder.append("# calls to dedicated handlers");
		builder.append("\t");
		builder.append("# classes que correspondem a 90% das chamadas a dedicated handlers");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		List <DedicatedHandler> copyList = new ArrayList<>(this.dedicatedHandlers);
		
		
		
		/*
		copyMap.entrySet().stream()
			.forEach( entry -> entry.getValue().removeIf(handler -> 
				!handler.isFinalHandler()));
		*/
				
		Collections.sort(copyList);
		Collections.reverse(copyList);
		
		/*
		for ( DedicatedHandler handler : copyList )
		{
			 handler.getCallsPerHandler().entrySet().stream()
			 	.forEach( entry -> entry.getValue().stream()
			 			.forEach(call -> System.out.println(handler.getType() + " :::: " + entry.getKey().hashCode() + " ::: " + call)));
		}
		*/
		
		long handlersWhichHasCallsToDedicatedHandlers = copyList.stream()
			.flatMap(dedicatedHandler -> dedicatedHandler.getCallsPerHandler().keySet().stream())
			.count();
			
		
		long totalCalls = 
				copyList.stream()
					.flatMap(dedicatedHandler -> dedicatedHandler.getCallsPerHandler().values().stream()
							.flatMap(List::stream))
					.count();
				
		double threshold = totalCalls * 0.9;
		double callsSum = 0;
		int typesCount = 0;
		
		Iterator<DedicatedHandler> dedicatedHandlersIterator = copyList.iterator();
		
		while ( callsSum < threshold )
		{
			callsSum += dedicatedHandlersIterator.next().getCallsPerHandler().values().stream()
					.flatMap(List::stream)
					.count();
			typesCount++;
		}
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.handlersOfProject.size());
		builder.append("\t");
		builder.append(handlersWhichHasCallsToDedicatedHandlers);
		builder.append("\t");
		builder.append(totalCalls);
		builder.append("\t");
		builder.append(typesCount);
		builder.append("\t");
		
		return builder.toString();
	}	
	
	@Override
	protected void clear ()
	{
		super.clear();
		this.dedicatedHandlers = new ArrayList<>();
	}
}

class DedicatedHandler implements Comparable<DedicatedHandler>
{
	private Type type;
	private Map<Handler, List<MethodCallExpr>> callsPerHandler;
	
	public DedicatedHandler ( Type type )
	{
		this.type = type;
		this.callsPerHandler = new HashMap<>();
	}
	
	public Type getType ()
	{
		return this.type;
	}
	
	public Map<Handler, List<MethodCallExpr>> getCallsPerHandler() {
		return this.callsPerHandler;
	}

	@Override
	public int compareTo(DedicatedHandler other)
	{
		long sumThis = this.callsPerHandler.values().stream()
			.flatMap(List::stream)
			.count();
		
		long sumOther = other.callsPerHandler.values().stream()
				.flatMap(List::stream)
				.count();
		
		return (int)(sumThis - sumOther);
	}
}