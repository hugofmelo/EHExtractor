package ufrn.dimap.lets.ehmetrics;

public class SonarLintEntry
{
	private String rule;
	private int occurrences;
	
	public SonarLintEntry (String rule, int occurrences)
	{
		this.rule = rule;
		this.occurrences = occurrences;
	}
	
	public String getRule() { return rule; }	
	
	public int getOccurrences() { return occurrences; }
}
