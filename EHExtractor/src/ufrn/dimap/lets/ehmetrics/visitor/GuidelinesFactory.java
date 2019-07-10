package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.List;

import ufrn.dimap.lets.ehmetrics.config.Guidelines;

public class GuidelinesFactory
{
	private GuidelinesFactory () {}
	
	private static List<GuidelineCheckerVisitor> visitors = null;
	
	public static List<GuidelineCheckerVisitor> loadVisitors ( boolean allowUnresolved )
	{
		if ( visitors != null )
			return visitors;
		
		visitors = new ArrayList<>();
		
		if ( Guidelines.CATCH_IN_SPECIFIC_LAYER )
			visitors.add(new CatchInSpecificLayerVisitor(allowUnresolved));
		
		if ( Guidelines.CONVERT_LIBRARY_EXCEPTIONS )
			visitors.add(new ConvertLibraryExceptionsVisitor(allowUnresolved));
		
		if ( Guidelines.CONVERT_TO_RUNTIME_EXCEPTIONS )
			visitors.add(new ConvertToRuntimeExceptionsVisitor(allowUnresolved));
		
		if ( Guidelines.DEFINE_A_SINGLE_EXCEPTION )
			visitors.add(new DefineSingleExceptionVisitor(allowUnresolved));
		
		if ( Guidelines.DEFINE_A_SUPER_TYPE )
			visitors.add(new DefineSuperTypeVisitor(allowUnresolved));
		
		if ( Guidelines.LOG_THE_EXCEPTION )
			visitors.add(new LogTheExceptionVisitor(allowUnresolved));
		
		if ( Guidelines.SEND_TO_A_GLOBAL_HANDLER )
			visitors.add(new SendToGlobalOrDefaultVisitor(allowUnresolved));
		
		return visitors;
	}
	
	public static void clearVisitors ()
	{
		visitors.forEach(GuidelineCheckerVisitor::clear);
	}
}
