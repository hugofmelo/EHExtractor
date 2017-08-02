package ufrn.dimap.lets.ehmetrics.dependencyresolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public class MavenResolver
{
	// Esse método executa o maven para baixar e localizar as dependencias do arquivo pom.xml.
	// Ele cria um arquivo temporário e depois o apaga
	// É necessário ter um maven instalado e o home setado.
	// obs.: Podem haver vários arquivos pom.xml em um mesmo projeto.
	
	//private static int i = 1;
	
	public static List<File> resolveDependencies(File pomFile) throws DependencyResolverException
	{
		
		File outputFile;		
		
		// Executar o maven de fato, baixando as dependencias do pom.xml para o repositório local
		try
		{
			outputFile = File.createTempFile("pom", "tmp", null);
			outputFile.deleteOnExit();
			//File outputFile = new File ("C:/keste/"+ i++ +".txt");
			
			InvocationRequest request = new DefaultInvocationRequest();
			request.setPomFile( pomFile );
			request.setGoals(Arrays.asList("dependency:list"));
			
			Properties properties = new Properties();
			properties.setProperty("outputFile", outputFile.getAbsolutePath()); // redirect output to a file
			properties.setProperty("outputAbsoluteArtifactFilename", "true"); // with paths
			//properties.setProperty("includeScope", "compile");
			//properties.setProperty("includeScope", "provided");
			// if only interested in scope runtime, you may replace with excludeScope = compile
			
			request.setProperties(properties);

			Invoker invoker = new DefaultInvoker();
			// the Maven home can be omitted if the "maven.home" system property is set
			//invoker.setMavenHome(new File("C:/Users/hugofm/apache-maven-3.5.0"));
			invoker.setOutputHandler(null); // not interested in Maven output itself
			InvocationResult result;
			result = invoker.execute(request);
			// Verificar se houve erro
			if (result.getExitCode() != 0)
			{
				throw new DependencyResolverException("Falha no MavenResolver. Maven resultou em erro.", pomFile);
			}
			else // ocorreu tudo bem. Processar arquivo e retornar.
			{
				List <File> dependencies = new ArrayList<File>();
				Pattern pattern = Pattern.compile("(?<=:compile:|:provided:)(.*)");
				try (BufferedReader reader = Files.newBufferedReader(outputFile.toPath()))
				{
					// Pular outras linhas
					while (!"The following files have been resolved:".equals(reader.readLine()));
					
					// Salvar os endereços dos jars
					String line;
					while ((line = reader.readLine()) != null && !line.isEmpty())
					{
						Matcher matcher = pattern.matcher(line);
						if (matcher.find())
						{
							// group 1 contains the path to the file
							dependencies.add(new File(matcher.group(1)));
						}
					}
					
					return dependencies;
				}
				catch (IOException e)
				{
					throw new DependencyResolverException("Falha no MavenResolver. Falha ao processar arquivo temporário com dependencias.", pomFile, e);
				}
			}
		}
		catch (IOException e)
		{
			throw new DependencyResolverException("Falha no MavenResolver. Falha ao criar arquivo temporário", pomFile, e);
		}
		catch (MavenInvocationException e)
		{
			throw new DependencyResolverException("Falha no MavenResolver. Falha ao executar maven.", pomFile, e);
		}		
	}
}
