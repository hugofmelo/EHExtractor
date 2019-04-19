package ufrn.dimap.lets.ehmetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.analyzer.Analyzer;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ArtifactResolver;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;
import ufrn.dimap.lets.ehmetrics.logger.ArtifactLogger;
import ufrn.dimap.lets.ehmetrics.logger.ErrorLogger;
import ufrn.dimap.lets.ehmetrics.logger.ModelLogger;
import ufrn.dimap.lets.ehmetrics.logger.StatelessPersistentLogger;

public class Main
{
	public static void main(String[] args)
	{
		Main main = new Main();

		try
		{
			//main.findBadSmells();
			//main.execute();
			main.executeCallGraph();
			//main.generateDependenciesFiles();
			//main.runSonarLint();
			//main.readSonarLintReport();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	Init logger
	List projects
	foreach project
		find files < projectRoot > files
		resolve dependencies < files > artifacts	
	 	configSolver < dependencies > solver
		configVisitor < model > visitor
		analyze < javaFiles, solver, visitor > model
		write report < model
	write report
	 */	

	/*
	public void findBadSmells() throws IOException
	{
		//ModelLogger.start();
		
		
		System.out.println("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();

		for ( File project : projects )
		{
			System.out.println(project.getName());
		}
		System.out.println();
		System.out.println();

		ProjectsUtil.startSmellsLog();
		for ( File project : projects )
		{
			ErrorLogger.start();

			System.out.println("****** PROJETO " + project.getName() + " ******");

			System.out.println("Identificando arquivos...");
			ProjectFiles projectFiles = FileFinder.find(project);

			System.out.println("Resolvendo artefatos...");			
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolveWithoutDependencies(projectFiles);
			
			// Logging
			ArtifactLogger.writeReport(project.getName(), projectFiles, projectArtifacts);

			System.out.println("Executando análise...");
			Analyzer.analyze2(project.getName(), projectArtifacts); 

			System.out.println("Salvando resultados...");
			// Logging
			////ModelLogger.writeReport(project.getName(), model);
			//ModelLogger.writeQuickMetrics(project.getName(), model);
			ErrorLogger.writeReport(project.getName());

			ErrorLogger.stop();

			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}
		ProjectsUtil.saveAndCloseSmellsLog();

		//ModelLogger.stop();

		System.out.println("FINALIZADO");
	}
	*/
	
	public void execute() throws IOException
	{
		ModelLogger.start();

		System.out.println("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();

		for ( File project : projects )
		{
			System.out.println(project.getName());
		}
		System.out.println();
		System.out.println();


		for ( File project : projects )
		{
			ErrorLogger.start();

			System.out.println("****** PROJETO " + project.getName() + " ******");

			System.out.println("Identificando arquivos...");
			ProjectFiles projectFiles = FileFinder.find(project, true, false, false, false, true );

			System.out.println("Resolvendo artefatos e dependencias...");
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolve(projectFiles);			
			//ProjectArtifacts projectArtifacts = ArtifactResolver.resolveWithoutDependencies(projectFiles);
			
			// Logging
			ArtifactLogger.writeReport(project.getName(), projectFiles, projectArtifacts);

			System.out.println("Executando análise...");
			MetricsModel model = Analyzer.analyze(projectArtifacts); 

			System.out.println("Salvando resultados...");
			// Logging
			//ModelLogger.writeReport(project.getName(), model);
			ModelLogger.writeQuickMetrics(project.getName(), model);
			ErrorLogger.writeReport(project.getName());

			ErrorLogger.stop();

			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}

		ModelLogger.stop();

		System.out.println("FINALIZADO");
	}

	public void executeCallGraph() throws IOException
	{
		ModelLogger.start();

		System.out.println("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();

		for ( File project : projects )
		{
			System.out.println(project.getName());
		}
		System.out.println();
		System.out.println();


		for ( File project : projects )
		{
			ErrorLogger.start();

			System.out.println("****** PROJETO " + project.getName() + " ******");

			System.out.println("Identificando arquivos...");
			ProjectFiles projectFiles = FileFinder.find(project, true, false, false, false, true );

			System.out.println("Resolvendo artefatos e dependencias...");
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolve(projectFiles);			
			
			// Logging
			ArtifactLogger.writeReport(project.getName(), projectFiles, projectArtifacts);

			System.out.println("Executando análise...");
			Analyzer.callgraph(projectArtifacts); 

			System.out.println("Salvando resultados...");
			// Logging
			//ModelLogger.writeReport(project.getName(), model);
			ErrorLogger.writeReport(project.getName());

			ErrorLogger.stop();

			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}

		ModelLogger.stop();

		System.out.println("FINALIZADO");
	}
	
	public void generateDependenciesFiles() throws IOException
	{
		System.out.println("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();

		for ( File project : projects )
		{
			System.out.println(project.getName());
		}
		System.out.println();
		System.out.println();


		for ( File project : projects )
		{
			ErrorLogger.start();

			System.out.println("****** PROJETO " + project.getName() + " ******");

			System.out.println("Identificando arquivos...");
			ProjectFiles projectFiles = FileFinder.find(project, true, false, true, true, true);

			System.out.println("Resolvendo artefatos e dependencias...");
			ArtifactResolver.resolve(projectFiles);			

			ErrorLogger.stop();

			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}

		System.out.println("FINALIZADO");
	}
}
