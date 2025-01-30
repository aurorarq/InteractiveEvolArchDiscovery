package net.sf.jclec.sbse.discovery.imo.gui.listeners;

import java.util.Observable;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import net.sf.jclec.sbse.discovery.control.InteractionController;

public class TextFieldListener extends Observable implements DocumentListener {

	InteractionController observer;
	boolean isMinValue;
	
	public TextFieldListener(InteractionController controller, boolean isMinValue){
		this.observer = controller;
		this.isMinValue = isMinValue;
	}
	
	public boolean isMinValue(){
		return this.isMinValue;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// do nothing
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		try {
			this.observer.update(this,e.getDocument().getText(0, e.getDocument().getLength()));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		this.observer.update(this,null);
	}
}
