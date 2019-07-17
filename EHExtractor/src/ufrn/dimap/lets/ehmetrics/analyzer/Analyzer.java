package ufrn.dimap.lets.ehmetrics.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javassist.NotFoundException;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.projectresolver.JavaProject;
import ufrn.dimap.lets.ehmetrics.visitor.UnsupportedSignalerException;
import ufrn.dimap.lets.ehmetrics.visitor.VisitorException;

public class Analyzer
{
	private static final Logger PROCESSING_LOGGER = LoggerFacade.getProcessingLogger();
	
	
	private Analyzer ()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Analyze one Java project using the given visitors. The visitors stores the results.
	 * */
	public static void analyze(JavaProject javaProject, List <VoidVisitorAdapter<Void>> visitors) throws FileNotFoundException
	{
		JavaParserFacade.clearInstances();
		CombinedTypeSolver typeSolver = Analyzer.configSolver(javaProject);
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser parser = new JavaParser();
		parser.getParserConfiguration().setSymbolResolver(symbolSolver);

		int fileCount = 1, totalFiles = javaProject.getFilesToParse().size();
		for ( File javaFile : javaProject.getFilesToParse() )
		{
			PROCESSING_LOGGER.fine(javaProject.getName() + " " + "[" + fileCount++ + " of " + totalFiles + "] - " + javaFile.getAbsolutePath());
			
			try
			{
				CompilationUnit compUnit = parser.parse(javaFile).getResult().get();

				for ( VoidVisitorAdapter<Void> visitor : visitors )
				{
					//visitor.setJavaFile (javaFile);
					compUnit.accept(visitor, null);
				}
			}
			catch (UnsupportedSignalerException e)
			{
				LoggerFacade.logAnalysisError(javaProject.getName(), javaFile, e);
			}
			catch (VisitorException e)
			{
				LoggerFacade.logAnalysisError(javaProject.getName(), javaFile, e);
			}
		}
	}

	private static CombinedTypeSolver configSolver(JavaProject javaProject)
	{
		CombinedTypeSolver solver = new CombinedTypeSolver();

		// Reflection OR android solver
		solver.add(Analyzer.getAndroidOrReflectionSolver(javaProject));

		// Sources solvers
		for ( File sourceDir : javaProject.getSourceDirs() )
		{
			solver.add( new JavaParserTypeSolver(sourceDir) );
		}

		// Dependencies solvers
		for ( File dependencyFile : javaProject.getDependencies() )
		{
			try
			{
				solver.add( new JarTypeSolver(dependencyFile) );
			}
			catch (RuntimeException e)
			{
				if ( e.getCause() instanceof NotFoundException )
				{
					LoggerFacade.logAnalysisError(javaProject.getName(), dependencyFile, e);
				}
				else
				{
					throw e;
				}
			}
			catch (IOException e)
			{
				LoggerFacade.logAnalysisError(javaProject.getName(), dependencyFile, e);		
			}
		}

		return solver;
	}

	private static TypeSolver getAndroidOrReflectionSolver (JavaProject javaProject)
	{
		TypeSolver solver = null;

		if ( !javaProject.isAndroidProject() )
		{
			solver = new ReflectionTypeSolver();
		}
		else
		{
			try
			{
				solver = new JarTypeSolver(javaProject.getAndroidJar().getAbsolutePath());
			}
			catch (RuntimeException e)
			{
				if ( e.getCause() instanceof NotFoundException )
				{
					LoggerFacade.logAnalysisError(javaProject.getName(), javaProject.getAndroidJar(), e);
				}
				else
				{
					throw e;
				}
			}
			catch (IOException e)
			{
				LoggerFacade.logAnalysisError(javaProject.getName(), javaProject.getAndroidJar(), e);		
			}
		}

		return solver;
	}
}
