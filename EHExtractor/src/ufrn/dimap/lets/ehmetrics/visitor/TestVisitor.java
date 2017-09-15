package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;

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