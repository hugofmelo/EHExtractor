package ufrn.dimap.lets.ehmetrics.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import ufrn.dimap.lets.ehmetrics.projectresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.projectresolver.ProjectFiles;
import ufrn.dimap.lets.ehmetrics.visitor.UnsupportedSignalerException;

public class LoggerFacade
{
	// PROCESSING_LOGGER - para acompanhar o processamento normal do programa
	// ERROR_LOGGER - para falhas graves que acontecem - 1 arquivo de saída para todos os projetos
	// GUIDELINE_LOGGER - para resultados dos visitors - 1 arquivo de saída para todos os visitors
	// PROJECT_LOGGER - para informações da análise, inclusive erros, como arquivos parseados e configuração utilizada - 1 arquivo de saída para cada projeto
	
	// Handlers
	private static ConsoleHandler processingHandler;
	private static FileHandler errorHandler;
	private static FileHandler guidelinesHandler;
	private static FileHandler projectHandler;
	
	// Loggers
	private static Logger processingLogger;
	private static Logger errorLogger;
	private static Logger guidelinesLogger;
	private static Logger projectLogger;
	
	public static Logger getProcessingLogger ()
	{
		return processingLogger;
	}
	
	public static Logger getProjectLogger (String projectName) throws SecurityException, IOException
	{
		if ( projectLogger != null && projectLogger.getName().equals(projectName) )
		{
			return projectLogger;
		}
		
		projectHandler = new FileHandler (projectName + ".log");
		projectHandler.setLevel(Level.INFO);
		projectHandler.setFormatter(new SingleLineFormatter());
		projectLogger = Logger.getLogger(projectName);
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
		errorHandler.setFormatter(new ErrorFormatter());
		errorLogger = Logger.getLogger("error");
		errorLogger.setLevel(Level.SEVERE);
		errorLogger.addHandler(errorHandler);
		
		guidelinesHandler = new FileHandler ("guidelines.log");
		guidelinesHandler.setLevel(Level.INFO);
		guidelinesHandler.setFormatter(new GuidelineFormatter());
		guidelinesLogger = Logger.getLogger("guidelines");
		guidelinesLogger.setLevel(Level.INFO);
		guidelinesLogger.addHandler(guidelinesHandler);
		
	}

	public static void logGuideline ( String message )
	{
		guidelinesLogger.info(message);
	}
	
	public static void logAnalysisError (String projectName, File javaFile, Throwable e) {
		errorLogger.log(Level.SEVERE, projectName+"/"+javaFile.getAbsolutePath(), e);
	}

	public static void logError(Throwable e)
	{
		errorLogger.log(Level.SEVERE, "", e);
	}
	
	
}
