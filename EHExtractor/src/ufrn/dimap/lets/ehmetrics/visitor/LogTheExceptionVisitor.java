package ufrn.dimap.lets.ehmetrics.visitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeHierarchy;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;

/**
 * Visitor para verificar o guideline "Log the exception".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todos os tratadores que não-resinalizam uma exceção realizam o log da mesma.
 * O log é definido a partir da chamada dos métodos: 
 * e.printStackTrace
 * System.out.print*(..., e, ...)
 * System.err.print*(..., e, ...)
 * 
 * */
public class LogTheExceptionVisitor extends VoidVisitorAdapter<Void> {

	public TypeHierarchy typeHierarchy;
	private File javaFile; // Java file being parsed
	private Stack <Handler> handlersInContext;

	private List<Signaler> signalersOfProject;
	private List<Handler> handlersOfProject;

	public LogTheExceptionVisitor ()
	{
		this.typeHierarchy = new TypeHierarchy();

		this.signalersOfProject = new ArrayList<>();
		this.handlersOfProject = new ArrayList<>();
	}
	
	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{
		this.handlersInContext = new Stack<>(); 
		
        super.visit(compilationUnit, arg);
    }

	/*
	@Override
	public void visit (ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Void arg)
	{		
		ResolvedReferenceTypeDeclaration referenceTypeDeclaration = classOrInterfaceDeclaration.resolve();

		if ( referenceTypeDeclaration.isClass() )
		{	
			Type type = this.typeHierarchy.findOrCreateResolvedType(referenceTypeDeclaration.asClass());
			type.setFile(javaFile);
			type.setNode(classOrInterfaceDeclaration);
		}


		//VISIT CHILDREN
		super.visit(classOrInterfaceDeclaration, arg);
	}
	 */

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = new Handler();

		VisitorsUtil.processCatchClause(catchClause, this.typeHierarchy, newHandler, javaFile);		

		this.handlersOfProject.add(newHandler);

		this.handlersInContext.push(newHandler);

		// VISIT CHILDREN
		super.visit(catchClause, arg);

		this.handlersInContext.pop();
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		Signaler newSignaler = new Signaler();

		VisitorsUtil.processThrowStatement(throwStatement, this.typeHierarchy, newSignaler, javaFile);

		this.signalersOfProject.add(newSignaler);

		// All handlers in context have this signaler as escaping exception
		this.handlersInContext.stream().forEach(handler -> handler.getEscapingSignalers().add(newSignaler));

		
		// VISIT CHILDREN
		super.visit(throwStatement, arg);
	}	


	/**
	 * Verifica se o projeto adota o guideline referenciado neste visitor.
	 * 
	 * Para entender as condições do guideline, ver Javadoc da classe
	 * */

	/*De todas as capturas de exceções externas em que aquele tratador re-signaliza
	 * uma exceção, a re-sinalização não é de uma exceção externa em pelo menos 95% dos tratadores.
	 * */
	public void checkGuidelineConformance ()
	{	
		List<Handler> handlersOfExternalExceptions = this.handlersOfProject.stream()
			.filter ( handler -> // check if there are external exception being handled
				handler.getExceptions().stream()
					.anyMatch(type ->
						type.getOrigin() == TypeOrigin.UNRESOLVED ||
						type.getOrigin() == TypeOrigin.LIBRARY))
			.collect (Collectors.toList());
		
		int numberOfHandlersOfExternalExceptions = handlersOfExternalExceptions.size();
		System.out.println("Number of handlers of external exceptions: " + numberOfHandlersOfExternalExceptions);
		
		
		List<Handler> handlersOfExternalExceptionsWhichResignalSomething = handlersOfExternalExceptions.stream()
				.filter ( handler -> !handler.getEscapingSignalers().isEmpty()) // check if there are resignaled
				.collect (Collectors.toList());

		int numberOfHandlersOfExternalExceptionsWhichResignalSomething = handlersOfExternalExceptionsWhichResignalSomething.size();
		System.out.println("Number of handlers of external exceptions which resignal somethings: " + numberOfHandlersOfExternalExceptionsWhichResignalSomething);

		
		List<Handler> handlersOfExternalExceptionsWhichResignalExternalExceptions = handlersOfExternalExceptionsWhichResignalSomething.stream()
				.filter ( handler -> 
					handler.getEscapingSignalers().stream()
						.anyMatch(signaler -> 
							signaler.getThrownType().getOrigin() == TypeOrigin.UNRESOLVED ||
							signaler.getThrownType().getOrigin() == TypeOrigin.LIBRARY))
				.collect(Collectors.toList());

		int numberOfHandlersOfExternalExceptionsWhichResignalExternalExceptions = handlersOfExternalExceptionsWhichResignalExternalExceptions.size();
		System.out.println("Number of handlers of external exceptions which resignal external exceptions: " + numberOfHandlersOfExternalExceptionsWhichResignalExternalExceptions);

		
		System.out.println("'Convert library exceptions' conformance: " + (1.0-(1.0*numberOfHandlersOfExternalExceptionsWhichResignalExternalExceptions/numberOfHandlersOfExternalExceptionsWhichResignalSomething)));
	}
}