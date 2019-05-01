package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProjectsUtil
{
	private static final boolean projectsOnDemand = true;
	
	
	
	// Colocar endereço com barra ('/') no final
	// LETS
	//public static final String projectsRoot = "C:/Users/mafeu_000/Projetos GitHub/analysis/";
	//public static final String dependenciesRoot = "./dependencies/lets/";
	
	// CASA
	public static final String projectsRoot = "D:/Desenvolvimento/Projetos Github/New survey/";
	public static final String dependenciesRoot = "./dependencies/home/";
	
	public static final String loggersRoot = "./loggers/";
	
	public static StringBuilder smellsBuilder;
	private static final String smellsLog = "./log/smells/log.txt";
	
	public static List<File> listProjects ()
	{
		if ( projectsOnDemand )
		{
			return ProjectsUtil.projectsOnDemand();
		}
		else
		{
			return ProjectsUtil.projectsOn(new File (projectsRoot));
		}
	}
	
	public static List <File> projectsOn (File rootDir)
	{
		List <File> projectsPaths = new ArrayList<File>();
		
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
		
		//projectsPaths.add(new File("D:/Desenvolvimento/Workspace/TestJavaParser"));
		
		
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
		Path path = Paths.get(ProjectsUtil.smellsLog);
		
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
