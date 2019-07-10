package ufrn.dimap.lets.ehmetrics.visitor;

public interface GuidelineMetrics
{
	/**
	 * Returns the guideline columns names
	 * */
	public abstract String getGuidelineHeader ();
	
	/**
	 * Returns the guideline data
	 * */
	public abstract String getGuidelineData();
}
