package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.Dimension;

import javax.swing.JComboBox;

public class PreferenceListElement {

	protected JComboBox<String> box;
	
	public PreferenceListElement(String [] preferenceList){
		String [] items = new String[preferenceList.length+1];
		items[0] = new String("Choose one preference...");
		for(int i=0, j=1; i<preferenceList.length;i++,j++){
			items[j]=preferenceList[i];
		}
		box = new JComboBox<String>(items);
		box.setSelectedIndex(0);
		box.setEditable(false);
		box.setMaximumSize(new Dimension(400,30));
	}
	
	public PreferenceListElement(String [] preferenceList, int index){
		String [] items = new String[preferenceList.length+1];
		items[0] = new String("Choose one preference...");
		for(int i=0, j=1; i<preferenceList.length;i++,j++){
			items[j]=preferenceList[i];
		}
		box = new JComboBox<String>(items);
		box.setEditable(false);
		box.setSelectedIndex(index);
		box.setMaximumSize(new Dimension(400,30));
	}
	
	public JComboBox<String> getComponent(){
		return box;
	}
	
	public void disable(){
		box.setEnabled(false);
		box.setMaximumSize(new Dimension(400,30));
	}
}
