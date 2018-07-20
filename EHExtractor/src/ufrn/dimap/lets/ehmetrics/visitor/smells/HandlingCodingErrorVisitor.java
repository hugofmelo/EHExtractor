package ufrn.dimap.lets.ehmetrics.visitor.smells;

import java.io.IOException;
import java.util.Iterator;

import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ufrn.dimap.lets.ehmetrics.visitor.SimpleVisitor;

public class HandlingCodingErrorVisitor extends SimpleVisitor
{
	private static int NP, CC, AIOoB;
	
	public HandlingCodingErrorVisitor ()
	{
		NP = 0;
		CC = 0;
		AIOoB = 0;
	}
	
	public void visit (CatchClause catchClause, Void v)
	{	
		Type type = catchClause.getParameter().getType();
		
		type.ifUnionType(HandlingCodingErrorVisitor::processMulticatch);
		
		type.ifClassOrInterfaceType(HandlingCodingErrorVisitor::countTypes);	
		
		super.visit(catchClause, v);
	}
	
	private static void processMulticatch (UnionType unionType)
	{
		Iterator<ReferenceType> i = unionType.getElements().iterator();
		while ( i.hasNext() )
		{
			i.next().ifClassOrInterfaceType(HandlingCodingErrorVisitor::countTypes);
		}
	}
	
	private static void countTypes (ClassOrInterfaceType type)
	{
		String typeName = type.getNameAsString();
		
		
		if ( typeName.equals(NullPointerException.class.getSimpleName()) )
		{
			HandlingCodingErrorVisitor.NP++;
		}
		
		if ( typeName.equals(ArrayIndexOutOfBoundsException.class.getSimpleName()) )
		{
			HandlingCodingErrorVisitor.AIOoB++;
		}
		
		if ( typeName.equals(ClassCastException.class.getSimpleName()) )
		{
			HandlingCodingErrorVisitor.CC++;
		}
	}
	
	@Override
	public String printHeader()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("NullPointerException caught\t");
		builder.append("ArrayIndexOutOfBoundsException caught\t");
		builder.append("ClassCastException caught\t");
		
		return builder.toString();
	}
	
	@Override
	public String printOutput()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(Integer.toString(NP) + "\t");
		builder.append(Integer.toString(AIOoB) + "\t");
		builder.append(Integer.toString(CC) + "\t");
		
		return builder.toString();
	}
}
