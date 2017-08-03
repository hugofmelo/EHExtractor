package ufrn.dimap.lets.ehmetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;

//ModelLogger é um stateless logger de longa duração, mas também possui operações instantaneas. O tempo de vida dele é longo, mas ele não precisa armazenar estado (somente o arquivo).
public class ModelLogger
{
	private static FileWriter result = null;
	private static FileWriter handlers = null;
	private static FileWriter signalers = null;

	public static void start () throws IOException
	{
		if (result != null)
		{
			throw new IllegalStateException ("O ModelLogger já havia sido iniciado.");
		}
		else
		{			
			result = new FileWriter (ProjectsUtil.logsRoot + "result.txt");
			handlers = new FileWriter (ProjectsUtil.logsRoot + "handlers.txt");
			signalers = new FileWriter (ProjectsUtil.logsRoot + "signalers.txt");
			
			result.write("PROJECT" + "\t" + "EXCEPTIONS" + "\t" + "HANDLERS" + "\t" + "AUTOCOMPLETE" + "\t" + "EMPTY\n" );
			result.flush();
			
			handlers.write("PROJECT\tHANDLER ID\tHANDLED EXCEPTION\tEXCEPTION TYPE\tEXCEPTION ORIGIN\tAUTOCOMPLETE\tEMPTY\n");
			handlers.flush();
		}
	}

	public static void stop () throws IOException
	{
		result.close();
		handlers.close();
		signalers.close();
		
		result = null;
		handlers = null;
		signalers = null;
	}

	public static void writeReport(String projectName, MetricsModel model) throws IOException
	{
		result.write( writeProjectCount (projectName, model) );
		result.flush();
		
		handlers.write(writeHandlers (projectName, model) );
		handlers.flush();
		
		FileWriter output = new FileWriter (ProjectsUtil.logsRoot + projectName + "-output.txt");
		output.write( writeHierarchy (projectName, model) ); output.write("\n\n");
		//output.write( writeTypesCount (projectName, model) ); output.write("\n\n");
		//output.write(writeSignalers (projectName, model) ); output.write("\n\n");
		output.close();
	}

	private static String writeProjectCount (String projectName, MetricsModel model) throws IOException
	{		
		int distinctHandledExceptions;
		//int distinctSignaledExceptions;
		int autocompleteHandlers;
		int emptyHandlers;

		distinctHandledExceptions = 0;
		autocompleteHandlers = 0;
		emptyHandlers = 0;

		List<Type> exceptions = model.listTypes();
		// Skip do tipo 0, que é Object
		for ( int i = 1 ; i < exceptions.size() ; i++ )
		{
			Type t = exceptions.get(i);

			// Alguns tipos não tem handlers, mas são listados porque fazem parte da hierarquia
			if ( t.getHandlers().size() > 0 )
			{
				distinctHandledExceptions++;
				
				for ( Handler h : t.getHandlers() )
				{				
					if (h.isAutoComplete())
					{
						autocompleteHandlers++;
					}

					if (h.isEmpty())
					{
						emptyHandlers++;
					}
				}
			}			
		}
		
		return projectName + "\t" + distinctHandledExceptions + "\t" + model.getHandlers().size() + "\t" + autocompleteHandlers + "\t" + emptyHandlers + "\n";
	}
	
	private static String writeHierarchy (String projectName, MetricsModel model) throws IOException
	{
		String result = "";
		
		result += "EXCEPTION HIERARCHY" + "\n";
		
		if ( model.getRoot().getSubTypes().size() != 0 )
		{
			result += writeHierarchyR (model.getRoot().getSubTypes().get(0), "");
		}
		
		return result;
	}
	
	private static String writeHierarchyR (Type type, String tabs)
	{
		String result = "";
		
		result = tabs + type.getQualifiedName() + "\n";
		
		for ( Type t : type.getSubTypes() )
		{
			result += writeHierarchyR(t, tabs+"\t");
		}
		
		return result;
	}
	
	private static String writeTypesCount (String projectName, MetricsModel model) throws IOException
	{
		String result = "";
		
		int autocompleteHandlers;
		int emptyHandlers;

		result += "PROJECT" + "\t" + "EXCEPTION" + "\t" + "HANDLERS" + "\t" + "AUTOCOMPLETE" + "\t" + "EMPTY" + "\n";
		
		List<Type> exceptions = model.listTypes();
		// Skip do tipo 0, que é Object
		for ( int i = 1 ; i < exceptions.size() ; i++ )
		{
			Type t = exceptions.get(i);

			autocompleteHandlers = 0;
			emptyHandlers = 0;
			
			// Alguns tipos não tem handlers, mas são listados porque fazem parte da hierarquia
			if ( t.getHandlers().size() > 0 )
			{				
				for ( Handler h : t.getHandlers() )
				{				
					if (h.isAutoComplete())
					{
						autocompleteHandlers++;
					}

					if (h.isEmpty())
					{
						emptyHandlers++;
					}
				}
			}
			
			result += projectName + "\t" + t.getQualifiedName() + "\t" + t.getHandlers().size() + "\t" + autocompleteHandlers + "\t" + emptyHandlers + "\n";
		}
		
		return result;
	}
	
	private static String writeSignalers (String projectName, MetricsModel model)
	{
		return null;
	}
	
	private static String writeHandlers (String projectName, MetricsModel model) throws IOException
	{	
		String result = "";
		
		int i = 1;
		for ( Handler handler : model.getHandlers() )
		{
			for ( Type exception : handler.getExceptions() )
			{
				result += projectName + "\t" + i + "\t" + exception.getQualifiedName()+"\t"+exception.getExceptionType()+"\t"+exception.getOrigin()+"\t"+handler.isAutoComplete()+"\t"+handler.isEmpty()+"\n";
			}
			i++;
		}
		
		return result;
	}
}

