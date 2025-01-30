package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JTable;

public class TableElement {
	
	protected JTable table;
	
	public TableElement(MeasureTableModel tableModel){
		table = new JTable(tableModel.getData(), tableModel.getColumnNames());
		int n = tableModel.getRowCount();
		table.setPreferredScrollableViewportSize(new Dimension(400,n*20));
		table.setFillsViewportHeight(true);
		table.setEnabled(false);
	}
	
	public JComponent getComponent(){
		return table;
	}
}
