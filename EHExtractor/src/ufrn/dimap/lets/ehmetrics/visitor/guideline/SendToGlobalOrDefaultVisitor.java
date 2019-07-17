package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.javaparserutil.JavaParserUtil;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.VisitorsUtil;

/**
 * Visitor para verificar o guideline "Send to a global or default handler".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????
 * */
public class SendToGlobalOrDefaultVisitor extends AbstractGuidelineVisitor
{
	private Optional<Handler> handlerInScopeOptional;
	
	private List<DedicatedHandler> dedicatedHandlers;
	
	public SendToGlobalOrDefaultVisitor (BaseGuidelineVisitor baseVisitor, boolean allowUnresolved)
	{
		super(baseVisitor, allowUnresolved);
		this.dedicatedHandlers = new ArrayList<>();
	}
	
	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = this.baseVisitor.findHandler (catchClause);

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
					Type type = this.baseVisitor.getTypeHierarchy().findOrCreateResolvedType(typeDeclaration.asClass());
					
					if ( type.getOrigin() == TypeOrigin.SYSTEM )
					{
						addDedicatedHandler(type, handlerMaybe.get(), methodCallExpression, dedicatedHandlers);
					}
				}
			}
			catch (UnsolvedSymbolException e)
			{
		//		System.out.print("Não resolveu: " + methodCallExpression);
		//		System.out.println();
				// O contexto não foi resolvido, logo não é de uma exceção do projeto, logo não é um dedicated handler
			}
			catch (RuntimeException e)
			{
				// Acontece quando um dos argumentos do método não foi resolvido, embora a classe que possui o método possa ser qualquer uma, inclusive do projeto
				// TODO é uma limitação real. Reportar.
		//		System.out.print("Não resolveu: " + methodCallExpression);
		//		System.out.println();
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
		builder.append("# resignaler handlers with calls to dedicated handlers");
		builder.append("\t");
		builder.append("# final handlers with calls to dedicated handlers");
		builder.append("\t");
		builder.append("# handlers per dedicated handler");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		List <DedicatedHandler> sortedDedicatedHandlers = new ArrayList<>(this.dedicatedHandlers);
				
		Collections.sort(sortedDedicatedHandlers);
		Collections.reverse(sortedDedicatedHandlers);
		
		List<Handler> handlersWhichHasCallsToDedicatedHandlers = sortedDedicatedHandlers.stream()
				.flatMap(dedicatedHandler -> dedicatedHandler.getCallsPerHandler().keySet().stream())
				.collect(Collectors.toList()); 
		
		List<Handler> resignalerHandlersWhichHasCallsToDedicatedHandlers = handlersWhichHasCallsToDedicatedHandlers.stream()
			.filter( handler -> !handler.isFinalHandler() )
			.collect(Collectors.toList());
		
		List<Handler> finalHandlersWhichHasCallsToDedicatedHandlers = handlersWhichHasCallsToDedicatedHandlers.stream()
				.filter( Handler::isFinalHandler )
				.collect(Collectors.toList());
		
		String handlersPerDedicatedHandlersCount = sortedDedicatedHandlers.stream()
				.map(dedicatedHandler -> dedicatedHandler.getCallsPerHandler().keySet().size())
				.map(Object::toString)
				.collect(Collectors.joining(" "));
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.baseVisitor.getHandlers().size());
		builder.append("\t");
		builder.append(resignalerHandlersWhichHasCallsToDedicatedHandlers.size());
		builder.append("\t");
		builder.append(finalHandlersWhichHasCallsToDedicatedHandlers.size());
		builder.append("\t");
		builder.append(handlersPerDedicatedHandlersCount);
		builder.append("\t");
		
		return builder.toString();
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