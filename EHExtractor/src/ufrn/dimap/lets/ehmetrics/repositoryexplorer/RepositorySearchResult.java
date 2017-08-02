package ufrn.dimap.lets.ehmetrics.repositoryexplorer;

import java.util.List;

import ufrn.dimap.lets.ehmetrics.repositoryexplorer.model.GHRepository;

public class RepositorySearchResult
{
	private int total_count;
	private List<GHRepository> items;
 
	
	public int getTotal_count() {
		return total_count;
	}
	public void setTotal_count(int total_count) {
		this.total_count = total_count;
	}
	public List<GHRepository> getItems() {
		return items;
	}
	public void setItems(List<GHRepository> items) {
		this.items = items;
	}
	
	
}
