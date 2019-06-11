package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProjectsUtil
{
	private static final Logger LOGGER = Logger.getLogger(ProjectsUtil.class.getName());
	
	private static final boolean PROJECTS_ON_DEMAND = true;
	
	
	
	// Colocar endereço com barra ('/') no final
	// LETS
	//public static final String projectsRoot = "C:/Users/mafeu_000/Projetos GitHub/analysis/";
	//public static final String dependenciesRoot = "./dependencies/lets/";
	
	// CASA
	public static final String PROJECTS_ROOT = "D:/Desenvolvimento/Projetos Github/New survey/";
	public static final String DEPENDENCIES_ROOT = "./dependencies/home/";
	
	public static final String LOGGERS_ROOT = "./loggers/";
	
	private static StringBuilder smellsBuilder;
	private static final String SMELLS_LOG = "./log/smells/log.txt";
	
	private ProjectsUtil () {}
	
	public static List<File> listProjects ()
	{
		if ( PROJECTS_ON_DEMAND )
		{
			LOGGER.info("Using projects on demand");
			return ProjectsUtil.projectsOnDemand();
		}
		else
		{
			LOGGER.info("Using projects in " + PROJECTS_ROOT);
			return ProjectsUtil.projectsOn(new File (PROJECTS_ROOT));
		}
	}
	
	public static List <File> projectsOn (File rootDir)
	{
		List <File> projectsPaths = new ArrayList<>();
		
		for (File project : rootDir.listFiles())
		{
			if ( project.isDirectory() )
			{
				projectsPaths.add(project);
			}
		}
		
		return projectsPaths;
	}
	
	
	public static List <File> projectsOnDemand ()
	{
		List <File> projectsPaths = new ArrayList<>();
		
		// Test self
		projectsPaths.add(new File("../EHExtractor"));
		
		//projectsPaths.add(new File("D:/git/bugsnag-java"));
		
		
		boolean projectDoesntExist = false;
		for (File f : projectsPaths)
		{
			if ( !f.exists() )
			{
				System.err.println("Baixar projeto " + f.getAbsolutePath());
				projectDoesntExist = true;
			}
		}
		
		if (projectDoesntExist)
			System.exit(1);
				
		return projectsPaths;
	}
	
	public static void startSmellsLog()
	{
		smellsBuilder = new StringBuilder();
	}
	
	public static void writeSmellsLog(String content)
	{
		smellsBuilder.append(content);
	}
	
	public static void saveAndCloseSmellsLog ()
	{
		Path path = Paths.get(ProjectsUtil.SMELLS_LOG);
		
		try
		{
			Files.write(path, smellsBuilder.toString().getBytes());
		}
		catch (IOException e)
		{
			throw new ThinkLaterException(e);
		}
	}
}
