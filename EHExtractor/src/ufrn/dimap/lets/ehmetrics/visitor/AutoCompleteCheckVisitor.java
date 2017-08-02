package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;

import ufrn.dimap.lets.ehmetrics.HandlerActionResolver;
import ufrn.dimap.lets.ehmetrics.Logger;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.HandlerActionType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;

public class AutoCompleteCheckVisitor extends VoidVisitorAdapter<JavaParserFacade>
{
	private MetricsModel model;
	/*
	private Stack<SimpleName> exceptionNamesStack;
	private Stack<Handler> handlersStack;
	*/
	//private int netBeansHandlers;
	
	public AutoCompleteCheckVisitor (MetricsModel model)
	{
		/*
		exceptionNamesStack = new Stack<SimpleName>();
		handlersStack = new Stack<Handler>();
		*/
		this.model = model;
	}

	public void visit (CatchClause catchClause, JavaParserFacade facade)
	{		
		//MetricsModel model = MetricsModel.getInstance();

		// CHECK CATCH TYPE
		List<ReferenceType> referenceTypes = new ArrayList<ReferenceType>();

		// Solving "regular" catch clause
		if ( catchClause.getParameter().getType() instanceof ClassOrInterfaceType)
		{
			try
			{
				ReferenceType refType = (ReferenceType) facade.convertToUsage(catchClause.getParameter().getType());
				referenceTypes.add(refType);
			}
			catch (UnsupportedOperationException e)
			{
				facade.getType(catchClause.getParameter());
				  
			}
		}
		// Solving multicatch clause
		else if (catchClause.getParameter().getType() instanceof UnionType)
		{
			// Im trying this and getting an UnsupportedOperationException
			UnionType multiCatch = (UnionType) catchClause.getParameter().getType();
			Iterator<com.github.javaparser.ast.type.ReferenceType> i = multiCatch.getElements().iterator();
			while ( i.hasNext() )
			{
				ReferenceType refType = (ReferenceType) facade.convertToUsage(i.next());
				referenceTypes.add(refType);
			}
		}

		// CREATE EXCEPTION TYPES ON MODEL
		List <Type> types = new ArrayList <Type> ();
		for ( ReferenceType refType : referenceTypes )
		{
			types.add( model.findOrCreate(null, refType) );
		}

		// SAVE HANDLER TO MODEL
		Handler handler = model.addHandler(catchClause, types);

		// CHECK IF AUTOCOMPLETE HANDLER
		handler.setAutoComplete(this.isAutoCompleteHandler(catchClause));
		
		// CHECK IF EMPTY HANDLER
		handler.setEmpty(this.isEmptyHandler(catchClause));
		/*
		// PUSH HANDLER TO STACK
		SimpleName exceptionName = catchClause.getParameter().getName();
		exceptionNamesStack.add(exceptionName);
		handlersStack.add(handler);		
		 */

		// VISIT CHILDREN
		super.visit(catchClause,facade);

		/*
		// POP HANDLER FROM STACK
		exceptionNamesStack.pop();
		handlersStack.pop();
		 */
	}

	/**
	 * ECLIPSE AUTOCOMPLETE
	 * // TODO Auto-generated catch block
	 * e.printStackTrace();
	 * 
	 * NETBEANS AUTOCOMPLETE
	 * Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
	 * 
	 * INTELLIJ AUTOCOMPLETE
	 * e.printStackTrace();
	 * 
	 * @param catchClause
	 * @return
	 */

	private boolean isAutoCompleteHandler (CatchClause catchClause)
	{
		if ( catchClause.getBody().getStatements().size() == 1 )
		{
			Statement statement = catchClause.getBody().getStatement(0);

			return isEclipseAutoComplete(statement) || isIntelliJAutoComplete(statement);
		}
		else
		{
			return false;
		}
	}

	private boolean isEclipseAutoComplete (Statement statement)
	{
		if ( statement instanceof ExpressionStmt )
		{
			Expression expressionStatement = ((ExpressionStmt) statement).getExpression();

			if ( expressionStatement instanceof MethodCallExpr )
			{
				MethodCallExpr methodCallExpression = (MethodCallExpr) expressionStatement;

				if (methodCallExpression.getNameAsString().equals("printStackTrace") &&
						methodCallExpression.getArguments().size() == 0)
				{
					if ( methodCallExpression.getScope().isPresent() )
					{
						Expression scopeExpression = methodCallExpression.getScope().get();

						if ( scopeExpression instanceof NameExpr )
						{
							NameExpr nameExpression = (NameExpr)scopeExpression;

							// Testar se chamada é "e.printStacktrace()"
							if (nameExpression.getNameAsString().equals("e") )
							{
								// Testar se comentário é "// TODO Auto-generated catch block"
								if (statement.getComment().isPresent())
								{
									Comment comment = statement.getComment().get();

									if (comment.getContent().equals(" TODO Auto-generated catch block"))
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

		return false;
	}

	private boolean isIntelliJAutoComplete (Statement statement)
	{
		if ( statement instanceof ExpressionStmt )
		{
			Expression expressionStatement = ((ExpressionStmt) statement).getExpression();

			if ( expressionStatement instanceof MethodCallExpr )
			{
				MethodCallExpr methodCallExpression = (MethodCallExpr) expressionStatement;

				if (methodCallExpression.getNameAsString().equals("printStackTrace") &&
						methodCallExpression.getArguments().size() == 0)
				{
					if ( methodCallExpression.getScope().isPresent() )
					{
						Expression scopeExpression = methodCallExpression.getScope().get();

						if ( scopeExpression instanceof NameExpr )
						{
							NameExpr nameExpression = (NameExpr)scopeExpression;

							// Testar se chamada é "e.printStacktrace()"
							if (nameExpression.getNameAsString().equals("e") )
							{
								// Testar se não tem comentário
								if (methodCallExpression.getComment().isPresent() == false)
								{
									return true;
								}	
							}
						}
					}
				}
			}
		}

		return false;
	}

	// Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
	// 
	private boolean isNetBeansAutoComplete (Statement statement)
	{
		throw new UnsupportedOperationException("Autocomplete do NetBeans não implementado.");
		/*
		if ( statement instanceof ExpressionStmt )
		{
			Expression expressionStatement = ((ExpressionStmt) statement).getExpression();

			if ( expressionStatement instanceof MethodCallExpr )
			{
				MethodCallExpr logCallExpression = (MethodCallExpr) expressionStatement;

				
				
				if ( logCallExpression.getScope().isPresent() )
				{
					Expression scopeExpression = logCallExpression.getScope().get();

					if (scopeExpression instanceof MethodCallExpr)
					{
						MethodCallExpr getLoggerCallExpression = (MethodCallExpr)scopeExpression;


					}



					else if (methodCallExpression.getNameAsString().equals("log") &&
							methodCallExpression.getScope().isPresent() &&
							methodCallExpression.getScope().get() instanceof MethodCallExpr &&
							((MethodCallExpr)methodCallExpression.getScope().get()).getNameAsString().equals("getLogger") &&
							methodCallExpression.getArguments().size() == 0)
				}
			}
		}
		 
		return false;
		*/
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
}