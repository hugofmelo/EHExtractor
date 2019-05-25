package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerParser;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerType;

/**
 * Visitor para verificar o guideline "Convert to runtime exceptions".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * De todas as exceções que são lançadas no contexto de um handler, 95% são não-checadas.
 * */
public class ConvertToRuntimeExceptionsVisitor extends GuidelineCheckerVisitor {

	private Optional<Handler> handlerInScopeOptional;
	
	public ConvertToRuntimeExceptionsVisitor (boolean allowUnresolved)
	{
		super (allowUnresolved);
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
		
		this.handlerInScopeOptional.ifPresent( handler -> handler.getNestedHandlers().add(newHandler) );
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
		List<Signaler> signalersInHandlersContext = this.handlersOfProject.stream()
			.flatMap ( handler -> handler.getEscapingSignalers().stream() )
			.collect (Collectors.toList());
		
		List<Signaler> signalersInHandlersContextWhichThrowRuntimeExceptions = signalersInHandlersContext.stream()
			.filter(signaler -> signaler.getThrownType().getClassType() == ClassType.UNCHECKED_EXCEPTION)
			.collect(Collectors.toList());
		
		System.out.println("Number of signalers in handlers context: " + signalersInHandlersContext.size());
		System.out.println("Number of signalers in handlers context which throw runtime exceptions: " + signalersInHandlersContextWhichThrowRuntimeExceptions.size());
		
		System.out.println("'Convert to runtime exception' conformance: " + 1.0*signalersInHandlersContextWhichThrowRuntimeExceptions.size() / signalersInHandlersContext.size());
	}
}