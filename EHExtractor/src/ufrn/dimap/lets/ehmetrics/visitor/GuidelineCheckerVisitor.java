package ufrn.dimap.lets.ehmetrics.visitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeHierarchy;
import ufrn.dimap.lets.ehmetrics.javaparserutil.JavaParserUtil;

/**
 * Generic visitor to parse exceptional types, signalers and handlers.
 * */
public abstract class GuidelineCheckerVisitor extends VoidVisitorAdapter<Void> implements GuidelineMetrics
{
	protected File javaFile; // Java file being parsed
	protected boolean allowUnresolved;
	
	protected TypeHierarchy typeHierarchy;
	protected List<Signaler> signalersOfProject;
	protected List<Handler> handlersOfProject;

	public GuidelineCheckerVisitor (boolean allowUnresolved)
	{
		this.allowUnresolved = allowUnresolved;
		this.clear();
	}

	/**
	 * Auxiliar method to process the a ClassOrInterfaceDeclaration and create a new type in the hierarchy
	 * if its a class (not a interface).
	 * 
	 * @return a type if the input declaration is a class declaration, or an empty optional otherwise
	 * */
	protected Optional<Type> createTypeFromClassDeclaration(ClassOrInterfaceDeclaration classOrInterfaceDeclaration)
	{
		ResolvedReferenceTypeDeclaration referenceTypeDeclaration = classOrInterfaceDeclaration.resolve();

		if ( referenceTypeDeclaration.isClass() )
		{	
			Type type = typeHierarchy.findOrCreateResolvedType(referenceTypeDeclaration.asClass());
			type.setFile(javaFile);
			type.setNode(classOrInterfaceDeclaration);
			
			return Optional.of(type);
		}
		else
		{
			return Optional.empty();
		}
	}
	
	/**
	 * Auxiliar method to process a CatchClause, adding the caught types in the hierarchy and creating a new Handler in the visitor.
	 * */
	protected Handler createHandler(CatchClause catchClause)
	{
		Handler newHandler = new Handler();
		newHandler.setFile(javaFile);
		newHandler.setNode(catchClause);


		List<Type> types = JavaParserUtil.getHandledTypes(catchClause).stream()
				.map(typeHierarchy::findOrCreateType)
				.collect (Collectors.toList());

		// Update handler references 
		types.stream()
		.forEach(t -> {
			newHandler.getExceptions().add(t);
			t.addHandler(newHandler);
		});

		/* If allowUnresolved is true, some types, created from its declaration but with unresolved 
		 * ancestors end up having the ClassType.UNRESOLVED. After being used in a catch clause, it is known
		 * they are exceptions.
		 */
		if ( allowUnresolved )
		{
			types.stream()
			.filter(t -> t.getClassType() == ClassType.UNRESOLVED)
			.forEach(t -> t.setClassType(ClassType.UNRESOLVED_EXCEPTION));
		}

		this.handlersOfProject.add(newHandler);

		return newHandler;
	}

	/**
	 * Auxiliar method to process the ThrowStatement, adding the thrown type in the hierarchy and creating a new Signaler in the model.
	 * */
	protected Signaler createSignaler(ThrowStmt throwStatement)
	{
		Signaler newSignaler = new Signaler();
		newSignaler.setFile(javaFile);
		newSignaler.setNode(throwStatement);

		Optional <ResolvedClassDeclaration> classDeclaration = JavaParserUtil.getThrownClassDeclaration(throwStatement);

		List<Type> thrownTypes = new ArrayList<>();
		if (classDeclaration.isPresent())
		{
			thrownTypes.add(typeHierarchy.findOrCreateResolvedType(classDeclaration.get()));
		}
		else
		{
			for ( ClassOrInterfaceType classOrInterfaceType : JavaParserUtil.getThrownClassOrInterfaceTypes(throwStatement) )
			{
				thrownTypes.add(typeHierarchy.findOrCreateType(classOrInterfaceType));
			}
		}

		/* If this type already exists in the hierarchy, and it was create by its declaration, and it
		 * dont had they ancestors resolved, it has the ClassType.UNRESOLVED. For being used
		 * in a throw or catch, we know this type is a UNRESOLVED_EXCEPTION.
		 */
		if (allowUnresolved)
		{
			thrownTypes.stream()
				.filter(type -> type.getClassType() == ClassType.UNRESOLVED)
				.forEach(type -> type.setClassType(ClassType.UNRESOLVED_EXCEPTION)); 
		}
		
		// Update signaler references 
		newSignaler.setThrownTypes( thrownTypes );
		thrownTypes.forEach( type -> type.addSignaler(newSignaler));
		
		this.signalersOfProject.add(newSignaler);
		
		return newSignaler;
	}

	protected void clear ()
	{
		this.typeHierarchy = new TypeHierarchy(allowUnresolved);
		this.signalersOfProject = new ArrayList<>();
		this.handlersOfProject = new ArrayList<>();
	}
	
	// GETTERS AND SETTERS **************************************
	
	public TypeHierarchy getTypeHierarchy()
	{
		return typeHierarchy;
	}
	
	public void setJavaFile (File javaFile)
	{
		this.javaFile = javaFile;
	}
}
