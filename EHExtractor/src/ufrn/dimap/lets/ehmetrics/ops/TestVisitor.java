package ufrn.dimap.lets.ehmetrics.ops;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.analyzer.Analyzer;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.visitor.UnsupportedSignalerException;
import ufrn.dimap.lets.ehmetrics.visitor.VisitorException;

public class TestVisitor extends VoidVisitorAdapter<Void>
{
	private Set<CatchClause> catchClauses;
	
	public static void main (String args[]) throws FileNotFoundException
	{
		TestVisitor visitor = new TestVisitor();
		JavaParser parser = new JavaParser();
		CompilationUnit compUnit = parser.parse(new File("src/ufrn/dimap/lets/ehmetrics/ops/TestVisitor.java")).getResult().get();
		compUnit.accept(visitor, null);
		
		System.out.println(visitor.getCatchClauses().size());
	}
		
	public TestVisitor ()
	{
		catchClauses = new HashSet<>();
	}
	
	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		this.catchClauses.add(catchClause);

		// VISIT CHILDREN
		super.visit(catchClause, arg);
	} 
	
	public Set<CatchClause> getCatchClauses ()
	{
		return this.catchClauses;
	}
	
	private void methodA ()
	{
		try
		{
			throw new Exception();
		}
		catch (Exception e)
		{
			System.out.println("Identical catch");
		}
	}
	
	private void methodB ()
	{
		try
		{
			throw new Exception();
		}
		catch (Exception e)
		{
			System.out.println("Identical catch");
		}
	}
}


