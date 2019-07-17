package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ufrn.dimap.lets.ehmetrics.config.Guidelines;
import ufrn.dimap.lets.ehmetrics.visitor.AbstractGuidelineVisitor;
import ufrn.dimap.lets.ehmetrics.visitor.BaseGuidelineVisitor;

public class GuidelinesFactory
{
	private GuidelinesFactory () {}
	
	private static BaseGuidelineVisitor baseGuidelineVisitor = null;
	private static List<AbstractGuidelineVisitor> guidelineVisitors = null;
	
	public static List<AbstractGuidelineVisitor> createGuidelineVisitors ( boolean allowUnresolved )
	{
		baseGuidelineVisitor = new BaseGuidelineVisitor(allowUnresolved);
		
		guidelineVisitors = new ArrayList<>();
		
		if ( Guidelines.CATCH_IN_SPECIFIC_LAYER )
			guidelineVisitors.add(new CatchInSpecificLayerVisitor(baseGuidelineVisitor, allowUnresolved));
		
		if ( Guidelines.CONVERT_LIBRARY_EXCEPTIONS )
			guidelineVisitors.add(new ConvertLibraryExceptionsVisitor(baseGuidelineVisitor, allowUnresolved));
		
		if ( Guidelines.CONVERT_TO_RUNTIME_EXCEPTIONS )
			guidelineVisitors.add(new ConvertToRuntimeExceptionsVisitor(baseGuidelineVisitor, allowUnresolved));
		
		if ( Guidelines.DEFINE_A_SINGLE_EXCEPTION )
			guidelineVisitors.add(new DefineSingleExceptionVisitor(baseGuidelineVisitor, allowUnresolved));
		
		if ( Guidelines.DEFINE_A_SUPER_TYPE )
			guidelineVisitors.add(new DefineSuperTypeVisitor(baseGuidelineVisitor, allowUnresolved));
		
		if ( Guidelines.LOG_THE_EXCEPTION )
			guidelineVisitors.add(new LogTheExceptionVisitor(baseGuidelineVisitor, allowUnresolved));
		
		if ( Guidelines.SEND_TO_A_GLOBAL_HANDLER )
			guidelineVisitors.add(new SendToGlobalOrDefaultVisitor(baseGuidelineVisitor, allowUnresolved));
		
		return guidelineVisitors;
	}

	public static List<VoidVisitorAdapter<Void>> getAllVisitors()
	{
		if ( baseGuidelineVisitor == null )
		{
			throw new IllegalStateException ("Chame o método para criar antes!");
		}
		else
		{
			List<VoidVisitorAdapter<Void>> allVisitors = new ArrayList<>();
			allVisitors.add(baseGuidelineVisitor);
			allVisitors.addAll(guidelineVisitors);
			return allVisitors;
		}
	}
}
