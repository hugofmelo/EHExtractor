package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;

public class GradleResolver
{
	
	public static List<File> resolveDependencies2(File gradleProjectPath)
	{
		// Output
		List <File> dependencies = new ArrayList<File>();

		// Auxiliary
		List<File> projects = new ArrayList<File>();
		
		GradleConnector gradleConnector = GradleConnector.newConnector();
		//gradleConnector.useInstallation(new File ("C:/Gradle/gradle-4.0.1"));
		gradleConnector.forProjectDirectory(gradleProjectPath.getParentFile());

		ProjectConnection gradleConnection = gradleConnector.connect();

		
		// Listando os projetos
		GradleBuild build = gradleConnection.getModel(GradleBuild.class);
		
		for ( BasicGradleProject project : build.getProjects() )
		{
			projects.add(project.getProjectDirectory());
		}
		
		gradleConnection.close();
		
		for ( File project : projects )
		{
			gradleConnector.forProjectDirectory(project);
			gradleConnection = gradleConnector.connect();
			
			EclipseProject eclipseProject = gradleConnection.getModel(EclipseProject.class);

			//if ( eclipseProject. )
			
			System.out.println("Project: " + eclipseProject.getName());
			System.out.println("Project directory: " + eclipseProject.getProjectDirectory());


			for (EclipseSourceDirectory srcDir : eclipseProject.getSourceDirectories())
			{
				System.out.println(srcDir.getPath());
			}

			for (ExternalDependency externalDependency : eclipseProject.getClasspath())
			{
				dependencies.add(externalDependency.getFile());
				System.out.println(externalDependency.getFile().getName());
			}

			dependencies.size();
			
			gradleConnection.close();
		}

			

			/*
			EclipseProject project = gradleConnection.getModel(EclipseProject.class);

            System.out.println("Project: " + project.getName());
            System.out.println("Project directory: " + project.getProjectDirectory());
            System.out.println("Source directories:");

            //if ( project.getClasspath().size() == 0 )

            for (EclipseSourceDirectory srcDir : project.getSourceDirectories())
            {
                System.out.println(srcDir.getPath());
            }

            System.out.println("Project classpath:");

            for (ExternalDependency externalDependency : project.getClasspath())
            {
            	dependencies.add(externalDependency.getFile());
                System.out.println(externalDependency.getFile().getName());
            }
            System.out.println("Associated gradle project:");
            System.out.println(project.getGradleProject());
			 */

		//System.exit(0);
		return dependencies;

		/*
		Configuration configuration = project.getConfigurations().getByName("compile");
		for (File file : configuration) {
			project.getLogger().lifecycle("Found project dependency @ " + file.getAbsolutePath());
		}
		 */
	}

	public static List<File> resolveDependencies(File gradleProjectPath)
	{
		// Output
		List <File> dependencies = new ArrayList<File>();

		GradleConnector gradleConnector = GradleConnector.newConnector();
		
		//gradleConnector.useInstallation(new File ("C:/Gradle/gradle-4.0.1"));
		gradleConnector.forProjectDirectory(gradleProjectPath.getParentFile());

		ProjectConnection gradleConnection = gradleConnector.connect();

		try {
			// Load the Eclipse model for the project
			//gradleConnection.newBuild();
			/*
			EclipseProject eclipseProject = gradleConnection.getModel(EclipseProject.class);



			for (ExternalDependency externalDependency : eclipseProject.getClasspath())
            {
            	dependencies.add(externalDependency.getFile());
            }

			dependencies.size();

			 */
			//org.gradle.api.Project p = gradleConnection.getModel (Project.)

			EclipseProject eclipseProject = gradleConnection.getModel(EclipseProject.class);
			/*
			System.out.println("Project: " + eclipseProject.getName());
			System.out.println("Project directory: " + eclipseProject.getProjectDirectory());
			 */
			/*
			for (EclipseSourceDirectory srcDir : eclipseProject.getSourceDirectories())
			{
				System.out.println(srcDir.getPath());
			}
			*/

			File dependencyFile;
			for (ExternalDependency externalDependency : eclipseProject.getClasspath())
			{
				//System.out.println(externalDependency.getFile().getAbsolutePath());
				dependencyFile = externalDependency.getFile();
				if ( dependencyFile.getAbsolutePath().endsWith(".jar") )
				{
					dependencies.add(externalDependency.getFile());
				}
			}

			/*
			EclipseProject project = gradleConnection.getModel(EclipseProject.class);

            System.out.println("Project: " + project.getName());
            System.out.println("Project directory: " + project.getProjectDirectory());
            System.out.println("Source directories:");

            //if ( project.getClasspath().size() == 0 )

            for (EclipseSourceDirectory srcDir : project.getSourceDirectories())
            {
                System.out.println(srcDir.getPath());
            }

            System.out.println("Project classpath:");

            for (ExternalDependency externalDependency : project.getClasspath())
            {
            	dependencies.add(externalDependency.getFile());
                System.out.println(externalDependency.getFile().getName());
            }
            System.out.println("Associated gradle project:");
            System.out.println(project.getGradleProject());
			 */
		}
		finally
		{
			// Clean up
			gradleConnection.close();
		}

		//System.exit(0);
		return dependencies;

		/*
		Configuration configuration = project.getConfigurations().getByName("compile");
		for (File file : configuration) {
			project.getLogger().lifecycle("Found project dependency @ " + file.getAbsolutePath());
		}
		 */
	}

}
