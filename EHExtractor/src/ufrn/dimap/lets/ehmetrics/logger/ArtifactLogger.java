package ufrn.dimap.lets.ehmetrics.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ufrn.dimap.lets.ehmetrics.ProjectsUtil;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;

// ArtifactLogger é um stateless logger instantaneo. Ele não precisa armazenar estado e suas chamadas são autocontidas.
public class ArtifactLogger
{
	private ArtifactLogger ()
	{
		throw new UnsupportedOperationException();
	}
	
	public static void writeReport(String projectName, ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		FileWriter logFile = new FileWriter (ProjectsUtil.loggersRoot + projectName + "-artifacts.txt");
		
		try
		{
			logFile.write( writeArtifactsCount(files, artifacts) );
			logFile.write( writeArtifactsDetails(files, artifacts) );
		}
		finally
		{
			logFile.close();
		}
	}

	private static String writeArtifactsCount(ProjectFiles files, ProjectArtifacts artifacts)
	{
		StringBuilder result = new StringBuilder();
		
		result.append("Java files: " + files.getJavaFiles().size() + "\n");
		result.append("Jar files: " + files.getJarFiles().size() + "\n");
		result.append("Maven files: " + files.getMavenFiles().size() + "\n");
		result.append("Gradle files: " + files.getGradleFiles().size() + "\n");
		result.append("Android file: " + (files.getAndroidManifest() == null ? "false":"true") + "\n");
		result.append("\n");

		result.append("Analyzed java files: " + artifacts.getJavaFiles().size() + "\n");
		result.append("Source dirs: " + artifacts.getSourceDirs().size() + "\n");
		result.append("Test dirs: " + artifacts.getTestDirs().size() + "\n");
		result.append("Dependencies files: " + artifacts.getDependencies().size() + "\n");
		result.append("\n");
		
		return result.toString();
	}

	private static String writeArtifactsDetails(ProjectFiles files, ProjectArtifacts artifacts)
	{
		StringBuilder result = new StringBuilder();
		
		result.append("JAVA FILES\n");
		for ( File f : files.getJavaFiles() )
		{
			result.append(f.getAbsolutePath()+"\n");
		}
		result.append("\n");

		result.append("JAR FILES\n");
		for ( File f : files.getJarFiles() )
		{
			result.append(f.getAbsolutePath()+"\n");
		}
		result.append("\n");

		result.append("MAVEN FILES\n");
		for ( File f : files.getMavenFiles() )
		{
			result.append(f.getAbsolutePath()+"\n");
		}
		result.append("\n");
		
		result.append("GRADLE FILES\n");
		for ( File f : files.getGradleFiles() )
		{
			result.append(f.getAbsolutePath()+"\n");
		}
		result.append("\n");

		result.append("ANDROID MANIFEST FILE\n");
		if ( files.getAndroidManifest() != null )
		{
			result.append(files.getAndroidManifest().getAbsolutePath()+"\n");
		}
		result.append("\n");

		result.append("ANALYZED JAVA FILES\n");
		for ( File f : artifacts.getJavaFiles() )
		{
			result.append(f.getAbsolutePath()+"\n");
		}
		result.append("\n");

		result.append("SOURCE DIRECTORIES\n");
		for ( File f : artifacts.getSourceDirs() )
		{
			result.append(f.getAbsolutePath()+"\n");
		}
		result.append("\n");

		result.append("TEST DIRECTORIES\n");
		for ( File f : artifacts.getTestDirs() )
		{
			result.append(f.getAbsolutePath()+"\n");
		}
		result.append("\n");

		result.append("DEPENDENCIES\n");
		for ( File f : artifacts.getDependencies() )
		{
			result.append(f.getAbsolutePath()+"\n");
		}
		result.append("\n");
		
		return result.toString();
	}
}
