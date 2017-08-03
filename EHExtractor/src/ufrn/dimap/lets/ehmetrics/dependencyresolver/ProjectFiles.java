package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectFiles
{
	private List <File> javaFiles;
	private List <File> jarFiles;
	private List <File> mavenFiles;
	private List <File> gradleFiles;
	private File androidManifest;
	
	public ProjectFiles ()
	{
		javaFiles = new ArrayList <File> ();
		jarFiles = new ArrayList <File> ();
		mavenFiles = new ArrayList <File> ();
		gradleFiles = new ArrayList <File> ();
		androidManifest = null;
	}
	
	public void addJavaFile(File file){
		this.javaFiles.add(file);		
	}
	
	public void addJarFile(File file){
		this.jarFiles.add(file);		
	}
	
	public void addMavenFile(File file){
		this.mavenFiles.add(file);		
	}
	
	public void addGradleFile(File file){
		this.gradleFiles.add(file);		
	}

	public void setAndroidManifestFile(File file) {
		this.androidManifest = file;
	}
	
	
	public List<File> getJavaFiles() {
		return this.javaFiles;
	}

	public List<File> getJarFiles() {
		return this.jarFiles;
	}

	public List<File> getMavenFiles() {
		return this.mavenFiles;
	}
	
	public List<File> getGradleFiles() {
		return this.gradleFiles;
	}

	public File getAndroidManifest() {
		return this.androidManifest;
	}
}