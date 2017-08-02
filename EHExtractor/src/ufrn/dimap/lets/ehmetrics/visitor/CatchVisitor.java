package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
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
import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;
import com.github.javaparser.symbolsolver.model.typesystem.TypeVariable;

import ufrn.dimap.lets.ehmetrics.Logger;
import ufrn.dimap.lets.ehmetrics.Util;
import ufrn.dimap.lets.ehmetrics.abstractmodel.ExceptionType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.HandlerActionType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.SignalerType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.HandlerActionResolver;

public class CatchVisitor extends VoidVisitorAdapter<JavaParserFacade>
{
	private MetricsModel model;
	private Stack<SimpleName> exceptionNamesStack;
	private Stack<Handler> handlersStack;
	
	public CatchVisitor (MetricsModel model)
	{
		exceptionNamesStack = new Stack<SimpleName>();
		handlersStack = new Stack<Handler>();
		this.model = model;
	}
	
	/*
	public void visit(MethodDeclaration n, Void arg) {

        Node node = n.getType();
        //System.out.println(node);
        CombinedTypeSolver solver = new CombinedTypeSolver();
		solver.add(new ReflectionTypeSolver());
		solver.add(new JavaParserTypeSolver(new File("./src/ufrn/dimap/lets/ehmetrics/test")));
		//JavaParserFacade.get(solver);
		//System.out.println("dsfsdf");
		//JavaParserFacade.
		//System.out.println(JavaParserFactory.getContext(node, solver).toString());
		//System.out.println(JavaParserFacade.get(solver).getTypeOfThisIn(node));
    }
	*/
	/*
	public void visit(MethodCallExpr n, JavaParserFacade javaParserFacade) {
        super.visit(n, javaParserFacade);
        System.out.println(n.toString() + " has type " + javaParserFacade.getType(n).describe());
        if (javaParserFacade.getType(n).isReferenceType()) {
            for (ReferenceType ancestor : javaParserFacade.getType(n).asReferenceType().getAllAncestors()) {
                System.out.println("Ancestor " + ancestor.describe());
            }
        }
    }
    */
	
	public void visit (ClassOrInterfaceDeclaration declaration, JavaParserFacade facade)
	{				
		if (declaration.isInterface() == false)
		{
			ReferenceTypeDeclaration referenceTypeDeclaration = facade.getTypeDeclaration(declaration);
			//Stack<String> typesNames = Util.getClassAncestorsNames(referenceTypeDeclaration);
			
			ExceptionType exceptionType = Util.resolveExceptionType(referenceTypeDeclaration);
			
			if ( exceptionType != ExceptionType.NO_EXCEPTION )
			{
				model.findOrCreate ( declaration, referenceTypeDeclaration );
			}
			
		}	
		
		super.visit(declaration,facade);
	}
	
	public void visit (ThrowStmt throwStatement, JavaParserFacade facade)
	{		
		// Simple throw
		if ( this.handlersStack.isEmpty() )
		{
			// Retrieve Exception Type from model
			com.github.javaparser.symbolsolver.model.typesystem.Type type = facade.getType(throwStatement.getExpression());
			
			if (type.isReferenceType())
			{
				ReferenceType thrownReferenceType = (ReferenceType) type;
				Type thrownType = model.findOrCreate ( null, thrownReferenceType );
				
				// Add signaler to model
				model.addSignaler(throwStatement, thrownType, SignalerType.SIMPLE);
			}
			else // if (type.isTypeVariable())
			{
				//Logger.getInstance().writeError("visit (ThrowStmt) : Solver retornou '" + type.getClass().getSimpleName() + "' ao invés de ReferenceType. Ignorando o throw.\n");
			}
			
			
		}
		// Rethrow ou wrapping
		else
		{
			Handler handler = handlersStack.peek();
			
			// Retrieve the expression of throw
			Expression exp = ((ThrowStmt) throwStatement).getExpression();
			ReferenceType thrownReferenceType = (ReferenceType) facade.getType(exp);
			Type thrownType = model.findOrCreate ( null, thrownReferenceType );
			
			// o statement é um "throw e". Provavelmente é um rethrow. Outros casos são improváveis, mas possíveis, e são relatados como erro.
			if ( exp instanceof NameExpr )
			{
				String thrownExceptionName = ((NameExpr) exp).getNameAsString();
				String peekExceptionName = this.exceptionNamesStack.peek().asString();
				
				// Rethrow simples
				if ( peekExceptionName.equals(thrownExceptionName) )
				{
					handler.addHandlerAction(throwStatement, HandlerActionType.RETHROW);
					model.addSignaler(throwStatement, thrownType, SignalerType.RETHROW);
				}
				else // é sinalizado um NameExpr, mas ele não é o topo da pilha
				{
					handler.addHandlerAction(throwStatement, HandlerActionType.UNKNOWN);
					model.addSignaler(throwStatement, thrownType, SignalerType.UNKNOWN);
					//Logger.getInstance().writeLog("Um NameExpr que não está no topo da pilha (ou estamos em um catch aninhado e foi sinalizado uma exceção mais externa ou foi instanciada uma nova exceção no corpo do tratador) ---> " + exp.getClass().getSimpleName() + "\n" +
					//		   						   "Code:\n" + throwStatement.getParentNode().get() + "\n");
					//throw new UnknownSignalerPatternException("Sinalizado um NameExpr que não está no topo da pilha.", statement);
				}
			}
			// O statement é um "throw new...". Temos que ver de que tipo, se é um novo fluxo ou se é um wrapping.
			else if ( exp instanceof ObjectCreationExpr )
			{
				ObjectCreationExpr objectCreationExp = (ObjectCreationExpr) exp;
				NodeList<Expression> argumentList = objectCreationExp.getArguments();
				boolean wrapping = false;
				
				// Verificar se a exceção capturada no contexto deste catch é um dos argumentos da instanciação
				for ( Iterator<Expression> argumentIte = argumentList.iterator() ; argumentIte.hasNext() ; )
				{
					Expression argument = argumentIte.next();
					
					if ( argument instanceof NameExpr )
					{
						String argumentName = ((NameExpr) argument).getNameAsString();
						String peekExceptionName = this.exceptionNamesStack.peek().asString();
						
						if ( argumentName.equals(peekExceptionName) )
						{
							wrapping = true;
						}
					}
				}
				// New throw dentro de catch (má prática)
				if ( !wrapping )
				{
					handler.addHandlerAction(throwStatement, HandlerActionType.THROW_NEW);
					
					model.addSignaler(throwStatement, thrownType, SignalerType.SIMPLE);
				}
				// Wrapping
				else
				{
					Type peekExceptionType = handlersStack.peek().getExceptions().get(0);
					
					// Wrap to same type
					if ( peekExceptionType.getQualifiedName().equals(thrownType.getQualifiedName()) )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_SAME);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap checked > checked
					else if ( peekExceptionType.getExceptionType() == ExceptionType.CHECKED_EXCEPTION && thrownType.getExceptionType() == ExceptionType.CHECKED_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_CHECKED_CHECKED);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap checked > unchecked
					else if ( peekExceptionType.getExceptionType() == ExceptionType.CHECKED_EXCEPTION && thrownType.getExceptionType() == ExceptionType.UNCHECKED_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_CHECKED_UNCHECKED);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap checked > error
					else if ( peekExceptionType.getExceptionType() == ExceptionType.CHECKED_EXCEPTION && thrownType.getExceptionType() == ExceptionType.ERROR_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_CHECKED_ERROR);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap unchecked > checked
					else if ( peekExceptionType.getExceptionType() == ExceptionType.UNCHECKED_EXCEPTION && thrownType.getExceptionType() == ExceptionType.CHECKED_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_UNCHECKED_CHECKED);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap unchecked > unchecked
					else if ( peekExceptionType.getExceptionType() == ExceptionType.UNCHECKED_EXCEPTION && thrownType.getExceptionType() == ExceptionType.UNCHECKED_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_UNCHECKED_UNCHECKED);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap unchecked > error
					else if ( peekExceptionType.getExceptionType() == ExceptionType.UNCHECKED_EXCEPTION && thrownType.getExceptionType() == ExceptionType.ERROR_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_UNCHECKED_ERROR);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap error > checked
					else if ( peekExceptionType.getExceptionType() == ExceptionType.ERROR_EXCEPTION && thrownType.getExceptionType() == ExceptionType.CHECKED_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_ERROR_CHECKED);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap error > unchecked
					else if ( peekExceptionType.getExceptionType() == ExceptionType.ERROR_EXCEPTION && thrownType.getExceptionType() == ExceptionType.UNCHECKED_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_ERROR_UNCHECKED);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					// Wrap error > error
					else if ( peekExceptionType.getExceptionType() == ExceptionType.ERROR_EXCEPTION && thrownType.getExceptionType() == ExceptionType.ERROR_EXCEPTION )
					{
						handler.addHandlerAction(throwStatement, HandlerActionType.WRAP_ERROR_ERROR);
						model.addSignaler(throwStatement, thrownType, SignalerType.WRAPPING);
					}
					else
					{
						//Logger.getInstance().writeLog("Wrapping de tipo não identificado." + "\n" +
		   				//		   "Code:\n" + throwStatement + "\n");
					}
				}
			}
			else if (exp instanceof CastExpr)
			{
				CastExpr castExpression = (CastExpr)exp;
				
				handler.addHandlerAction(throwStatement, HandlerActionType.RETHROW);
				model.addSignaler(throwStatement, thrownType, SignalerType.RETHROW);
			}
			else
			{
				//Logger.getInstance().writeError("O statement é um throw, mas não é 'throw e', 'throw new...' ou 'throw (type) e'." + "\n" +
					//	   "Code:\n" + throwStatement.getParentNode().get() + "\n");
			}
		}
		
		super.visit(throwStatement,facade);
	}
	
	public void visit (CatchClause catchClause, JavaParserFacade facade)
	{		
		// Retrieve Exception Type
		if ( catchClause.getParameter().getType() instanceof UnionType )
		{
			//Logger.getInstance().writeLog(""+this.getClass().getCanonicalName()+"#public void visit (CatchClause, JavaParserFacade):" + "Multicatch não suportado\n");
		}
		else if ( catchClause.getParameter().getType() instanceof ClassOrInterfaceType )
		{
			// Retrieve Exception Type
			ReferenceType referenceType;
			try
			{
				referenceType = (ReferenceType) facade.getType(catchClause.getParameter());
			}
			catch (UnsupportedOperationException e)
			{
				//Logger.getInstance().writeError("Catched UnsupportedOperationException when trying to resolve this catch clause:\n" + catchClause.getParentNode().get().toString()+ "\n");
				return;
			}
			
			
			
			// Add handler to model
			Type type = model.findOrCreate ( null, referenceType );
			List<Type> types = new ArrayList<Type>();
			types.add(type);
			
			
			Handler handler = model.addHandler(catchClause, types);
			
			// Push exception name e handler
			SimpleName exceptionName = catchClause.getParameter().getName();
			exceptionNamesStack.add(exceptionName);
			handlersStack.add(handler);		
			
			// Visit children
			this.processHandler(catchClause);
			super.visit(catchClause,facade);

			// Pop exception name e handler
			exceptionNamesStack.pop();
			handlersStack.pop();
		}
	}

	public void visit ( ReturnStmt returnStatement , JavaParserFacade facade )
	{
		// Queremos somente os nodes dentro de catchs
		if ( !this.handlersStack.isEmpty() )
		{
			Handler handler = handlersStack.peek();
			
			// return algo
			if ( returnStatement.getExpression().isPresent() )
			{
				Expression returnExpression = returnStatement.getExpression().get();
				
				// return null
				if ( returnExpression instanceof NullLiteralExpr )
				{
					handler.addHandlerAction(returnStatement, HandlerActionType.RETURN_NULL);
				}
				// return non-null
				else
				{
					handler.addHandlerAction(returnStatement, HandlerActionType.RETURN_VALUE);
				}
			}
			// return vazio
			else
			{
				handler.addHandlerAction(returnStatement, HandlerActionType.RETURN_EMPTY);
			}
		}
		
		super.visit(returnStatement, facade);
	}
	
	public void visit ( ContinueStmt continueStatement , JavaParserFacade facade )
	{
		// Queremos somente os nodes dentro de catchs
		if ( !this.handlersStack.isEmpty() )
		{
			Handler handler = handlersStack.peek();
			
			handler.addHandlerAction(continueStatement, HandlerActionType.CONTINUE);
		}
		
		super.visit(continueStatement, facade);
	}
	
	public void visit ( BreakStmt breakStatement , JavaParserFacade facade )
	{
		// Queremos somente os nodes dentro de catchs
		if ( !this.handlersStack.isEmpty() )
		{
			Handler handler = handlersStack.peek();
			
			handler.addHandlerAction(breakStatement, HandlerActionType.BREAK);
		}
		
		super.visit(breakStatement, facade);
	}
	
	public void visit ( ExpressionStmt expressionStatement , JavaParserFacade facade )
	{
		// Queremos somente os nodes dentro de catchs
		if ( !this.handlersStack.isEmpty() )
		{
			Handler handler = handlersStack.peek();
			
			Expression expression = expressionStatement.getExpression();
			
			if ( expression instanceof MethodCallExpr )
			{
				handler.addHandlerAction(expression, HandlerActionResolver.resolve(expression));
			}
			else if (expression instanceof VariableDeclarationExpr)
			{
				handler.addHandlerAction(expression, HandlerActionResolver.resolve(expression));
			}
			else if (expression instanceof AssignExpr)
			{
				handler.addHandlerAction(expression, HandlerActionResolver.resolve(expression));
			}
			else
			{
				handler.addHandlerAction(expression, HandlerActionType.UNKNOWN);
				//Logger.getInstance().writeLog("Um ExpressionStmt que não é MethodCallExpr || VariableDeclarationExpr || AssignExpr --> " + expression.getClass().getSimpleName() + "\n");
			}
		}
		
		super.visit(expressionStatement, facade);
	}	
	
	private void processHandler (CatchClause catchClause)
	{
		//Logger logger = Logger.getInstance();
		
		for ( Iterator<Statement> i = catchClause.getBody().getStatements().iterator() ; i.hasNext() ; )
		{
			Statement statement = i.next();
			
			if ( !(statement instanceof ExpressionStmt) && 
				 !(statement instanceof ThrowStmt) &&
				 !(statement instanceof IfStmt) &&
				 !(statement instanceof ContinueStmt) &&
				 !(statement instanceof BreakStmt) &&
				 !(statement instanceof TryStmt) &&
				 !(statement instanceof ForeachStmt) &&
				 !(statement instanceof ReturnStmt))
			{
				//logger.writeLog("## " + statement.getClass().getCanonicalName() + " >>> " + statement + "\n");
			}
			
		}
	}
}
