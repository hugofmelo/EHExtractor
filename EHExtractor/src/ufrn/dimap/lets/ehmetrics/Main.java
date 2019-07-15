package ufrn.dimap.lets.ehmetrics;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import ufrn.dimap.lets.ehmetrics.analyzer.Analyzer;
import ufrn.dimap.lets.ehmetrics.config.Guidelines;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.projectresolver.JavaProject;
import ufrn.dimap.lets.ehmetrics.projectresolver.ProjectsResolver;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelinesFactory;

public class Main
{
	static 
	{
		try
		{
			LoggerFacade.configLogging();
		}
		catch (Exception e)
		{
			System.exit(1);
		}
	}
	
	private static final Logger PROCESSING_LOGGER = LoggerFacade.getProcessingLogger();
	
	public static void main(String[] args)
	{	
		try
		{
			Main main = new Main();
			
			//main.findBadSmells();
			main.executeCompleteAnalysis();
			//main.quickJavaParserTest();
			//main.executeCallGraph();
			//main.generateDependenciesFiles();
			//main.runSonarLint();
			//main.readSonarLintReport();
		}
		catch (Exception e)
		{
			PROCESSING_LOGGER.fine("Aplicação encerrada com uma exceção. Ver log de erros.");
			LoggerFacade.logError(e);
		}
	}
	
	public void executeCompleteAnalysis() throws IOException
	{
		// Idetifying projects paths
		PROCESSING_LOGGER.fine("Identificando projetos para analise...");
		List<JavaProject> projects = ProjectsResolver.findProjects();
		
		// Load guidelines visitors
		List<GuidelineCheckerVisitor> guidelinesVisitors = GuidelinesFactory.loadVisitors(Guidelines.allowUnresolvedTypes);

		PROCESSING_LOGGER.fine("Preparando cabeçalho do arquivo de resultados...");
		writeGuidelinesHeader(guidelinesVisitors);
		
		// Running the analysis
		for ( int i = 0 ; i < projects.size() ; i++ )
		{
			PROCESSING_LOGGER.fine("\n\n\n\n\nProcessando project [" + (i+1) + " of " + projects.size() + "]");
			PROCESSING_LOGGER.fine("Identificando arquivos e resolvendo artefatos...");
			projects.get(i).findFiles();
			projects.get(i).resolveArtifacts();
			
			PROCESSING_LOGGER.fine("Executando análise...");
			Analyzer.analyze(projects.get(i), guidelinesVisitors); 

			PROCESSING_LOGGER.fine("Writing guidelines results...");
			LoggerFacade.logGuideline(projects.get(i).getName() + "\t");
			for ( GuidelineCheckerVisitor visitor : guidelinesVisitors )
			{
				LoggerFacade.logGuideline(visitor.getGuidelineData() + "\t");
			}
			
			LoggerFacade.logGuideline("\n");
			
			Logger projectLogger = LoggerFacade.getProjectLogger(projects.get(i).getName());
			projectLogger.info("\n"+guidelinesVisitors.get(0).getTypeHierarchy().toString());
			
			GuidelinesFactory.clearVisitors();
		}

		PROCESSING_LOGGER.fine("\n\n\nFinalizada a aplicação!!");
	}

	private void writeGuidelinesHeader(List<GuidelineCheckerVisitor> guidelinesVisitors)
	{
		StringBuilder guidelinesHeaderBuilder = new StringBuilder ();
		guidelinesHeaderBuilder.append("\t");
		
		for ( GuidelineCheckerVisitor visitor : guidelinesVisitors )
		{
			guidelinesHeaderBuilder.append(visitor.getGuidelineHeader());
			guidelinesHeaderBuilder.append("\t");
		}
		
		guidelinesHeaderBuilder.append("\n");
		
		LoggerFacade.logGuideline(guidelinesHeaderBuilder.toString());
	}
	
	public void generateDependenciesFiles() throws IOException, SecurityException
	{
		List<JavaProject> projects = ProjectsResolver.findProjects();
		
		for ( JavaProject project : projects )
		{
			project.findFiles();
			project.resolveArtifacts();
		}
	}
}
