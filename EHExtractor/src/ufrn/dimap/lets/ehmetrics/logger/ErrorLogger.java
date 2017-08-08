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
	

	public static void start () throws IOException
	{
		if (errors != null)
		{
			throw new IllegalStateException ("O ErrorLogger já havia sido iniciado.");
		}
		else
		{	
			maven = new ArrayList<String>();
			gradle = new ArrayList<String>();
			
			errors = new ArrayList<String>();
			unsolved = new ArrayList<String>();
			unsupported = new ArrayList<String>();
			unknownAncestral = new ArrayList<String>();
			
			unknownSignalers = new ArrayList<String>();
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
		FileWriter errorFile = new FileWriter (ProjectsUtil.logsRoot + projectName + "-error.txt");
		
		errorFile.write( writeReportCount() );
		errorFile.write( writeReportDetails() );
		
		errorFile.close();
	}

	private static String writeReportCount() throws IOException
	{
		String result = "";
		
		result += "Maven errors: " + maven.size() + "\n";
		result += "Gradle errors: " + gradle.size() + "\n";
		
		result += "Unsolved types: " + unsolved.size() + "\n";
		result += "Unsupported errors: " + unsupported.size() + "\n";
		result += "Unknown ancestral: " + unknownAncestral.size() + "\n";
		result += "Unknown signaler: " + unknownSignalers.size() + "\n";
		result += "\n";
		
		return result;
	
	}

	private static String writeReportDetails() throws IOException
	{
		String result = "";
		
		result += "GENERAL ERRORS\n";
		for ( String str : errors )
		{
			result += str + "\n";
		}
		result += "\n";
		
		result += "MAVEN ERROR FILES\n";
		for ( String str : maven )
		{
			result += str + "\n";
		}
		result += "\n";
		
		result += "GRADLE ERROR FILES\n";
		for ( String str : gradle )
		{
			result += str + "\n";
		}
		result += "\n";
		
		result += "UNSOLVED ERROR FILES\n";
		for ( String str : unsolved )
		{
			result += str + "\n";
		}
		result += "\n";

		result += "UNSUPPORTED ERROR FILES\n";
		for ( String str : unsupported )
		{
			result += str + "\n";
		}
		result += "\n";

		result += "UNKNOWN ANCESTRAL\n";
		for ( String str : unknownAncestral )
		{
			result += str + "\n";
		}
		result += "\n";
		
		result += "UNKNOWN SIGNALERS\n";
		for ( String str : unknownSignalers )
		{
			result += str + "\n";
		}
		result += "\n";
		
		return result;
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
