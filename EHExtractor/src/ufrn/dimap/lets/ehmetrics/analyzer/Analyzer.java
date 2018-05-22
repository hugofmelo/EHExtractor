package ufrn.dimap.lets.ehmetrics.analyzer;

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
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javassist.NotFoundException;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.logger.ErrorLogger;
import ufrn.dimap.lets.ehmetrics.visitor.HandlerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.QuickMetricsVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.SignalerVisitor;

public class Analyzer
{
	private Analyzer ()
	{
		throw new UnsupportedOperationException();
	}
	
	// Ao encerrar este metodo, o modelo injetado no visitor terá o resultado do processamento
	public static MetricsModel analyze(ProjectArtifacts artifacts)
	{
		JavaParserFacade.clearInstances();
		CombinedTypeSolver solver = Analyzer.configSolver(artifacts);
		MetricsModel model = new MetricsModel ();
		VoidVisitorAdapter <Void> visitor = new QuickMetricsVisitor(model); 
				
		System.out.println("Total de arquivos java: " + artifacts.getJavaFiles().size());
		int fileCount = 1;
		for ( File javaFile : artifacts.getJavaFiles() )
		{
			//List <VoidVisitorAdapter<JavaParserFacade>> visitors = getVisitors(javaFile, model);
			
			System.out.print("Parsing " + fileCount++ + "...");
			try
			{
				CompilationUnit compUnit = JavaParser.parse(new FileInputStream(javaFile.getAbsolutePath()));
				
				
				compUnit.accept(visitor, null);
				
				/*
				for ( VoidVisitorAdapter<JavaParserFacade> visitor : visitors )
				{
					compUnit.accept(visitor, JavaParserFacade.get(solver));
				}
				*/
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
			catch (UnknownSignalerException e)
			{
				System.out.println(" Error - Signaler não reconhecido.");
				ErrorLogger.addUnknownSignaler("Falha no Analyzer. " + e.getMessage() + " File: " + javaFile.getAbsolutePath());
			}
			catch (UnknownAncestralException e)
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

		// Reflection OR android solver
		solver.add(Analyzer.getAndroidOrReflectionSolver(artifacts));
		
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
	
	private static TypeSolver getAndroidOrReflectionSolver (ProjectArtifacts artifacts)
	{
		TypeSolver solver = null;
		
		if ( artifacts.getAndroidJar() == null )
		{
			solver = new ReflectionTypeSolver();
		}
		else
		{
			try
			{
				solver = new JarTypeSolver(artifacts.getAndroidJar().getAbsolutePath());
			}
			catch (RuntimeException e)
			{
				if ( e.getCause() instanceof NotFoundException )
				{
					ErrorLogger.addError("Falha no Analyzer. Falha ao adicionar android.jar. File: " + artifacts.getAndroidJar().getAbsolutePath());
				}
				else
				{
					throw e;
				}
			}
			catch (IOException e)
			{
				ErrorLogger.addError("Falha no Analyzer. Falha ao adicionar android.jar. File: " + artifacts.getAndroidJar().getAbsolutePath());		
			}
		}
		
		return solver;
	}
	
	private static List <VoidVisitorAdapter<JavaParserFacade>> getVisitors (File javaFile, MetricsModel model)
	{
		List <VoidVisitorAdapter<JavaParserFacade>> visitors = new ArrayList <>();
		
		//visitors.add(new TestVisitor());
		visitors.add(new HandlerVisitor(javaFile.getAbsolutePath(), model));
		visitors.add(new SignalerVisitor(javaFile.getAbsolutePath(), model));
		
		return visitors;
	}
}
