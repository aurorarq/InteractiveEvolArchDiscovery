package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ListNumberComponentsElement {
	JList<String> list;
	ButtonGroup group;
	JPanel panel;
	
	public ListNumberComponentsElement(int min, int max, int selected){
		group = new ButtonGroup();
		panel = new JPanel();
		JRadioButton button;
		
		for(int i=min; i<=max; i++){
			button = new JRadioButton(i + " components");
			if(i==(selected))
				button.setSelected(true);
			else	
				button.setSelected(false);
			group.add(button);
			panel.add(button);
		}
		panel.setMaximumSize(new Dimension(400,30));
	}
	
	public JComponent getComponent(){
		return panel;
	}
	
	public ButtonGroup getButtonGroup(){
		return group;
	}
}
