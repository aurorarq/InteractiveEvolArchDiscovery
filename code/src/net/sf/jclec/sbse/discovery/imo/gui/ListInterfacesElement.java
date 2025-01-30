package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ListInterfacesElement {

	JList<String> list;
	ButtonGroup group;
	JPanel panel;

	public ListInterfacesElement(int nComponents, int [] nInterfaces){
		group = new ButtonGroup();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JRadioButton button;

		for(int i=1; i<=nComponents; i++){
			for(int j=1; j<=nInterfaces[i-1]; j++){
				button = new JRadioButton("Component " + i + " - Interface " + j);
				button.setSelected(false);
				group.add(button);
				panel.add(button);
			}
		}
		((JRadioButton)group.getElements().nextElement()).setSelected(true); // Set the first button as selected
		panel.setMaximumSize(new Dimension(400,30));
	}

	public JComponent getComponent(){
		return panel;
	}

	public ButtonGroup getButtonGroup(){
		return group;
	}

}
