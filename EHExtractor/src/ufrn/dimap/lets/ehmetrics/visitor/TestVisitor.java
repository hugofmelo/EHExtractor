package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ExceptionType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.analyzer.Util;

public class TestVisitor extends VoidVisitorAdapter<JavaParserFacade>
{	
	public TestVisitor ()
	{
	}

	public void visit (ClassOrInterfaceDeclaration declaration, JavaParserFacade facade)
	{				
		ReferenceTypeDeclaration referenceTypeDeclaration = facade.getTypeDeclaration(declaration);
		//Stack<String> typesNames = Util.getClassAncestorsNames(referenceTypeDeclaration);
		for ( ReferenceType refType : referenceTypeDeclaration.getAllAncestors() )
		{
			System.err.print(refType.getQualifiedName() + " >> ");
		}
	}
}