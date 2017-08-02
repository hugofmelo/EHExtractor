package ufrn.dimap.lets.ehmetrics.repositoryexplorer.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

public class Repository
{
	private String html_url;
	private String name;
	private int stargazers_count;
	private Date pushed_at;
	private String releases_url;
	
	private List<Release> releases;
	
	public void fetchReleases () throws UnsupportedEncodingException, IOException
	{
		URL url = new URL(releases_url);
		String jsonString = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8")).readLine();
		
		Gson gson = new Gson();
		
		Release releases[] = gson.fromJson(jsonString, Release[].class);
		this.releases = Arrays.asList(releases);
	}
	
	public String getUrl() { return html_url; }	
	public String getName() { return name; }
	public int getStars() { return stargazers_count; }
	public Date getLastCommit() { return pushed_at; }
	
	
	public List<Release> getReleases() { return releases; }	
	
}
