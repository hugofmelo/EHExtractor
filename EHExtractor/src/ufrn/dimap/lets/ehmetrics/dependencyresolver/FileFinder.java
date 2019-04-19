package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;
import java.util.Stack;

/** FileFinder recebe o diretorio de um projeto e busca por arquivos relevantes, que são armazenados em um objeto ProjectFiles.
 * 
 */
public class FileFinder
{
	private FileFinder ()
	{
		
	}
	
	public static ProjectFiles find (File projectRoot, boolean findJava, boolean findAndroid, boolean findJar, boolean findBuildGradle, boolean findPomXml)
	{
		// Verificar se o caminho existe e é uma pasta
		if ( !projectRoot.exists() )
		{
			throw new IllegalStateException ("Caminho do projeto não existe: " + projectRoot);
		}
		else if ( !projectRoot.isDirectory() )
		{
			throw new IllegalStateException ("Caminho do projeto não é uma pasta: " + projectRoot);
		}
		else
		{
			ProjectFiles files = new ProjectFiles();
			
			files.setProjectName(projectRoot.getName());
			
			File parent;
			Stack<File> filesStack = new Stack<>();

			parent = projectRoot;

			// Percorre os arquivos do projeto (varredura em profundidade). Os tipos procurados são java, jars, AndroidManifest.xml, pom.xml e build.gradle 
			filesStack.add(parent);
			while ( !filesStack.isEmpty() )
			{
				parent = filesStack.pop();

				// Para cada documento na pasta
				for (File child : parent.listFiles())
				{
					// Se for uma pasta, empilha para ser processado depois 
					if ( child.isDirectory() )
					{
						filesStack.add(child);
					}
					// É um arquivo...
					else
					{
						
						// Arquivo *.java
						if ( findJava && child.getName().endsWith(".java") )
						{
							files.addJavaFile(child);
						}
						// Arquivo AndroidManifest.xml. O projeto é android e o android.jar será adicionado como dependencia.
						else if ( findAndroid && child.getName().equals(("AndroidManifest.xml") ) )
						{
							files.setAndroidManifestFile(child);
						}
						// Arquivo *.jar. É considerado como sendo uma dependencia do projeto
						else if ( findJar && child.getName().endsWith((".jar") ) )
						{
							files.addJarFile(child);
						}
						// Arquivo build.gradle
						else if ( findBuildGradle && child.getName().equals("build.gradle") )
						{
							files.addGradleFile(child);
						}
						// Arquivo pom.xml.
						else if ( findPomXml && child.getName().equals("pom.xml") )
						{
							files.addMavenFile(child);
						}
					}
				}
			}
			
			return files;
		}
	}
	
	// Verifica se o projeto é android, sem armazenar nenhum dos arquivos.
	public static boolean isAndroidProject (File projectRoot)
	{
		// Verificar se o caminho existe e é uma pasta
		if ( !projectRoot.exists() )
		{
			throw new IllegalStateException ("Caminho do projeto não existe: " + projectRoot);
		}
		else if ( !projectRoot.isDirectory() )
		{
			throw new IllegalStateException ("Caminho do projeto não é uma pasta: " + projectRoot);
		}
		else
		{
			File parent;
			Stack<File> filesStack = new Stack<>();

			parent = projectRoot;

			// Percorre os arquivos do projeto (varredura em profundidade). O arquivo procurado é p AndroidManifest.xml
			filesStack.add(parent);
			while ( !filesStack.isEmpty() )
			{
				parent = filesStack.pop();

				// Para cada documento na pasta
				for (File child : parent.listFiles())
				{
					// Se for uma pasta, empilha para ser processado depois 
					if ( child.isDirectory() )
					{
						filesStack.add(child);
					}
					// É um arquivo...
					else
					{
						// Arquivo AndroidManifest.xml
						if ( child.getName().equals(("AndroidManifest.xml") ) )
						{
							return true;
						}
					}
				}
			}
			
			// Se o laço encerrou, o projeto não é android.
			return false;
		}
	}	
}
