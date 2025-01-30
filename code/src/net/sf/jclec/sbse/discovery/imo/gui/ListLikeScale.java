package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ListLikeScale {

	JList<String> list;
	ButtonGroup group;
	JPanel panel;
	
	public ListLikeScale(){
		group = new ButtonGroup();
		panel = new JPanel();
		JRadioButton button;

		for(int i=1; i<=5; i++){
			button = new JRadioButton(""+i);
			if(i==3)
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
