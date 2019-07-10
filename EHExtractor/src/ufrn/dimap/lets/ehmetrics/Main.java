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
	private static final Logger ERROR_LOGGER = LoggerFacade.getProcessingLogger();
	
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
			ERROR_LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public void executeCompleteAnalysis() throws IOException
	{
		// Idetifying projects paths and resolve all artifacts
		List<ProjectArtifacts> projects = ProjectsResolver.resolveProjects();
		
		// Load guidelines visitors
		List<GuidelineCheckerVisitor> guidelinesVisitors = GuidelinesFactory.loadVisitors(Guidelines.allowUnresolvedTypes);

		
		PROCESSING_LOGGER.fine("Preparando cabeçalho do arquivo de resultados...");
		StringBuilder builder = new StringBuilder ();
		builder.append("\t");
		for ( GuidelineCheckerVisitor visitor : guidelinesVisitors )
		{
			visitor.getGuidelineHeader();
			builder.append("\t");
		}
		builder.append("\n");
		
		// Running the analysis - The guidelines visitors are just cleared between each project
		for ( int i = 0 ; i < projects.size() ; i++ )
		{
			PROCESSING_LOGGER.fine("Executando análise...");
			Analyzer.analyze(projects.get(i), guidelinesVisitors); 

			PROCESSING_LOGGER.fine("Writing guidelines results...");
			for ( GuidelineCheckerVisitor visitor : guidelinesVisitors )
			{
				PROCESSING_LOGGER.fine(visitor.getGuidelineData());
			}
		}

		PROCESSING_LOGGER.fine("Finalizada a aplicação!!");
	}
	
	public void generateDependenciesFiles() throws IOException
	{
		ProjectsResolver.resolveProjects();

		PROCESSING_LOGGER.fine("Finalizada a aplicação!!");
	}
}
