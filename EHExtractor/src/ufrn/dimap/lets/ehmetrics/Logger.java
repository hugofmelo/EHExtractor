package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;

// Logger é um stateless logger instantaneo. Ele não precisa armazenar estado e suas chamadas são autocontidas.

public class Logger
{
	public static void writeReport(String projectName, ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		FileWriter logFile = new FileWriter (ProjectsUtil.logsRoot + projectName + "-log.txt");
		
		writeReportCount(logFile, files, artifacts);
		writeReportDetails(logFile, files, artifacts);
		
		logFile.close();
	}

	private static void writeReportCount(FileWriter logFile, ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		logFile.write ("Java files: " + files.getJavaFiles().size() + "\n");
		logFile.write ("Jar files: " + files.getJarFiles().size() + "\n");
		logFile.write ("Maven files: " + files.getMavenFiles().size() + "\n");
		logFile.write ("Gradle files: " + files.getGradleFiles().size() + "\n");
		logFile.write ("Android file: " + (files.getAndroidManifest() == null ? "false":"true") + "\n");
		logFile.write("\n");

		logFile.write ("Analyzed java files: " + artifacts.getJavaFiles().size() + "\n");
		logFile.write ("Source dirs: " + artifacts.getSourceDirs().size() + "\n");
		logFile.write ("Test dirs: " + artifacts.getTestDirs().size() + "\n");
		logFile.write ("Dependencies files: " + artifacts.getDependencies().size() + "\n");
		logFile.write("\n");	
	}

	private static void writeReportDetails(FileWriter logFile, ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		logFile.write("JAVA FILES\n");
		for ( File f : files.getJavaFiles() )
		{
			logFile.write(f.getAbsolutePath()+"\n");
		}
		logFile.write("\n");

		logFile.write("JAR FILES\n");
		for ( File f : files.getJarFiles() )
		{
			logFile.write(f.getAbsolutePath()+"\n");
		}
		logFile.write("\n");

		logFile.write("MAVEN FILES\n");
		for ( File f : files.getMavenFiles() )
		{
			logFile.write(f.getAbsolutePath()+"\n");
		}
		logFile.write("\n");

		logFile.write("GRADLE FILES\n");
		for ( File f : files.getGradleFiles() )
		{
			logFile.write(f.getAbsolutePath()+"\n");
		}
		logFile.write("\n");

		logFile.write("ANDROID MANIFEST FILE\n");
		if ( files.getAndroidManifest() != null )
		{
			logFile.write(files.getAndroidManifest().getAbsolutePath()+"\n");
		}
		logFile.write("\n");

		logFile.write("ANALYZED JAVA FILES\n");
		for ( File f : artifacts.getJavaFiles() )
		{
			logFile.write(f.getAbsolutePath()+"\n");
		}
		logFile.write("\n");

		logFile.write("SOURCE DIRECTORIES\n");
		for ( File f : artifacts.getSourceDirs() )
		{
			logFile.write(f.getAbsolutePath()+"\n");
		}
		logFile.write("\n");

		logFile.write("TEST DIRECTORIES\n");
		for ( File f : artifacts.getTestDirs() )
		{
			logFile.write(f.getAbsolutePath()+"\n");
		}
		logFile.write("\n");

		logFile.write("DEPENDENCIES\n");
		for ( File f : artifacts.getDependencies() )
		{
			logFile.write(f.getAbsolutePath()+"\n");
		}
		logFile.write("\n");
	}
}
