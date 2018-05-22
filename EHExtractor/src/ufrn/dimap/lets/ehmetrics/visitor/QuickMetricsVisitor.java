package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;

public class QuickMetricsVisitor extends VoidVisitorAdapter<Void> {

	private MetricsModel model;

	public QuickMetricsVisitor (MetricsModel model)
	{
		this.model = model;
	}

	public void visit (CatchClause catchClause, Void arg)
	{		
		this.model.totalCatches++;

		if ( isEmptyHandler(catchClause) )
		{
			this.model.emptyHandlers++;
		}

		List<ClassOrInterfaceType> types = getHandledTypes(catchClause);

		for ( ClassOrInterfaceType t : types )
		{
			if ( isGenericType(t) )
			{
				this.model.genericCatch++;
			}
		}

		// VISIT CHILDREN
		super.visit(catchClause, arg);
	}

	private List<ClassOrInterfaceType> getHandledTypes ( CatchClause catchClause )
	{
		// output 
		List<ClassOrInterfaceType> types = new ArrayList<ClassOrInterfaceType>();

		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		return types;
	}

	private boolean isGenericType (ClassOrInterfaceType type)
	{
		String typeName = type.getNameAsString();
		
		if ( typeName.equals(Exception.class.getSimpleName()))
		{
			return true;
		}
		else if (typeName.equals(Throwable.class.getSimpleName()))
		{
			return true;
		}
		else if (typeName.equals(RuntimeException.class.getSimpleName()))
		{
			return true;
		}
		else
		{
			return false;
		}		
	}


	private boolean isEmptyHandler(CatchClause catchClause)
	{
		if (catchClause.getBody().getStatements().size() == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		Expression thrownExpression = throwStatement.getExpression();

		if ( thrownExpression instanceof ObjectCreationExpr )
		{
			this.model.totalThrows++;

			ObjectCreationExpr objectCreationExp = (ObjectCreationExpr) thrownExpression;

			if (isGenericType (objectCreationExp.getType()))
			{
				this.model.genericThrows++;
			}

			// VISIT CHILDREN
			super.visit(throwStatement, arg);
		}
	}
}