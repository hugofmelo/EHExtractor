package ufrn.dimap.lets.ehmetrics.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javassist.NotFoundException;
import ufrn.dimap.lets.ehmetrics.ProjectsUtil;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.logger.ErrorLogger;
import ufrn.dimap.lets.ehmetrics.visitor.DefineSingleExceptionVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.DefineSuperTypeVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.HandlerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.MainMethodVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.QuickMetricsVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.SignalerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.SimpleVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.smells.UnprotectedMainVisitor;

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
		CombinedTypeSolver typeSolver = Analyzer.configSolver(artifacts);
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser parser = new JavaParser();
		parser.getParserConfiguration().setSymbolResolver(symbolSolver);
		MetricsModel model = new MetricsModel ();
		DefineSuperTypeVisitor visitor = new DefineSuperTypeVisitor(); 
		//DefineSingleExceptionVisitor visitor = new DefineSingleExceptionVisitor();
		
		System.out.println("Total de arquivos java: " + artifacts.getJavaFiles().size());
		int fileCount = 1;
		for ( File javaFile : artifacts.getJavaFiles() )
		{
			//List <VoidVisitorAdapter<JavaParserFacade>> visitors = getVisitors(javaFile, model);

			System.out.print("Parsing " + fileCount++ + "...");
			try
			{
				CompilationUnit compUnit = parser.parse(javaFile).getResult().get();

				visitor.setJavaFile (javaFile);

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

		System.out.println(visitor.typeHierarchy.toString());
		
		visitor.checkGuidelineConformance();
		
		return model;
	}
	
	// Ao encerrar este metodo, o modelo injetado no visitor terá o resultado do processamento
		public static void callgraph(ProjectArtifacts artifacts)
		{
			JavaParserFacade.clearInstances();
			CombinedTypeSolver typeSolver = Analyzer.configSolver(artifacts);
			JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
			JavaParser parser = new JavaParser();
			parser.getParserConfiguration().setSymbolResolver(symbolSolver);
			MainMethodVisitor <Void> visitor = new MainMethodVisitor<>(); 

			//System.out.println("Total de arquivos java: " + artifacts.getJavaFiles().size());
			//int fileCount = 1;
			for ( File javaFile : artifacts.getJavaFiles() )
			{
				//List <VoidVisitorAdapter<JavaParserFacade>> visitors = getVisitors(javaFile, model);

				//System.out.print("Parsing " + fileCount++ + "...");
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
			
			for ( MethodDeclaration mainMethod : visitor.getMainMethods() )
			{
				List<MethodCallExpr> calls = mainMethod.findAll(MethodCallExpr.class);
				
				for ( MethodCallExpr call : calls )
				{
					ResolvedMethodDeclaration decla = call.resolve();
					System.out.println(decla.getName());
					//decla.toAst().get().findAll(MethodCallExpr.class);
					
					//List<MethodCallExpr> calls2 = decla.toAst().get().findAll(MethodCallExpr.class);
					
					
				}
			}
		}


	public static void analyze2(String projectName, ProjectArtifacts artifacts)
	{
		List <SimpleVisitor> visitors = getVisitors2(); 

		// Criando cabeçalho do log
		ProjectsUtil.writeSmellsLog("Projects\t");
		for ( SimpleVisitor visitor : visitors )
		{
			ProjectsUtil.writeSmellsLog(visitor.printHeader());
		}
		ProjectsUtil.writeSmellsLog("\n");
		
		System.out.println("Total de arquivos java: " + artifacts.getJavaFiles().size());
		
		int fileCount = 1;
		for ( File javaFile : artifacts.getJavaFiles() )
		{
			System.out.print("Parsing " + fileCount++ + "...");
			try
			{
				CompilationUnit compUnit = StaticJavaParser.parse(javaFile);

				for ( VoidVisitorAdapter<Void> visitor : visitors )
				{
					compUnit.accept(visitor, null);
				}

				System.out.println(" OK");
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
		}
		
		ProjectsUtil.writeSmellsLog(projectName + "\t");
		for ( SimpleVisitor visitor : visitors )
		{
			ProjectsUtil.writeSmellsLog(visitor.printOutput());
		}
		ProjectsUtil.writeSmellsLog("\n");
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

	private static List <VoidVisitorAdapter<JavaParserFacade>> getVisitors (File javaFile, MetricsModel model)
	{
		List <VoidVisitorAdapter<JavaParserFacade>> visitors = new ArrayList <>();

		//visitors.add(new TestVisitor());
		visitors.add(new HandlerVisitor(javaFile.getAbsolutePath(), model));
		visitors.add(new SignalerVisitor(javaFile.getAbsolutePath(), model));

		return visitors;
	}
	
	private static List <SimpleVisitor> getVisitors2 ()
	{
		List <SimpleVisitor> visitors = new ArrayList <>();

		//visitors.add(new TestVisitor());
		//visitors.add(new HandlingCodingErrorVisitor());
		visitors.add(new UnprotectedMainVisitor());

		return visitors;
	}
}
