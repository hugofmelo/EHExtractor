package ufrn.dimap.lets.ehmetrics.repositoryexplorer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sound.midi.MidiUnavailableException;

import org.apache.commons.io.FileUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHDirection;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepository.Contributor;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GHRepositorySearchBuilder.Sort;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.PagedIterator;
import org.kohsuke.github.PagedSearchIterable;

import ufrn.dimap.lets.ehmetrics.ProjectsUtil;
import ufrn.dimap.lets.ehmetrics.dependencyresolver.FileFinder;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.GHRepositorySeacher.LANGUAGE;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.GHRepositorySeacher.ORDER;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.GHRepositorySeacher.SORT;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.model.RepositorySearchResult;

public class GithubExplorer
{
	private static final String created = "<2018-01-01";
	private static final String lastCommit = ">=2018-05-19";
	private static final String stars = "<=35";
	private static final int minimumActiveContributors = 2;
	private static final int minimumRecentCommits = 20;
	private static final int minimumContributorCommits = 5;
	private static final int monthsWindow = 1;
	
	private static final LocalDate lastCommitWindow = LocalDate.now().minusMonths(monthsWindow);



	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	public void findProjects () throws IOException
	{	
		try(BufferedWriter writer = Files.newBufferedWriter (Paths.get(ProjectsUtil.projectsRoot+"aaalog.txt")))
		{
			GitHub gitHub = GitHub.connectAnonymously();
			

			GHRepositorySearchBuilder search = gitHub.searchRepositories();
			search.sort(Sort.STARS);
			search.order(GHDirection.DESC);
			search.created(created);
			search.pushed(lastCommit);
			search.stars(stars);
			search.language("Java");		

			StringBuilder searchInfo = new StringBuilder();
			searchInfo.append("Maximum stars: " + stars);
			searchInfo.append("\n");
			searchInfo.append("Created at: " + created);
			searchInfo.append("\n");
			searchInfo.append("Minimum active contributors: " + minimumActiveContributors);
			searchInfo.append("\n");
			searchInfo.append("Minimum recent commits: " + minimumRecentCommits);
			searchInfo.append("\n");
			searchInfo.append("Minimum commits by contributor: " + minimumContributorCommits);
			searchInfo.append("\n");
			searchInfo.append("Months of inactivity: " + monthsWindow);
			searchInfo.append("\n");

			writer.write(searchInfo.toString());


			// Executando a busca por projetos Java
			PagedSearchIterable<GHRepository> repositories = search.list();

			int repositoryCounter = 0;
			int repositoryTotal = repositories.getTotalCount();

			Map<GHUser, List<GHCommit>> activeContributors;

			for ( GHRepository repository : repositories )
			{
				repositoryCounter++;
				System.out.println("Repository " + repositoryCounter + " from " + repositoryTotal);
				
				int tentativas = 0;
				do
				{
					tentativas++;
					
					try
					{
						StringBuilder repositoryInfo = new StringBuilder();
						repositoryInfo.append(repository.getStargazersCount() + "\t");
						repositoryInfo.append(repository.getHtmlUrl() + "\t");
	
	
						
						// Pegando autores ativos
						activeContributors = new HashMap<>();
						PagedIterable<GHCommit> commits = repository.queryCommits().since(DateUtils.asDate(lastCommitWindow)).list(); 
	
						for ( GHCommit commit : commits )
						{
							
							
							GHUser user = commit.getAuthor();
							
							if (user != null)
							{
								List<GHCommit> userCommits = activeContributors.get(user);
	
								if ( userCommits == null )
								{
									userCommits = new ArrayList<>();
									activeContributors.put(user, userCommits);
								}
								
								userCommits.add(commit);
							}
						}
	
						// Lendo informações de autores e seus commits
						StringBuilder contributorInfo = new StringBuilder();
						boolean first = true;
						for ( GHUser user : activeContributors.keySet() )
						{
							if (!first)
							{
								contributorInfo.append("\t\t\t");
							}
							first = false;
							
							contributorInfo.append(activeContributors.get(user).size()+"\t");
							contributorInfo.append(user.getEmail()+"\t");
							contributorInfo.append(user.getCompany()+"\t");
							contributorInfo.append(user.getLogin()+"\t");
							contributorInfo.append(user.getName()+"\t");
							
							boolean javaCommitter = false;
							for ( GHCommit commit : activeContributors.get(user) )
							{
								for ( GHCommit.File file : commit.getFiles() )
								{
									if ( file.getFileName().endsWith(".java") )
									{
										javaCommitter = true;
										break;
									}
								}
								
								if ( javaCommitter )
								{
									break;
								}
							}
							
							if ( javaCommitter )
							{
								contributorInfo.append("Java committer"+"\t");
							}
							else
							{
								contributorInfo.append("non-Java committer"+"\t");
							}
							
							String separator = "";
							for ( GHCommit commit : activeContributors.get(user) )
							{
								contributorInfo.append(separator+commit.getHtmlUrl());
								separator = ";";
							}
							contributorInfo.append("\t");
							contributorInfo.append("\n");
						}
	
						if ( contributorInfo.length() == 0 )
						{
							contributorInfo.append("\n");
						}
						
						//if ( activeContributors >= minimumActiveContributors &&
						//		recentCommits >= minimumRecentCommits)
						//{
						// TODO verificar se tem licença?
						// TODO Verificar se tem issues?
	
						// Verificar se é android
						if ( isAndroidProject (repository) )
						{
							repositoryInfo.append("Android\t");
						}
						else
						{
							repositoryInfo.append("Non Android\t");
						}
	
						writer.write(repositoryInfo.toString());
						writer.write(contributorInfo.toString());
						writer.flush();
						tentativas = 100; // Gambiarra maravilhosa
					} 
					catch (Throwable e)
					{
						System.out.println("Falha enquanto processava repositorio '" + repository.getHtmlUrl() + "'. Mensagem da exceção: " + e.getMessage());
						
						try 
						{
							System.out.println("Dormindo 5 minutos antes de tentar novamente...");
							TimeUnit.MINUTES.sleep(5);
						} 
						catch (InterruptedException ignore)
						{
							System.out.println("Falha ao tentar dormir.....");
						}
						
						System.out.println("Tentando novamente...");
					}
				} while (tentativas < 3);
			}
		}
	}

	private boolean isAndroidProject(GHRepository repository) throws IOException
	{
		String projectDownloadURL = repository.getUrl()+"/zipball";
		File zipFile = new File (ProjectsUtil.projectsRoot+repository.getName()+".zip");
		File projectRoot;
		boolean isAndroidProject;

		GithubExplorer.downloadProjectZip(projectDownloadURL, zipFile);
		projectRoot = GithubExplorer.unZip(zipFile, ProjectsUtil.projectsRoot);

		isAndroidProject = FileFinder.isAndroidProject(projectRoot);

		try 
		{
			FileUtils.deleteDirectory(projectRoot);
		}
		catch (Exception e)
		{
			System.err.println("Failed to delete the directory.");
		}
		
		try 
		{
			Files.delete(Paths.get(zipFile.toURI()));
		}
		catch (Exception e)
		{
			System.err.println("Failed to delete the zip file.");
		}

		return isAndroidProject;
	}

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
		File zipFile;
		File projectRoot;

		int androidProjects = 0;

		while ( repositoryIte.hasNext() && androidProjects < 100 )
		{
			repository = repositoryIte.next();

			projectDownloadURL = repository.getUrl()+"/zipball";
			zipFile = new File (ProjectsUtil.projectsRoot+repository.getName()+".zip");

			GithubExplorer.downloadProjectZip(projectDownloadURL, zipFile);
			projectRoot = GithubExplorer.unZipAndRename(zipFile, ProjectsUtil.projectsRoot, repository.getFullName().replaceAll("/", " "));


			if ( FileFinder.isAndroidProject(projectRoot) )
			{
				androidProjects++;

				System.out.print((androidProjects)+"\t");
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
				System.out.println();


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

		try (ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(zipFile))
		{
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
	}

	// Dezipa o projeto e retorna a raiz do projeto dezipado com o nome indicado
	private static File unZipAndRename(File zipFile, String outputFolder, String projectRootName) throws IOException
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


		File projectRootNewName = new File ( ProjectsUtil.projectsRoot + File.separator + projectRootName );

		// Verificar se houve erro
		if ( !projectRoot.renameTo(projectRootNewName) )
		{
			throw new IllegalStateException ("Falha ao renomear raiz do projeto dezipado.");
		}

		return projectRootNewName;
	}
	
	// Dezipa o projeto e retorna a raiz do projeto dezipado com o nome indicado
		private static File unZip(File zipFile, String outputFolder) throws IOException
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


			return projectRoot;
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
			explorer.findProjects();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
