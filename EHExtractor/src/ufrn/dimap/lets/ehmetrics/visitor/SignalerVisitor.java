package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;

import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.SignalerType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.analyzer.UnknownSignalerException;

public class SignalerVisitor extends VoidVisitorAdapter<JavaParserFacade>
{
	private MetricsModel model;

	private Stack<String> handledNamesStack;
	private Stack<List<ReferenceType>> handledTypesStack;

	public SignalerVisitor (MetricsModel model)
	{			
		this.model = model;

		handledNamesStack = new Stack<String>();
		handledTypesStack = new Stack<List<ReferenceType>>();
	}

	public void visit (ThrowStmt throwStatement, JavaParserFacade facade)
	{	
		// PEGAR O REFERENCETYPE SINALIZADO
		Expression thrownExpression = throwStatement.getExpression();
		com.github.javaparser.symbolsolver.model.typesystem.Type type;
		try
		{
			type = facade.getType(thrownExpression);
		}
		catch (RuntimeException e)
		{			
			throw new UnknownSignalerException ("Tipo sinalizado não pôde ser resolvido. Sinalização: '" + throwStatement + "'.");
		}

		// É esperado que o tipo sinalizado seja um ReferenceType, mas já vi ocorrencia de TypeVariable, então vamos testar para garantir.
		if (type.isReferenceType() )
		{
			ReferenceType thrownReferenceType = (ReferenceType) type;
			
			// VERIFICAR TIPO DE THROW
			SignalerType signalerType = this.getSignalerType(thrownExpression);

			// CRIAR/PROCURAR TYPE NO MODELO
			Type thrownType = model.findOrCreate ( null, thrownReferenceType );

			// ADICIONAR SIGNALER NO MODELO
			model.addSignaler(throwStatement, thrownType, signalerType);

			super.visit(throwStatement,facade);
		}
		else
		{
			throw new UnknownSignalerException ("Sinalizado um tipo que não é uma classe.");	
			//super.visit(throwStatement,facade);

		}
	}

	public void visit (CatchClause catchClause, JavaParserFacade facade)
	{		
		List<ReferenceType> referenceTypes = Util.getHandledTypes(catchClause, facade);

		// PUSH TYPES TO HANDLERS STACK
		this.handledTypesStack.add (referenceTypes);
		this.handledNamesStack.add(catchClause.getParameter().getName().asString());

		// VISIT CHILDREN
		super.visit(catchClause,facade);

		// POP TYPES FROM STACK
		this.handledTypesStack.pop();
		this.handledNamesStack.pop();
	}

	private SignalerType getSignalerType (Expression thrownExpression)
	{
		if ( this.handledTypesStack.isEmpty() )
		{
			return SignalerType.SIMPLE;

			//throw new AnalyzerException("Falha no Analyzer. Exceção sinalizada não é ReferenceType. Investigar.");	
		}
		else // Rethrow ou wrapping
		{
			if ( thrownExpression instanceof NameExpr )
			// o statement é um "throw e". Provavelmente é um rethrow. Outros casos são improváveis, mas possíveis, e são relatados como erro.
			{
				String thrownExceptionName = ((NameExpr) thrownExpression).getNameAsString();

				// Rethrow simples
				if ( this.wasHandled(thrownExceptionName) )
				{
					return SignalerType.RETHROW;
				}
				else
				// é sinalizado um NameExpr, mas ele não está na pilha
				{
					throw new UnknownSignalerException ("Sinalizado um NameExpr, mas a exceção não está na pilha. Expressão sinalizada: '" + thrownExpression + "'.");
					//ErrorLogger.addError("Erro no SignalerVisitor. É sinalizado um NameExpr, mas a exceção não está na pilha.");
					//return SignalerType.UNKNOWN;
					
				}
			}
			else if ( thrownExpression instanceof ObjectCreationExpr )
				// O statement é um "throw new...". Temos que ver de que tipo, se é um novo fluxo ou se é um wrapping.
			{
				ObjectCreationExpr objectCreationExp = (ObjectCreationExpr) thrownExpression;

				if ( this.wasHandled(objectCreationExp.getArguments()) )
					// Houve wrapping da exceção. "throw new ... (..., e)"
				{
					return SignalerType.WRAPPING;
				}
				else
					// Sem wrapping, um novo fluxo é iniciado e o atual é suprimido.
				{
					return SignalerType.SIMPLE;
				}
			}
			else if (thrownExpression instanceof CastExpr)
			{
				CastExpr castExpression = (CastExpr)thrownExpression;
				
				// Verificar os casos: 'throw (NewTypeException) e;' e 'throw (newTypeException)e.getCause();'
				
				// O tipo target
				// castExpression.getType()
				
				// O casteado
				// castExpression.getExpression()
				
				throw new UnsupportedOperationException("Sinalização do tipo 'throw (Exception)e;'.");

				//return SignalerType.RETHROW;
			}
			else
			{
				throw new UnknownSignalerException ("A sinalização não é de um dos tipos suportados. Sinalização: '" + thrownExpression + "'.");
				//ErrorLogger.addError("Erro no SignalerVisitor. A sinalização não é do tipo 'throw e', 'throw new...' ou 'throw (type) e'");
				//return SignalerType.UNKNOWN;
				
			}
		}
	}

	// Verifica se determinada exceção (nome da instancia) está na pilha de handlers
	private boolean wasHandled (String exceptionName)
	{
		for ( String stackName : this.handledNamesStack )
		{
			if ( stackName.equals(exceptionName) )
			{
				return true;
			}
		}

		return false;
	}

	// Verifica se em uma lista de argumentos existe uma exceção (nome da instancia) que na pilha de handlers
	private boolean wasHandled (NodeList<Expression> arguments)
	{
		Iterator<Expression> argumentIte = arguments.iterator();

		while ( argumentIte.hasNext() )
		{
			Expression argument = argumentIte.next();

			if ( argument instanceof NameExpr )
			{
				String argumentName = ((NameExpr) argument).getNameAsString();

				if ( this.wasHandled(argumentName) )
				{
					return true;
				}
			}
		}

		return false;
	}

	/*
	private boolean wasHandledR (SimpleName exceptionName)
	{
		boolean wasHandled;

		if ( this.handledNamesStack.empty() )
		{
			wasHandled = false;
		}
		else
		{
			List<ReferenceType> topTypes = this.handledTypesStack.peek();
			SimpleName topName = this.handledNamesStack.peek();

			if ( topName == exceptionName )
			{
				wasHandled = true;
			}
			else
			{
				this.handledTypesStack.pop();
				this.handledNamesStack.pop();

				wasHandled = wasHandledR (exceptionName);

				this.handledTypesStack.push(topTypes);
				this.handledNamesStack.push(topName);
			}
		}


		return wasHandled;
	}
	 */
}