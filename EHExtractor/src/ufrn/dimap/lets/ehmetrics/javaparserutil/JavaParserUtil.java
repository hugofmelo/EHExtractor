package ufrn.dimap.lets.ehmetrics.javaparserutil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;

import ufrn.dimap.lets.ehmetrics.analyzer.UnknownSignalerException;

/**
 * Classe utilitária para resolver tipos de ThrowStmt's and CatchClause's.
 * */
public class JavaParserUtil {

	private JavaParserUtil() {}
	
	/**
	 * Returns a list of ClassOrInterfaceType's from a CatchClause. The resolving of the types were not yet tried.
	 * */
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
	
	/**
	 * Returns a Optional of a ResolvedClassDeclaration from a ThrowStmt.
	 * */
	public static Optional<ResolvedClassDeclaration> getThrownClassDeclaration (ThrowStmt throwStatement)
	{
		try
		{
			return Optional.of(throwStatement.getExpression().calculateResolvedType().asReferenceType().getTypeDeclaration().asClass());
		}
		catch ( UnsolvedSymbolException e )
		{
			return Optional.empty();
		}
		/* TODO Reportar isso como uma sugestão no projeto do JavaParser no GitHub.
		 * 
		 * Como reproduzir: tentar resolver uma chamada de um método que é definido em uma super classe que não é resolvida.
		 * 
		 * Exemplo: Dada uma classe A não resolvida, criar uma classe vazia B que herda de A. No main,
		 * instanciar B e chamar toString().
		 * 
		 * O que deveria acontecer: UnsolvedSymbolException
		 * 
		 * O que acontece: RuntimeException
		 */
		catch (RuntimeException e)
		{
			return Optional.empty();
		}
		
	}
	
	/**
	 * Returns a {@link ClassOrInterfaceType} from a ThrowStmt.
	 * 
	 * This method must be used when {@link #getThrownClassDeclaration (ThrowStmt)} returns a empty Optional.
	 * 
	 * @throws UnknownSignalerException when the signaled type could not be resolved.
	 * */
	public static ClassOrInterfaceType getThrownClassOrInterfaceType (ThrowStmt throwStatement)
	{
		return getThrownClassOrInterfaceType(throwStatement.getExpression());
	}
	
	/**
	 * Auxiliar method to process the thrown Expression.
	 * 
	 * @throws UnknownSignalerException when the signaled type could not be resolved.
	 * */
	private static ClassOrInterfaceType getThrownClassOrInterfaceType (Expression throwExpression)
	{
		if ( throwExpression instanceof NameExpr )
		{
			try
			{
				ResolvedValueDeclaration declaration = throwExpression.asNameExpr().resolve();

				return findVariableType (declaration);
			}
			catch (UnsolvedSymbolException e)
			{
				throw new UnknownSignalerException ("Sinalização de NameExpr cuja declaração não foi encontrada.", throwExpression.findAncestor(ThrowStmt.class).get(), e);
			}
		}
		else if ( throwExpression instanceof ObjectCreationExpr )
		{
			ObjectCreationExpr objectCreationExp = throwExpression.asObjectCreationExpr();

			return objectCreationExp.getType();
		}
		else if (throwExpression instanceof CastExpr)
		{
			CastExpr castExpression = throwExpression.asCastExpr();

			if ( castExpression.getType() instanceof ClassOrInterfaceType )
			{
				return castExpression.getType().asClassOrInterfaceType();
			}
			else
			{
				throw new UnknownSignalerException("Sinalização com cast cujo tipo não foi resolvido.", throwExpression.findAncestor(ThrowStmt.class).get());
			}
		}
		else if ( throwExpression instanceof MethodCallExpr )
		{
			throw new UnknownSignalerException ("Sinalização de uma chamada de método que não pôde ter seu tipo resolvido.", throwExpression.findAncestor(ThrowStmt.class).get());
		}
		else
		{
			throw new UnknownSignalerException ("A sinalização não é de um dos padrões suportados.", throwExpression.findAncestor(ThrowStmt.class).get());
		}
	}
	
	/**
	 * Dada uma variável no código, tenta resolver o seu tipo.
	 * 
	 * Suporta variáveis de métodos, atributos de classes, parametros de métodos e parametros de catch block.
	 * */
	private static ClassOrInterfaceType findVariableType (ResolvedValueDeclaration resolvedValueDeclaration)
	{		
		// Uma variável declarada no corpo de um método
		if ( resolvedValueDeclaration instanceof JavaParserSymbolDeclaration )
		{
			VariableDeclarator declarator = (VariableDeclarator) ((JavaParserSymbolDeclaration)resolvedValueDeclaration).getWrappedNode();

			return declarator.getType().asClassOrInterfaceType();
		}
		// Uma variável que é parametro de um método ou em um catch block
		else if ( resolvedValueDeclaration instanceof JavaParserParameterDeclaration )
		{
			Parameter parameter = ((JavaParserParameterDeclaration)resolvedValueDeclaration).getWrappedNode();

			return parameter.getType().asClassOrInterfaceType();
		}
		// Uma variável que é atributo da classe
		else if ( resolvedValueDeclaration instanceof JavaParserFieldDeclaration )
		{
			FieldDeclaration fieldDeclaration = ((JavaParserFieldDeclaration)resolvedValueDeclaration).getWrappedNode();

			return fieldDeclaration.getVariable(0).getType().asClassOrInterfaceType();
		}
		else
		{
			throw new UnsupportedOperationException("Formato não suportado - vê o stack trace ai");
		}
	}
}
