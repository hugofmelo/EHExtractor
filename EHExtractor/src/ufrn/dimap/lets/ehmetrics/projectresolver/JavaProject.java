package ufrn.dimap.lets.ehmetrics.projectresolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import ufrn.dimap.lets.ehmetrics.config.Projects;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;

/**
 * Um JavaProject. Recebe um caminho e resolve os arquivos e artefatos do projeto.
 * 
 * Algums métodos da classe servem como fachada para não expor ProjectArtifacts.
 * */
public class JavaProject
{
	private String name;
	private File projectPath;
	private ProjectFiles projectFiles;
	private ProjectArtifacts projectArtifacts;
	
	public JavaProject ( File projectPath )
	{	
		this.projectPath = projectPath;
		this.name = projectPath.getName();
		this.projectFiles = null;
		this.projectArtifacts = null;
	}
	
	public void findFiles ()
	{
		this.projectFiles = FileFinder.find(
				this.projectPath,
				Projects.includeJavaFiles,
				Projects.includeAndroidFile,
				Projects.includeJarFiles,
				Projects.includeGradleFiles,
				Projects.includeMavenFiles );
	}
	
	public void resolveArtifacts () throws SecurityException, IOException
	{
		if ( projectFiles == null )
			throw new IllegalStateException ("Call #findFiles first.");
			
		this.projectArtifacts = ArtifactResolver.resolve(projectFiles, Projects.resolveDependencies);
		
		Logger projectLogger = LoggerFacade.getProjectLogger(projectPath.getName());
		
		projectLogger.info(projectFiles.toString());
		projectLogger.info(projectArtifacts.toString());
	}
	
	
	public String getName ()
	{
		return this.name;
	}
	
	// Facade method to cover ProjectArtifacts attribute
	
	public List<File> getFilesToParse ()
	{
		return this.projectArtifacts.getJavaFiles();
	}

	public List<File> getSourceDirs()
	{
		return this.projectArtifacts.getSourceDirs();
	}
	
	public List<File> getDependencies()
	{
		return this.projectArtifacts.getDependencies();
	}

	public boolean isAndroidProject()
	{
		return this.projectArtifacts.getAndroidJar() != null;
	}
	
	public File getAndroidJar()
	{
		return this.projectArtifacts.getAndroidJar();
	}
}
