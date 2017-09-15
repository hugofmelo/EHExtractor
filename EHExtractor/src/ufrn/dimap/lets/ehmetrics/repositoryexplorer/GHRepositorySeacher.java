package ufrn.dimap.lets.ehmetrics.repositoryexplorer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

import ufrn.dimap.lets.ehmetrics.repositoryexplorer.model.RepositorySearchResult;

public class GHRepositorySeacher
{
	private static final String searchRepositoriesURL = "search/repositories";
	
	private String language;
	private String size;
	private String stars;
	private String creationDate;
	private String lastUpdateDate;
	
	private String sort;
	private String order;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public GHRepositorySeacher ()
	{
		this.language = null;
		this.size = null;
		this.stars = null;
		this.creationDate = null;
		this.lastUpdateDate = null;
		
		//this.sort = "sort:" + SORT.STARS.toString();
		//this.order = "order:" + ORDER.DESC.toString();
	}
	
	public RepositorySearchResult doIt () throws MalformedURLException, IOException, RepositoryExplorerException
	{
		List<String> criteria = new ArrayList<>();
		
		if ( this.language != null )		criteria.add(this.language);
		if ( this.size != null )			criteria.add(this.size);
		if ( this.stars != null )			criteria.add(this.stars);
		if ( this.creationDate != null )	criteria.add(this.creationDate);
		if ( this.lastUpdateDate != null )	criteria.add(this.lastUpdateDate);
		
		if ( criteria.size() == 0 )
		{
			throw new RepositoryExplorerException ("É necessário definir pelo menos um critério de busca.");
		}
		else
		{
			String query = 	"q=" +
							String.join("+", criteria)/* +
							"&" + 
							this.sort+ "&" +
							this.order*/;
			
			System.out.println ("Query string: " + query);
			
			String jsonResponse = GitHubConnector.getJsonResponse(searchRepositoriesURL, query);
			
			Gson gson = new Gson();
			RepositorySearchResult result = gson.fromJson(jsonResponse, RepositorySearchResult.class);
			
			return result;
		}
		
		
	}
	
	public void setLanguage (LANGUAGE language) { this.language = "language:" + language.toString(); }
	
	public void setSizeExactly (int size) { this.size = "size:"+size; }
	public void setSizeGreaterThen (int size) { this.size = "size:>"+size; }
	public void setSizeGreaterOrEqualThen (int size) { this.size = "size:>="+size; }
	public void setSizeLesserThen (int size) { this.size = "size:<"+size; }
	public void setSizeLesserOrEqualThen (int size) { this.size = "size:<="+size; }
	public void setSizeFromTo (int from, int to) { this.size = "size:"+from+".."+to; }
	
	public void setStarsExactly (int stars) { this.stars = "stars:"+stars; }
	public void setStarsGreaterThen (int stars) { this.stars = "stars:>"+stars; }
	public void setStarsGreaterOrEqualThen (int stars) { this.stars = "stars:>="+stars; }
	public void setStarsLesserThen (int stars) { this.stars = "stars:<"+stars; }
	public void setStarsLesserOrEqualThen (int stars) { this.stars = "stars:<="+stars; }
	public void setStarsFromTo (int from, int to) { this.stars = "stars:"+from+".."+to; }
	
	public void setCreationDateExactly (Date date) { this.creationDate = "created:"+dateFormat.format(date); }
	public void setCreationDateGreaterThen (Date date) { this.creationDate = "created:>"+dateFormat.format(date); }
	public void setCreationDateGreaterOrEqualThen (Date date) { this.creationDate = "created:>="+dateFormat.format(date); }
	public void setCreationDateLesserThen (Date date) { this.creationDate = "created:<"+dateFormat.format(date); }
	public void setCreationDateLesserOrEqualThen (Date date) { this.creationDate = "created:<="+dateFormat.format(date); }
	public void setCreationDateFromTo (Date from, Date to) { this.creationDate = "created:"+dateFormat.format(from)+".."+dateFormat.format(to); }
	
	public void setLastUpdateDateExactly (Date date) { this.lastUpdateDate = "pushed:"+dateFormat.format(date); }
	public void setLastUpdateDateGreaterThen (Date date) { this.lastUpdateDate = "pushed:>"+dateFormat.format(date); }
	public void setLastUpdateDateGreaterOrEqualThen (Date date) { this.lastUpdateDate = "pushed:>="+dateFormat.format(date); }
	public void setLastUpdateDateLesserThen (Date date) { this.lastUpdateDate = "pushed:<"+dateFormat.format(date); }
	public void setLastUpdateDateLesserOrEqualThen (Date date) { this.lastUpdateDate = "pushed:<="+dateFormat.format(date); }
	public void setLastUpdateDateFromTo (Date from, Date to) { this.lastUpdateDate = "pushed:"+dateFormat.format(from)+".."+dateFormat.format(to); }
	
	public void sortBy (SORT sort, ORDER order) { this.sort = "sort:"+sort;
												  this.order = "order:"+order;}
	
	public enum LANGUAGE
	{
		JAVA ("Java")
		;
		
		private final String text;
		private LANGUAGE (final String text)
		{
			this.text = text;
		}
		
		public String toString ()
		{
			return this.text;
		}
	}
	
	public enum SORT
	{
		STARS, FORKS, UPDATED;
	}
	
	public enum ORDER
	{
		ASC, DESC;
	}
}
