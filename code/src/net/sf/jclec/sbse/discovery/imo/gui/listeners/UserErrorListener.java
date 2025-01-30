package net.sf.jclec.sbse.discovery.imo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

import net.sf.jclec.sbse.discovery.control.InteractionController;

public class UserErrorListener extends Observable implements ActionListener {

	InteractionController observer;
	
	public UserErrorListener(InteractionController controller){
		this.observer = controller;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		this.observer.update(this, 0);
	}

}
