package ufrn.dimap.lets.ehmetrics.test;

import java.io.IOException;

import com.github.javaparser.ParseException;

public class Test
{
	private void methodA ()
	{
		new BaseClass.NestedStaticClass()
		{
			public void methodB ()
			{
				try
				{
					throw new Exception();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};
	}
	
	private void signaler () throws IOException
	{
		throw new IOException();
	}
	
}

class Logger
{
	public static int SEVERE = 1;
	
	public static Logger getLogger (String s)
	{
		return null;
		
	}
	
	public void log (Level i, String s, Exception ex)
	{
		
	}
}

enum Level
{
	SEVERE;
}
