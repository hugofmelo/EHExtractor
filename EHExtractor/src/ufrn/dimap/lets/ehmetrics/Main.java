package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import ufrn.dimap.lets.ehmetrics.analyzer.Analyzer;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ArtifactResolver;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;

public class Main
{
	static 
	{
		try
		{
			LoggerFacade.configLogging();
		}
		catch (Exception e)
		{
			System.exit(1);
		}
	}
	
	private static final Logger PROCESSING_LOGGER = LoggerFacade.getProcessingLogger();
	private static final Logger ERROR_LOGGER = LoggerFacade.getProcessingLogger();
	
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
			ERROR_LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
		PROCESSING_LOGGER.fine("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.findProjects();

		PROCESSING_LOGGER.fine("Projetos a serem analisados...");
		for ( File project : projects )
		{
			PROCESSING_LOGGER.fine(project.getName());
		}
		
		for ( int i = 0 ; i < projects.size() ; i++ )
		{
			PROCESSING_LOGGER.fine("Processando projeto " + (i+1) + " de " + projects.size() + ": " + projects.get(i).getName());

			PROCESSING_LOGGER.fine("Identificando arquivos .java...");
			ProjectFiles projectFiles = FileFinder.find(projects.get(i), true, false, false, false, false );

			PROCESSING_LOGGER.fine("Resolvendo artefatos (e ignorando dependencias)...");			
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolveWithoutDependencies(projectFiles);
			
			logFilesAndArtifacts (projects.get(i).getName(), projectFiles, projectArtifacts);

			PROCESSING_LOGGER.fine("Executando análise...");
			Analyzer.analyze(projectArtifacts, true); 

			PROCESSING_LOGGER.fine("Finalizada a analise do projeto " + projects.get(i).getName() + ".");
		}

		PROCESSING_LOGGER.fine("Finalizada a aplicação!!");
	}
	
	public void generateDependenciesFiles()
	{
		PROCESSING_LOGGER.fine("Gerando arquivos com dependencias");
		
		PROCESSING_LOGGER.fine("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.findProjects();

		PROCESSING_LOGGER.fine("Projetos a serem analisados...");
		for ( File project : projects )
		{
			PROCESSING_LOGGER.fine(project.getName());
		}

		for ( int i = 1 ; i <= projects.size() ; i++ )
		{
			PROCESSING_LOGGER.fine("Processando projeto " + i + " de " + projects.size() + ": " + projects.get(i).getName());

			PROCESSING_LOGGER.fine("Identificando arquivos .java, .jar, pom.xml e gradle.properties...");
			ProjectFiles projectFiles = FileFinder.find(projects.get(i), true, false, true, true, true);

			PROCESSING_LOGGER.fine("Resolvendo artefatos (e ignorando dependencias)...");
			ArtifactResolver.resolve(projectFiles);			

			PROCESSING_LOGGER.fine("Finalizada a analise do projeto " + projects.get(i).getName() + ".");
		}

		PROCESSING_LOGGER.fine("Finalizada a aplicação!!");
	}
	
	private static void logFilesAndArtifacts (String projectName, ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		Logger projectLogger = LoggerFacade.getProjectLogger(Main.class, projectName);
		
		projectLogger.info(files.toString());
		projectLogger.info(artifacts.toString());		
	}
}
