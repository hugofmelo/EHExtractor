package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ErrorLogger
{
	private static ErrorLogger instance;

	private FileWriter error;
	private List<File> skippedJavaFiles;
	private List<File> skippedDependencies;

	private String projectName;

	private ErrorLogger ()
	{
			projectName = null;
			this.error = null;
	}

	public void initProjectLogger (String projectName)
	{
		if (this.error != null)
		{
			throw new IllegalStateException ("Tentando iniciar o errorlogger de um projeto enquanto outro ainda está aberto!");
		}
		else
		{
			try
			{
				this.projectName = projectName;
				this.error = new FileWriter (ProjectsUtil.logsRoot + projectName + "-error.txt");
				this.skippedJavaFiles = new ArrayList<File>();
				this.skippedDependencies = new ArrayList<File>();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void closeProjectLogger ()
	{
		try
		{
			this.error.close();
			
			this.projectName = null;
			this.error = null;
			this.skippedJavaFiles = null;
			this.skippedDependencies = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static ErrorLogger getInstance ()
	{
		if (instance == null)
		{
			instance = new ErrorLogger();
		}

		return instance;
	}

	public void write (String msg)
	{
		try
		{
			this.error.write(msg);
			this.error.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void addSkippedFile (File file, ErrorLogger.FileType fileType, ErrorLogger.SkipCause cause)
	{
		if ( fileType == ErrorLogger.FileType.JAVA )
		{
			this.skippedJavaFiles.add(file);
		}
		else if ( fileType == ErrorLogger.FileType.DEPENDENCY )
		{
			this.skippedDependencies.add(file);
		}
		
		if ( cause == ErrorLogger.SkipCause.NOT_FOUND )
		{
			write("Arquivo não encontrado: " + file.getAbsolutePath() + "\n");
		}
		else if ( cause == ErrorLogger.SkipCause.PARSE_FAILED )
		{
			write("Erro ao parsear arquivo: " + file.getAbsolutePath() + "\n");
		}
		else if ( cause == ErrorLogger.SkipCause.INVALID_FILE )
		{
			write("Arquivo inválido: " + file.getAbsolutePath() + "\n");
		}
		
		
	}
	
	public void writeFilesAndArtifacts()
	{
		write ("Skipped java files: " + this.skippedJavaFiles.size() + "\n");
		write("\n");
		
	}
	
	public static enum FileType
	{
		JAVA,
		DEPENDENCY;
	}
	
	public static enum SkipCause
	{
		NOT_FOUND,
		PARSE_FAILED,
		INVALID_FILE;
	}
	/*
	public void writeDetailedFilesAndArtifacts(ProjectFiles files, ProjectArtifacts artifacts)
	{
		write("JAVA FILES\n");
		for ( File f : files.getJavaFiles() )
		{
			write(f.getAbsolutePath()+"\n");
		}
		write("\n");
		
		write("JAR FILES\n");
		for ( File f : files.getJarFiles() )
		{
			write(f.getAbsolutePath()+"\n");
		}
		write("\n");
		
		write("MAVEN FILES\n");
		for ( File f : files.getMavenFiles() )
		{
			write(f.getAbsolutePath()+"\n");
		}
		write("\n");

		writeLog("GRADLE FILES\n");
		for ( File f : files.getGradleFiles() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");
		
		writeLog("ANDROID MANIFEST FILE\n");
		writeLog(files.getAndroidManifest().getAbsolutePath()+"\n");
		writeLog("\n");
		
		writeLog("SOURCE DIRECTORIES\n");
		for ( File f : artifacts.getSourceDirs() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");

		writeLog("TEST DIRECTORIES\n");
		for ( File f : artifacts.getTestDirs() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");

		writeLog("DEPENDENCIES FILES\n");
		for ( File f : artifacts.getDependencies() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");

		writeLog("DEPENDENCIES JARS FILES\n");
		for ( File f : artifacts.getDependencies() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");
	}
*/
	/*
	public void writeProjectname()
	{
		if ( this.projectName != null )
		{
			writeLog("************** " + projectName + " ***************\n");
		}
		else
		{
			writeLog("************** " + "Unnamed project" + " ***************\n");
		}
	}
	*/
	/*
	public void writeModel(MetricsModel model)
	{
		// TYPES
		this.writeOutputLn("EXCEPTION\tTYPE\tORIGIN\tSIGNALERS\tHANDLERS\t");
		for ( Type t : model.listTypes() )
		{
			this.writeOutputLn(t.getQualifiedName()+"\t"+t.getExceptionType()+"\t"+t.getOrigin()+"\t"+t.getSignalers().size()+"\t"+t.getHandlers().size());
		}
		this.writeOutputLn("");
		this.writeOutputLn("");



		// SIGNALERS
		this.writeOutputLn("SIGNALED EXCEPTION\tEXCEPTION TYPE\tEXCEPTION ORIGIN\tTHROW TYPE\tSIGNALER CODE");
		for ( Signaler s : model.getSignalers() )
		{
			if ( s.getType() == SignalerType.UNKNOWN )
			{
				this.writeOutputLn(s.getException().getQualifiedName()+"\t"+s.getException().getExceptionType()+"\t"+s.getException().getOrigin()+"\t"+s.getType()+"\t"+s.getNode());
			}
			else
			{
				this.writeOutputLn(s.getException().getQualifiedName()+"\t"+s.getException().getExceptionType()+"\t"+s.getException().getOrigin()+"\t"+s.getType());
			}

		}
		this.writeOutputLn("");
		this.writeOutputLn("");


		// HANDLERS
		this.writeOutputLn("HANDLER ID\tHANDLED EXCEPTION\tEXCEPTION TYPE\tEXCEPTION ORIGIN\tAUTOCOMPLETE");
		int i = 1;
		for ( Handler handler : model.getHandlers() )
		{
			for ( Type exception : handler.getExceptions() )
			{
				this.writeOutputLn(""+ i + "\t" + exception.getQualifiedName()+"\t"+exception.getExceptionType()+"\t"+exception.getOrigin()+"\t"+handler.isAutoComplete());
			}
			i++;



			if ( handler.getActions().size() == 0 )
			{
				this.writeOutputLn(i+"\t"+handler+"\t"+"EMPTY");
			}
			else
			{
				for ( HandlerAction a : handler.getActions() )
				{
					if ( a.getActionType() == HandlerActionType.UNKNOWN )
					{
						this.writeOutputLn(i+"\t"+handler+"\t"+a.getActionType()+"\t"+a.getNode());
					}
					else
					{
						this.writeOutputLn(i+"\t"+handler+"\t"+a.getActionType()+"\t");
					}

				}
			}

			i++;

		}

		this.writeOutputLn("");
		this.writeOutputLn("");
	}
	 */
	
}
