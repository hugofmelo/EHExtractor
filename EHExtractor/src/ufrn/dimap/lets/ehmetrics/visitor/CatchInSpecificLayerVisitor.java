package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;

/**
 * Visitor para verificar o guideline "Catch in a specific layer".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????????????????
 * */
public class CatchInSpecificLayerVisitor extends GuidelineCheckerVisitor {

	private Optional<Handler> handlerInScopeOptional;
	
	private String packageDeclarationName;
	private Map<String, List<Handler>> handlersPerPackage;
	
	
	public CatchInSpecificLayerVisitor (boolean allowUnresolved)
	{
		super (allowUnresolved);
		
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
	 * Verifica se o projeto adota o guideline referenciado neste visitor.
	 * 
	 * Para entender as condições do guideline, ver Javadoc da classe
	 * */
	public void checkGuidelineConformance ()
	{	
		// TODO Organizar essa saída
		for ( String packageDeclaration : this.handlersPerPackage.keySet() )
		{
			System.out.println(packageDeclaration);
			for ( Handler handler : this.handlersPerPackage.get(packageDeclaration) )
			{
				if ( handler.getEscapingSignalers().isEmpty() )
				{
					System.out.println("\t" + handler + ":" + handler.getFile());
				}
				
			}
		}
	}
}