package ufrn.dimap.lets.ehmetrics.visitor;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public abstract class SimpleVisitor extends VoidVisitorAdapter<Void> {

	public abstract String printHeader();
	public abstract String printOutput(); 
}
