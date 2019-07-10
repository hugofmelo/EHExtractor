package ufrn.dimap.lets.ehmetrics;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ufrn.dimap.lets.ehmetrics.analyzer.Analyzer;
import ufrn.dimap.lets.ehmetrics.config.Guidelines;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.projectresolver.ProjectArtifacts;
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
			LoggerFacade.logError(e);
		}
	}
	
	public void executeCompleteAnalysis() throws IOException
	{
		// Idetifying projects paths and resolve all artifacts
		List<ProjectArtifacts> projects = ProjectsResolver.resolveProjects();
		
		// Load guidelines visitors
		List<GuidelineCheckerVisitor> guidelinesVisitors = GuidelinesFactory.loadVisitors(Guidelines.allowUnresolvedTypes);

		PROCESSING_LOGGER.fine("Preparando cabeçalho do arquivo de resultados...");
		writeGuidelinesHeader(guidelinesVisitors);
		
		// Running the analysis - The guidelines visitors are just cleared between each project
		for ( int i = 0 ; i < projects.size() ; i++ )
		{
			PROCESSING_LOGGER.fine("Executando análise...");
			Analyzer.analyze(projects.get(i), guidelinesVisitors); 

			PROCESSING_LOGGER.fine("Writing guidelines results...");
			LoggerFacade.logGuideline(projects.get(i).getProjectName() + "\t");
			for ( GuidelineCheckerVisitor visitor : guidelinesVisitors )
			{
				LoggerFacade.logGuideline(visitor.getGuidelineData() + "\t");
			}
			
			LoggerFacade.logGuideline("\n");
			
			Logger projectLogger = LoggerFacade.getProjectLogger(projects.get(i).getProjectName());
			projectLogger.info("\n"+guidelinesVisitors.get(0).getTypeHierarchy().toString());
		}

		PROCESSING_LOGGER.fine("Finalizada a aplicação!!");
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
	
	public void generateDependenciesFiles() throws IOException
	{
		ProjectsResolver.resolveProjects();

		PROCESSING_LOGGER.fine("Finalizada a aplicação!!");
	}
}
