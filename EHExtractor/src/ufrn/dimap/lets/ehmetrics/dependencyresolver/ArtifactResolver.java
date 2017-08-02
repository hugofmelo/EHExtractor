package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import ufrn.dimap.lets.ehmetrics.ErrorLogger;

public class ArtifactResolver
{
	public static ProjectArtifacts resolve (ProjectFiles projectFiles)
	{
		ProjectArtifacts artifacts = new ProjectArtifacts();

		findArtifacts(projectFiles, artifacts);
		findDependencies(projectFiles, artifacts);
		
		return artifacts;
	}

	// artifacts � output do m�todo
	private static void findArtifacts(ProjectFiles files, ProjectArtifacts artifacts)
	{
		for ( File javaFile : files.getJavaFiles() )
		{
			try
			{
				String path = getSourceDir(javaFile);

				// Verifica se � um dir de classes para teste.
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

	// artifacts � output do m�todo
	private static void findDependencies(ProjectFiles projectFiles, ProjectArtifacts artifacts)
	{
		// Resolve android dependencies
		if ( projectFiles.getAndroidManifest() != null )
		{
			Map<String, String> enviromentVariables = System.getenv();
			String androidHomePath = enviromentVariables.get("ANDROID_HOME");

			if ( androidHomePath == null )
			{
				ErrorLogger.addError("Falha no DependencyResolver. ANDROID_HOME n�o foi encontrado.");
			}
			else
			{
				//File androidHomeDir = new File(androidHomePath);

				// TODO remover caminho hardcoded
				File androidJar = new File (androidHomePath + "/platforms/android-26/android.jar");
				if ( !androidJar.exists() )
				{
					ErrorLogger.addError("Falha no DependencyResolver. android.jar n�o encontrado.");
				}
				else
				{
					artifacts.addDependency(androidJar);
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
				ErrorLogger.addError(e.getMessage() + " Pom file: " + e.getFile().getAbsolutePath() + "\n");
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
				
				ErrorLogger.addError("Falha no GradleResolver. " + t.getMessage() + ". File: " + gradleFile.getAbsolutePath() + "\n");
				//continue;
			}
		}
	}

	// M�todo auxiliar. Dado um arquivo java, retorna o caminho para o source root dele.
	// Para verificar se o arquivo � valido, ele � colocado no parser do JavaParser
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

				// O package declaration n�o bate com o caminho at� o arquivo .java. Deve ser um arquivo de exemplo, template ou outro tipo n�o v�lido.
				if ( indexOfPackage != -1 )
				{
					sourceDirPath = parentDirPath.substring(0,  indexOfPackage);
				}
				else
				{
					throw new DependencyResolverException ("Falha em ArtifactResolver. O arquivo java n�o � v�lido.", javaFile);
				}
			}
			else
			{
				throw new DependencyResolverException ("Falha em ArtifactResolver. O arquivo java n�o possui package declaration.", javaFile);
				// sourceDirPath = parentDirPath + "\\";
			}

			return sourceDirPath;
		}
		catch (FileNotFoundException e)
		{
			throw new DependencyResolverException ("Falha em ArtifactResolver. Arquivo n�o encontrado.", javaFile, e);
		}
		catch (ParseProblemException e)
		{
			throw new DependencyResolverException ("Falha em ArtifactResolver. Arquivo falhou ao parsear.", javaFile, e); 
		}
	}

	// Verifica, de maneira bem fraca, se o source dir � de classes de teste. No caso considera-se que � source de teste se houver uma pasta chamada "test" no caminho.
	private static boolean isTestDir(String path)
	{
		return path.indexOf("\\test\\") != -1;
	}

}
