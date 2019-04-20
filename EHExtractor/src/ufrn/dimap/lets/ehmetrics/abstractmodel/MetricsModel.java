package ufrn.dimap.lets.ehmetrics.abstractmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import ufrn.dimap.lets.ehmetrics.analyzer.Util;

public class MetricsModel
{
	private TypeHierarchy typeHierarchy;
	private List<Signaler> signalers;
	private List<Handler> handlers;
	
	public int totalCatches;
	public int emptyHandlers;
	public int genericCatch;

	public int totalThrows;
	public int genericThrows;
	
	public MetricsModel ()
	{	
		signalers = new ArrayList<> ();
		handlers = new ArrayList<> ();
		typeHierarchy = new TypeHierarchy();
	}
	
	
	
	public Signaler addSignaler(String filePath, ThrowStmt throwNode, Type thrownType, Type catchedType, SignalerType type)
	{
		Signaler signaler = new Signaler (filePath, throwNode, thrownType, catchedType, type);
		
		this.signalers.add(signaler);
		
		return signaler;
	}
	
	public Handler addHandler(String filePath, CatchClause catchNode, List<Type> types)
	{
		Handler handler = new Handler (filePath, catchNode, types);
		
		this.handlers.add(handler);
		
		return handler;
	}
	
	
	
	
	
	public List<Signaler> getSignalers()
	{
		return this.signalers;
	}

	public List<Handler> getHandlers()
	{
		return this.handlers;
	}
}
