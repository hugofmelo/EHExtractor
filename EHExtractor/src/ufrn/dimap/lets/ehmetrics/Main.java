package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ArtifactResolver;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;
import ufrn.dimap.lets.ehmetrics.visitor.AutoCompleteCheckVisitor;

public class Main
{
	public static void main(String[] args)
	{
		Main main = new Main();
		
		try
		{
			main.execute();
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
			Logger.writeReport(project.getName(), projectFiles, projectArtifacts);

			System.out.println("Criando modelo...");
			MetricsModel model = new MetricsModel();

			System.out.println("Criando visitor...");
			//TestVisitor visitor = new TestVisitor();
			AutoCompleteCheckVisitor visitor = new AutoCompleteCheckVisitor(model);

			System.out.println("Executando análise...");
			Analyzer.analyze(projectArtifacts, visitor);

			System.out.println("Salvando resultados...");
			// Logging
			ModelLogger.writeModel(project.getName(), model);
			ErrorLogger.writeReport(project.getName());
			ErrorLogger.stop();
			
			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}

		ModelLogger.stop();

		System.out.println("FINALIZADO");
	}
}
