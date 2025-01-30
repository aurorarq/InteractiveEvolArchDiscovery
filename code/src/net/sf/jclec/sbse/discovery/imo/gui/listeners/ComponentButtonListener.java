package net.sf.jclec.sbse.discovery.imo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

import javax.swing.JRadioButton;

import net.sf.jclec.sbse.discovery.control.InteractionController;

public class ComponentButtonListener extends Observable implements ActionListener {

	InteractionController observer;
	int index;
	
	public ComponentButtonListener(InteractionController controller, int index){
		//addObserver(controller);
		this.observer = controller;
		this.index = index;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		boolean selected = ((JRadioButton)arg0.getSource()).isSelected();
		if(selected)
			//notifyObservers(index);
			observer.update(this, index);
	}
}
