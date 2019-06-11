package ufrn.dimap.lets.ehmetrics.logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;

public class LoggerUtil
{
	public static final Logger FILES_LOGGER = Logger.getLogger(LoggerUtil.class.getName());
	
	public static void logFilesAndArtifacts (Logger logger, String projectName, ProjectFiles files, ProjectArtifacts artifacts)
	{
		FileHandler fileHandler;
		
		try 
		{
			fileHandler = new FileHandler (projectName+"-files and artifacts");
			logger.addHandler(fileHandler);
			
			logger.info(files.toString());
			
			logger.info(artifacts.toString());
			
			logger.removeHandler(fileHandler);
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
}
