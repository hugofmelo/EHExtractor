package ufrn.dimap.lets.ehmetrics.repositoryexplorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.kohsuke.github.GHDirection;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GHRepositorySearchBuilder.Sort;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;

import ufrn.dimap.lets.ehmetrics.ProjectsUtil;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.GHRepositorySeacher.LANGUAGE;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.GHRepositorySeacher.ORDER;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.GHRepositorySeacher.SORT;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.model.RepositorySearchResult;

public class GithubExplorer
{
	private int searchID = -1;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	public void queryProjects (boolean download) throws IOException
	{	
		GitHub gitHub = GitHub.connectAnonymously();
		GHRepositorySearchBuilder search = gitHub.searchRepositories();
		search.sort(Sort.STARS);
		search.order(GHDirection.DESC);
		//search.size("1024..1100"); // Tamanho em kbytes
		//search.size("<1240"); // Tamanho em kbytes
		//search.created(">=YYYY-MM-DD");
		//search.pushed(">=YYYY-MM-DD"); // Último commit
		//search.stars("0");
		//search.forks(">=1000");
		search.language("Java");

		PagedSearchIterable<GHRepository> result = search.list();
		System.out.println("Total: " + result.getTotalCount());
		Iterator <GHRepository> repoIte = result.iterator();
		GHRepository repo;

		int i = 0;
		while ( repoIte.hasNext() )
		{
			repo = repoIte.next();
			
			System.out.print((i+1)+"\t");
			System.out.print(repo.getHtmlUrl()+"\t");
			System.out.print(repo.getFullName()+"\t");
			System.out.print(repo.getStargazersCount()+"\t");
			System.out.print(repo.getSize()+"\t");
			System.out.print(dateFormat.format(repo.getCreatedAt())+"\t");
			System.out.print(dateFormat.format(repo.getPushedAt())+"\t");
			//System.out.print(repo.listReleases().asList().size() + "\t");
			//System.out.print(repo.listCommits().asList().size() + "\t");
			//System.out.print(repo.listIssues(GHIssueState.ALL).asList().size() + "\t");
			//System.out.print(dateFormat.format(repo.getUpdatedAt())+"\t");
			System.out.println();

			if ( download & i >= 100 && i < 200 )
			{
				String projectDownloadURL = repo.getUrl()+"/zipball";
				String zipFilePath = ProjectsUtil.projectsRoot+repo.getName()+".zip";
				
				//GithubExplorer.downloadProjectZip(projectDownloadURL, zipFilePath);
				//GithubExplorer.unZip(zipFilePath, ProjectsUtil.projectsRoot);
			}
			i++;
		}
	}

	public void queryProjects2 () throws IOException, RepositoryExplorerException
	{	
		GHRepositorySeacher seacher = new GHRepositorySeacher();

		seacher.setLanguage(LANGUAGE.JAVA);
		//repositorySeacher.setStarsExactly(0);
		seacher.sortBy(SORT.STARS, ORDER.DESC);

		RepositorySearchResult result = seacher.doIt();

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("****************** RESULTADO******************");
		System.out.println(result.getTotal());

		for (ufrn.dimap.lets.ehmetrics.repositoryexplorer.model.Repository repository : result.getRepositories()) 
		{
			System.out.println(repository.getUrl());
			//System.out.println(repository.getStars());
			//System.out.println(dateFormat.format(repository.getLastCommit()));
			//System.out.println();

			/*
			repository.fetchReleases();
			for ( Release release : repository.getReleases() )
			{
				System.out.println(release.getName());
			}
			 */
		}
	}
	/*
	private GHRepositorySearchBuilder configSearchBuilder (GitHub gitHub, int lastID)
	{
		GHRepositorySearchBuilder search = null;

		if ( searchID < 0 )
		{
			search = gitHub.searchRepositories();

		}
		else
		{

		}
		return search;

	}
	 */

	public void findAndroidProjects () throws IOException
	{	
		GitHub gitHub = GitHub.connectAnonymously();
		GHRepositorySearchBuilder search = gitHub.searchRepositories();
		search.sort(Sort.STARS);
		search.order(GHDirection.DESC);
		search.language("Java");		

		// Executando a busca por projetos Java quaisquer
		PagedSearchIterable<GHRepository> result = search.list();
		System.out.println("Total: " + result.getTotalCount());
		
		// Para cada projeto, vamos baixá-lo, dezipa-lo e verificar se ele é android. Se for, são escritos seus dados
		Iterator <GHRepository> repositoryIte = result.iterator();
		GHRepository repository;
		String projectDownloadURL;
		File zipFile, projectRoot;

		int androidProjects = 0;
		
		while ( repositoryIte.hasNext() && androidProjects < 100 )
		{
			repository = repositoryIte.next();
			
			projectDownloadURL = repository.getUrl()+"/zipball";
			zipFile = new File (ProjectsUtil.donwloadTo+repository.getName()+".zip");
			
			GithubExplorer.downloadProjectZip(projectDownloadURL, zipFile);
			projectRoot = GithubExplorer.unZip(zipFile, ProjectsUtil.donwloadTo, repository.getFullName().replaceAll("/", " "));
			
			
			if ( FileFinder.isAndroidProject(projectRoot) )
			{
				System.out.print((androidProjects+1)+"\t");
				/*
				System.out.print(repository.getHtmlUrl()+"\t");
				System.out.print(repository.getFullName()+"\t");
				System.out.print(repository.getStargazersCount()+"\t");
				System.out.print(repository.getSize()+"\t");
				System.out.print(dateFormat.format(repository.getCreatedAt())+"\t");
				System.out.print(dateFormat.format(repository.getPushedAt())+"\t");
				*/
				//System.out.print(repo.listReleases().asList().size() + "\t");
				//System.out.print(repo.listCommits().asList().size() + "\t");
				//System.out.print(repo.listIssues(GHIssueState.ALL).asList().size() + "\t");
				//System.out.print(dateFormat.format(repo.getUpdatedAt())+"\t");
				//System.out.println();
				
				androidProjects++;
			}
			else
			{
				FileUtils.deleteDirectory(projectRoot);
			}
			
			zipFile.delete();
		}
	}
	
	private static void downloadProjectZip (String sourceUrl, File zipFile) throws IOException
	{
		// Download do arquivo
		
		URL website = new URL(sourceUrl);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(zipFile);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		rbc.close();
	}
	
	// Dezipa o projeto e retorna a raiz do projeto dezipado com o nome indicado
	private static File unZip(File zipFile, String outputFolder, String projectRootName) throws IOException
	{
		byte[] buffer = new byte[1024];

		//get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		
		// A primeira entry é a raiz do projeto. Vamos armazenar para retorná-la ao final
		ZipEntry ze = zis.getNextEntry();
		File projectRoot = new File(outputFolder + File.separator + ze.getName());
		projectRoot.mkdirs();
		
		ze = zis.getNextEntry();
				
		while(ze != null)
		{
			String fileName = ze.getName();
			File newFile = new File(outputFolder + File.separator + fileName);
			
			if ( ze.isDirectory() )
			{
				newFile.mkdirs();
			}
			else
			{
				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0)
				{
					fos.write(buffer, 0, len);
				}

				fos.close();
			}
			
			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();
		
		
		File projectRootNewName = new File ( ProjectsUtil.donwloadTo + File.separator + projectRootName );
		
		// Verificar se houve erro
		if ( !projectRoot.renameTo(projectRootNewName) )
		{
			throw new IllegalStateException ("Falha ao renomear raiz do projeto dezipado.");
		}
		
		return projectRootNewName;
	}

	private void test () throws IOException
	{
		GitHub gitHub = GitHub.connectAnonymously();
		GHRepositorySearchBuilder search = gitHub.searchRepositories();
		search.language("Java");

		PagedSearchIterable<GHRepository> result = search.list();
		Iterator <GHRepository> repoIte = result.iterator();
		GHRepository repo;
		while ( repoIte.hasNext() )
		{
			repo = repoIte.next();

			System.out.println(repo.getHtmlUrl());
		}
		//List<GHRepository> repos = search.list();
		/*
		for ( GHRepository r : repos )
		{
			System.out.println(r.getHtmlUrl());
			r.ur
			r.listReleases();
		}
		 */
	}

	public static void main (String args[])
	{
		GithubExplorer explorer = new GithubExplorer();

		try
		{
			//explorer.test();
			explorer.findAndroidProjects();
			//explorer.queryProjects2();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		catch (RepositoryExplorerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */
	}
}
