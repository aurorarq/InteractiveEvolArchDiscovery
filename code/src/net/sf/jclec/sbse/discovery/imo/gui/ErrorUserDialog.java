package net.sf.jclec.sbse.discovery.imo.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.jclec.sbse.discovery.control.InteractionController;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.UserErrorListener;

public class ErrorUserDialog {

	JOptionPane dialog;
	JFrame frame;
	
	public ErrorUserDialog(InteractionController controller){
		String message = "You must select a preference from the list.";
		JButton buttons [] = new JButton[1];
		buttons[0] = new JButton("Ok");
		buttons[0].addActionListener(new UserErrorListener(controller));
		dialog = new JOptionPane(message, JOptionPane.ERROR_MESSAGE, 1, null, buttons);
		
		frame = new JFrame("Error message");
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
	
}
