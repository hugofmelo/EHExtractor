package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.visitor.AutoCompleteCheckVisitor;

public class Analyzer
{
	// Ao encerrar este metodo, o modelo injetado no visitor terá o resultado do processamento
	public static void analyze(ProjectArtifacts artifacts, VoidVisitorAdapter<JavaParserFacade> visitor)
	{
		CombinedTypeSolver solver = Analyzer.configSolver(artifacts);
	
		int fileCount = 1;
		for ( File javaFile : artifacts.getJavaFiles() )
		{
			System.out.print("Parsing " + fileCount++ + "...");
			try
			{
				CompilationUnit compUnit = JavaParser.parse(new FileInputStream(javaFile.getAbsolutePath()));
				compUnit.accept(visitor, JavaParserFacade.get(solver));
				System.out.println(" OK");
			}
			catch (UnsolvedSymbolException e)
			{
				System.out.println(" Error - Unsolved Symbol");
				ErrorLogger.getInstance().write("Falha no Analyzer. Classe '" + e.getMessage() + "' não encontrada. File: " + javaFile.getAbsolutePath() + "\n");
			}
			catch (FileNotFoundException e)
			{
				System.out.println(" Error - File not found.");
				ErrorLogger.getInstance().write("Falha no Analyzer. Arquivo não encontrado. File:" + javaFile.getAbsolutePath() + "\n");
			}
			catch (UnsupportedOperationException e)
			{
				System.out.println(" Error - Unsupported operation.");
				ErrorLogger.getInstance().write("Falha no Analyzer. UnsupportedOperation ao parsear arquivo. File: " + javaFile.getAbsolutePath() + "\n");
			}
			catch (AnalyzerException e)
			{
				System.out.println(" Error - Ancestral não resolvido.");
				ErrorLogger.getInstance().write("Falha no Analyzer. " + e.getMessage() + " File: " + javaFile.getAbsolutePath() + "\n");
			}
		}
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
			catch (IOException e)
			{
				ErrorLogger.getInstance().write("Falha no Analyzer. Falha ao adicionar JarSolver. File: " + dependencyFile.getAbsolutePath() + "\n");		
			}
		}
		
		return solver;
	}
}
