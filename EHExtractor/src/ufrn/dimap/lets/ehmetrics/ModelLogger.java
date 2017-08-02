package ufrn.dimap.lets.ehmetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;

//ModelLogger é um stateless logger de longa duração. O tempo de vida dele é longo, mas ele não precisa armazenar estado (somente o arquivo).
public class ModelLogger
{
	private static FileWriter file = null;

	public static void start () throws IOException
	{
		if (file != null)
		{
			throw new IllegalStateException ("O ModelLogger já havia sido iniciado.");
		}
		else
		{			
			file = new FileWriter (ProjectsUtil.logsRoot + "result.txt");
			writeResult("PROJECT" + "\t" + "EXCEPTIONS" + "\t" + "HANDLERS" + "\t" + "AUTOCOMPLETE" + "\t" + "EMPTY\n" );
		}
	}

	public static void stop () throws IOException
	{
		file.close();
		file = null;
	}

	public static void writeModel(String projectName, MetricsModel model) throws IOException
	{
		FileWriter output;

		int countTotalDistinctExceptions;
		int countTotalAutocompleteHandler;
		int countTotalEmptyHandler;

		int countTypeAutocompleteHandler;
		int countTypeEmptyHandler;

		countTotalDistinctExceptions = 0;
		countTotalAutocompleteHandler = 0;
		countTotalEmptyHandler = 0;


		output = new FileWriter (ProjectsUtil.logsRoot + projectName + "-output.txt");
		output.write("PROJECT\tEXCEPTION\tTYPE\tORIGIN\tHANDLERS\tAUTOCOMPLETE\tEMPTY\n");

		List<Type> exceptions = model.listTypes();
		for ( int i = 1 ; i < exceptions.size() ; i++ )
		{
			Type t = exceptions.get(i);

			// Alguns tipos não tem handlers, mas são listados porque fazem parte da hierarquia
			if ( t.getHandlers().size() > 0 )
			{
				countTypeAutocompleteHandler = 0;
				countTypeEmptyHandler = 0;

				for ( Handler h : t.getHandlers() )
				{				
					if (h.isAutoComplete())
					{
						countTypeAutocompleteHandler++;
					}

					if (h.isEmpty())
					{
						countTypeEmptyHandler++;
					}
				}

				countTotalDistinctExceptions++;
				countTotalAutocompleteHandler += countTypeAutocompleteHandler;
				countTotalEmptyHandler += countTypeEmptyHandler;

				output.write(projectName+"\t"+t.getQualifiedName()+"\t"+t.getExceptionType()+"\t"+t.getOrigin()+"\t"+t.getHandlers().size()+"\t"+countTypeAutocompleteHandler+"\t"+countTypeEmptyHandler+"\n");
			}			
		}
		output.write("\n\n\n");


		// HANDLERS
		output.write("HANDLER ID\tHANDLED EXCEPTION\tEXCEPTION TYPE\tEXCEPTION ORIGIN\tAUTOCOMPLETE\tEMPTY\n");
		int i = 1;
		for ( Handler handler : model.getHandlers() )
		{
			for ( Type exception : handler.getExceptions() )
			{
				output.write(""+ i + "\t" + exception.getQualifiedName()+"\t"+exception.getExceptionType()+"\t"+exception.getOrigin()+"\t"+handler.isAutoComplete()+"\t"+handler.isEmpty()+"\n");
			}
			i++;
		}

		output.close();

		writeResult(projectName + "\t" + countTotalDistinctExceptions + "\t" + model.getHandlers().size() + "\t" + countTotalAutocompleteHandler + "\t" + countTotalEmptyHandler + "\n" );		
	}

	private static void writeResult (String msg) throws IOException
	{
		file.write(msg);
		file.flush();
	}
}

