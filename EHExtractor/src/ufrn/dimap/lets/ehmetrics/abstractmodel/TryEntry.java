package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.ast.Node;

public class TryEntry extends AbstractEHModelElement
{
	public TryEntry( Node node )
	{
		super (node);
		
		//CompilationUnit compilationUnit = (CompilationUnit) node.getRoot();
		//Block tryBlock = node.getBody();
		/*
		this.startPosition = node.getStartPosition();
		this.length = tryBlock.getStartPosition() - this.startPosition + tryBlock.getLength();
		
		this.initLineNumber = compilationUnit.getLineNumber(this.startPosition);
		this.endLineNumber = compilationUnit.getLineNumber(this.startPosition + this.length);
		this.LoCs = endLineNumber - initLineNumber + 1;
		*/
	}

}