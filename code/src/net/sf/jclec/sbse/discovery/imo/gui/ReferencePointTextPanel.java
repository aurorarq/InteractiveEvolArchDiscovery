package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ReferencePointTextPanel {

	JPanel panel;
	ButtonGroup group;
	JTextField weightsText;
	JTextField pointText;
	
	public ReferencePointTextPanel(int nObjectives){
		group = new ButtonGroup();
		panel = new JPanel();
		
		// The default value for both text fields
		String wText = "";
		String pText = "";
		for(int i=0; i<nObjectives-1; i++){
			wText += "1.0,";
			pText += "0.0,";
		}
		wText += "1.0";
		pText += "0.0";
		
		// Range for the measure
		JLabel weightsLabel = new JLabel("Weights");
		weightsText = new JTextField(wText);
		JLabel pointLabel = new JLabel("Aspiration Levels");
		pointText = new JTextField(pText);
		panel.add(weightsLabel);
		panel.add(weightsText);
		panel.add(pointLabel);
		panel.add(pointText);
		panel.setMaximumSize(new Dimension(400,30));
	}
	
	public JComponent getComponent(){
		return panel;
	}
	
	public ButtonGroup getButtonGroup(){
		return group;
	}
	
	public JTextField getWeightsTextField(){
		return weightsText;
	}
	
	public JTextField getPointTextField(){
		return pointText;
	}
}
