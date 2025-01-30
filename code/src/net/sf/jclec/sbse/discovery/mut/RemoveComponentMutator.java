package net.sf.jclec.sbse.discovery.mut;

import java.util.ArrayList;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.TerminalNode;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual removing a component.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (February 2013)
 * 	<li>2.0: Refactoring (July 2013)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 2.0
 * */
public class RemoveComponentMutator extends AbstractCmpMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = -6206551014598508699L;

	/** The removed component required interfaces (interface node position on schema is saved) */
	private ArrayList<Integer> reqInterfaces;

	/** The removed component provided interfaces (interface node position on schema is saved) */
	private ArrayList<Integer> provInterfaces;

	/** The destination component number for the classes in the removal component */
	private int [] classToRemove;

	/** The new class distribution */
	private int [] classDistribution;

	/** The names of classes to be moved to a new component */
	private String [] classNames;

	/** Number of components */
	private int numOfComponents;

	/** Component to remove */
	private int component;
	
	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public RemoveComponentMutator(){
		super();
		this.reqInterfaces = new ArrayList<Integer>();
		this.provInterfaces = new ArrayList<Integer>();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////
	
	@Override
	public IIndividual mutateIndividual(Individual ind, IRandGen randgen) {

		// Select a component with more than one class
		this.numOfComponents = ind.getNumberOfComponents();
		this.ind=ind;
		int newComponent;

		if(this.isRandom)
			this.component = randgen.choose(0, this.numOfComponents);
		else{ 
			this.component = heuristicSelection();
			if(this.component==-1){
				this.isRandom=true;
				this.component = randgen.choose(0, this.numOfComponents);
			}
		}
		// New classes distribution in the resultant components
		int iNumClasses = this.ind.getNumberOfClasses(this.component);
		this.classToRemove = new int[iNumClasses];

		for(int i=0; i<iNumClasses; i++){
			do{
				newComponent = randgen.choose(0, this.numOfComponents);
			}while(newComponent==this.component);
			// Set the new component, if it is posterior component, the component number will decrease after removal
			this.classToRemove[i]=(newComponent>this.component ? newComponent-1 : newComponent);
		}

		SyntaxTree mutTree = mutateSyntaxTree();
		return this.species.createIndividual(mutTree);	// Copy id from parent
	}

	@Override
	public boolean isApplicable(Individual ind) {
		if(ind.getNumberOfComponents() > this.schema.getMinNumOfComp())
			return true;
		return false;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Select the component to be removed.
	 * The most coupled component will
	 * be selected.
	 * @return The component index
	 * */
	private int heuristicSelection() {
		int worstComponent = -1;
		int maxCoupling = 0;
		int [] connectivity = this.ind.getExternalConnections();
		for(int i=0; i<this.numOfComponents; i++){
			if(connectivity[i]>maxCoupling){
				worstComponent=i;
				maxCoupling=connectivity[i];
			}
		}
		return worstComponent;
	}

	/**
	 * Create a new genotype using the component division.
	 * */
	private SyntaxTree mutateSyntaxTree() {

		SyntaxTree tree = this.ind.getGenotype();
		SyntaxTree mutTree = new SyntaxTree();
		int actualComp = -1, actualClass = -1, index, j=0;
		String symbol;
		boolean isClass = false, isRequired=false, isProvided=false, isConnector=false;
		int numOfClasses = schema.getNumOfClasses();
		this.classDistribution = new int [numOfClasses];
		this.classNames = new String [this.classToRemove.length];
		String class1, class2;
		int index1, index2;

		// First, locate the component to be divided
		// Also save the new class distribution
		for(int i=0; !isConnector; i++){
			symbol = tree.getNode(i).getSymbol();
			if(tree.getNode(i).arity()!=0){
				if(tree.getNode(i).getSymbol().equalsIgnoreCase("component")){
					actualComp++;
				}
				else if(symbol.equalsIgnoreCase("classes")){
					isClass=true;	// The beginning of a set of classes
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass=false;	// The end of a set of classes
					actualClass=-1;
				}
				else if(symbol.equalsIgnoreCase("connectors"))
					isConnector=true;	// The end of classes and interfaces information
			}
			// Terminal, class
			else if(isClass){

				actualClass++;
				index = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(symbol));
				// Another component, if it located later, his component number will be decreased because divided component will be the located at the end
				if(actualComp != this.component)	
					this.classDistribution[index]=(actualComp>this.component ? actualComp-1 : actualComp);
				// The divided component, the component number is iNumOfComp-1 or iNumOfComp
				else{
					this.classDistribution[index]=this.classToRemove[actualClass];
					this.classNames[j]=tree.getNode(i).getSymbol();
					j++;
				}
			}
		}

		isClass = isConnector = false;
		actualComp=-1;
		int newComp=-1;
		TerminalNode [] terminals = this.schema.getTerminals();

		// Copy the individual genotype and perform the class movement
		for(int i=0; !isConnector; i++){
			symbol = tree.getNode(i).getSymbol();

			// Non terminal, identify the subtree of the model decomposition
			if(tree.getNode(i).arity()!=0){
				if(symbol.equalsIgnoreCase("component")){
					actualComp++;
					isProvided=false;
					isRequired=false;
					isClass=false;
					newComp = (actualComp>this.component ? actualComp-1 : actualComp);
					if(actualComp != this.component){
						searchInterfaces(newComp);
					}
				}
				else if(symbol.equalsIgnoreCase("classes")){
					isClass=true;
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isRequired=true;
					isClass=false;
				}
				else if(symbol.equalsIgnoreCase("provided-interfaces")){
					isProvided=true;
					isRequired=false;
				}
				else if(symbol.equalsIgnoreCase("connectors")){
					isProvided=false;
					isConnector=true;	// loop end, connectors are processed later
				}

				// Add node if the actual component is not the component to be remove
				if(actualComp != this.component){
					mutTree.addNode(tree.getNode(i));

					if(isClass){
						for(j=0; j<this.classNames.length; j++)
							if(this.classToRemove[j]==newComp)
								mutTree.addNode(this.schema.getTerminal(this.classNames[j]));
					}

					else if(isRequired){
						for(j=0; j<this.reqInterfaces.size(); j++)
							mutTree.addNode(terminals[this.reqInterfaces.get(j)]);
					}

					else if(isProvided){
						for(j=0; j<this.provInterfaces.size(); j++)
							mutTree.addNode(terminals[this.provInterfaces.get(j)]);
					}
				}
				// The connectors symbol must be added if the last component was the removed component
				else if(isConnector)	
					mutTree.addNode(tree.getNode(i));
			}
			// Terminal, when processed components that receives classes, old classes are copied, 
			// old interfaces are checked and only added if the classes belong to different components 
			else{
				if(actualComp != this.component){
					if(isClass)
						mutTree.addNode(tree.getNode(i));
					else{ // An interface, check if the two classes are in the same component
						class1 = symbol.substring(0, symbol.indexOf("_"));
						class2 = symbol.substring(symbol.lastIndexOf("_")+1);

						index1 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class1));
						index2 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class2));

						if(this.classDistribution[index1]!=this.classDistribution[index2])
							mutTree.addNode(tree.getNode(i));
					}
				}
			}
		}

		// Finally, set connectors
		this.schema.setConnectors(mutTree, actualComp);
		return mutTree;
	}

	/**
	 * Search the new interfaces given a component.
	 * @param origComponent The origin component.
	 * */
	private void searchInterfaces(int origComponent) {

		TerminalNode [] terminals = this.schema.getTerminals();
		int numOfClasses = this.classDistribution.length;
		int numberOfInterfaces = this.schema.getTerminals().length-numOfClasses;
		String class1, class2;
		int index1, index2;
		String interfaceName;

		this.reqInterfaces.clear();
		this.provInterfaces.clear();

		// Check the interfaces defines in schema
		for(int i=0; i<numberOfInterfaces; i++){
			interfaceName = terminals[i+numOfClasses].getSymbol();

			for(int j=0; j<this.classNames.length; j++){

				// Get the classes involved in the interface definition
				class1 = interfaceName.substring(0, interfaceName.indexOf("_"));
				class2 = interfaceName.substring(interfaceName.lastIndexOf("_")+1);

				index1 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class1));
				index2 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class2));

				if(this.classDistribution[index1]==origComponent &&
						this.classNames[j].equalsIgnoreCase(class1) && 
						this.classDistribution[index1] != this.classDistribution[index2]){
					if(interfaceName.contains("req"))
						this.reqInterfaces.add(i+numOfClasses);

					else
						this.provInterfaces.add(i+numOfClasses);
				}
			}
		}
	}
}
