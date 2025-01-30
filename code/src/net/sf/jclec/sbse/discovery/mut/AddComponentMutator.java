package net.sf.jclec.sbse.discovery.mut;

import java.util.ArrayList;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.NonTerminalNode;
import net.sf.jclec.sbse.discovery.TerminalNode;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual adding a new component.
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
public class AddComponentMutator extends AbstractCmpMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = 501997763408833174L;

	/** The origin components of classes moved */
	private int [] originComponents;

	/** The origin components required interfaces (terminal number is saved) */
	private ArrayList<Integer> origReqInterfaces;

	/** The origin components provided interfaces (terminal number is saved) */
	private ArrayList<Integer> origProvInterfaces;

	/** The destination component required interfaces (terminal number is saved) */
	private ArrayList<Integer> destReqInterfaces;

	/** The destination component provided interfaces (terminal number is saved) */
	private ArrayList<Integer> destProvInterfaces;

	/** The new classes distribution */
	private int [] classDistribution;

	/** The names of classes to be moved to a new component */
	private String [] classNames;

	/** Dataset classes indexes to be altered */
	private ArrayList<Integer> indexes;

	/** Number of components in the individual */
	private int numOfComp;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public AddComponentMutator(){
		super();
		this.origReqInterfaces = new ArrayList<Integer>();
		this.origProvInterfaces = new ArrayList<Integer>();
		this.destReqInterfaces = new ArrayList<Integer>();
		this.destProvInterfaces = new ArrayList<Integer>();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public IIndividual mutateIndividual(Individual ind, IRandGen randgen) {

		this.numOfComp = ind.getNumberOfComponents();
		this.indexes = new ArrayList<Integer>(); // Save component and class indexes
		this.ind = ind;

		if(this.isRandom)
			randomSelection(randgen);
		else if(!heuristicSelection(randgen)){
			this.indexes.clear();
			this.isRandom=true;
			randomSelection(randgen);
		}

		SyntaxTree mutTree = mutateSyntaxTree();
		return this.species.createIndividual(mutTree);	
	}

	@Override
	public boolean isApplicable(Individual ind) {
		// Cond1: The actual number of components can't be the maximum configured
		// Cond2: One component, at least, must have more than one class
		int numOfComp = ind.getNumberOfComponents();
		if(numOfComp == this.schema.getMaxNumOfComp() || numOfComp == this.schema.getNumOfClasses())
			return false;
		return true;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Randomly selection of classes that 
	 * will be allocated in the new component
	 * @param randgen Random numbers generator
	 * */
	private void randomSelection(IRandGen randgen){
		int numSelected = 0;
		int numClasses;
		int [] numSelectComp = new int[this.numOfComp]; // Number of selected classes in each component
		// Select some classes for the new component
		do{
			for(int i=0; i<this.numOfComp; i++){
				numClasses = this.ind.getNumberOfClasses(i);
				numSelectComp[i]=0;

				// If the component contains more than one class
				if(numClasses>1){
					for(int j=0; j<numClasses; j++){
						// Be sure that not all classes in a component are selected
						if(numSelectComp[i]<(numClasses-numSelectComp[i]) && randgen.coin()){
							this.indexes.add(i); // Component index
							this.indexes.add(j); // Class index (relative to its position into the component)
							numSelectComp[i]++;
							numSelected++;
						}	
					}
				}
			}
		}while(numSelected==0);
	}

	/**
	 * Heuristic selection of classes that
	 * will be allocated in the new component.
	 * Classes not clustered in some component are
	 * candidates to be part of the new component. Each component
	 * has its own probability to contribute new component
	 * based on its number of groups. Group of classes is randomly 
	 * selected when ties occurs.
	 * @param randgen Random number generator
	 * @return true if heuristic selection can be realized, false otherwise 
	 * */
	private boolean heuristicSelection(IRandGen randgen){
		int numGroups;
		int [] classesPerGroup;
		int numClasses = super.species.getDataset().getColumns().size();
		ArrayList<Integer> minGroups = new ArrayList<Integer>();
		int minNumClasses, numSelected=0;

		// Get the maximum number of groups
		int maxGroups = -1;
		for(int i=0; i<this.numOfComp; i++){
			if(this.ind.getNumberOfGroups(i)>maxGroups)
				maxGroups=this.ind.getNumberOfGroups(i);
		}
		
		// Search classes not linked inside each component
		for(int i=0; i<this.numOfComp; i++){
			
			// Number of group in component i
			numGroups = this.ind.getNumberOfGroups(i);
			
			// Component i participates in the new component with n_groups/max_groups of probability
			if(randgen.coin(numGroups/maxGroups)){
				
				classesPerGroup = new int [numGroups];

				// More than one group, count classes in each group
				if(numGroups>1){
					for(int j=0; j<numGroups; j++)
						classesPerGroup[j]=0;

					for(int j=0; j<numClasses; j++)
						if(this.ind.getComponent(j)==i)
							classesPerGroup[this.ind.getGroup(j)]++;

					// Search the minimum number of clustered classes
					minNumClasses=numClasses;
					for(int j=0; j<numGroups; j++){
						if(classesPerGroup[j]<minNumClasses){
							minNumClasses = classesPerGroup[j];
						}
					}

					// Search the groups with the minimum number of clustered classes
					for(int j=0; j<numGroups; j++)
						if(classesPerGroup[j]==minNumClasses)
							minGroups.add(j);

					// Randomly select one group with the minimum number of classes
					if(minGroups.size()>0 && numGroups>1){
						int rndIndex = randgen.choose(0, minGroups.size());
						// Add the classes belonging to the selected group to array of indexes
						for(int k=0; k<numClasses; k++)
							if(this.ind.getComponent(k)==i && this.ind.getGroup(k)==minGroups.get(rndIndex)){
								this.indexes.add(i);	// Component index
								this.indexes.add(k);	// Class index (relative to the dataset)
								numSelected++;
							}
					}
					minGroups.clear();
				}
			}
		}
		// All components have only one group, deterministic selection is not possible
		if(numSelected==0){
			return false;
		}
		return true;
	}

	/**
	 * Mutate genotype adding a new component with the given classes.
	 * */
	private SyntaxTree mutateSyntaxTree() {
		
		SyntaxTree tree = this.ind.getGenotype();
		SyntaxTree mutTree = new SyntaxTree();
		int newComponent = this.numOfComp;
		int actualComponent = -1;
		String symbol;
		boolean isClass = false, isRequired=false, isProvided=false, isConnector=false;
		int index, numOfComp = -1, lastAdded=0;
		int actualClass = -1;
		boolean isMovedElement=false;

		int numOfClasses = this.species.getDataset().getColumns().size();
		this.classDistribution = new int [numOfClasses];
		this.classNames = new String[indexes.size()/2];
		this.originComponents = new int[classNames.length];

		// Clean destination interfaces
		this.destProvInterfaces.clear();
		this.destReqInterfaces.clear();

		// First, locate the classes to be moved. Also, save the new class distribution
		for(int i=0; !isConnector; i++){
			symbol = tree.getNode(i).getSymbol();

			// Non terminal
			if(tree.getNode(i).arity()!=0){
				if(symbol.equalsIgnoreCase("component")){
					numOfComp++; // Increment component's count
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
				this.classDistribution[index]=numOfComp;

				// The classes to be moved
				for(int j=0; j<indexes.size(); j+=2){
					if(indexes.get(j)==numOfComp &&
							((this.isRandom && indexes.get(j+1)==actualClass) || (!this.isRandom && indexes.get(j+1)==index))
							){
						this.classNames[lastAdded] = symbol;
						this.classDistribution[index]=newComponent;
						this.originComponents[lastAdded]=numOfComp;
						lastAdded++;
						break;
					}
				}
			}
		}

		// Clear control variables
		isClass = isConnector = false;
		actualComponent=-1;
		TerminalNode [] terminals = this.schema.getTerminals(); // Used to locate interfaces nodes

		// Copy the individual genotype and perform the classes movements
		for(int i=0; !isConnector; i++){
			symbol = tree.getNode(i).getSymbol();

			// Non terminal, identify the subtree of the model decomposition
			if(tree.getNode(i).arity()!=0){

				if(symbol.equalsIgnoreCase("component")){
					actualComponent++;
					isProvided=false;
					isRequired=false;
					isClass=false;
					// Search the new interfaces in the actual component
					searchInterfaces(actualComponent);
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
					// Add the new component
					addNewComponent(mutTree);
				}

				// Add the node
				mutTree.addNode(tree.getNode(i));

				// Check if it is a component that has lost classes
				if(isOriginComponent(actualComponent)){
					if(isRequired){
						for(int j=0; j<this.origReqInterfaces.size(); j++){
							mutTree.addNode(terminals[this.origReqInterfaces.get(j)]);
						}
					}
					else if(isProvided){
						for(int j=0; j<this.origProvInterfaces.size(); j++){
							mutTree.addNode(terminals[this.origProvInterfaces.get(j)]);
						}
					}
				}
			}

			// Terminal symbol
			else{
				// The component's element is not one origin, add the node
				if(!isOriginComponent(actualComponent)){
					mutTree.addNode(tree.getNode(i));
				}
				// Origin component, add node if it a class not moved (old interfaces are processed apart)
				else if(isClass){ 
					isMovedElement=false;
					for(int j=0; j<this.classNames.length;j++)
						if(symbol.equalsIgnoreCase(this.classNames[j])){	// Is a class
							isMovedElement=true;
						}
					if(!isMovedElement)
						mutTree.addNode(tree.getNode(i));
				}
			}
		}

		// Set connectors
		this.schema.setConnectors(mutTree, (numOfComp+2));
		return mutTree;
	}

	/**
	 * Search the new interfaces given a component.
	 * @param originComponent The origin component.
	 * */
	private void searchInterfaces(int originComponent) {

		TerminalNode [] terminals = this.schema.getTerminals();
		int numOfClasses = this.classDistribution.length;
		int numOfInterfaces = this.schema.getTerminals().length-numOfClasses;
		String class1, class2;
		int index1, index2;
		String interfaceName;

		this.origReqInterfaces.clear();
		this.origProvInterfaces.clear();

		// Check the interfaces defines in schema
		for(int i=0; i<numOfInterfaces; i++){
			interfaceName = terminals[i+numOfClasses].getSymbol();
			
			for(int j=0; j<this.classNames.length; j++){

				// Get the classes involved in the interface definition
				class1 = interfaceName.substring(0, interfaceName.indexOf("_"));
				class2 = interfaceName.substring(interfaceName.lastIndexOf("_")+1);

				index1 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class1));
				index2 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class2));

				// Check if the class to be moved are included in the interface and the other class belong to other component
				if(this.classDistribution[index1] != this.classDistribution[index2]){

					// A required interface
					if(interfaceName.contains("req")){
						// The moved class required functionality from class2
						if(this.classNames[j].equalsIgnoreCase(class1) && !this.destReqInterfaces.contains(i+numOfClasses))	{
							this.destReqInterfaces.add(i+numOfClasses);
						}

						// The origin component needs class moved
						else if(this.classDistribution[index1]==originComponent && !this.origReqInterfaces.contains(i+numOfClasses)) {
							this.origReqInterfaces.add(i+numOfClasses);
						}
					}
					// A provided interface
					else {
						// The moved class provide functionality to class2
						if(this.classNames[j].equalsIgnoreCase(class1) && !this.destProvInterfaces.contains(i+numOfClasses))	{
							this.destProvInterfaces.add(i+numOfClasses);
						}
						// A class in the origin component provided functionality to class2
						else if(this.classDistribution[index1]==originComponent && !this.origProvInterfaces.contains(i+numOfClasses))	{
							this.origProvInterfaces.add(i+numOfClasses);
						}
					}
				}
			}
		}
	}

	/**
	 * Add a new component in the tree using actual properties
	 * @param mutTree The tree to be modified.
	 * */
	private void addNewComponent(SyntaxTree mutTree) {

		NonTerminalNode [] nonTerminals = this.schema.getNonTerminal("component");
		TerminalNode [] terminals = this.schema.getTerminals();
		mutTree.addNode(nonTerminals[0]);
		String [] symbols = nonTerminals[0].getElements();

		for(int i=0; i<symbols.length; i++){
			mutTree.addNode(this.schema.getNonTerminal(symbols[i])[0]);
			// Classes
			if(symbols[i].equalsIgnoreCase("classes"))
				for(int j=0; j<this.classNames.length; j++)
					mutTree.addNode(this.schema.getTerminal(classNames[j]));
			// Required interfaces
			else if(symbols[i].equalsIgnoreCase("required-interfaces"))
				for(int j=0; j<this.destReqInterfaces.size(); j++)
					mutTree.addNode(terminals[this.destReqInterfaces.get(j)]);
			// Provided interfaces
			else if(symbols[i].equalsIgnoreCase("provided-interfaces"))
				for(int j=0; j<this.destProvInterfaces.size(); j++)
					mutTree.addNode(terminals[this.destProvInterfaces.get(j)]);
		}
	}

	/**
	 * Check if the given component contributes, with classes 
	 * and interfaces, in the new component
	 * @param component The number of component
	 * @return True if the component contributes in the 
	 * new component, false otherwise.
	 * */
	private boolean isOriginComponent(int component){
		for(int i=0; i<this.originComponents.length; i++)
			if(this.originComponents[i]==component)
				return true;
		return false;
	}
}
