package ufrn.dimap.lets.ehmetrics.repositoryexplorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.json.JsonObject;

import org.kohsuke.github.GHDirection;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GHRepositorySearchBuilder.Sort;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.response.JsonResponse;

import ufrn.dimap.lets.ehmetrics.repositoryexplorer.GHRepositorySeacher.LANGUAGE;
import ufrn.dimap.lets.ehmetrics.repositoryexplorer.model.GHRelease;

public class GithubExplorer
{
	private int searchID = -1;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public void queryProjects () throws IOException
	{	
		GitHub gitHub = GitHub.connectAnonymously();
		GHRepositorySearchBuilder search = gitHub.searchRepositories();
		search.sort(Sort.STARS);
		search.order(GHDirection.DESC);
		search.size("1024..1100"); // Tamanho em kbytes
		//search.size("<1240"); // Tamanho em kbytes
		//search.created(">=YYYY-MM-DD");
		//search.pushed(">=YYYY-MM-DD"); // Último commit
		search.stars("0");
		//search.forks(">=1000");
		search.language("Java");
		
		PagedSearchIterable<GHRepository> result = search.list();
		System.out.println("Total: " + result.getTotalCount());
		Iterator <GHRepository> repoIte = result.iterator();
		GHRepository repo;
		
		
		
		while ( repoIte.hasNext() )
		{
			repo = repoIte.next();
			
			
			
			System.out.print(repo.getHtmlUrl()+"\t");
			System.out.print(repo.getName()+"\t");
			System.out.print(repo.getStargazersCount()+"\t");
			System.out.print(repo.getSize()+"\t");
			System.out.print(dateFormat.format(repo.getCreatedAt())+"\t");
			System.out.print(dateFormat.format(repo.getPushedAt())+"\t");
			//System.out.print(dateFormat.format(repo.getUpdatedAt())+"\t");
			System.out.println();
		}
	}
	
	public void queryProjects2 () throws IOException, RepositoryExplorerException
	{	
		GHRepositorySeacher repositorySeacher = new GHRepositorySeacher();
		
		repositorySeacher.setLanguage(LANGUAGE.JAVA);
		
		RepositorySearchResult result = repositorySeacher.doIt();
		
		for (ufrn.dimap.lets.ehmetrics.repositoryexplorer.model.GHRepository repository : result.getItems()) 
		{
			repository.fetchReleases();
			
			for ( GHRelease release : repository.getReleases() )
			{
				System.out.println(release.getName());
			}
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
			explorer.queryProjects2();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryExplorerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
