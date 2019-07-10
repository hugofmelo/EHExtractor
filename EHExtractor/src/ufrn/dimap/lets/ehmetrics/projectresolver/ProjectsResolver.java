package ufrn.dimap.lets.ehmetrics.projectresolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ufrn.dimap.lets.ehmetrics.Main;
import ufrn.dimap.lets.ehmetrics.config.Projects;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;

public class ProjectsResolver
{
	private static final Logger PROCESSING_LOGGER = LoggerFacade.getProcessingLogger();
	
	private ProjectsResolver () {}
	
	public static List<ProjectArtifacts> resolveProjects () throws IOException
	{
		List<ProjectArtifacts> projectsArtifacts = new ArrayList<>();
		
		PROCESSING_LOGGER.fine("Identificando projetos para analise...");
		List<File> projectsPaths = findProjectsPaths ();
		
		
		for ( int i = 0 ; i < projectsPaths.size() ; i++ )
		{
			PROCESSING_LOGGER.fine("Processando projeto " + (i+1) + " de " + projectsPaths.size() + ": " + projectsPaths.get(i).getName());
			
			PROCESSING_LOGGER.fine("Identificando arquivos...");
			ProjectFiles projectFiles = FileFinder.find(
					projectsPaths.get(i),
					Projects.includeJavaFiles,
					Projects.includeAndroidFile,
					Projects.includeJarFiles,
					Projects.includeGradleFiles,
					Projects.includeMavenFiles );
		
			PROCESSING_LOGGER.fine("Resolvendo artefatos...");			
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolve(projectFiles, Projects.resolveDependencies);
			
			projectsArtifacts.add(projectArtifacts);
			
			logFilesAndArtifacts (projectsPaths.get(i).getName(), projectFiles, projectArtifacts);
		}
		
		return projectsArtifacts;
	}
	
	private static List<File> findProjectsPaths ()
	{
		if ( Projects.PROJECTS_ON_DEMAND )
		{
			PROCESSING_LOGGER.fine("Using projects on demand - list on ufrn.dimap.lets.ehmetrics.config.Projects");
			return Projects.PROJECTS.stream()
					.map(File::new)
					.filter(File::exists)
					.collect(Collectors.toList());
		}
		else
		{
			PROCESSING_LOGGER.fine("Using projects in " + Projects.PROJECTS_ROOT);
			return Arrays.stream(new File (Projects.PROJECTS_ROOT).listFiles())
					.filter(File::isDirectory)
					.collect(Collectors.toList());
		}
	}
	
	private static void logFilesAndArtifacts (String projectName, ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		Logger projectLogger = LoggerFacade.getProjectLogger(projectName);
		
		projectLogger.info(files.toString());
		projectLogger.info(artifacts.toString());		
	}
}
