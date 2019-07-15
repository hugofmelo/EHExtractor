package ufrn.dimap.lets.ehmetrics.visitor.guideline;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.google.common.base.Predicate;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeOrigin;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;
import ufrn.dimap.lets.ehmetrics.visitor.GuidelineCheckerVisitor;

/**
 * Visitor para verificar o guideline "Define a super type".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todas as exceções definidas pela aplicação possuem um mesmo supertipo
 * */
public class DefineSuperTypeVisitor extends GuidelineCheckerVisitor
{
	public DefineSuperTypeVisitor (boolean allowUnresolved)
	{
		super(allowUnresolved);
	}

	@Override
	public void visit (ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Void arg)
	{		
		this.createTypeFromClassDeclaration(classOrInterfaceDeclaration);

		//VISIT CHILDREN
		super.visit(classOrInterfaceDeclaration, arg);
	}

	@Override
	public void visit (CatchClause catchClause, Void arg)
	{		
		this.createHandler(catchClause);

		// VISIT CHILDREN
		super.visit(catchClause, arg);
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		this.createSignaler(throwStatement);
		
		// VISIT CHILDREN
		super.visit(throwStatement, arg);
	}

	/**
	 * Returns the guideline columns names
	 * */
	@Override
	public String getGuidelineHeader ()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("# project exceptions");
		builder.append("\t");
		builder.append("# subtypes of most subtyped project exception");
		builder.append("\t");
		
		return builder.toString();
	}
	
	/**
	 * Returns the guideline data
	 * */
	@Override
	public String getGuidelineData ()
	{	
		long numberOfSystemExceptionTypes = this.typeHierarchy.listTypes().stream()
				.filter(Type::isSystemExceptionType)
				.count();
		
		Optional<Long> mostSubtypedSystemException = this.typeHierarchy.listTypes().stream()
			.filter(Type::isSystemExceptionType)
			.map(Type::getSystemExceptionRootType)
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
			.values()
			.stream()
			.max(Comparator.naturalOrder());
		
		StringBuilder builder = new StringBuilder();
		
		if ( numberOfSystemExceptionTypes != 0 && mostSubtypedSystemException.isPresent() )
		{
			builder.append(numberOfSystemExceptionTypes);
			builder.append("\t");
			builder.append(mostSubtypedSystemException.get());
			builder.append("\t");
		}
		else
		{
			builder.append(numberOfSystemExceptionTypes);
			builder.append("\t");
			builder.append("0");
			builder.append("\t");
		}	
		
		return builder.toString();
	}
}