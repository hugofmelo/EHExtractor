package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;

public class Util
{
	protected static List<ReferenceType> getHandledTypes ( CatchClause catchClause, JavaParserFacade facade  )
	{
		// CHECK CATCH TYPE
		List<ReferenceType> referenceTypes = new ArrayList<ReferenceType>();

		// Solving "regular" catch clause
		if ( catchClause.getParameter().getType() instanceof ClassOrInterfaceType)
		{
			try
			{
				ReferenceType refType = (ReferenceType) facade.convertToUsage(catchClause.getParameter().getType());
				referenceTypes.add(refType);
			}
			catch (UnsupportedOperationException e)
			{
				facade.getType(catchClause.getParameter());

			}
		}
		// Solving multicatch clause
		else if (catchClause.getParameter().getType() instanceof UnionType)
		{
			// Im trying this and getting an UnsupportedOperationException
			UnionType multiCatch = (UnionType) catchClause.getParameter().getType();
			Iterator<com.github.javaparser.ast.type.ReferenceType> i = multiCatch.getElements().iterator();
			while ( i.hasNext() )
			{
				ReferenceType refType = (ReferenceType) facade.convertToUsage(i.next());
				referenceTypes.add(refType);
			}
		}
		
		return referenceTypes;
	}
}
