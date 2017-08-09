package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectArtifacts
{
	private List <File> javaFiles;
	private List <File> sourcesDirs;
	private List <File> testDirs;
	private List <File> dependencies; // jars de dependencias externas ao projeto, como pom.xml e build.gradle
	private File androidJar;
	
	public ProjectArtifacts ()
	{
		javaFiles = new ArrayList<File>();
		sourcesDirs = new ArrayList<File>();
		testDirs = new ArrayList<File>();
		dependencies = new ArrayList <File> ();
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
}
