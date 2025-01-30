package net.sf.jclec.sbse.discovery.imo.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.syntaxtree.SyntaxTreeNode;

/**
 * 
 * */
public class DiagramTreeModel {

	/**
	 * Internal tree
	 * */
	protected DefaultTreeModel tree;

	protected InteractiveMOIndividual individual;

	/**
	 * Parameterized constructor.
	 * @param The individual to be represented in the tree
	 * */
	public DiagramTreeModel(InteractiveMOIndividual individual) {
		this.individual = individual;
		createTreeFromIndividual();
	}

	public TreeModel getModel(){
		return this.tree;
	}

	public InteractiveMOIndividual getIndividual(){
		return this.individual;
	}

	/**
	 * Create the tree model from the individual genotype
	 * */
	private void createTreeFromIndividual(){

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Component-based Architecture");
		SyntaxTree genotype = individual.getGenotype();
		String symbol;
		SyntaxTreeNode node;
		int nCmp=-1, nCnn=-1, nReq=-1, nProv=-1;
		boolean isComponent = false, isConnector=false, isClass = false, isRequired = false, isProvided = false;
		DefaultMutableTreeNode parent = null;

		//System.out.println(individual.toString());
		
		for(int i=1; i<genotype.size(); i++){
			node = genotype.getNode(i);
			symbol = node.getSymbol();

			/////////////////////////////////////////////
			/*System.out.println("Symbol: " + symbol + " arity: " + node.arity());
			System.out.println("\t isComponent=" + isComponent + " isConnector=" + isConnector + " isClass=" + isClass + " isReq=" + isRequired + " isProv=" + isProvided);
			System.out.println("\t nComp=" + nCmp + " nConn=" + nCnn + " nReq=" + nReq + " nProv=" + nProv);
*/
			// Non-terminal
			if(node.arity()!=0){
				if(symbol.equalsIgnoreCase("components") || symbol.equalsIgnoreCase("connectors")){
					root.add(new DefaultMutableTreeNode(symbol));

					if(symbol.equalsIgnoreCase("components")){
						isComponent = true;
						isConnector = false;
					}
					else{
						isComponent = false;
						isConnector = true;
					}
				}
				else{
					if(symbol.equalsIgnoreCase("component")){
						isClass = false;
						isRequired = false; nReq=-1;
						isProvided = false; nProv=-1;
						nCmp++;
						((DefaultMutableTreeNode)root.getFirstChild()).add(new DefaultMutableTreeNode("component-"+(nCmp+1)));
					}
					else if(symbol.equalsIgnoreCase("classes")){
						parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getFirstChild()).getChildAt(nCmp));
						parent.add(new DefaultMutableTreeNode(symbol));
						isClass = true;
						isRequired = false; //nReq=-1;
						isProvided = false; //nProv=-1;
					}
					else if(symbol.equalsIgnoreCase("required-interfaces")){
						if(isComponent){
							parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getFirstChild()).getChildAt(nCmp));
							parent.add(new DefaultMutableTreeNode(symbol));
							isClass = false;
							isRequired = true; //nReq++;
							isProvided = false; //nProv=-1;
						}
						else if(isConnector){
							parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getLastChild()).getChildAt(nCnn));
							parent.add(new DefaultMutableTreeNode(symbol));
							isClass = false;
							isRequired = true; nReq++;
							isProvided = false; nProv=-1;
						}
					}
					else if(symbol.equalsIgnoreCase("provided-interfaces")){
						parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getFirstChild()).getChildAt(nCmp));
						parent.add(new DefaultMutableTreeNode(symbol));
						isClass = false;
						isRequired = false; //nReq=-1;
						isProvided = true; //nProv++;
					}
					else if(symbol.equalsIgnoreCase("interface")){
						if(isComponent && isRequired){
							nReq++;
							parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getFirstChild()).getChildAt(nCmp));
							parent = (DefaultMutableTreeNode)parent.getChildAt(1);
							parent.add(new DefaultMutableTreeNode(symbol+"-"+(nReq+1)));
						}
						else if(isComponent && isProvided){
							nProv++;
							parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getFirstChild()).getChildAt(nCmp));
							parent = (DefaultMutableTreeNode)parent.getChildAt(2);
							parent.add(new DefaultMutableTreeNode(symbol+"-"+(nProv+1)));	
						}				
					}
					else if(symbol.equalsIgnoreCase("connector")){
						((DefaultMutableTreeNode)root.getLastChild()).add(new DefaultMutableTreeNode(symbol));
						nCnn++;
						nCmp=-1;
						isClass = false;
						isRequired = false; nReq++;
						isProvided = false; nProv=-1;
					}
					else if(symbol.equalsIgnoreCase("provided-interface")){
						parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getLastChild()).getChildAt(nCnn));
						parent.add(new DefaultMutableTreeNode(symbol));
						isRequired = false; nReq=-1;
						isProvided = true; nProv++;
					}
				}
			}

			// Terminal
			else{
				if(isComponent){

					if(isClass){
						parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getFirstChild()).getChildAt(nCmp));
						parent = (DefaultMutableTreeNode)parent.getFirstChild();
						parent.add(new DefaultMutableTreeNode(symbol));
					}
					else if(isRequired){
						parent = (DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getFirstChild()).getChildAt(nCmp);
						parent = (DefaultMutableTreeNode)((DefaultMutableTreeNode)parent.getChildAt(1)).getChildAt(nReq);
						parent.add(new DefaultMutableTreeNode(symbol));
					}
					else if(isProvided){
						parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getFirstChild()).getChildAt(nCmp));
						parent = (DefaultMutableTreeNode)((DefaultMutableTreeNode)parent.getLastChild()).getChildAt(nProv);
						parent.add(new DefaultMutableTreeNode(symbol));
					}

				}
				else if(isConnector){
					if(isRequired){
						parent = (DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getLastChild()).getChildAt(nCnn);
						parent = (DefaultMutableTreeNode)parent.getLastChild();
						parent.add(new DefaultMutableTreeNode(symbol));
					}else if(isProvided){
						parent = (DefaultMutableTreeNode)((DefaultMutableTreeNode)root.getLastChild()).getChildAt(nCnn);
						parent = (DefaultMutableTreeNode)parent.getFirstChild();
						parent.add(new DefaultMutableTreeNode(symbol));
					}
				}
			}
		}

		// Create the tree model
		this.tree = new DefaultTreeModel(root);
	}
}