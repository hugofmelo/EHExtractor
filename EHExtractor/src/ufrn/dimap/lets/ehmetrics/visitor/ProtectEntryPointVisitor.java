package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.UnsolvedSymbolException;

import ufrn.dimap.lets.ehmetrics.javaparserutil.JavaParserUtil;

/**
 * Visitor para verificar o guideline "Protect entrypoint ".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 90% de todos os entrypoints são protegidos.
 * 
 * Um entrypoint é protegido se possui todas as chamadas de método dentro de blocos try que estão associados
 * a catches genéricos (Exception e Throwable).
 * */
public class ProtectEntryPointVisitor extends GuidelineCheckerVisitor
{
	private List<MethodDeclaration> protectedEntryPoints;
	private List<MethodDeclaration> unProtectedEntryPoints;
	
	private boolean traversingEntryPoint;
	private boolean traversingProtectedTryBlock;
	private boolean isProtectedEntryPoint;
	
	public ProtectEntryPointVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
		this.protectedEntryPoints = new ArrayList<>();
		this.unProtectedEntryPoints = new ArrayList<>();
	}
	

	@Override
	public void visit ( MethodDeclaration methodDeclaration, Void arg )
	{
		if ( isEntryPoint(methodDeclaration) )
		{
			traversingEntryPoint = true;
			
			
			isProtectedEntryPoint = true;
			traversingProtectedTryBlock = false;
			
			super.visit(methodDeclaration, arg);
			
			traversingEntryPoint = false;
			
			
			if ( isProtectedEntryPoint )
			{
				this.protectedEntryPoints.add(methodDeclaration);
			}
			else
			{
				this.unProtectedEntryPoints.add(methodDeclaration);
			}
		}
	}
	
	@Override
	public void visit ( TryStmt tryStatement, Void arg )
	{
		if ( traversingEntryPoint )
		{
			if ( isProtectedTryBlock (tryStatement))
			{
				traversingProtectedTryBlock = true;
			}
			else
			{
				super.visit(tryStatement, arg);
			}
		}
		else
		{
			throw new IllegalStateException ("Não deveria visitar um try que não está em um entrypoint.");
		}
	}
	
	@Override
	public void visit ( MethodCallExpr methodCallExpression, Void arg )
	{
		if ( traversingEntryPoint && !traversingProtectedTryBlock )
		{
			isProtectedEntryPoint = false;
		}
	}
	
	@Override
	public void visit ( ObjectCreationExpr objectCreationExpression, Void arg )
	{
		if ( traversingEntryPoint && !traversingProtectedTryBlock )
		{
			isProtectedEntryPoint = false;
		}
	}
	
	/**
	 * Returns true if any catch is catching java.lang.Exception or java.lang.Throwable.
	 * */
	private static boolean isProtectedTryBlock(TryStmt tryBlock)
	{
		return tryBlock.getCatchClauses().stream()
			.map(JavaParserUtil::getHandledTypes)
			.flatMap(List::stream)
			.map(ProtectEntryPointVisitor::isGenericType)
			.findAny()
			.isPresent();
	}

	/**
	 * Returns true if the type is java.lang.Exception or java.lang.Throwable. Returns false otherwise.
	 * */
	private static boolean isGenericType (ClassOrInterfaceType type)
	{
		try
		{
			return 
					type.resolve().getTypeDeclaration().getQualifiedName().equals(Throwable.class.getCanonicalName()) ||
					type.resolve().getTypeDeclaration().getQualifiedName().equals(Exception.class.getCanonicalName());
		}
		catch (UnsolvedSymbolException e)
		{
			return false;
		}
	}

	private static boolean isEntryPoint (MethodDeclaration methodDeclaration)
	{
		return isMainMethod (methodDeclaration);
	}
	
	private static boolean isMainMethod (MethodDeclaration methodDeclaration)
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
						return true;
					}
				}
			}
		}
		
		return false;
	}

	@Override
	public void checkGuidelineConformance()
	{
		System.out.println("Number of protected entrypoints: " + this.protectedEntryPoints.size());
		System.out.println("Number of unprotected entrypoints: " + this.unProtectedEntryPoints.size());

		
		System.out.println("'Protect entrypoints' conformance: " + 1.0*protectedEntryPoints.size()/(protectedEntryPoints.size() + this.unProtectedEntryPoints.size()) );
	}
}
