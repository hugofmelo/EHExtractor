package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerType;

/**
 * Visitor para verificar o guideline "Add Contextual Information".
 * 
 * Para confirmar o guideline a seguinte heur�stica � usada:
 * 50% de todas as instancia��es de exce��es incluem mais argumentos do que 1 string e 1 Throwable.
 * */
public class AddContextualInformationVisitor extends GuidelineCheckerVisitor
{
	private Stack<Handler> handlersInContext;
	
	private List<ObjectCreationExpr> exceptionsWithAdditionalContextualInformation;
	private List<ObjectCreationExpr> exceptionsWithoutAdditionalContextualInformation;
	
	public AddContextualInformationVisitor (boolean allowUnresolved)
	{
		super (allowUnresolved);
		
		exceptionsWithAdditionalContextualInformation = new ArrayList<>();
		exceptionsWithoutAdditionalContextualInformation = new ArrayList<>();
	}
	
	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{
		// Forces the stack to reset. Sometimes um error when parsing precious java files could finish the visitor without reseting the stack.
		this.handlersInContext = new Stack<>(); 
		
        super.visit(compilationUnit, arg);
    }
	
	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		Handler newHandler = createHandler(catchClause);	
		
		this.handlersInContext.push(newHandler);
		
		// VISIT CHILDREN
		super.visit(catchClause, arg);
		
		this.handlersInContext.pop();
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		createSignaler(throwStatement);
		
		// VISIT CHILDREN
		super.visit(throwStatement, arg);
	}	
	
	@Override
	public void visit (ObjectCreationExpr objectCreationExpr, Void arg)
	{
		ClassOrInterfaceType createdType = objectCreationExpr.getType();
		
		String typeName;
		try
		{
			typeName = createdType.resolve().getQualifiedName();
		}
		catch (UnsolvedSymbolException e)
		{
			typeName = createdType.getNameAsString();
		}
		
		Optional<Type> maybeType = this.typeHierarchy.findTypeByName(typeName);
		
		
		if ( maybeType.isPresent() )
		{
			Type type = maybeType.get();
			if ( type.isException() )
			{
				if ( hasAdditionalContextualInformation(objectCreationExpr.getArguments()) )
				{
					this.exceptionsWithAdditionalContextualInformation.add(objectCreationExpr);
				}
				else
				{
					this.exceptionsWithoutAdditionalContextualInformation.add(objectCreationExpr);
				}
			}
		}
		
		super.visit(objectCreationExpr, arg);
	}

	private boolean hasAdditionalContextualInformation(NodeList<Expression> arguments)
	{
		if ( arguments.size() > 2 )
		{
			return true;
		}
		
		return false;
		
		/*
		List<SimpleName> simpleNamesInObjectCreation = argumentsInObjectCreation.stream()
				.filter(Expression::isNameExpr)
				.map(expression -> expression.asNameExpr().getName())
				.collect(Collectors.toList());
		
		this.relatedCatchClause = catchesInContext.stream()
				.filter (catchClause -> simpleNamesInObjectCreation.contains((catchClause.getParameter().getName())))
				.findAny();

		if ( this.relatedCatchClause.isPresent() )
		{
			this.signalerType = SignalerType.WRAPPING;
		}
		else
		{
			this.signalerType = SignalerType.DESTRUCTIVE_SIMPLE_THROW;
		}
		*/
	}

	/**
	 * Verifica se o projeto adota o guideline referenciado neste visitor.
	 * 
	 * Para entender as condi��es do guideline, ver Javadoc da classe
	 * */
	public void checkGuidelineConformance ()
	{	
		System.out.println("Number of exception instantiations with additional information: " + this.exceptionsWithAdditionalContextualInformation.size());
		System.out.println("Number of exception instantiations without additional information: " + this.exceptionsWithoutAdditionalContextualInformation.size());
		
		System.out.println("'Add contextual information' conformance: " + 1.0*this.exceptionsWithAdditionalContextualInformation.size()/(this.exceptionsWithAdditionalContextualInformation.size()+this.exceptionsWithoutAdditionalContextualInformation.size()));
	}
}