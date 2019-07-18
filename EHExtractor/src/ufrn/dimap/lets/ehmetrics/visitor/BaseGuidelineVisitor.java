package ufrn.dimap.lets.ehmetrics.visitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ObjectIdentityEqualsVisitor;
import com.github.javaparser.ast.visitor.ObjectIdentityHashCodeVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.utils.VisitorList;

import ufrn.dimap.lets.ehmetrics.abstractmodel.ClassType;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Handler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Signaler;
import ufrn.dimap.lets.ehmetrics.abstractmodel.Type;
import ufrn.dimap.lets.ehmetrics.abstractmodel.TypeHierarchy;
import ufrn.dimap.lets.ehmetrics.javaparserutil.JavaParserUtil;

/**
 * Generic visitor to parse exceptional types, signalers and handlers.
 * */
public class BaseGuidelineVisitor extends VoidVisitorAdapter<Void>
{
	protected File javaFile; // Java file being parsed
	protected boolean allowUnresolved;
	
	// Atributos que persistem por todo o projeto
	protected TypeHierarchy typeHierarchy;
	protected List<Signaler> signalersOfProject;
	protected List<Handler> handlersOfProject;
	
	
	/** Atributos que existem durante o processamento de cada arquivo .java. No final do processamento de cada arquivo, signalers e handlers s�o adicionados aos do projeto
	 * Essa solu��o foi necess�ria por 2 motivos: (1) permitir um sistema de rollback. Se houver uma falha durante a visita a um arquivo, nenhum signaler ou handler � adicionado. Por outro
	 * lado, tipos s�o adicionados � hierarquia. (2) permitir � procura de handlers pelo CatchClause e de signalers pelo ThrowStmt, que devido � implementa��o do JP, poderiam ser erroneamente
	 * considerados duplicatas em uma lista. Para lidar com isso, usamos uma lista especial do JP (VisitorList), o que impede o uso direto da lista de Handlers e Signalers.
	 * Isso tudo seria resolvido com a poss�vel implementa��o de VisitorListeners. No caso, os guidelines seriam listeners, e n�o visitors como agora. Eles escutariam cada m�todo do BaseVisitor
	 * antes ou depois da visita, e assim n�o precisariam revisitar os n�s, o que leva � necessidade de procurar os n�s na lista.
	 */
	private List<Signaler> signalersOfFile;
	private VisitorList<ThrowStmt> throwsOfFile; 

	private List<Handler> handlersOfFile;
	private VisitorList<CatchClause> catchesOfFile; 
	

	private Optional<Handler> handlerInScopeOptional;
	
	public BaseGuidelineVisitor (boolean allowUnresolved)
	{
		this.allowUnresolved = allowUnresolved;
		
		this.typeHierarchy = new TypeHierarchy(allowUnresolved);
		this.signalersOfProject = new ArrayList<>();
		this.handlersOfProject = new ArrayList<>();
		
		this.signalersOfFile = new ArrayList<>();
		this.throwsOfFile = new VisitorList<>(new ObjectIdentityHashCodeVisitor(), new ObjectIdentityEqualsVisitor());
		this.handlersOfFile = new ArrayList<>();
		this.catchesOfFile = new VisitorList<>(new ObjectIdentityHashCodeVisitor(), new ObjectIdentityEqualsVisitor());
		
		this.handlerInScopeOptional = Optional.empty();
	}

	@Override
	public void visit (CompilationUnit compilationUnit, Void arg)
	{		
		handlerInScopeOptional = Optional.empty();
		
		this.throwsOfFile.clear();
		this.signalersOfFile.clear();
		
		this.catchesOfFile.clear();
		this.handlersOfFile.clear();

		super.visit(compilationUnit, arg);
		
		this.signalersOfProject.addAll(this.signalersOfFile);
		this.handlersOfProject.addAll(this.handlersOfFile);
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
		Handler newHandler = this.createHandler(catchClause);
		this.catchesOfFile.add(catchClause);
		
		this.handlerInScopeOptional.ifPresent(handler ->
		{
			handler.getNestedHandlers().add(newHandler);
			newHandler.setParentHandler(handler);
		});

		this.handlerInScopeOptional = Optional.of(newHandler);


		// VISIT CHILDREN
		super.visit(catchClause, arg);

		this.handlerInScopeOptional = this.handlerInScopeOptional.get().getParentHandler();
	}

	@Override
	public void visit (ThrowStmt throwStatement, Void arg)
	{		
		try
		{
			Signaler newSignaler = this.createSignaler(throwStatement);
			this.throwsOfFile.add(throwStatement);
			
			// All handlers in context have this signaler as escaping exception
			if (handlerInScopeOptional.isPresent())
			{
				handlerInScopeOptional.get().getAllHandlersInContext()
						.forEach(handler -> handler.getEscapingSignalers().add(newSignaler));

				newSignaler.setRelatedHandler(handlerInScopeOptional);
			}
			
			// VISIT CHILDREN
			super.visit(throwStatement, arg);
		}
		catch (UnsupportedSignalerException e)
		{
			// Dont visit further
			// TODO log? Rethrow?
			return;
		}		
	}
	
	/**
	 * Auxiliar method to process the a ClassOrInterfaceDeclaration and create a new type in the hierarchy
	 * if its a class (not a interface).
	 * 
	 * @return a type if the input declaration is a class declaration, or an empty optional otherwise
	 * */
	private Optional<Type> createTypeFromClassDeclaration(ClassOrInterfaceDeclaration classOrInterfaceDeclaration)
	{
		ResolvedReferenceTypeDeclaration referenceTypeDeclaration = classOrInterfaceDeclaration.resolve();

		if ( referenceTypeDeclaration.isClass() )
		{	
			Type type = typeHierarchy.findOrCreateResolvedType(referenceTypeDeclaration.asClass());
			type.setFile(javaFile);
			//type.setNode(classOrInterfaceDeclaration);
			
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
	private Handler createHandler(CatchClause catchClause)
	{
		Handler newHandler = new Handler();
		newHandler.setFile(javaFile);
		newHandler.setVariableName(catchClause.getParameter().getNameAsString());
		//newHandler.setNode(catchClause);

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

		this.handlersOfFile.add(newHandler);

		return newHandler;
	}

	/**
	 * Auxiliar method to process the ThrowStatement, adding the thrown type in the hierarchy and creating a new Signaler in the model.
	 * */
	private Signaler createSignaler(ThrowStmt throwStatement)
	{
		Signaler newSignaler = new Signaler();
		newSignaler.setFile(javaFile);
		//newSignaler.setNode(throwStatement);

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
		
		this.signalersOfFile.add(newSignaler);
		
		return newSignaler;
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

	public Handler findHandler(CatchClause catchClause)
	{
		return this.handlersOfFile.get(this.catchesOfFile.indexOf(catchClause));
	}
	
	/**
	 * Retorna um signaler que j� deve ter sido parseado. Pode retornar Optional.empty caso o Signaler
	 * para este throwStatement n�o tenha sido adicionado, o que acontece quando ele � de um padr�o n�o
	 * suportado.
	 * */
	public Optional<Signaler> findSignaler(ThrowStmt throwStatement)
	{
		int index = this.throwsOfFile.indexOf(throwStatement);
		
		if (index != -1)
		{
			return Optional.of(this.signalersOfFile.get(index));
		}
		else
		{
			return Optional.empty();
		}
	}

	public List<Type> getTypes()
	{
		return this.typeHierarchy.listTypes();
	}
	
	public List<Handler> getHandlers()
	{
		return this.handlersOfProject;
	}
	
	public List<Signaler> getSignalers()
	{
		return this.signalersOfProject;
	}
}
