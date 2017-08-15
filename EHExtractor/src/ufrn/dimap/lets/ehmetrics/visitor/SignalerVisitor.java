package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ExceptionType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.MetricsModel;
import ufrn.dimap.lets.ehmetrics.abstractmodel.SignalerType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.analyzer.UnknownSignalerException;

public class SignalerVisitor extends VoidVisitorAdapter<JavaParserFacade>
{
	private MetricsModel model;

	private Stack<NamedHandler> handlersStack;

	public SignalerVisitor (MetricsModel model)
	{			
		this.model = model;

		handlersStack = new Stack<NamedHandler>();
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
			// CRIAR/PROCURAR TYPE NO MODELO
			Type thrownType = model.findOrCreate ( null, thrownReferenceType );
			
			// VERIFICAR TIPO DE THROW
			SignalerType signalerType = this.getSignalerType(thrownExpression, thrownType);

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
		List<Type> types = this.getHandledTypes(catchClause, facade);

		// PUSH TYPES TO HANDLERS STACK
		this.handlersStack.add (new NamedHandler (types, catchClause.getParameter().getName().asString()));

		// VISIT CHILDREN
		super.visit(catchClause,facade);

		// POP TYPES FROM STACK
		this.handlersStack.pop();
	}

	private SignalerType getSignalerType (Expression thrownExpression, Type thrownType)
	{
		if ( this.handlersStack.isEmpty() )
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
				Type rethrowType = this.wasHandled(thrownExceptionName);
				if ( rethrowType != null )
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

				Type wrappedType = this.wasHandled(objectCreationExp.getArguments()); 
				if ( wrappedType != null )
					// Houve wrapping da exceção. "throw new ... (..., e)"
				{
					if ( wrappedType.getType() == ExceptionType.CHECKED_EXCEPTION && thrownType.getType() == ExceptionType.CHECKED_EXCEPTION )
					{
						return SignalerType.WRAPPING_CHECKED_CHECKED;
					}
					else if ( wrappedType.getType() == ExceptionType.CHECKED_EXCEPTION && thrownType.getType() == ExceptionType.UNCHECKED_EXCEPTION )
					{
						return SignalerType.WRAPPING_CHECKED_UNCHECKED;
					}
					else if ( wrappedType.getType() == ExceptionType.CHECKED_EXCEPTION && thrownType.getType() == ExceptionType.ERROR_EXCEPTION)
					{
						return SignalerType.WRAPPING_CHECKED_ERROR;
					}
					else if ( wrappedType.getType() == ExceptionType.UNCHECKED_EXCEPTION && thrownType.getType() == ExceptionType.CHECKED_EXCEPTION )
					{
						return SignalerType.WRAPPING_UNCHECKED_CHECKED;
					}
					else if ( wrappedType.getType() == ExceptionType.UNCHECKED_EXCEPTION && thrownType.getType() == ExceptionType.UNCHECKED_EXCEPTION )
					{
						return SignalerType.WRAPPING_UNCHECKED_UNCHECKED;
					}
					else if ( wrappedType.getType() == ExceptionType.UNCHECKED_EXCEPTION && thrownType.getType() == ExceptionType.ERROR_EXCEPTION )
					{
						return SignalerType.WRAPPING_UNCHECKED_ERROR;
					}
					else if ( wrappedType.getType() == ExceptionType.ERROR_EXCEPTION && thrownType.getType() == ExceptionType.CHECKED_EXCEPTION )
					{
						return SignalerType.WRAPPING_ERROR_CHECKED;
					}
					else if ( wrappedType.getType() == ExceptionType.ERROR_EXCEPTION && thrownType.getType() == ExceptionType.UNCHECKED_EXCEPTION )
					{
						return SignalerType.WRAPPING_ERROR_UNCHECKED;
					}
					else if ( wrappedType.getType() == ExceptionType.ERROR_EXCEPTION && thrownType.getType() == ExceptionType.ERROR_EXCEPTION )
					{
						return SignalerType.WRAPPING_ERROR_ERROR;
					}
					else 
					{
						throw new UnknownSignalerException ("Um wrapping de tipo desconhecido. Sinalização: '" + thrownExpression + "'.");
					}
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

	private List<Type> getHandledTypes ( CatchClause catchClause, JavaParserFacade facade  )
	{
		// output 
		List<Type> types = new ArrayList<Type>();

		// CHECK CATCH TYPE
		// Solving "regular" catch clause
		if ( catchClause.getParameter().getType() instanceof ClassOrInterfaceType)
		{
			ReferenceType refType = (ReferenceType) facade.convertToUsage(catchClause.getParameter().getType());
			types.add(this.model.findOrCreate(null, refType));			
		}
		// Solving multicatch clause
		else if (catchClause.getParameter().getType() instanceof UnionType)
		{
			UnionType multiCatch = (UnionType) catchClause.getParameter().getType();
			Iterator<com.github.javaparser.ast.type.ReferenceType> i = multiCatch.getElements().iterator();
			while ( i.hasNext() )
			{
				ReferenceType refType = (ReferenceType) facade.convertToUsage(i.next());
				types.add(this.model.findOrCreate(null, refType));
			}
		}
		
		return types;
	}
	
	// Verifica se o nome de uma exceção (o nome da variável) está na pilha de handlers. Se estiver, retorna o tipo do handler associado àquele nome. Se não estiver, retorna null.
	private Type wasHandled (String exceptionName)
	{
		for ( NamedHandler handler : this.handlersStack )
		{
			if ( handler.getName().equals(exceptionName) )
			{
				return handler.getTypes().get(0);
			}
		}

		return null;
	}

	// Verifica se em uma lista de argumentos existe uma exceção (nome da instancia) que está na pilha de handlers
	private Type wasHandled (NodeList<Expression> arguments)
	{
		Iterator<Expression> argumentIte = arguments.iterator();

		while ( argumentIte.hasNext() )
		{
			Expression argument = argumentIte.next();

			if ( argument instanceof NameExpr )
			{
				String argumentName = ((NameExpr) argument).getNameAsString();

				Type t = this.wasHandled(argumentName); 
				if ( t != null )
				{
					return t;
				}
			}
		}

		return null;
	}
}