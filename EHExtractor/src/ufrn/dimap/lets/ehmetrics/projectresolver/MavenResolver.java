package ufrn.dimap.lets.ehmetrics.projectresolver;

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
	private MavenResolver (){}
	
	// Esse método executa o maven para baixar e localizar as dependencias do arquivo pom.xml.
	// Ele cria um arquivo temporário e depois o apaga
	// É necessário ter um maven instalado e o home setado.
	// obs.: Podem haver vários arquivos pom.xml em um mesmo projeto.
	
	public static List<File> resolveDependencies(File pomFile) throws ProjectResolverException
	{
		// Generate file where the dependency list will be downloaded
		File outputFile;		
		try
		{
			outputFile = File.createTempFile("pom", "tmp", null);
		}
		catch (IOException e)
		{
			throw new ProjectResolverException("Falha no MavenResolver. Falha ao criar arquivo temporário", pomFile, e);
		}
		outputFile.deleteOnExit();
		
		
		downloadDependencies (pomFile, outputFile);
		
		
		try
		{
			return extractDependencies (outputFile);	
		}
		catch (IOException e)
		{
			throw new ProjectResolverException("Falha no MavenResolver. Falha ao processar arquivo com dependencias.", pomFile, e);
		}
	}
	
	/**
	 * Roda o Maven e baixa a lista de dependencias do pomFile para o outputFile.
	 * 
	 * @throws DependencyResolverException se o Maven não pôde ser executado ou se a execução resultou em erros.
	 * */
	private static void downloadDependencies ( File pomFile, File outputFile ) throws ProjectResolverException
	{
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
		
		try
		{
			result = invoker.execute(request);
		}
		catch (MavenInvocationException e)
		{
			throw new ProjectResolverException("Falha no MavenResolver. Falha ao executar maven.", pomFile, e);
		}	
		
		// Verificar se houve erro
		if (result.getExitCode() != 0)
		{
			throw new ProjectResolverException("Falha no MavenResolver. Maven resultou em erro.", pomFile);
		}
	}
	
	private static List<File> extractDependencies (File outputFile) throws IOException
	{
		List <File> dependencies = new ArrayList<>();
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
	}
}
