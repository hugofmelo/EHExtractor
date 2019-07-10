package ufrn.dimap.lets.ehmetrics.projectresolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Cada ProjectArtifacts armazena artefatos de um projeto Java.
 * 
 * Os artefatos armazenados são .java, source dirs, test dirs e dependencias (jars)
 * */
public class ProjectArtifacts
{
	private String projectName;
	private List <File> javaFiles;
	private List <File> sourcesDirs;
	private List <File> testDirs;
	private List <File> dependencies; // jars de dependencias externas ao projeto, como pom.xml e build.gradle
	private File androidJar;
	
	public ProjectArtifacts (String projectName)
	{
		this.projectName = projectName;
		javaFiles = new ArrayList<>();
		sourcesDirs = new ArrayList<>();
		testDirs = new ArrayList<>();
		dependencies = new ArrayList <> ();
		androidJar = null;
	}
	
	public void addJavaFile (File javaFile)
	{
		if ( !this.javaFiles.contains(javaFile) )
		{
			this.javaFiles.add(javaFile);
		}
	}
	
	public void addSourceDir (File sourceDir)
	{
		if ( !this.sourcesDirs.contains(sourceDir) )
		{
			this.sourcesDirs.add(sourceDir);
		}
	}
	
	public void addTestDir (File testDir)
	{
		if ( !this.testDirs.contains(testDir) )
		{
			this.testDirs.add(testDir);
		}
	}
	
	public void addDependency (File dependency)
	{
		if ( !this.dependencies.contains(dependency) )
		{
			this.dependencies.add(dependency);
		}
		
	}
	
	public void setAndroidJar (File androidJar) {
		this.androidJar = androidJar;
	}
	
	public List<File> getJavaFiles() {
		return this.javaFiles;
	}
	
	public List<File> getSourceDirs() {
		return this.sourcesDirs;
	}
	
	public List<File> getTestDirs() {
		return this.testDirs;
	}
	
	public List<File> getDependencies() {
		return this.dependencies;
	}
	
	public File getAndroidJar () {
		return this.androidJar;
	}
	
	@Override
	public String toString ()
	{
		StringBuilder result = new StringBuilder ();
		
		result.append(this.projectName + " artifacts...\n");
		
		result.append("-- Java files\n");
		this.javaFiles.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		
		result.append("-- Source dirs\n");
		this.sourcesDirs.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		
		result.append("-- Test dirs\n");
		this.testDirs.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		
		/*
		result.append("-- Dependencies\n");
		this.dependencies.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		*/
		
		return result.toString();
	}
}
