package ufrn.dimap.lets.ehmetrics.repositoryexplorer.model;

import java.util.List;

public class RepositorySearchResult
{
	// Os nomes dos atributos devem coincidir com as tags do JSON que é retornado pelo GitHub. E não é necessário ter getters e setters
	private int total_count;
	private List<Repository> items;
 
	
	public int getTotal() {
		return total_count;
	}
	
	public List<Repository> getRepositories() {
		return items;
	}
	
	
}
