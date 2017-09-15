package ufrn.dimap.lets.ehmetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.analyzer.Analyzer;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ArtifactResolver;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectArtifacts;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.ProjectFiles;
import ufrn.dimap.lets.ehmetrics.logger.ArtifactLogger;
import ufrn.dimap.lets.ehmetrics.logger.ErrorLogger;
import ufrn.dimap.lets.ehmetrics.logger.ModelLogger;
import ufrn.dimap.lets.ehmetrics.logger.StatelessPersistentLogger;

public class Main
{
	public static void main(String[] args)
	{
		Main main = new Main();

		try
		{
			//main.execute();
			//main.generateDependenciesFiles();
			//main.runSonarLint();
			main.readSonarLintReport();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	Init logger
	List projects
	foreach project
		find files < projectRoot > files
		resolve dependencies < files > artifacts	
	 	configSolver < dependencies > solver
		configVisitor < model > visitor
		analyze < javaFiles, solver, visitor > model
		write report < model
	write report
	 */	

	public void execute() throws IOException
	{
		ModelLogger.start();

		System.out.println("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();

		for ( File project : projects )
		{
			System.out.println(project.getName());
		}
		System.out.println();
		System.out.println();


		for ( File project : projects )
		{
			ErrorLogger.start();

			System.out.println("****** PROJETO " + project.getName() + " ******");

			System.out.println("Identificando arquivos...");
			ProjectFiles projectFiles = FileFinder.find(project);

			System.out.println("Resolvendo artefatos e dependencias...");
			ProjectArtifacts projectArtifacts = ArtifactResolver.resolve(projectFiles);			

			// Logging
			ArtifactLogger.writeReport(project.getName(), projectFiles, projectArtifacts);

			System.out.println("Executando análise...");
			MetricsModel model = Analyzer.analyze(projectArtifacts); 

			System.out.println("Salvando resultados...");
			// Logging
			ModelLogger.writeReport(project.getName(), model);
			ErrorLogger.writeReport(project.getName());

			ErrorLogger.stop();

			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}

		ModelLogger.stop();

		System.out.println("FINALIZADO");
	}

	public void generateDependenciesFiles() throws IOException
	{
		System.out.println("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();

		for ( File project : projects )
		{
			System.out.println(project.getName());
		}
		System.out.println();
		System.out.println();


		for ( File project : projects )
		{
			ErrorLogger.start();

			System.out.println("****** PROJETO " + project.getName() + " ******");

			System.out.println("Identificando arquivos...");
			ProjectFiles projectFiles = FileFinder.find(project);

			System.out.println("Resolvendo artefatos e dependencias...");
			ArtifactResolver.resolve(projectFiles);			

			ErrorLogger.stop();

			System.out.println("Finalizada a analise do projeto " + project.getName() + ".");
			System.out.println();	
		}

		System.out.println("FINALIZADO");
	}

	public void runSonarLint() throws IOException
	{
		System.out.println("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();

		for ( File project : projects )
		{
			System.out.println(project.getName());
		}
		System.out.println();
		System.out.println();


		for ( File project : projects )
		{
			System.out.println("****** PROJETO " + project.getName() + " ******");

			ProcessBuilder builder = new ProcessBuilder(
					"cmd.exe", "/c", "cd " + project.getAbsolutePath() + " && sonarlint.bat --charset ISO-8859-1");

			builder.redirectErrorStream(true);
			Process p = builder.start();

			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while (true)
			{
				line = r.readLine();

				if (line == null) { break; }

				System.out.println(line);
			}	
		}

		System.out.println("FINALIZADO");	
	}

	public void readSonarLintReport() throws IOException
	{
		List<String> sonarLintExceptionHandlingRules = new ArrayList<> ();
		sonarLintExceptionHandlingRules.add("\"throws\" declarations should not be superfluous");
		sonarLintExceptionHandlingRules.add("Generic exceptions should never be thrown");
		sonarLintExceptionHandlingRules.add("Try-catch blocks should not be nested");
		sonarLintExceptionHandlingRules.add("Jump statements should not occur in \"finally\" blocks");
		sonarLintExceptionHandlingRules.add("Throwable.printStackTrace(...) should not be called");
		sonarLintExceptionHandlingRules.add("Exceptions should not be thrown in finally blocks");
		sonarLintExceptionHandlingRules.add("Exception classes should be immutable");
		sonarLintExceptionHandlingRules.add("Throwable and Error should not be caught");
		sonarLintExceptionHandlingRules.add("Exception types should not be tested using \"instanceof\" in catch blocks");
		sonarLintExceptionHandlingRules.add("Exceptions should not be thrown from servlet methods");
		sonarLintExceptionHandlingRules.add("Try-with-resources should be used");
		sonarLintExceptionHandlingRules.add("Catches should be combined");
		sonarLintExceptionHandlingRules.add("Classes named like \"Exception\" should extend \"Exception\" or a subclass");
		sonarLintExceptionHandlingRules.add("IllegalMonitorStateException should not be caught");
		sonarLintExceptionHandlingRules.add("\"Iterator.next()\" methods should throw \"NoSuchElementException\"");
		sonarLintExceptionHandlingRules.add("\"catch\" clauses should do more than rethrow");
		sonarLintExceptionHandlingRules.add("Exception should not be created without being thrown");
		
		StatelessPersistentLogger.start("sonarlint issues.txt");
		StatelessPersistentLogger.write("PROJECT\t");
		for ( String s : sonarLintExceptionHandlingRules )
		{
			StatelessPersistentLogger.write(s+"\t");
		}
		StatelessPersistentLogger.write("\n");
		
		
		System.out.println("Identificando projetos para analise...");
		List<File> projects = ProjectsUtil.listProjects();
		
		for ( File project : projects )
		{
			StatelessPersistentLogger.write(project.getName() + "\t");
			
			File reportFile = new File(project.getAbsolutePath() + File.separator + ".sonarlint" + File.separator + "sonarlint-report.html");
			
			SonarLintReport report = parseSonarLintReport (reportFile);
			
			for ( Integer i : applyFilterToReport (report, sonarLintExceptionHandlingRules ) )
			{
				StatelessPersistentLogger.write(i+"\t");
			}
			StatelessPersistentLogger.write("\n");
		}

		StatelessPersistentLogger.stop();
		System.out.println("FINALIZADO");	
	}

	private SonarLintReport parseSonarLintReport(File reportFile) throws IOException
	{
		SonarLintReport report = new SonarLintReport();
		
		Document htmlDocument = Jsoup.parse(reportFile, "UTF-8", "");
		Element summary = htmlDocument.getElementById("summary");
		Elements rules = summary.getElementsByClass("hoverable");
		for ( Element rule : rules )
		{
			String ruleDescription = rule.getElementsByAttributeValue("align","left").get(0).text(); 
			int occurrences = Integer.parseInt( rule.getElementsByAttributeValue("align","right").get(0).text() );

			report.addEntry(ruleDescription, occurrences);
		}
		
		return report;
	}
	
	private static List<Integer> applyFilterToReport ( SonarLintReport report, List<String> filterRules )
	{
		List <Integer> occurrences = new ArrayList<>();
		
		for ( String rule : filterRules )
		{
			occurrences.add( report.getOccurrencesOfRule(rule) );
		}
		
		return occurrences;
	}
}
