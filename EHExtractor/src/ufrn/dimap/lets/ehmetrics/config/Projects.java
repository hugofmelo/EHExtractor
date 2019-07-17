package ufrn.dimap.lets.ehmetrics.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Projects
{
	// DIRECTORIES *****************************
	
	public static final boolean PROJECTS_ON_DEMAND = true;
	
	// if PROJECT_ON_DEMAND is false, set
	public static final String PROJECTS_ROOT = "D:/Desenvolvimento/Projetos Github/A executar/";
	
	// if PROJECT_ON_DEMAND is true, set
	public static final List<String> PROJECTS = Arrays.asList(
			"../EHExtractor"
			//"D:/Desenvolvimento/Projetos Github/allegro-hermes/"
			//"D:/Desenvolvimento/Projetos Github/openhab-openhab2-addons/"
			);
			
	public static final String DEPENDENCIES_ROOT = "./dependencies/home/";
	
	
	// CONFIGURATIONS **************************
	public static final boolean resolveDependencies = false;
	
	public static final boolean includeJavaFiles = true;
	public static final boolean includeJarFiles = false;
	public static final boolean includeMavenFiles = false;
	public static final boolean includeGradleFiles = false;
	public static final boolean includeAndroidFile = false;
	

	private Projects () {}
}
