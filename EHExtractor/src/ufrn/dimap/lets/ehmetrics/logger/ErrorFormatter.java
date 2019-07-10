package ufrn.dimap.lets.ehmetrics.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ErrorFormatter extends Formatter {

	/**
	 * Format the given LogRecord.
	 * @param record the log record to be formatted.
	 * @return a formatted log record
	 */
	public synchronized String format(LogRecord record)
	{
		// Project, File, Exception_Message, Class, Method, Date?, Stacktrace
		StringBuilder stringBuilder = new StringBuilder();

		if ( !record.getMessage().equals("") )
		{
			String [] messageSplit = record.getMessage().split("/");
			
			String projectName = messageSplit[0];
			String javaFile = messageSplit[1];
			
			stringBuilder.append(projectName);
			stringBuilder.append("\t");
			
			stringBuilder.append(javaFile);
			stringBuilder.append("\t");
		}
		else
		{
			stringBuilder.append("\t");
			stringBuilder.append("\t");
		}
		
		
		stringBuilder.append(record.getThrown().getMessage());
		stringBuilder.append("\t");
		
		String sourceClassName = record.getThrown().getStackTrace()[0].getClassName();
		String sourceMethodName = record.getThrown().getStackTrace()[0].getMethodName();
		String sourceLineNumber = Integer.toString(record.getThrown().getStackTrace()[0].getLineNumber());
				
		stringBuilder.append(sourceClassName);
		stringBuilder.append("\t");
		 
		stringBuilder.append(sourceMethodName);
		stringBuilder.append("\t");
		
		stringBuilder.append(sourceLineNumber);
		stringBuilder.append("\t");
		
		stringBuilder.append(getStackTrace(record.getThrown()));
		stringBuilder.append("\n");
		
		return stringBuilder.toString();
	}

	private static String getStackTrace(Throwable thrown)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		thrown.printStackTrace(pw);
		pw.close();
		
		// TODO gambiarra doida porque não sei usar logger
		String result = "\n"+sw.toString();
		result = result.substring(0, result.lastIndexOf('\n'));
		result = result.replaceAll("\n", "\n\t\t\t\t\t\t");
		return result;
	}
}