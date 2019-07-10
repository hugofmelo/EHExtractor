package ufrn.dimap.lets.ehmetrics.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ufrn.dimap.lets.ehmetrics.config.ProjectsUtil;

// Logger sem estado de longa duração: não guarda estado E se invocado com o mesmo nome, vai fazer append dos dados.
public class StatelessPersistentLogger
{
	private static FileWriter output = null;

	private StatelessPersistentLogger ()
	{
		
	}
	
	public static void start (String fileName) throws IOException
	{
		if (output != null)
		{
			throw new IllegalStateException ("O Logger já havia sido iniciado.");
		}
		else
		{	
			File file; 
			
			// Result
			file = new File (ProjectsUtil.LOGGERS_ROOT + fileName);
			if ( file.exists() )
			{
				output = new FileWriter (file, true);
			}
			else
			{
				output = new FileWriter (file);
			}
		}
	}

	public static void stop () throws IOException
	{
		output.close();
		output = null;
	}

	public static void write(String text) throws IOException
	{
		output.write( text );
		output.flush();
	}
}

