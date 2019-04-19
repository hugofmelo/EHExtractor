package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;

public class QuickMetricsVisitor extends VoidVisitorAdapter<Void> {

	private MetricsModel model;

	public QuickMetricsVisitor (MetricsModel model)
	{
		this.model = model;
	}

	@Override
	public void visit (ClassOrInterfaceDeclaration classDeclaration, Void arg)
	{		
		ResolvedReferenceTypeDeclaration rtd = classDeclaration.resolve();
		
		if ( rtd.isClass() )
		{
			System.out.println(rtd.getQualifiedName());
			for ( ResolvedReferenceType rt : rtd.getAncestors() )
			{
				System.out.println(rt.getQualifiedName());
			}
		}
		
		System.out.println("\n\n");

				// VISIT CHILDREN
		super.visit(classDeclaration, arg);
	}
	
	@Override
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

	private boolean isGenericType (ClassOrInterfaceType type)
	{
		String typeName = type.getNameAsString();

		if ( typeName.equals(Exception.class.getSimpleName()) || typeName.equals(Exception.class.getCanonicalName()) )
		{
			return true;
		}
		else if (typeName.equals(Throwable.class.getSimpleName()) || typeName.equals(Throwable.class.getCanonicalName()))
		{
			return true;
		}
		else if (typeName.equals(RuntimeException.class.getSimpleName()) || typeName.equals(RuntimeException.class.getCanonicalName()))
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

	@Override
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