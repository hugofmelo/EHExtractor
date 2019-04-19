package ufrn.dimap.lets.ehmetrics.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ufrn.dimap.lets.ehmetrics.ProjectsUtil;

// ErrorLogger é um stateful logger instataneo. Ele armazena estado, mas o arquivo é criado e fechado na mesma chamada.
public class ErrorLogger
{
	private static List<String> maven;
	private static List<String> gradle;
	
	private static List<String> errors;
	private static List<String> unsolved;
	private static List<String> unsupported;
	private static List<String> unknownAncestral;
	
	private static List<String> unknownSignalers;
	
	private ErrorLogger ()
	{
		
	}
	
	public static void start ()
	{
		if (errors != null)
		{
			throw new IllegalStateException ("O ErrorLogger já havia sido iniciado.");
		}
		else
		{	
			maven = new ArrayList<>();
			gradle = new ArrayList<>();
			
			errors = new ArrayList<>();
			unsolved = new ArrayList<>();
			unsupported = new ArrayList<>();
			unknownAncestral = new ArrayList<>();
			
			unknownSignalers = new ArrayList<>();
		}
	}
	
	public static void stop()
	{
		maven = null;
		gradle = null;
		
		errors = null;
		unsolved = null;
		unsupported = null;
		unknownAncestral = null;		
		
		unknownSignalers = null;
	}

	public static void writeReport(String projectName) throws IOException
	{
		FileWriter errorFile = new FileWriter (ProjectsUtil.loggersRoot + projectName + "-error.txt");
		
		try
		{
			errorFile.write( writeReportCount() );
			errorFile.write( writeReportDetails() );
		}
		finally
		{
			errorFile.close();
		}
	}

	private static String writeReportCount()
	{
		StringBuilder result = new StringBuilder();
		
		result.append("Maven errors: " + maven.size() + "\n");
		result.append("Gradle errors: " + gradle.size() + "\n");
		
		result.append("Unsolved types: " + unsolved.size() + "\n");
		result.append("Unsupported errors: " + unsupported.size() + "\n");
		result.append("Unknown ancestral: " + unknownAncestral.size() + "\n");
		result.append("Unknown signaler: " + unknownSignalers.size() + "\n");
		result.append("\n");
		
		return result.toString();
	
	}

	private static String writeReportDetails()
	{
		StringBuilder result = new StringBuilder();
		
		result.append("GENERAL ERRORS\n");
		for ( String str : errors )
		{
			result.append(str + "\n");
		}
		result.append("\n");
		
		result.append("MAVEN ERROR FILES\n");
		for ( String str : maven )
		{
			result.append(str + "\n");
		}
		result.append("\n");
		
		result.append("GRADLE ERROR FILES\n");
		for ( String str : gradle )
		{
			result.append(str + "\n");
		}
		result.append("\n");
		
		result.append("UNSOLVED ERROR FILES\n");
		for ( String str : unsolved )
		{
			result.append(str + "\n");
		}
		result.append("\n");

		result.append("UNSUPPORTED ERROR FILES\n");
		for ( String str : unsupported )
		{
			result.append(str + "\n");
		}
		result.append("\n");

		result.append("UNKNOWN ANCESTRAL\n");
		for ( String str : unknownAncestral )
		{
			result.append(str + "\n");
		}
		result.append("\n");
		
		result.append("UNKNOWN SIGNALERS\n");
		for ( String str : unknownSignalers )
		{
			result.append(str + "\n");
		}
		result.append("\n");
		
		return result.toString();
	}
	
	public static void addMavenError (String errorMessage)
	{
		maven.add(errorMessage);
	}
	
	public static void addGradleError (String errorMessage)
	{
		gradle.add(errorMessage);
	}
	
	public static void addError (String errorMessage)
	{
		errors.add(errorMessage);
	}
	
	public static void addUnsolved (String errorMessage)
	{
		unsolved.add(errorMessage);
	}
	
	public static void addUnsupported (String errorMessage)
	{
		unsupported.add(errorMessage);
	}
	
	public static void addUnknownAncestral (String errorMessage)
	{
		unknownAncestral.add(errorMessage);
	}
	
	public static void addUnknownSignaler (String errorMessage)
	{
		unknownSignalers.add(errorMessage);
	}
}
