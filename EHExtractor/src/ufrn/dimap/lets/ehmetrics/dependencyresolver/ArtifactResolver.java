package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import ufrn.dimap.lets.ehmetrics.ProjectsUtil;
import ufrn.dimap.lets.ehmetrics.logger.ErrorLogger;

public class ArtifactResolver
{
	public static ProjectArtifacts resolve (ProjectFiles projectFiles)
	{
		ProjectArtifacts artifacts = new ProjectArtifacts();

		findArtifacts(projectFiles, artifacts);
		
		boolean loadedDependencies = ArtifactResolver.loadDependencies(projectFiles.getProjectName(), artifacts);
		
		if ( !loadedDependencies )
		{
			findDependencies(projectFiles, artifacts);
			saveDependencies(projectFiles.getProjectName(), artifacts);
		}
		
		return artifacts;
	}
	
	public static ProjectArtifacts resolveWithoutDependencies(ProjectFiles projectFiles) {
		ProjectArtifacts artifacts = new ProjectArtifacts();

		findArtifacts(projectFiles, artifacts);
		
		return artifacts;
	}

	// artifacts é output do método
	private static void findArtifacts(ProjectFiles files, ProjectArtifacts artifacts)
	{
		for ( File javaFile : files.getJavaFiles() )
		{
			try
			{
				String path = getSourceDir(javaFile);

				// Verifica se é um dir de classes para teste.
				if ( isTestDir(path) )
				{
					artifacts.addTestDir(new File(path));
				}
				else
				{
					artifacts.addSourceDir(new File(path));
					artifacts.addJavaFile(javaFile);
				}

			}
			catch ( DependencyResolverException e )
			{
				ErrorLogger.addError(e.getMessage() + " File: " + e.getFile().getAbsolutePath());
			}
		}	
	}

	// artifacts é output do método
	private static void findDependencies(ProjectFiles projectFiles, ProjectArtifacts artifacts)
	{
		// Resolve android dependencies
		if ( projectFiles.getAndroidManifest() != null )
		{
			Map<String, String> enviromentVariables = System.getenv();
			String androidHomePath = enviromentVariables.get("ANDROID_HOME");

			if ( androidHomePath == null )
			{
				ErrorLogger.addError("Falha no DependencyResolver. ANDROID_HOME não foi encontrado.");
			}
			else
			{
				//File androidHomeDir = new File(androidHomePath);

				// TODO remover caminho hardcoded
				File androidJar = new File (androidHomePath + "/platforms/android-26/android.jar");
				if ( !androidJar.exists() )
				{
					ErrorLogger.addError("Falha no DependencyResolver. android.jar não encontrado.");
				}
				else
				{
					artifacts.setAndroidJar(androidJar);
				}
			}
		}

		// Resolving pom files dependencies
		for ( File pomFile : projectFiles.getMavenFiles() )
		{
			try
			{
				for ( File dependency : MavenResolver.resolveDependencies(pomFile) ) 
				{
					artifacts.addDependency(dependency);
				}
			}
			catch (DependencyResolverException e)
			{
				ErrorLogger.addMavenError(e.getMessage() + " Pom file: " + e.getFile().getAbsolutePath() + "\n");
			}
			
		}

		// Resolving build.gradle files dependencies
		for ( File gradleFile : projectFiles.getGradleFiles() )
		{
			try
			{
				for ( File dependency : GradleResolver.resolveDependencies(gradleFile) ) 
				{
					artifacts.addDependency(dependency);
				}
			}
			catch (Throwable e)
			{
				Throwable t = e;
				while (t.getCause() != null)
				{
						t=t.getCause();
				}
				
				ErrorLogger.addGradleError("Falha no GradleResolver. " + t.getMessage() + ". File: " + gradleFile.getAbsolutePath() + "\n");
				//continue;
			}
		}
	}

	// Método auxiliar. Dado um arquivo java, retorna o caminho para o source root dele.
	// Para verificar se o arquivo é valido, ele é colocado no parser do JavaParser
	private static String getSourceDir(File javaFile) throws DependencyResolverException
	{
		String parentDirPath = javaFile.getParent();
		String sourceDirPath;

		try
		{
			CompilationUnit compilationUnit = JavaParser.parse(javaFile);

			Optional<PackageDeclaration> optionalPackageDeclaration = compilationUnit.getPackageDeclaration();

			if ( optionalPackageDeclaration.isPresent() )
			{
				PackageDeclaration packageDeclaration = optionalPackageDeclaration.get();

				String packageDeclarationString = packageDeclaration.getNameAsString();

				packageDeclarationString = packageDeclarationString.replace('.', '\\');

				int indexOfPackage = parentDirPath.lastIndexOf(packageDeclarationString);

				// O package declaration não bate com o caminho até o arquivo .java. Deve ser um arquivo de exemplo, template ou outro tipo não válido.
				if ( indexOfPackage != -1 )
				{
					sourceDirPath = parentDirPath.substring(0,  indexOfPackage);
				}
				else
				{
					throw new DependencyResolverException ("Falha em ArtifactResolver. O arquivo java não é válido.", javaFile);
				}
			}
			else
			{
				throw new DependencyResolverException ("Falha em ArtifactResolver. O arquivo java não possui package declaration.", javaFile);
				// sourceDirPath = parentDirPath + "\\";
			}

			return sourceDirPath;
		}
		catch (FileNotFoundException e)
		{
			throw new DependencyResolverException ("Falha em ArtifactResolver. Arquivo não encontrado.", javaFile, e);
		}
		catch (ParseProblemException e)
		{
			throw new DependencyResolverException ("Falha em ArtifactResolver. Arquivo falhou ao parsear.", javaFile, e); 
		}
	}

	// Verifica, de maneira bem fraca, se o source dir é de classes de teste. No caso considera-se que é source de teste se houver uma pasta chamada "test" no caminho.
	private static boolean isTestDir(String path)
	{
		return path.indexOf("\\test\\") != -1 || path.indexOf("\\tests\\") != -1;
	}

	private static boolean loadDependencies(String projectName, ProjectArtifacts artifacts)
	{
		Gson gson = new Gson();
		try
		{
			JsonReader reader = new JsonReader (new FileReader(ProjectsUtil.dependenciesRoot+File.separator+projectName+".json"));
			String[] dependencies = gson.fromJson(reader, String[].class);
			
			int i;
			if ( dependencies.length > 0 && dependencies[0].endsWith("android.jar") )
			{
				artifacts.setAndroidJar(new File (dependencies[0]));
				i = 1;
			}
			else
			{
				i = 0;
			}
			
			for (  ; i < dependencies.length ; i++ )
			{
				artifacts.addDependency(new File (dependencies[i]));
			}
			
			reader.close();
			
			return true;
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private static void saveDependencies(String projectName, ProjectArtifacts artifacts)
	{
		Gson gson = new Gson();
		FileWriter jsonFile;
		List<String> dependencies = new ArrayList<>();
		
		// A primeira dependencies salva precisa ser o android.jar
		if ( artifacts.getAndroidJar() != null )
		{
			dependencies.add(artifacts.getAndroidJar().getAbsolutePath());
		}
		
		for ( File file : artifacts.getDependencies() )
		{
			dependencies.add(file.getAbsolutePath());
		}
		
		try
		{
			jsonFile = new FileWriter (ProjectsUtil.dependenciesRoot + File.separator + projectName + ".json");
			
			String dependenciesString = gson.toJson(dependencies);
			
			jsonFile.write(dependenciesString);
			jsonFile.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			
		}
	}

	
	
}
