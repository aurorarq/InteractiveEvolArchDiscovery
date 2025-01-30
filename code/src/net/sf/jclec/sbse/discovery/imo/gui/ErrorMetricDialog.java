package net.sf.jclec.sbse.discovery.imo.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.jclec.sbse.discovery.control.InteractionController;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.UserErrorMetricListener;

public class ErrorMetricDialog {

	JOptionPane dialog;
	JFrame frame;
	
	public ErrorMetricDialog(InteractionController controller){
		String message = "Metric values should be between 0.0 and 1.0, and the minimum should be less or equal than the maximum.";
		JButton buttons [] = new JButton[1];
		buttons[0] = new JButton("Ok");
		buttons[0].addActionListener(new UserErrorMetricListener(controller));
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
