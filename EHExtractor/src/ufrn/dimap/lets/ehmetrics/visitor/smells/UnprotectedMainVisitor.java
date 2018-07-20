package ufrn.dimap.lets.ehmetrics.visitor.smells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;

import ufrn.dimap.lets.ehmetrics.visitor.SimpleVisitor;

public class UnprotectedMainVisitor extends SimpleVisitor
{
	private static int unprotectedMains;
	private static int protectedMains;
	
	private boolean onMain;
	private boolean isProtected;
	private boolean unprotectedMain;
	
	public UnprotectedMainVisitor ()
	{
		unprotectedMains = 0;
		protectedMains = 0;
	}
	
	public void visit (MethodDeclaration methodDeclaration, Void v)
	{	
		if ( isMainMethod (methodDeclaration) )
		{
			unprotectedMain = false;
			onMain = true;
			super.visit(methodDeclaration, v);
			onMain = false;
			
			if (unprotectedMain)
			{
				unprotectedMains++;
			}
			else
			{
				protectedMains++;
			}
		}
	}
	
	public void visit (TryStmt tryStmt, Void v)
	{
		if ( !onMain ) return;
		
		isProtected = false;
		
		if (hasGeneralHandler(tryStmt))
			isProtected = true;
		
		super.visit(tryStmt, v);
		
		isProtected = false;
	}
	
	private boolean hasGeneralHandler(TryStmt tryStmt)
	{
		List<ClassOrInterfaceType> caughtExceptions = new ArrayList<>();
		
		Iterator<CatchClause> i = tryStmt.getCatchClauses().iterator();
		while (i.hasNext())
		{
			caughtExceptions.addAll(getExceptions(i.next()));
		}
		
		for ( ClassOrInterfaceType type : caughtExceptions )
		{
			if ( type.getNameAsString().equals(Exception.class.getSimpleName()) ||
				 type.getNameAsString().equals(Throwable.class.getSimpleName()) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	private List<ClassOrInterfaceType> getExceptions (CatchClause catchClause)
	{
		List<ClassOrInterfaceType> result = new ArrayList<>();
		
		if ( catchClause.getParameter().getType() instanceof ClassOrInterfaceType )
		{
			result.add((ClassOrInterfaceType) catchClause.getParameter().getType());
		}
		else if (catchClause.getParameter().getType() instanceof UnionType)
		{
			UnionType unionType = (UnionType) catchClause.getParameter().getType();
			
			Iterator<ReferenceType> i = unionType.getElements().iterator();
			while ( i.hasNext() )
			{
				result.add((ClassOrInterfaceType) i.next());
			}
		}
		
		return result;
	}

	private boolean isMainMethod(MethodDeclaration methodDeclaration)
	{
		if ( methodDeclaration.getNameAsString().equals("main") )
		{
			if (methodDeclaration.isPublic())
			{
				if ( methodDeclaration.isStatic() )
				{
					if (methodDeclaration.getTypeAsString().equals("void"))
					{
						if (methodDeclaration.getParameters().size() == 1)
						{
							Parameter p = methodDeclaration.getParameter(0);
							
							if (p.getNameAsString().equals("args"))
							{
								if (p.getType() instanceof ArrayType)
								{
									ArrayType arrayType = (ArrayType) p.getType();
									if (arrayType.getComponentType() instanceof ClassOrInterfaceType)
									{
										if ( ((ClassOrInterfaceType)arrayType.getComponentType()).getNameAsString().equals("String"))
										{
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		return false;
	}

	public void visit (ArrayAccessExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (AssignExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (BinaryExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (BooleanLiteralExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (CastExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (CharLiteralExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (ClassExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (ConditionalExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (ExpressionStmt n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (FieldAccessExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (InstanceOfExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (LambdaExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (LongLiteralExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (MethodCallExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (MethodReferenceExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (NameExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (NullLiteralExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (ObjectCreationExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (SingleMemberAnnotationExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (SuperExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (ThrowStmt n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (UnaryExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	public void visit (ThisExpr n, Void v)
	{
		if ( !onMain ) return;
		
		if ( !isProtected ) unprotectedMain = true;
	}
	
	
	
	
	@Override
	public String printHeader()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Protected mains\t");
		builder.append("Unprotected mains\t");
		
		return builder.toString();
	}
	
	@Override
	public String printOutput()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(protectedMains + "\t");
		builder.append(unprotectedMains + "\t");
		return builder.toString();
	}
}
