package ufrn.dimap.lets.ehmetrics.visitor;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;

import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.logger.LoggerFacade;

/**
 * Visitor para verificar o guideline "Define a super type".
 * 
 * Para confirmar o guideline a seguinte heurística é usada:
 * 95% de todas as exceções definidas pela aplicação possuem um mesmo supertipo
 * */
public class DefineSuperTypeVisitor extends GuidelineCheckerVisitor
{
	private static final Logger GUIDELINE_LOGGER = LoggerFacade.getGuidelinesLogger(DefineSuperTypeVisitor.class);
	
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
	 * Verifica se o projeto adota o guideline referenciado neste visitor.
	 * 
	 * Para entender as condições do guideline, ver Javadoc da classe
	 * */
	@Override
	public void checkGuidelineConformance ()
	{	
		long numberOfSystemExceptionTypes = this.typeHierarchy.listTypes().stream()
				.filter(Type::isSystemExceptionType)
				.count();
		GUIDELINE_LOGGER.info("Number of custom exceptions: " + numberOfSystemExceptionTypes);
		
		
		Optional<Long> mostSubtypedSystemException = this.typeHierarchy.listTypes().stream()
			.filter(Type::isSystemExceptionType)
			.map(Type::getSystemExceptionRootType)
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
			.values()
			.stream()
			.max(Comparator.naturalOrder());
		
		
		if ( numberOfSystemExceptionTypes != 0 && mostSubtypedSystemException.isPresent() )
		{
			GUIDELINE_LOGGER.info("Number of subtypes of the most subtyped system exception: " + mostSubtypedSystemException.get());
			
			GUIDELINE_LOGGER.info("'Define a super type' conformance: " + 1.0*mostSubtypedSystemException.get()/numberOfSystemExceptionTypes);
		}
		else
		{
			GUIDELINE_LOGGER.info("None of the system exceptions has subtypes");
			
			GUIDELINE_LOGGER.info("'Define a super type' conformance: 0.0");
		}	
	}
}