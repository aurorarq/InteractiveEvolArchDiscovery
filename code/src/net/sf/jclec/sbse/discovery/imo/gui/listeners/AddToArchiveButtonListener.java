package net.sf.jclec.sbse.discovery.imo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

import javax.swing.JRadioButton;

import net.sf.jclec.sbse.discovery.control.InteractionController;

public class AddToArchiveButtonListener extends Observable implements ActionListener {

	InteractionController observer;
	
	public AddToArchiveButtonListener(InteractionController controller){
		this.observer = controller;
		//addObserver(controller);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.observer.update(this, ((JRadioButton)arg0.getSource()).isSelected());
	}
}