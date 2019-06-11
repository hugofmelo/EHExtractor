package ufrn.dimap.lets.ehmetrics.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javassist.NotFoundException;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.logger.ErrorLogger;
import ufrn.dimap.lets.ehmetrics.visitor.CatchInSpecificLayerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;

public class Analyzer
{
	private static final Logger LOGGER = Logger.getLogger(Analyzer.class.getName());
	
	private Analyzer ()
	{
		throw new UnsupportedOperationException();
	}

	public static void analyze(ProjectArtifacts artifacts, boolean allowUnresolved) throws FileNotFoundException
	{
		JavaParserFacade.clearInstances();
		CombinedTypeSolver typeSolver = Analyzer.configSolver(artifacts);
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser parser = new JavaParser();
		parser.getParserConfiguration().setSymbolResolver(symbolSolver);

		List <GuidelineCheckerVisitor> guidelinesVisitors = getGuidelineVisitors( allowUnresolved );
		
		LOGGER.info("Total de arquivos java: " + artifacts.getJavaFiles().size());
		int fileCount = 1;
		for ( File javaFile : artifacts.getJavaFiles() )
		{
			LOGGER.info("Parsing " + fileCount++ + "...");
			
			try
			{
				CompilationUnit compUnit = parser.parse(javaFile).getResult().get();

				for ( GuidelineCheckerVisitor visitor : guidelinesVisitors )
				{
					visitor.setJavaFile (javaFile);
					compUnit.accept(visitor, null);
				}

				LOGGER.info(" Done.");
			}
			catch (UnknownSignalerException e)
			{
				LOGGER.log(Level.SEVERE, "Exception occurred when processing '" + javaFile.getAbsolutePath() + "' file.", e);
			}
		}

		LOGGER.info("Guidelines results...");
		for ( GuidelineCheckerVisitor visitor : guidelinesVisitors )
		{
			//System.out.println("Project type hierarchy:");
			//System.out.println(visitor.getTypeHierarchy().toString());
			
			visitor.checkGuidelineConformance();
		}
	}

	private static List<GuidelineCheckerVisitor> getGuidelineVisitors(boolean allowUnresolved)
	{
		List<GuidelineCheckerVisitor> visitors = new ArrayList<>();
		
		//visitors.add(new DefineSuperTypeVisitor(allowUnresolved));
		//visitors.add(new DefineSingleExceptionVisitor(allowUnresolved));
		//visitors.add(new ConvertLibraryExceptionsVisitor(allowUnresolved));
		//visitors.add(new LogTheExceptionVisitor(allowUnresolved));
		//visitors.add(new UseJavaBuiltinExceptionsVisitor(allowUnresolved));
		//visitors.add(new ThrowSpecificExceptionsVisitor(allowUnresolved));
		//visitors.add(new ProtectEntryPointVisitor(allowUnresolved));
		//visitors.add(new SaveTheCauseVisitor(allowUnresolved));
		//visitors.add(new ConvertToRuntimeExceptionsVisitor(allowUnresolved));
		//visitors.add(new AddContextualInformationVisitor(allowUnresolved));
		//visitors.add(new SendToGlobalOrDefaultVisitor(allowUnresolved));
		visitors.add(new CatchInSpecificLayerVisitor(allowUnresolved));
		
		LOGGER.info("Visitors carregados: ");
		visitors.forEach( visitor -> LOGGER.info(visitor.getClass().getName()));
		
		return visitors;
	}

	// Ao encerrar este metodo, o modelo injetado no visitor terá o resultado do processamento
	public static void quickAnalyze(ProjectArtifacts artifacts)
	{
		JavaParserFacade.clearInstances();
		CombinedTypeSolver typeSolver = Analyzer.configSolver(artifacts);
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser parser = new JavaParser();
		parser.getParserConfiguration().setSymbolResolver(symbolSolver);

		VoidVisitorAdapter<Void> visitor = new VoidVisitorAdapter<Void> ()
		{
			@Override
			public void visit (MethodCallExpr callExpression, Void arg)
			{
				callExpression.resolve();
			}
		};

		System.out.println("Total de arquivos java: " + artifacts.getJavaFiles().size());
		int fileCount = 1;
		for ( File javaFile : artifacts.getJavaFiles() )
		{
			System.out.print("Parsing " + fileCount++ + "... " + javaFile.getAbsolutePath() + " ...");
			try
			{
				CompilationUnit compUnit = parser.parse(javaFile).getResult().get();

				compUnit.accept(visitor, null);

				/*
					for ( VoidVisitorAdapter<JavaParserFacade> visitor : visitors )
					{
						compUnit.accept(visitor, JavaParserFacade.get(solver));
					}
				 */
				System.out.println(" OK");
			}
			catch (FileNotFoundException e)
			{
				System.out.println(" Error - File not found.");
				e.printStackTrace();
				ErrorLogger.addError("Falha no Analyzer. Arquivo não encontrado. File:" + javaFile.getAbsolutePath());
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
}
