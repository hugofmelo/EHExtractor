package ufrn.dimap.lets.ehmetrics.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class GuidelineFormatter extends Formatter {

	/**
	 * Format the given LogRecord.
	 * @param record the log record to be formatted.
	 * @return a formatted log record
	 */
	public synchronized String format(LogRecord record)
	{
		return record.getMessage();
	}
}