package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.analyzer.Analyzer;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ArtifactResolver;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;
import ufrn.dimap.lets.ehmetrics.logger.ArtifactLogger;
import ufrn.dimap.lets.ehmetrics.logger.ErrorLogger;
import ufrn.dimap.lets.ehmetrics.logger.ModelLogger;

public class Main
{
	public static void main(String[] args)
	{
		Main main = new Main();
		
		try
		{
			main.execute();
			//main.checkAndroid();
			
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
			ProjectFiles projectFiles = FileFinder.find(project);
			
			System.out.println("Resolvendo artefatos e dependencias...");
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolve(projectFiles);
			
			// Logging
			ArtifactLogger.writeReport(project.getName(), projectFiles, projectArtifacts);

			System.out.println("Executando análise...");
			MetricsModel model = Analyzer.analyze(projectArtifacts); 

			System.out.println("Salvando resultados...");
			// Logging
			ModelLogger.writeReport(project.getName(), model);
			ErrorLogger.writeReport(project.getName());
			ErrorLogger.stop();
			
			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}

		ModelLogger.stop();

		System.out.println("FINALIZADO");
	}
	
	public void checkAndroid() throws IOException
	{
		System.out.println("Identificando projetos para analise...");
		System.out.println();
		List<File> projects = ProjectsUtil.listProjects();

		for ( File project : projects )
		{	
			ProjectFiles projectFiles = FileFinder.find(project);

			System.out.print(project.getName() + "\t");
			System.out.println ( projectFiles.getAndroidManifest() != null );
		}

		System.out.println();
		System.out.println("FINALIZADO");
	}
}
