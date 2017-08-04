package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javassist.NotFoundException;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.visitor.HandlerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.SignalerVisitor;

public class Analyzer
{
	// Ao encerrar este metodo, o modelo injetado no visitor terá o resultado do processamento
	public static MetricsModel analyze(ProjectArtifacts artifacts)
	{
		CombinedTypeSolver solver = Analyzer.configSolver(artifacts);
		MetricsModel model = new MetricsModel ();
		
		int fileCount = 1;
		for ( File javaFile : artifacts.getJavaFiles() )
		{
			List <VoidVisitorAdapter<JavaParserFacade>> visitors = getVisitors(model);
			
			System.out.print("Parsing " + fileCount++ + "...");
			try
			{
				CompilationUnit compUnit = JavaParser.parse(new FileInputStream(javaFile.getAbsolutePath()));
				
				for ( VoidVisitorAdapter<JavaParserFacade> visitor : visitors )
				{
					compUnit.accept(visitor, JavaParserFacade.get(solver));
				}
				
				System.out.println(" OK");
			}
			catch (UnsolvedSymbolException e)
			{
				System.out.println(" Error - Unsolved Symbol");
				ErrorLogger.addUnsolved("Falha no Analyzer. Classe '" + e.getMessage() + "' não encontrada. File: " + javaFile.getAbsolutePath());
			}
			catch (FileNotFoundException e)
			{
				System.out.println(" Error - File not found.");
				ErrorLogger.addError("Falha no Analyzer. Arquivo não encontrado. File:" + javaFile.getAbsolutePath());
			}
			catch (UnsupportedOperationException e)
			{
				System.out.println(" Error - Unsupported operation.");
				ErrorLogger.addUnsupported("Falha no Analyzer. UnsupportedOperation ao parsear arquivo. File: " + javaFile.getAbsolutePath());
			}
			catch (AnalyzerException e)
			{
				System.out.println(" Error - Ancestral não resolvido.");
				ErrorLogger.addUnknownAncestral("Falha no Analyzer. " + e.getMessage() + " File: " + javaFile.getAbsolutePath());
			}
		}
		
		return model;
	}

	private static CombinedTypeSolver configSolver(ProjectArtifacts artifacts)
	{
		CombinedTypeSolver solver = new CombinedTypeSolver();

		// Reflection solver
		solver.add(new ReflectionTypeSolver());

		// Sources solvers
		for ( File sourceDir : artifacts.getSourceDirs() )
		{
			solver.add( new JavaParserTypeSolver(sourceDir) );
		}
		
		// Dependencies solvers
		for ( File dependencyFile : artifacts.getDependencies() )
		{
			try
			{
				solver.add( new JarTypeSolver(dependencyFile.getAbsolutePath()) );
			}
			catch (RuntimeException e)
			{
				if ( e.getCause() instanceof NotFoundException )
				{
					ErrorLogger.addError("Falha no Analyzer. Falha ao adicionar JarSolver. File: " + dependencyFile.getAbsolutePath());
				}
				else
				{
					throw e;
				}
			}
			catch (IOException e)
			{
				ErrorLogger.addError("Falha no Analyzer. Falha ao adicionar JarSolver. File: " + dependencyFile.getAbsolutePath());		
			}
		}
		
		return solver;
	}
	
	private static List <VoidVisitorAdapter<JavaParserFacade>> getVisitors (MetricsModel model)
	{
		List <VoidVisitorAdapter<JavaParserFacade>> visitors = new ArrayList <VoidVisitorAdapter<JavaParserFacade>>();
		
		//visitors.add(new TestVisitor());
		visitors.add(new HandlerVisitor(model));
		visitors.add(new SignalerVisitor(model));
		
		return visitors;
	}
}
