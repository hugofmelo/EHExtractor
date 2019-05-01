package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.stmt.CatchClause;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;

public class VisitorsUtil {

	private VisitorsUtil ()	{}
	
	/**
	 * Convenient method to "convert" Handlers to CatchClauses.
	 * */
	public static List<CatchClause> getCatchClausesFromHandlers( List<Handler> handlers )
	{
		return handlers.stream()
			.map(handler -> (CatchClause) handler.getNode())
			.collect(Collectors.toList());
	}
}
