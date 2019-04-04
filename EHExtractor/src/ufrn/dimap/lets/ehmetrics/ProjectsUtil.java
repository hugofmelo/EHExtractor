package ufrn.dimap.lets.ehmetrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ProjectsUtil
{
	private static final boolean projectsOnDemand = true;
	
	
	
	// Colocar endereço com barra ('/') no final
	// LETS
	//public static final String projectsRoot = "C:/Users/mafeu_000/Projetos GitHub/analysis/";
	//public static final String dependenciesRoot = "./dependencies/lets/";
	
	// CASA
	public static final String projectsRoot = "D:/Desenvolvimento/Projetos Github/New survey/";
	public static final String dependenciesRoot = "./dependencies/home/";
	
	public static final String logsRoot = "C:/Users/mafeu_000/Projetos GitHub/log/";
	
	public static StringBuilder smellsBuilder;
	private static final String smellsLog = "./log/smells/log.txt";
	
	public static List<File> listProjects ()
	{
		if ( projectsOnDemand )
		{
			return ProjectsUtil.projectsOnDemand();
		}
		else
		{
			return ProjectsUtil.projectsOn(new File (projectsRoot));
		}
	}
	
	public static List <File> projectsOn (File rootDir)
	{
		List <File> projectsPaths = new ArrayList<File>();
		
		for (File project : rootDir.listFiles())
		{
			if ( project.isDirectory() )
			{
				projectsPaths.add(project);
			}
		}
		
		return projectsPaths;
	}
	
	
	public static List <File> projectsOnDemand ()
	{
		List <File> projectsPaths = new ArrayList<File>();
		
		// Test self
		//projectsPaths.add(new File("../EHExtractor"));
		
		projectsPaths.add(new File(projectsRoot+"elasticsearch/server/src/main"));
		projectsPaths.add(new File(projectsRoot+"netty/handler/src"));
		projectsPaths.add(new File(projectsRoot+"vert.x/src/main/java/io"));
		projectsPaths.add(new File(projectsRoot+"mybatis-3/src/main/java"));
		projectsPaths.add(new File(projectsRoot+"presto/presto-accumulo/src/main/java"));
		projectsPaths.add(new File(projectsRoot+"presto/presto-main/src/main/java"));
		projectsPaths.add(new File(projectsRoot+"hadoop/hadoop-yarn-project/hadoop-yarn"));
		projectsPaths.add(new File(projectsRoot+"hadoop/hadoop-hdfs-project/hadoop-hdfs/src/main/java"));
		projectsPaths.add(new File(projectsRoot+"druid/common/src/main/java"));
		projectsPaths.add(new File(projectsRoot+"pinpoint/bootstrap-core/src/main/java"));
				
		// Github
		/*
		projectsPaths.add(new File(projectsRoot+"afollestad-material-dialogs"));
		projectsPaths.add(new File(projectsRoot+"airbnb-lottie-android"));
		projectsPaths.add(new File(projectsRoot+"alibaba-fastjson"));
		projectsPaths.add(new File(projectsRoot+"Blankj-AndroidUtilCode"));
		projectsPaths.add(new File(projectsRoot+"bumptech-glide"));
		projectsPaths.add(new File(projectsRoot+"chrisbanes-PhotoView"));
		projectsPaths.add(new File(projectsRoot+"elastic-elasticsearch"));
		projectsPaths.add(new File(projectsRoot+"facebook-fresco"));
		projectsPaths.add(new File(projectsRoot+"google-guava"));
		projectsPaths.add(new File(projectsRoot+"google-iosched"));
		projectsPaths.add(new File(projectsRoot+"greenrobot-EventBus"));
		projectsPaths.add(new File(projectsRoot+"iluwatar-java-design-patterns"));
		projectsPaths.add(new File(projectsRoot+"JakeWharton-butterknife"));
		//projectsPaths.add(new File(projectsRoot+"JetBrains-kotlin"));
		projectsPaths.add(new File(projectsRoot+"jfeinstein10-SlidingMenu"));
		projectsPaths.add(new File(projectsRoot+"kdn251-interviews"));
		projectsPaths.add(new File(projectsRoot+"lgvalle-Material-Animations"));
		//projectsPaths.add(new File(projectsRoot+"libgdx-libgdx"));
		projectsPaths.add(new File(projectsRoot+"loopj-android-async-http"));
		projectsPaths.add(new File(projectsRoot+"Netflix-Hystrix"));
		projectsPaths.add(new File(projectsRoot+"netty-netty-4.1"));
		projectsPaths.add(new File(projectsRoot+"nostra13-Android-Universal-Image-Loader"));
		projectsPaths.add(new File(projectsRoot+"PhilJay-MPAndroidChart"));
		projectsPaths.add(new File(projectsRoot+"ReactiveX-RxAndroid-2.x"));
		//projectsPaths.add(new File(projectsRoot+"ReactiveX-RxJava-2.x"));
		projectsPaths.add(new File(projectsRoot+"spring-projects-spring-boot"));
		//projectsPaths.add(new File(projectsRoot+"spring-projects-spring-framework"));
		projectsPaths.add(new File(projectsRoot+"square-leakcanary"));
		projectsPaths.add(new File(projectsRoot+"square-okhttp"));
		projectsPaths.add(new File(projectsRoot+"square-picasso"));
		projectsPaths.add(new File(projectsRoot+"square-retrofit"));
		projectsPaths.add(new File(projectsRoot+"zxing-zxing"));
		*/
		
		boolean stop = false;
		for (File f : projectsPaths)
		{
			
			if ( !f.exists() )
			{
				System.err.println("Baixar projeto " + f.getAbsolutePath());
				stop = true;
			}
		}
		
		if (stop)
			System.exit(1);
				
		return projectsPaths;
	}
	
	public static void startSmellsLog()
	{
		smellsBuilder = new StringBuilder();
	}
	
	public static void writeSmellsLog(String content)
	{
		smellsBuilder.append(content);
	}
	
	public static void saveAndCloseSmellsLog ()
	{
		Path path = Paths.get(ProjectsUtil.smellsLog);
		
		try
		{
			Files.write(path, smellsBuilder.toString().getBytes());
		}
		catch (IOException e)
		{
			throw new ThinkLaterException(e);
		}
	}
}
