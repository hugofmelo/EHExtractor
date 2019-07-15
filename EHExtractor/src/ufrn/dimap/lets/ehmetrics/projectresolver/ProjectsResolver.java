package ufrn.dimap.lets.ehmetrics.projectresolver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ufrn.dimap.lets.ehmetrics.config.Projects;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;

public class ProjectsResolver
{
	private static final Logger PROCESSING_LOGGER = LoggerFacade.getProcessingLogger();
	
	private ProjectsResolver () {}
	
	public static List<JavaProject> findProjects () throws IOException
	{
		List<File> projectsPaths = findProjectsPaths ();
		
		return projectsPaths.stream()
			.map(JavaProject::new)
			.collect(Collectors.toList());
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
}
