package ufrn.dimap.lets.ehmetrics.logger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import ufrn.dimap.lets.ehmetrics.SingleLineFormatter;
import ufrn.dimap.lets.ehmetrics.projectresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.projectresolver.ProjectFiles;

public class LoggerFacade
{
	// para acompanhar o processamento normal do programa
	// para falhas graves que acontecem - 1 arquivo de saída para todos os projetos
	// para resultados dos visitors - 1 arquivo de saída para todos os visitors
	// para informações da análise, inclusive erros, como arquivos parseados e configuração utilizada - 1 arquivo de saída para cada projeto
	
	public static final Logger FILES_AND_ARTIFACTS_LOGGER = Logger.getLogger(LoggerFacade.class.getName());
	
	// Handlers
	private static ConsoleHandler processingHandler;
	private static FileHandler errorHandler;
	private static FileHandler guidelinesHandler;
	private static  FileHandler projectHandler;
	
	private static Logger processingLogger;
	private static Logger errorLogger;
	private static Logger guidelinesLogger;
	private static Logger projectLogger;
	
	public static Logger getProcessingLogger ()
	{
		return processingLogger;
	}
	
	public static Logger getErrorLogger (Class clazz)
	{
		errorLogger = Logger.getLogger("Error"+clazz.getName());
		errorLogger.setLevel(Level.SEVERE);
		errorLogger.addHandler(errorHandler);
		
		return errorLogger;
	}
	
	public static Logger getGuidelinesLogger (Class clazz)
	{
		guidelinesLogger = Logger.getLogger("Guideline"+clazz.getName());
		guidelinesLogger.setLevel(Level.INFO);
		guidelinesLogger.addHandler(guidelinesHandler);
		
		return guidelinesLogger;
	}
	
	public static Logger getProjectLogger (Class clazz, String projectName) throws SecurityException, IOException
	{
		projectHandler = new FileHandler (projectName + ".log");
		projectHandler.setLevel(Level.INFO);
		projectHandler.setFormatter(new SingleLineFormatter());
		projectLogger = Logger.getLogger("Project"+clazz.getName());
		projectLogger.setLevel(Level.INFO);
		projectLogger.addHandler(projectHandler);
		
		return projectLogger;
	}
	
	public static Logger getProjectLogger ()
	{
		return projectLogger;
	}
	
	public static void configLogging () throws SecurityException, IOException
	{
		LogManager.getLogManager().reset();
		
		processingHandler = new ConsoleHandler ();
		processingHandler.setLevel(Level.FINE);
		processingHandler.setFormatter(new SingleLineFormatter());
		processingLogger = Logger.getLogger("processing");
		processingLogger.setLevel(Level.FINE);
		processingLogger.addHandler(processingHandler);
		
		errorHandler = new FileHandler ("errors.log");
		errorHandler.setLevel(Level.SEVERE);
		errorHandler.setFormatter(new SingleLineFormatter());
		
		guidelinesHandler = new FileHandler ("guidelines.log");
		guidelinesHandler.setLevel(Level.INFO);
		guidelinesHandler.setFormatter(new SingleLineFormatter());
	}
	
	
}
