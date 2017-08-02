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

public class GHRepository
{
	private String html_url;
	private String name;
	private int stargazers_count;
	private Date pushed_at;
	private String releases_url;
	
	private List<GHRelease> releases;
	
	public void fetchReleases () throws UnsupportedEncodingException, IOException
	{
		URL url = new URL(releases_url);
		String jsonString = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8")).readLine();
		
		Gson gson = new Gson();
		
		GHRelease releases[] = gson.fromJson(jsonString, GHRelease[].class);
		this.setReleases( Arrays.asList(releases) ) ;
	}
	
	public String getHtml_url() { return html_url; }
	public void setHtml_url(String html_url) { this.html_url = html_url; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public int getStargazers_count() { return stargazers_count; }
	public void setStargazers_count(int stargazers_count) { this.stargazers_count = stargazers_count; }
	
	public Date getPushed_at() { return pushed_at; }
	public void setPushed_at(Date pushed_at) { this.pushed_at = pushed_at; }
	
	public String getReleases_url() { return releases_url; }
	public void setReleases_url(String releases_url) { this.releases_url = releases_url; }
	
	
	public List<GHRelease> getReleases() { return releases; }
	public void setReleases(List<GHRelease> releases) { this.releases = releases; }
	
	
	
}
