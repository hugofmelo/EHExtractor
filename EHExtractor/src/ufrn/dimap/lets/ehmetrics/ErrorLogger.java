package ufrn.dimap.lets.ehmetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// ErrorLogger é um stateful logger instataneo. Ele armazena estado, mas o arquivo é criado e fechado na mesma chamada.
public class ErrorLogger
{
	private static List<String> errors;
	private static List<String> unsolved;
	private static List<String> unsupported;
	private static List<String> unknownAncestral;

	public static void start () throws IOException
	{
		if (errors != null)
		{
			throw new IllegalStateException ("O ErrorLogger já havia sido iniciado.");
		}
		else
		{	
			errors = new ArrayList<String>();
			unsolved = new ArrayList<String>();
			unsupported = new ArrayList<String>();
			unknownAncestral = new ArrayList<String>();
		}
	}
	
	public static void stop()
	{
		errors = null;
		unsolved = null;
		unsupported = null;
		unknownAncestral = null;
	}

	public static void writeReport(String projectName) throws IOException
	{
		FileWriter logFile = new FileWriter (ProjectsUtil.logsRoot + projectName + "-error.txt");
		
		writeReportCount(logFile);
		writeReportDetails(logFile);
		
		logFile.close();
	}

	private static void writeReportCount(FileWriter logFile) throws IOException
	{
		logFile.write ("Unsolved types: " + unsolved.size() + "\n");
		logFile.write ("Unsupported errors: " + unsupported.size() + "\n");
		logFile.write ("Unkown ancestral: " + unknownAncestral.size() + "\n");
		logFile.write ("\n");
	
	}

	private static void writeReportDetails(FileWriter logFile) throws IOException
	{
		logFile.write("GENERAL ERRORS\n");
		for ( String str : errors )
		{
			logFile.write(str + "\n");
		}
		logFile.write("\n");
		
		logFile.write("UNSOLVED ERROR FILES\n");
		for ( String str : unsolved )
		{
			logFile.write(str + "\n");
		}
		logFile.write("\n");

		logFile.write("UNSUPPORTED ERROR FILES\n");
		for ( String str : unsupported )
		{
			logFile.write(str + "\n");
		}
		logFile.write("\n");

		logFile.write("UNKNOWN ANCESTRAL\n");
		for ( String str : unknownAncestral )
		{
			logFile.write(str + "\n");
		}
		logFile.write("\n");
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
}
