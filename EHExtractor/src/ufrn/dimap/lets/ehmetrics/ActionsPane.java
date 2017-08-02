package ufrn.dimap.lets.ehmetrics;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ufrn.dimap.lets.ehmetrics.abstractmodel.HandlerActionType;

public class ActionsPane extends JFrame implements ActionListener
{
	private boolean waitingOption;
	private HandlerActionType selectedAction;
	
	public ActionsPane (String codeSnippet)
	{
		initComponents(codeSnippet);
	}
	
	private void initComponents(String completeMethodName)
	{
		// Criar popup
		JPanel panel = new JPanel();
		JLabel label = new JLabel(completeMethodName, SwingConstants.CENTER);
		label.setFont(new Font("Serif", Font.BOLD, 30));
		panel.add(label);
		this.setSize(1000, 800);
		this.setLocationRelativeTo(null);
		panel.setLayout(new GridLayout(0,1));
		for (HandlerActionType actionType : HandlerActionType.values())
		{
			JButton button = new JButton(actionType.name());
			button.addActionListener(this);
			panel.add(button);
		}
		
		this.add(panel);
	}

	public HandlerActionType getActionType ()
	{
		this.waitingOption = true;
		this.selectedAction = null;
		
		this.setVisible(true);
		
		while (waitingOption)
		{
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("A");
		}
		
		this.setVisible(false);
		this.dispose();
		return this.selectedAction;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		this.waitingOption = false;
		this.selectedAction = HandlerActionType.valueOf(((JButton)e.getSource()).getText());
	}
}
