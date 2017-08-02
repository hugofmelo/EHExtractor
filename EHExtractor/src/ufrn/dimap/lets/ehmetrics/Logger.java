package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;

public class Logger
{
	private static Logger instance;

	private FileWriter log;
	private FileWriter result;

	private String projectName;

	private Logger ()
	{
		try
		{
			this.result = new FileWriter (ProjectsUtil.logsRoot + "result.txt");
			this.writeResultLn("PROJECT" + "\t" + "EXCEPTIONS" + "\t" + "HANDLERS" + "\t" + "AUTOCOMPLETE" + "\t" + "EMPTY" );

			projectName = null;
			this.log = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Logger getInstance ()
	{
		if (instance == null)
		{
			instance = new Logger();
		}
	
		return instance;
	}

	public void initProjectLogger (String projectName)
	{
		if (this.log != null)
		{
			throw new IllegalStateException ("Tentando iniciar o logger de um projeto enquanto outro ainda está aberto!");
		}
		else
		{
			try
			{
				this.projectName = projectName;
				this.log = new FileWriter (ProjectsUtil.logsRoot + projectName + "-log.txt");
				this.writeProjectname();
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
			this.log.close();

			this.projectName = null;
			this.log = null;

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeLog (String msg)
	{
		try
		{
			this.log.write(msg);
			this.log.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeResultLn (String msg)
	{
		try
		{
			this.result.write(msg + "\n");
			this.result.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void closeLog ()
	{
		try
		{
			this.log.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void closeResult ()
	{
		try
		{
			this.result.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeFilesAndArtifacts(ProjectFiles files, ProjectArtifacts artifacts)
	{
		this.writeFilesAndArtifactsCount(files, artifacts);
		this.writeFilesAndArtifactsDetails(files, artifacts);
		
	}
	
	private void writeFilesAndArtifactsCount(ProjectFiles files, ProjectArtifacts artifacts)
	{
		writeLog ("Java files: " + files.getJavaFiles().size() + "\n");
		writeLog ("Jar files: " + files.getJarFiles().size() + "\n");
		writeLog ("Maven files: " + files.getMavenFiles().size() + "\n");
		writeLog ("Gradle files: " + files.getGradleFiles().size() + "\n");
		writeLog ("Android file: " + (files.getAndroidManifest() == null ? "false":"true") + "\n");
		writeLog("\n");
	
		writeLog ("Analyzed java files: " + artifacts.getJavaFiles().size() + "\n");
		writeLog ("Source dirs: " + artifacts.getSourceDirs().size() + "\n");
		writeLog ("Test dirs: " + artifacts.getTestDirs().size() + "\n");
		writeLog ("Dependencies files: " + artifacts.getDependencies().size() + "\n");
		writeLog("\n");		
	}
	
	private void writeFilesAndArtifactsDetails(ProjectFiles files, ProjectArtifacts artifacts)
	{
		
		writeLog("JAVA FILES\n");
		for ( File f : files.getJavaFiles() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");
		
		writeLog("JAR FILES\n");
		for ( File f : files.getJarFiles() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");
		
		writeLog("MAVEN FILES\n");
		for ( File f : files.getMavenFiles() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");
		 
		writeLog("GRADLE FILES\n");
		for ( File f : files.getGradleFiles() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");
		
		writeLog("ANDROID MANIFEST FILE\n");
		if ( files.getAndroidManifest() != null )
		{
			writeLog(files.getAndroidManifest().getAbsolutePath()+"\n");
		}
		writeLog("\n");
		
		writeLog("ANALYZED JAVA FILES\n");
		for ( File f : artifacts.getJavaFiles() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
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
	
		writeLog("DEPENDENCIES\n");
		for ( File f : artifacts.getDependencies() )
		{
			writeLog(f.getAbsolutePath()+"\n");
		}
		writeLog("\n");
	}

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

	public void writeModel(MetricsModel model)
	{
		FileWriter output;
		int countTypeAutocomplete;
		int countTypeEmpty;
		
		try
		{
			output = new FileWriter (ProjectsUtil.logsRoot + projectName + "-output.txt");
			output.write("PROJECT\tEXCEPTION\tTYPE\tORIGIN\tHANDLERS\tAUTOCOMPLETE\tEMPTY\n");
			
			List<Type> exceptions = model.listTypes();
			for ( int i = 1 ; i < exceptions.size() ; i++ )
			{
				Type t = exceptions.get(i);

				// Alguns tipos não tem handlers, mas são listados porque fazem parte da hierarquia
				if ( t.getHandlers().size() > 0 )
				{
					countTypeAutocomplete = 0;
					countTypeEmpty = 0;
					
					for ( Handler h : t.getHandlers() )
					{				
						if (h.isAutoComplete())
						{
							countTypeAutocomplete++;
						}
						
						if (h.isEmpty())
						{
							countTypeEmpty++;
						}
					}

					
					
					output.write(this.projectName+"\t"+t.getQualifiedName()+"\t"+t.getExceptionType()+"\t"+t.getOrigin()+"\t"+t.getHandlers().size()+"\t"+countTypeAutocomplete+"\t"+countTypeEmpty+"\n");
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
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
