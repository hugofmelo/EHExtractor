package ufrn.dimap.lets.ehmetrics;

import java.util.HashMap;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;

import ufrn.dimap.lets.ehmetrics.abstractmodel.HandlerActionType;

public class HandlerActionResolver
{
	private static HashMap<String, HandlerActionType> actions = new HashMap<String, HandlerActionType>();
	
	public static HandlerActionType resolve (Expression expression)
	{
		String actionKey = null;
		HandlerActionType actionType = null;

		// Get action key
		actionKey = getActionKey (expression);
		if ( actionKey != null && actions.containsKey(actionKey))
		{
			return actions.get(actionKey);
		}
		
		// Resolving and saving the action type
		if ( expression instanceof MethodCallExpr )
		{
			MethodCallExpr methodCall = (MethodCallExpr) expression;
			
			actionType = resolve(methodCall);
			
			if ( actionType == null )
				actionType = ask(methodCall);
			
			actions.put(actionKey, actionType);
			
			return actionType;
		}
		else if ( expression instanceof VariableDeclarationExpr )
		{
			VariableDeclarationExpr variableDeclaration = (VariableDeclarationExpr) expression;
			
			if ( variableDeclaration.getVariable(0).getInitializer().isPresent() )
			{
				return resolve(variableDeclaration.getVariable(0).getInitializer().get());
			}
			else
			{
				return HandlerActionType.IRRELEVANT;
			}
		}
		else if ( expression instanceof AssignExpr )
		{
			AssignExpr assign = (AssignExpr) expression;
			
			return resolve(assign.getValue());
		}
		else if ( expression instanceof BinaryExpr )
		{
			//actionType = ask(expression.getParentNode().get());
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof StringLiteralExpr )
		{
			//actionType = ask(expression.getParentNode().get());
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof IntegerLiteralExpr )
		{
			//actionType = ask(expression.getParentNode().get());
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof ObjectCreationExpr )
		{
			//actionType = ask(expression.getParentNode().get());
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof ConditionalExpr )
		{
			//ConditionalExpr conditionExpr = (ConditionalExpr) expression;
			
			//conditionExpr.getThenExpr();
			//actionType = ask(expression.getParentNode().get());
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof NullLiteralExpr )
		{
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof CastExpr )
		{
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof BooleanLiteralExpr )
		{
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof NameExpr )
		{
			return HandlerActionType.IRRELEVANT;
		}
		else if ( expression instanceof FieldAccessExpr )
		{
			return HandlerActionType.IRRELEVANT;
		}
		else
		{
			//Logger.getInstance().writeLog("Um Expression que não é MethodCallExpr || VariableDeclarationExpr || BinaryExpr || StringLiteralExpr || IntegerLiteralExpr || ObjectCreationExpr || ConditionalExpr || FieldAccessExpr --> " + expression.getClass().getSimpleName() + "\n");
			return HandlerActionType.UNKNOWN;
		}
	}
	
	private static String getActionKey (Expression expression)
	{
		String actionKey = null;
		
		if (expression instanceof MethodCallExpr)
		{
			actionKey = getCompleteMethodName((MethodCallExpr) expression);
		}
		
		return actionKey;
	}
	
	private static HandlerActionType resolve(MethodCallExpr methodCallExpression)
	{
		String methodName = methodCallExpression.getNameAsString();
		
		// É possível usar o nome da classe, o nome do método e os argumentos para inferir o tipo de ação. Por simplicidade vou só usar o nome do método
		// Não podendo inferir a ação de tratamento, é exibido um popup com o código try-catch e com as opções para o usuário selecionar.
		if (methodName.equals("printStackTrace"))
		{
			return HandlerActionType.PRINTSTACKTRACE;
		}
		else if (methodName.equals("println"))
		{
			Expression scopeExp = methodCallExpression.getScope().get();
			
			if ( scopeExp != null && scopeExp instanceof FieldAccessExpr )
			{
				scopeExp = ((FieldAccessExpr) scopeExp).getScope();
				
				if ( scopeExp instanceof NameExpr)
				{
					if ( ((NameExpr)scopeExp).getName().asString().equals("System") )
					{
						return HandlerActionType.SYSTEM_PRINT;
					}
				}
			}
		}
		else if (methodName.equals("close"))
		{
			return HandlerActionType.CLOSE_RESOURCE;
		}
		else if (methodName.equals("exit"))
		{
			Expression scopeExp = methodCallExpression.getScope().get();
			
			if ( scopeExp != null && scopeExp instanceof NameExpr && ((NameExpr)scopeExp).getNameAsString().equals("System") )
			{
				return HandlerActionType.TERMINATE;
			}	
		}
		
		return null;
	}
	
	private static HandlerActionType ask(Node expression)
	{
		if ( ProjectsUtil.manualHandlerActionMode )
		{
			ActionsPane actionsPane = new ActionsPane(expression.toString());
			HandlerActionType actionType = actionsPane.getActionType(); 
		
			return actionType;
		}
		else
		{
			return HandlerActionType.UNKNOWN;
		}
	}
	/*
	private static HandlerActionType automaticResolveHandlingAction(VariableDeclarationExpr variableDeclarationExpression)
	{
		variableDeclarationExpression.get
		
		String methodName = methodCallExpression.getNameAsString();
		
		// É possível usar o nome da classe, o nome do método e os argumentos para inferir o tipo de ação. Por simplicidade vou só usar o nome do método
		// Não podendo inferir a ação de tratamento, é exibido um popup com o código try-catch e com as opções para o usuário selecionar.
		if (methodName.equals("printStackTrace"))
		{
			return HandlerActionType.PRINTSTACKTRACE;
		}
		else if (methodName.equals("println"))
		{
			Expression scopeExp = methodCallExpression.getScope().get();
			
			if ( scopeExp != null && scopeExp instanceof FieldAccessExpr )
			{
				scopeExp = ((FieldAccessExpr) scopeExp).getScope();
				
				if ( scopeExp instanceof NameExpr)
				{
					if ( ((NameExpr)scopeExp).getName().asString().equals("System") )
					{
						return HandlerActionType.SYSTEM_PRINT;
					}
				}
			}
		}
		else if (methodName.equals("close"))
		{
			return HandlerActionType.CLOSE_RESOURCE;
		}
		else if (methodName.equals("exit"))
		{
			Expression scopeExp = methodCallExpression.getScope().get();
			
			if ( scopeExp != null && scopeExp instanceof NameExpr && ((NameExpr)scopeExp).getNameAsString().equals("System") )
			{
				return HandlerActionType.TERMINATE;
			}	
		}
		
		return null;
	}
	*/
	
	
	private static String getCompleteMethodName (MethodCallExpr expression)
	{
		return getCompleteMethodNameR(expression);
	}
	
	private static String getCompleteMethodNameR (Expression expression)
	{
		if ( expression == null )
		{
			return "";
		}
		else
		{
			if ( expression instanceof  MethodCallExpr)
			{
				MethodCallExpr castExpression = (MethodCallExpr)expression;
				if ( castExpression.getScope().isPresent() )
				{
					return getCompleteMethodNameR( castExpression.getScope().get() ) + castExpression.getNameAsString();
				}
				else
				{
					return castExpression.getNameAsString();
				}
			}
			else if ( expression instanceof FieldAccessExpr )
			{
				FieldAccessExpr castExpression = (FieldAccessExpr)expression;
				
				return getCompleteMethodNameR(castExpression.getScope()) + castExpression.getName() + ".";
			}	
			else if ( expression instanceof NameExpr)
			{
				NameExpr castExpression = (NameExpr)expression;
				
				return castExpression.getName().asString() + ".";
			}
			
			else
			{
				return null;
			}
		}
	}
}
