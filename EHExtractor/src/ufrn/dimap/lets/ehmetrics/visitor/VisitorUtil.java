package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;

public class VisitorUtil {

	public static List<ClassOrInterfaceType> getHandledTypes ( CatchClause catchClause )
	{
		// output 
		List<ClassOrInterfaceType> types = new ArrayList<>();

		// CHECK CATCH TYPE
		// Solving "regular" catch clause
		if ( catchClause.getParameter().getType() instanceof ClassOrInterfaceType)
		{
			types.add((ClassOrInterfaceType)catchClause.getParameter().getType());			
		}
		// Solving multicatch clause
		else if (catchClause.getParameter().getType() instanceof UnionType)
		{
			UnionType multiCatch = (UnionType) catchClause.getParameter().getType();
			Iterator<ReferenceType> i = multiCatch.getElements().iterator();
			while ( i.hasNext() )
			{
				types.add((ClassOrInterfaceType)i.next());
			}
		}

		return types;
	}
}
