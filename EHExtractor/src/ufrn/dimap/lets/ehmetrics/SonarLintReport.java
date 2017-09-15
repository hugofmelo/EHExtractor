package ufrn.dimap.lets.ehmetrics;

import java.util.ArrayList;
import java.util.List;

public class SonarLintReport
{
	private List<SonarLintEntry> entries;	
	
	public SonarLintReport ()
	{
		entries = new ArrayList<>();
	}
	
	public void addEntry ( String rule, int occurrences )
	{
		this.entries.add( new SonarLintEntry(rule, occurrences) );
	}

	public Integer getOccurrencesOfRule(String rule)
	{
		for ( SonarLintEntry entry : this.entries )
		{
			if ( entry.getRule().equals(rule) )
			{
				return entry.getOccurrences();
			}
		}
		
		return 0;
	}
}
