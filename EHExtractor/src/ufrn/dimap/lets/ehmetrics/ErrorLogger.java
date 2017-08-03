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
		FileWriter errorFile = new FileWriter (ProjectsUtil.logsRoot + projectName + "-error.txt");
		
		errorFile.write( writeReportCount() );
		errorFile.write( writeReportDetails() );
		
		errorFile.close();
	}

	private static String writeReportCount() throws IOException
	{
		String result = "";
		
		result += "Unsolved types: " + unsolved.size() + "\n";
		result += "Unsupported errors: " + unsupported.size() + "\n";
		result += "Unkown ancestral: " + unknownAncestral.size() + "\n";
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
		
		return result;
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
