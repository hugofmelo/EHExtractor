package ufrn.dimap.lets.ehmetrics.abstractmodel;

import com.github.javaparser.ast.Node;

public class HandlerAction extends AbstractEHModelElement
{
	private HandlerActionType actionType;
	
	public HandlerAction(Node node, HandlerActionType actionType)
	{
		super(node);
		this.actionType = actionType;
	}
	
	public HandlerActionType getActionType ()
	{
		return this.actionType;
	}

	public String toString()
	{
		return actionType.toString();
	}
}
