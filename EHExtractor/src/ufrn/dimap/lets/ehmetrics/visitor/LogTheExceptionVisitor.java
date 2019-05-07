package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.javaparserutil.MethodCallParser;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerParser;
import ufrn.dimap.lets.ehmetrics.javaparserutil.SignalerType;

/**
 * Visitor para verificar o guideline "Log the exception".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * ???????????????????????????
 * */
public class LogTheExceptionVisitor extends GuidelineCheckerVisitor
{
	private List <CatchClause> handlersWithResignalers;
	private List <CatchClause> handlersWithLog;
	private List <CatchClause> handlersWithoutLog;
	
	public LogTheExceptionVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
		
		this.handlersWithResignalers = new ArrayList<>();
		this.handlersWithLog = new ArrayList<>();
		this.handlersWithoutLog = new ArrayList<>();
	}

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		LogMethodVisitor logMethodVisitor = new LogMethodVisitor(catchClause);
		
		catchClause.accept(logMethodVisitor, arg);
		
		
		
		for ( LogMethodVisitor visitor : logMethodVisitor.getAllLogMethodVisitors() )
		{
			if ( visitor.hasResignaler() )
			{
				this.handlersWithResignalers.add(visitor.getRootCatchClause());
			}
			else if ( visitor.hasLoggingAction() )
			{
				this.handlersWithLog.add( visitor.getRootCatchClause() );
			}
			else
			{
				this.handlersWithoutLog.add( visitor.getRootCatchClause() );
			}
		}
	}	
	
	/**
	 * Verifica se o projeto adota o guideline referenciado neste visitor.
	 * 
	 * Para entender as condições do guideline, ver Javadoc da classe
	 * */
	public void checkGuidelineConformance ()
	{
		int numberOfHhandlersWithResignalers = handlersWithResignalers.size();
		int numberOfHandlersWithLog = handlersWithLog.size();
		int numberOfHandlersWithoutLog = handlersWithoutLog.size();
		
		System.out.println("Number of handlers which resignal: " + numberOfHhandlersWithResignalers);
		
		System.out.println("Number of handlers which doesn't resignal and has loggin actions: " + numberOfHandlersWithLog);
		
		System.out.println("Number of handlers which doesn't resignal and doesn't has loggin actions: " + numberOfHandlersWithoutLog);
	}	
}

/**
 * Visit a CatchClause and search for log-related method calls. If a nested catch clause is found, a nested 
 * instance of this class is created to this new scope.
 * 
 * If the related catch clause resignal an exception, the logging actions doesn't matter, so the visitor can early finish.
 * */
class LogMethodVisitor extends VoidVisitorAdapter<Void>
{
	// Static stack to keep track of catches scopes
	private static Stack<CatchClause> catchesInContext = new Stack<>();

	private CatchClause rootCatchClause;
	private List<LogMethodVisitor> nestedLogMethodVisitors;
	private boolean hasLoggingAction;
	private boolean hasResignaler;
	
	public LogMethodVisitor (CatchClause rootCatchClause)
	{
		this.rootCatchClause = rootCatchClause;
		this.nestedLogMethodVisitors = new ArrayList<>();
		this.hasLoggingAction = false;
		this.hasResignaler = false;
	}

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		if ( !catchClause.equals(this.rootCatchClause) )
		{
			LogMethodVisitor newLogMethodVisitor = new LogMethodVisitor(catchClause);
			this.nestedLogMethodVisitors.add(newLogMethodVisitor);
			catchClause.accept(newLogMethodVisitor, arg);
			
			if ( newLogMethodVisitor.hasResignaler() )
			{
				this.hasResignaler = true;
			}
		}
		else
		{
			catchesInContext.push(catchClause);
			
			// VISIT CHILDREN
			super.visit(catchClause, arg);
			
			catchesInContext.pop();
		}
	}
	
	@Override
	public void visit ( MethodCallExpr methodCallExpression, Void arg )
	{
		MethodCallParser methodCallParser = new MethodCallParser (methodCallExpression);
		
		methodCallParser.parse();
		
		if ( methodCallParser.isPrintStackTrace() ||
			 methodCallParser.isJavaLogger() ||
			 methodCallParser.isPrintln() ||
			 methodCallParser.isGenericLog())
		{
			this.hasLoggingAction = true;
		}
		
		// VISIT CHILDREN
		super.visit(methodCallExpression, arg);
	}
	
	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{
		SignalerParser signalerParser = new SignalerParser(throwStatement, catchesInContext);
		signalerParser.parse();
		
		if ( signalerParser.getType() == SignalerType.RETHROW ||
			 signalerParser.getType() == SignalerType.WRAPPING ||
			 signalerParser.getType() == SignalerType.UNWRAPPING)
		{
			this.hasResignaler = true;
		}
		
		
		// VISIT CHILDREN
		super.visit(throwStatement, arg);
	}
	
	
	public CatchClause getRootCatchClause () {
		return this.rootCatchClause;
	}
	
	/**
	 * Returns all LogMethodVisitor, including this, in DFS order.
	 * */
	public List<LogMethodVisitor> getAllLogMethodVisitors()
	{
		List<LogMethodVisitor> result = new ArrayList<>();

		result.add(this);
		
		for ( LogMethodVisitor visitor : this.nestedLogMethodVisitors )
		{
			result.addAll(visitor.getAllLogMethodVisitors());
		}

		return result;
	}

	public boolean hasLoggingAction() {
		return hasLoggingAction;
	}
	
	public boolean hasResignaler() {
		return hasResignaler;
	}
}