package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MainMethodVisitor<T> extends VoidVisitorAdapter<Void>
{
	private List<MethodDeclaration> mainMethods;
	
	public MainMethodVisitor ()
	{
		this.mainMethods = new ArrayList<>();
	}
	

	@Override
	public void visit ( MethodDeclaration methodDeclaration, Void arg )
	{
		if ( methodDeclaration.getName().toString().equals("main") )
		{
			if ( methodDeclaration.isPublic() && methodDeclaration.isStatic() && methodDeclaration.getType().isVoidType() )
			{
				NodeList<Parameter> parameters = methodDeclaration.getParameters();
				
				if (parameters.size() == 1)
				{
					Parameter parameter = parameters.get(0);
					
					Type t = parameter.getType();
					
					if (t.asString().equals("String[]"))
					{
						this.mainMethods.add(methodDeclaration);
					}
				}
			}
		}
	}
	
	public List<MethodDeclaration> getMainMethods ()
	{
		return this.mainMethods;
	}
}
