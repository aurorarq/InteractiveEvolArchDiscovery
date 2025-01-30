package net.sf.jclec.sbse.discovery.imo.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.jclec.sbse.discovery.control.InteractionController;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.UserWaitListener;

public class WaitUserDialog {

	JOptionPane dialog;
	JFrame frame;
	
	public WaitUserDialog(InteractionController controller){
		String message = "The next interaction is prepared. Click on the button to start it";
		JButton buttons [] = new JButton[1];
		buttons[0] = new JButton("I am ready");
		buttons[0].addActionListener(new UserWaitListener(controller));
		dialog = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, 1, null, buttons);
		
		frame = new JFrame("Interaction message");
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
