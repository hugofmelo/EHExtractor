package ufrn.dimap.lets.ehmetrics.visitor;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeHierarchy;

public class VisitorsUtil {

	/**
	 * Auxiliar method to process the ClassDeclaration and create a new type in the hierarchy.
	 * */
	public static void processClassDeclaration(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
			TypeHierarchy typeHierarchy, File javaFile)
	{
		ResolvedReferenceTypeDeclaration referenceTypeDeclaration = classOrInterfaceDeclaration.resolve();

		if ( referenceTypeDeclaration.isClass() )
		{	
			Type type = typeHierarchy.findOrCreateResolvedType(referenceTypeDeclaration.asClass());
			type.setFile(javaFile);
			type.setNode(classOrInterfaceDeclaration);
		}
	}

	/**
	 * Auxiliar method to process a CatchClause, adding the caught types in the hierarchy and creating a new Handler in the model.
	 * */
	public static void processCatchClause(CatchClause catchClause, TypeHierarchy typeHierarchy, Handler handler, File javaFile)
	{
		handler.setFile(javaFile);
		handler.setNode(catchClause);

		List<Type> types = JavaParserUtil.getHandledTypes(catchClause).stream()
			.map(typeHierarchy::findOrCreateType)
			.collect (Collectors.toList());
		
		
		/* Some types were created from its declaration, but they ancestors could not be found and
		 * they ended having the ClassType.UNRESOLVED. After being used in a catch clause, it is known
		 * they are exceptions.
		 */
		types.stream()
			.filter(t -> t.getClassType() == ClassType.UNRESOLVED)
			.forEach(t -> t.setClassType(ClassType.UNRESOLVED_EXCEPTION));
		
		// Update handlers refs 
		types.stream()
			.forEach(t -> {
				 handler.getExceptions().add(t);
				 t.addHandler(handler);
			});
	}

	/**
	 * Auxiliar method to process the ThrowStatement, adding the thrown type in the hierarchy and creating a new Signaler in the model.
	 * */
	public static void processThrowStatement(ThrowStmt throwStatement, TypeHierarchy typeHierarchy, Signaler signaler,
			File javaFile)
	{
		signaler.setFile(javaFile);
		signaler.setNode(throwStatement);
		
		Optional <ResolvedClassDeclaration> classDeclaration = JavaParserUtil.getThrownClassDeclaration(throwStatement);
		
		Type thrownType = null;
		if (classDeclaration.isPresent())
		{
			thrownType = typeHierarchy.findOrCreateResolvedType(classDeclaration.get());
		}
		else
		{
			thrownType = typeHierarchy.findOrCreateType(JavaParserUtil.getThrownClassOrInterfaceType(throwStatement));
		}

		/* If this type already exists in the hierarchy, and it was create by its declaration, and it
		 * dont had they ancestors resolved, it has the ClassType.UNRESOLVED. For being used
		 * in a throw or catch, we know this type is a UNRESOLVED_EXCEPTION.
		 */
		if ( thrownType.getClassType() == ClassType.UNRESOLVED )
		{
			thrownType.setClassType(ClassType.UNRESOLVED_EXCEPTION);
		}

		signaler.setThrownType( thrownType );
		thrownType.addSignaler(signaler);
	}

	/**
	 * Convenient method to "convert" Handlers to CatchClauses.
	 * */
	public static List<CatchClause> getCatchClausesFromHandlers( List<Handler> handlers )
	{
		return handlers.stream()
			.map(handler -> (CatchClause) handler.getNode())
			.collect(Collectors.toList());
	}
}
