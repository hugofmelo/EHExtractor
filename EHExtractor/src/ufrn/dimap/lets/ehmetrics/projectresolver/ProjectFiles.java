package ufrn.dimap.lets.ehmetrics.projectresolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que armazena os arquivos de um projeto Java.
 * 
 * Os arquivos que são armezandos são .java, .jar, AndroidManifest.xml, pom.xml, build.grade e jars de dependencias
 * */
public class ProjectFiles
{
	private String projectName;
	private List <File> javaFiles;
	private List <File> jarFiles;
	private List <File> mavenFiles;
	private List <File> gradleFiles;
	private List <File> dependencies;
	private File androidManifest;
	
	public ProjectFiles ()
	{
		javaFiles = new ArrayList <> ();
		jarFiles = new ArrayList <> ();
		mavenFiles = new ArrayList <> ();
		gradleFiles = new ArrayList <> ();
		dependencies = new ArrayList <> ();
		androidManifest = null;
	}
	
	public void setProjectName (String projectName) {
		this.projectName = projectName;
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

	public void addDependency (File file) {
		this.dependencies.add(file);
	}
	
	public void setAndroidManifestFile(File file) {
		this.androidManifest = file;
	}
		
	
	public String getProjectName ()	{
		return this.projectName;
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
	
	public List<File> getDependencies() {
		return this.dependencies;
	}

	public File getAndroidManifest() {
		return this.androidManifest;
	}
	
	@Override
	public String toString ()
	{
		StringBuilder result = new StringBuilder ();
		
		result.append(this.projectName + " files...\n");
		
		result.append("-- Java files\n");
		this.javaFiles.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		
		/*
		result.append("-- Jar files\n");
		this.jarFiles.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		
		result.append("-- Maven files\n");
		this.mavenFiles.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		
		result.append("-- Gradle files\n");
		this.gradleFiles.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		
		result.append("-- Dependencies\n");
		this.dependencies.forEach(file -> result.append(file.getAbsolutePath()+"\n"));
		*/
		return result.toString();
	}
}
