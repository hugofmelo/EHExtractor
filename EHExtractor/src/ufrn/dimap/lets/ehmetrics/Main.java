package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ufrn.dimap.lets.ehmetrics.analyzer.Analyzer;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ArtifactResolver;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;
import ufrn.dimap.lets.ehmetrics.logger.ArtifactLogger;
import ufrn.dimap.lets.ehmetrics.logger.ErrorLogger;
import ufrn.dimap.lets.ehmetrics.logger.LoggerUtil;

public class Main
{
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args)
	{
		try
		{
			Main main = new Main();
			
			//main.findBadSmells();
			main.execute();
			//main.quickJavaParserTest();
			//main.executeCallGraph();
			//main.generateDependenciesFiles();
			//main.runSonarLint();
			//main.readSonarLintReport();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "", e);
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

			System.out.println("Executando an�lise...");
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

		LOGGER.info("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();

		LOGGER.info("Projetos a serem analisados...");
		for ( File project : projects )
		{
			LOGGER.info(project.getName());
		}
		
		for ( int i = 1 ; i <= projects.size() ; i++ )
		{
			LOGGER.info("Processando projeto " + i + " de " + projects.size() + ": " + projects.get(i).getName());

			LOGGER.info("Identificando arquivos .java...");
			ProjectFiles projectFiles = FileFinder.find(projects.get(i), true, false, false, false, false );

			LOGGER.info("Resolvendo artefatos (e ignorando dependencias)...");			
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolveWithoutDependencies(projectFiles);
			
			LoggerUtil.logFilesAndArtifacts (LOGGER, projects.get(i).getName(), projectFiles, projectArtifacts);

			LOGGER.info("Executando an�lise...");
			Analyzer.analyze(projectArtifacts, true); 

			LOGGER.info("Finalizada a analise do projeto " + projects.get(i).getName() + ".");
		}

		LOGGER.info("Finalizada a aplica��o!!");
	}

	public void quickJavaParserTest() throws IOException
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
			ProjectFiles projectFiles = FileFinder.find(project, true, false, false, false, false );

			System.out.println("Resolvendo artefatos e dependencias...");
			//ProjectArtifacts projectArtifacts = ArtifactResolver.resolve(projectFiles);			
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolveWithoutDependencies(projectFiles);
			
			// Logging
			ArtifactLogger.writeReport(project.getName(), projectFiles, projectArtifacts);

			System.out.println("Executando an�lise...");
			Analyzer.quickAnalyze(projectArtifacts); 

			System.out.println("Salvando resultados...");
			// Logging
			//ModelLogger.writeReport(project.getName(), model);
			ErrorLogger.writeReport(project.getName());

			ErrorLogger.stop();

			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}

		System.out.println("FINALIZADO");
	}
	
	public void generateDependenciesFiles()
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
