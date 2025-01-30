package net.sf.jclec.sbse.discovery.imo.gui;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

public class TreeElement {

	protected JTree tree;
	
	public TreeElement(TreeModel treeModel){
		tree = new JTree();
		tree.setModel(treeModel);
	}
	
	public JComponent getElement(){
		return tree;
	}
}
