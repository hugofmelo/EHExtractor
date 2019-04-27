package ufrn.dimap.lets.ehmetrics.visitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeHierarchy;

/**
 * Visitor para verificar o guideline "Define a single exception".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todas as sinalizações de exceções da aplicação são de uma mesma exceção
 * */
public class DefineSingleExceptionVisitor extends VoidVisitorAdapter<Void> {

	public TypeHierarchy typeHierarchy;
	private List<Signaler> signalers;
	private File javaFile; // Java file being parsed

	public DefineSingleExceptionVisitor ()
	{
		this.typeHierarchy = new TypeHierarchy();
		this.signalers = new ArrayList<>();
		this.javaFile = null;
	}

	@Override
	public void visit (ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Void arg)
	{		
		VisitorsUtil.processClassDeclaration ( classOrInterfaceDeclaration, this.typeHierarchy, this.javaFile );
		
		//VISIT CHILDREN
		super.visit(classOrInterfaceDeclaration, arg);
	}

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		VisitorsUtil.processCatchClause(catchClause, this.typeHierarchy, new Handler (), javaFile);
		

		// VISIT CHILDREN
		super.visit(catchClause, arg);
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		Signaler newSignaler = new Signaler();
		
		VisitorsUtil.processThrowStatement(throwStatement, this.typeHierarchy, newSignaler, javaFile);
		
		this.signalers.add(newSignaler);
		
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
	public void checkGuidelineConformance ()
	{
		Map <Type, Long> systemExceptionSignalersToOccurrences = this.signalers.stream()
				.filter(s -> s.getThrownType().isSystemExceptionType())
				.collect (Collectors.groupingBy(Signaler::getThrownType, Collectors.counting()));
		
		
		Long sumOfSystemExceptionSignalersOccurrences = systemExceptionSignalersToOccurrences.values().stream()
				.mapToLong(Long::longValue)
				.sum();
		System.out.println("Number of system signalers: " + sumOfSystemExceptionSignalersOccurrences);
		
		Long mostSignaledSystemExceptionOccurrences = systemExceptionSignalersToOccurrences.values().stream()
				.max(Comparator.naturalOrder())
				.get();
		System.out.println("Number of signalers of the most signaled exception: " + mostSignaledSystemExceptionOccurrences);
		
		
		System.out.println("'Define a single exception' conformance: " + 1.0*mostSignaledSystemExceptionOccurrences/sumOfSystemExceptionSignalersOccurrences);
	}	
}