package ufrn.dimap.lets.ehmetrics.repositoryexplorer.model;

import java.util.Date;

public class Release
{
	private String name;
	private Date published_at;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getPublished_at() {
		return published_at;
	}
	public void setPublished_at(Date published_at) {
		this.published_at = published_at;
	}
	
	
}
