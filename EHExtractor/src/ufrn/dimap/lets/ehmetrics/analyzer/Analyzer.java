package ufrn.dimap.lets.ehmetrics.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javassist.NotFoundException;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.projectresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.UnsupportedSignalerException;
import ufrn.dimap.lets.ehmetrics.visitor.VisitorException;

public class Analyzer
{
	private static final Logger PROCESSING_LOGGER = LoggerFacade.getProcessingLogger();
	private static final Logger ERROR_LOGGER = LoggerFacade.getErrorLogger(Analyzer.class);
	
	private Analyzer ()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Analyze one Java project using the given visitors. The visitors stores the results.
	 * */
	public static void analyze(ProjectArtifacts artifacts, List <GuidelineCheckerVisitor> visitors) throws FileNotFoundException
	{
		JavaParserFacade.clearInstances();
		CombinedTypeSolver typeSolver = Analyzer.configSolver(artifacts);
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser parser = new JavaParser();
		parser.getParserConfiguration().setSymbolResolver(symbolSolver);
		
		PROCESSING_LOGGER.fine("Total de arquivos java: " + artifacts.getJavaFiles().size());
		int fileCount = 1;
		for ( File javaFile : artifacts.getJavaFiles() )
		{
			PROCESSING_LOGGER.fine("Parsing " + fileCount++ + "...");
			
			try
			{
				CompilationUnit compUnit = parser.parse(javaFile).getResult().get();

				for ( GuidelineCheckerVisitor visitor : visitors )
				{
					visitor.setJavaFile (javaFile);
					compUnit.accept(visitor, null);
				}

				PROCESSING_LOGGER.fine(" Done.");
			}
			catch (UnsupportedSignalerException e)
			{
				ERROR_LOGGER.log(Level.SEVERE, "Exception occurred when processing '" + javaFile.getAbsolutePath() + "' file.", e);
			}
			catch (VisitorException e)
			{
				ERROR_LOGGER.log(Level.SEVERE, "Exception occurred when processing '" + javaFile.getAbsolutePath() + "' file.", e);
			}
		}
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
				solver.add( new JarTypeSolver(dependencyFile) );
			}
			catch (RuntimeException e)
			{
				if ( e.getCause() instanceof NotFoundException )
				{
					ERROR_LOGGER.severe("Falha no Analyzer. Falha ao adicionar JarSolver. File: " + dependencyFile.getAbsolutePath());
				}
				else
				{
					throw e;
				}
			}
			catch (IOException e)
			{
				ERROR_LOGGER.severe("Falha no Analyzer. Falha ao adicionar JarSolver. File: " + dependencyFile.getAbsolutePath());		
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
					ERROR_LOGGER.severe("Falha no Analyzer. Falha ao adicionar android.jar. File: " + artifacts.getAndroidJar().getAbsolutePath());
				}
				else
				{
					throw e;
				}
			}
			catch (IOException e)
			{
				ERROR_LOGGER.severe("Falha no Analyzer. Falha ao adicionar android.jar. File: " + artifacts.getAndroidJar().getAbsolutePath());		
			}
		}

		return solver;
	}
}
