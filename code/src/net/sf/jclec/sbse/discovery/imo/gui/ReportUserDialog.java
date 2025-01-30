package net.sf.jclec.sbse.discovery.imo.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.jclec.sbse.discovery.control.InteractionController;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.UserReportListener;

/**
 * A frame that show a dialog to the user
 * @author Aurora Ramirez
 * */
public class ReportUserDialog {

	
	private JOptionPane dialog;
	private JFrame frame;
	
	private String message1 = "Now it is time to write a comment on the sheet. Please, click on the button to continue once "
			+ "you have FINISHED to fill it.";
	private String button1Msg = "I have FINISHED the report";
	private String message2 = "Now it is time to write some comments about the overall interaction on the sheet. Please, click on the button when you"
			+ " START to fill it";
	private String button2Msg = "I START to write the report";
	
	public ReportUserDialog(InteractionController controller, boolean isLastInteraction){
		
		String message, messageButton;
		if(isLastInteraction){
			message = message2;
			messageButton = button2Msg;
		}
		else{
			message = message1;
			messageButton = button1Msg;
		}
		JButton buttons [] = new JButton[1];
		buttons[0] = new JButton(messageButton);
		buttons[0].addActionListener(new UserReportListener(controller));
		dialog = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, 1, null, buttons);
		
		frame = new JFrame("Information message");
		frame.add(dialog);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
	}
	
	public void show(){
		frame.setVisible(true);
	}
	
	public void close(){
		frame.setVisible(false);
		frame.dispose();
	}
	
	/*public static void main(String [] args){
		new ReportUserDialog();
	}*/
}
