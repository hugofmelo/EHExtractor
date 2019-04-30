package ufrn.dimap.lets.ehmetrics.visitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.SignalerType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeHierarchy;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.analyzer.UnknownSignalerException;

/**
 * Visitor para verificar o guideline "Convert library exceptions".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * De todas as capturas de exceções externas em que aquele tratador re-signaliza
 * uma exceção, a re-sinalização não é de uma exceção externa em pelo menos 95% dos tratadores.
 * */
public class ConvertLibraryExceptionsVisitor extends VoidVisitorAdapter<Void> {

	public TypeHierarchy typeHierarchy;
	private File javaFile; // Java file being parsed
	private Stack <Handler> handlersInContext;

	private List<Signaler> signalersOfProject;
	private List<Handler> handlersOfProject;

	public ConvertLibraryExceptionsVisitor ()
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

	public void setJavaFile (File javaFile)
	{
		this.javaFile = javaFile;
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