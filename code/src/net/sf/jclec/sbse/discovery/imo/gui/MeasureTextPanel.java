package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class MeasureTextPanel {
	JPanel panel;
	ButtonGroup group;
	JTextField minText;
	JTextField maxText;
	
	public MeasureTextPanel(String [] metricNames, int selected){
		group = new ButtonGroup();
		panel = new JPanel();
		JRadioButton button;

		// List of metrics
		for(int i=0; i<metricNames.length; i++){
			button = new JRadioButton(metricNames[i]);
			if(i==selected)
				button.setSelected(true);
			else	
				button.setSelected(false);
			group.add(button);
			panel.add(button);
		}
		
		// Range for the measure
		JLabel minLabel = new JLabel("Minimum");
		minText = new JTextField("0.0");
		JLabel maxLabel = new JLabel("Maximum");
		maxText = new JTextField("1.0");
		panel.add(minLabel);
		panel.add(minText);
		panel.add(maxLabel);
		panel.add(maxText);
		panel.setMaximumSize(new Dimension(400,30));
	}
	
	public JComponent getComponent(){
		return panel;
	}
	
	public ButtonGroup getButtonGroup(){
		return group;
	}
	
	public JTextField getMinTextField(){
		return minText;
	}
	
	public JTextField getMaxTextField(){
		return maxText;
	}
}
