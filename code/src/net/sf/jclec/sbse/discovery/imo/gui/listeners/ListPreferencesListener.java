package net.sf.jclec.sbse.discovery.imo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import javax.swing.JComboBox;
import net.sf.jclec.sbse.discovery.control.InteractionController;

public class ListPreferencesListener extends Observable implements ActionListener{

	InteractionController observer;

	public ListPreferencesListener(InteractionController observer){
		this.observer = observer;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		@SuppressWarnings("unchecked")
		JComboBox<String> cb = (JComboBox<String>)e.getSource();
		int item = cb.getSelectedIndex();
		observer.update(this, item);
	}
}
