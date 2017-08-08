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
	public static void writeReport(String projectName, ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		FileWriter logFile = new FileWriter (ProjectsUtil.logsRoot + projectName + "-artifacts.txt");
		
		logFile.write( writeArtifactsCount(files, artifacts) );
		logFile.write( writeArtifactsDetails(files, artifacts) );
		
		logFile.close();
	}

	private static String writeArtifactsCount(ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		String result = "";
		
		result += "Java files: " + files.getJavaFiles().size() + "\n";
		result += "Jar files: " + files.getJarFiles().size() + "\n";
		result += "Maven files: " + files.getMavenFiles().size() + "\n";
		result += "Gradle files: " + files.getGradleFiles().size() + "\n";
		result += "Android file: " + (files.getAndroidManifest() == null ? "false":"true") + "\n";
		result += "\n";

		result += "Analyzed java files: " + artifacts.getJavaFiles().size() + "\n";
		result += "Source dirs: " + artifacts.getSourceDirs().size() + "\n";
		result += "Test dirs: " + artifacts.getTestDirs().size() + "\n";
		result += "Dependencies files: " + artifacts.getDependencies().size() + "\n";
		result += "\n";
		
		return result;
	}

	private static String writeArtifactsDetails(ProjectFiles files, ProjectArtifacts artifacts) throws IOException
	{
		String result = "";
		
		result += "JAVA FILES\n";
		for ( File f : files.getJavaFiles() )
		{
			result += f.getAbsolutePath()+"\n";
		}
		result += "\n";

		result += "JAR FILES\n";
		for ( File f : files.getJarFiles() )
		{
			result += f.getAbsolutePath()+"\n";
		}
		result += "\n";

		result += "MAVEN FILES\n";
		for ( File f : files.getMavenFiles() )
		{
			result += f.getAbsolutePath()+"\n";
		}
		result += "\n";

		result += "GRADLE FILES\n";
		for ( File f : files.getGradleFiles() )
		{
			result += f.getAbsolutePath()+"\n";
		}
		result += "\n";

		result += "ANDROID MANIFEST FILE\n";
		if ( files.getAndroidManifest() != null )
		{
			result += files.getAndroidManifest().getAbsolutePath()+"\n";
		}
		result += "\n";

		result += "ANALYZED JAVA FILES\n";
		for ( File f : artifacts.getJavaFiles() )
		{
			result += f.getAbsolutePath()+"\n";
		}
		result += "\n";

		result += "SOURCE DIRECTORIES\n";
		for ( File f : artifacts.getSourceDirs() )
		{
			result += f.getAbsolutePath()+"\n";
		}
		result += "\n";

		result += "TEST DIRECTORIES\n";
		for ( File f : artifacts.getTestDirs() )
		{
			result += f.getAbsolutePath()+"\n";
		}
		result += "\n";

		result += "DEPENDENCIES\n";
		for ( File f : artifacts.getDependencies() )
		{
			result += f.getAbsolutePath()+"\n";
		}
		result += "\n";
		
		return result;
	}
}
