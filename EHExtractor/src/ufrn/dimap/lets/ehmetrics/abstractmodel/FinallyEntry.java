package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.ast.Node;

public class FinallyEntry extends AbstractEHModelElement
{
	public FinallyEntry( Node node )
	{
		super (node);
		
		/*
		CompilationUnit compilationUnit = (CompilationUnit) node.getRoot();
		Block finallyBlock = node.getFinally();
		
		this.offset = finallyBlock.getStartPosition();
		this.length = finallyBlock.getLength();
		
		this.initLineNumber = compilationUnit.getLineNumber(this.offset);
		this.endLineNumber = compilationUnit.getLineNumber(this.length);
		this.LoCs = endLineNumber - initLineNumber + 1;
		*/
	}

}
