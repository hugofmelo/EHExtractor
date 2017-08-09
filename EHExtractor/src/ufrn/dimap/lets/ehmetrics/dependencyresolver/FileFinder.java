package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import ufrn.dimap.lets.ehmetrics.ProjectsUtil;

// FileFinder recebe o diretorio de um projeto e busca por arquivos relevantes, que s�o armazenados em um objeto ProjectFiles.
public class FileFinder
{
	public static ProjectFiles find (File projectRoot)
	{
		// Verificar se o caminho existe e � uma pasta
		if ( projectRoot.exists() == false )
		{
			throw new IllegalStateException ("Caminho do projeto n�o existe: " + projectRoot);
		}
		else if ( projectRoot.isDirectory() == false )
		{
			throw new IllegalStateException ("Caminho do projeto n�o � uma pasta: " + projectRoot);
		}
		else
		{
			ProjectFiles files = new ProjectFiles();
			
			files.setProjectName(projectRoot.getName());
			
			File parent;
			Stack<File> filesStack = new Stack<File>();

			parent = projectRoot;

			// Percorre os arquivos do projeto (varredura em profundidade). Os tipos procurados s�o java, jars, AndroidManifest.xml, pom.xml e build.gradle 
			filesStack.add(parent);
			while (filesStack.isEmpty() == false)
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
					// � um arquivo...
					else
					{
						
						// Arquivo *.java
						if ( child.getName().endsWith(".java") )
						{
							files.addJavaFile(child);
						}
						// Arquivo AndroidManifest.xml. O projeto � android e o android.jar ser� adicionado como dependencia.
						else if ( child.getName().equals(("AndroidManifest.xml") ) )
						{
							files.setAndroidManifestFile(child);
						}
						// Arquivo *.jar. � considerado como sendo uma dependencia do projeto
						else if ( child.getName().endsWith((".jar") ) )
						{
							files.addJarFile(child);
						}
						// Arquivo build.gradle
						else if ( child.getName().equals("build.gradle") )
						{
							files.addGradleFile(child);
						}
						// Arquivo pom.xml.
						else if ( child.getName().equals("pom.xml") )
						{
							files.addMavenFile(child);
						}
					}
				}
			}
			
			return files;
		}
	}
	
	// Verifica se o projeto � android, sem armazenar nenhum dos arquivos.
	public static boolean isAndroidProject (File projectRoot)
	{
		// Verificar se o caminho existe e � uma pasta
		if ( projectRoot.exists() == false )
		{
			throw new IllegalStateException ("Caminho do projeto n�o existe: " + projectRoot);
		}
		else if ( projectRoot.isDirectory() == false )
		{
			throw new IllegalStateException ("Caminho do projeto n�o � uma pasta: " + projectRoot);
		}
		else
		{
			File parent;
			Stack<File> filesStack = new Stack<File>();

			parent = projectRoot;

			// Percorre os arquivos do projeto (varredura em profundidade). O arquivo procurado � p AndroidManifest.xml
			filesStack.add(parent);
			while (filesStack.isEmpty() == false)
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
					// � um arquivo...
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
			
			// Se o la�o encerrou, o projeto n�o � android.
			return false;
		}
	}	
}
