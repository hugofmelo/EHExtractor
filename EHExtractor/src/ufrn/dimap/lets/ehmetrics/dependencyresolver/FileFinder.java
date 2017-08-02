package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.File;
import java.util.Stack;

// FileFinder recebe o diretorio de um projeto e busca por arquivos relevantes, que s�o armazenados em um objeto ProjectFiles.
// IMPORTANTE: � comum ocorrer de um arquivo n�o ser parseado ou alguma dependencia falhar. Isso n�o vai parar o processamento. FileFinderException s� � sinalizado se um erro fatal ocorrer, impedindo o projeto de ser processado.
public class FileFinder
{
	// Esse m�todo assume que 
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
			
			File parent;
			Stack<File> filesStack = new Stack<File>();

			parent = projectRoot;

			// Percorre os arquivos do projeto (varredura em profundidade). Os tipos procurados s�o java, jars, AndroidManifest.xml, pom.xml e build.gradle 
			filesStack.add(parent);
			while (filesStack.isEmpty() == false)
			{
				parent = filesStack.pop();

				// TODO esse if previne que se analise todos os projetos quando tiver analisando self. Resolver isso criando configura��o para excluir caminhos.
				if ( parent.getName().equals("projects") )
				{
					continue;
				}

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
						// Arquivo build.gradle
						if ( child.getName().equals("build.gradle") )
						{
							files.addGradleFile(child);
						}
						// Arquivo *.java
						else if ( child.getName().endsWith(".java") )
						{
							files.addJavaFile(child);
						}
						// Arquivo pom.xml.
						else if ( child.getName().equals("pom.xml") )
						{
							files.addMavenFile(child);
						}
						// Arquivo *.jar. � considerado como sendo uma dependencia do projeto
						else if ( child.getName().endsWith((".jar") ) )
						{
							files.addJarFile(child);
						}
						// Arquivo AndroidManifest.xml. O projeto � android e o android.jar ser� adicionado como dependencia.
						else if ( child.getName().equals(("AndroidManifest.xml") ) )
						{
							files.setAndroidManifestFile(child);
						}
					}
				}
			}
			
			return files;
		}
	}	
}
