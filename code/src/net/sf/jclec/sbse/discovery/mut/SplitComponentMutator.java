package net.sf.jclec.sbse.discovery.mut;

import java.util.ArrayList;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.NonTerminalNode;
import net.sf.jclec.sbse.discovery.TerminalNode;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual splitting a component in two new components.
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
public class SplitComponentMutator extends AbstractCmpMutator{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = 8982472603086165569L;

	/** The destination component required interfaces (interface node position on schema is saved) */
	private ArrayList<Integer> reqInterfaces;

	/** The destination component provided interfaces (interface node position on schema is saved) */
	private ArrayList<Integer> provInterfaces;

	/** The new classes distribution */
	private int [] classDistribution;

	/** The names of classes to be moved to a new component */
	private String [] classNames;

	/** The class-component distribution in the divided component */
	private int [] classesSplitDistr;

	/** Number of components in the individual */
	private int numberOfComponents;

	/** Component to split */
	private int componentSplit;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public SplitComponentMutator(){
		super();
		this.reqInterfaces = new ArrayList<Integer>();
		this.provInterfaces = new ArrayList<Integer>();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public IIndividual mutateIndividual(Individual ind, IRandGen randgen) {

		this.numberOfComponents = ind.getNumberOfComponents();
		this.ind = ind;

		if(this.isRandom)
			randomSelection(randgen);
		else if(!heuristicSelection(randgen)){
			this.isRandom=true;
			randomSelection(randgen);
		}

		SyntaxTree mutTree = mutateSyntaxTree();
		return this.species.createIndividual(mutTree);	// Copy id from parent
	}

	@Override
	public boolean isApplicable(Individual ind) {
		// The actual number of components can't be the maximum configured
		int numOfComp = ind.getNumberOfComponents();
		if(numOfComp == this.schema.getMaxNumOfComp())
			return false;
		// One component, at least, must have more than one class
		if(numOfComp == this.schema.getNumOfClasses())
			return false;
		return true;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Random selection of the component to be split
	 * @param randgen Random number generator
	 * */
	private void randomSelection(IRandGen randgen) {
		this.componentSplit=-1;
		// Select a component with more than one class
		do{
			this.componentSplit = randgen.choose(0, this.numberOfComponents);
		}while(this.ind.getNumberOfClasses(this.componentSplit)<2);

		// New classes distribution in the resultant components
		int numberOfClasses = this.ind.getNumberOfClasses(componentSplit);
		this.classesSplitDistr = new int[numberOfClasses];
		for(int i=0; i<numberOfClasses; i++)
			this.classesSplitDistr[i]=-1;

		// Guarantee one class for component
		int rndIndex1 = randgen.choose(0,numberOfClasses);
		this.classesSplitDistr[rndIndex1]=numberOfComponents-1;
		int rndIndex2;
		do{
			rndIndex2 = randgen.choose(0,numberOfClasses);
		}while(rndIndex1==rndIndex2);
		this.classesSplitDistr[rndIndex2]=numberOfComponents;

		// Distribute rest of classes
		for(int i=0; i<numberOfClasses; i++)
			if(this.classesSplitDistr[i]==-1)
				this.classesSplitDistr[i]=numberOfComponents+randgen.choose(0,2)-1;	
	}

	/**
	 * Heuristic selection of the component to be split.
	 * A component with more than one group is selected.
	 * @param randgen Random number generator
	 * */
	private boolean heuristicSelection(IRandGen randgen) {

		ArrayList<Integer> candidates = new ArrayList<Integer>();
		// Search components with more than one group
		for(int i=0; i<this.numberOfComponents; i++){
			if(this.ind.getNumberOfGroups(i)>1)
				candidates.add(i);
		}

		int numOfClasses = super.species.getDataset().getColumns().size();
		if(candidates.size()>1){

			// Random selection of the component to be split (between candidates)
			this.componentSplit = candidates.get(randgen.choose(0, candidates.size()));
			this.classesSplitDistr = new int[ind.getNumberOfClasses(this.componentSplit)];

			// Choose one group in the selected component
			int group1 = randgen.choose(0, this.ind.getNumberOfGroups(this.componentSplit));
			for(int i=0, j=0; i<numOfClasses; i++){
				if(this.ind.getComponent(i)==this.componentSplit){
					if(this.ind.getGroup(i)==group1)
						this.classesSplitDistr[j]=this.numberOfComponents-1; // Class from the selected group, first new component
					else
						this.classesSplitDistr[j]=this.numberOfComponents; // Other class, second new component
					j++;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Create a new genotype using the component division.
	 * */
	private SyntaxTree mutateSyntaxTree() {

		SyntaxTree tree = this.ind.getGenotype();
		SyntaxTree mutTree = new SyntaxTree();
		int actualComp = -1, actualClass = -1, index;
		String symbol;
		boolean isClass = false, isConnector=false;
		int numOfClasses = schema.getNumOfClasses();
		this.classNames = new String[this.classesSplitDistr.length];
		this.classDistribution = new int [numOfClasses];

		int j=0;
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
				if(actualComp != componentSplit)	
					this.classDistribution[index]=(actualComp>componentSplit ? actualComp-1 : actualComp);
				// The divided component, the component number is iNumOfComp-1 or iNumOfComp
				else{
					this.classDistribution[index]=this.classesSplitDistr[actualClass];
					this.classNames[j]=tree.getNode(i).getSymbol();
					j++;
				}
			}
		}

		// Clear control variables
		isClass = isConnector = false;
		actualComp=-1;

		// Copy the individual genotype and perform the classes movements
		int i=0;
		for(i=0; !isConnector; i++){
			symbol = tree.getNode(i).getSymbol();

			if(symbol.equalsIgnoreCase("component")){
				actualComp++;
			}
			else if(symbol.equalsIgnoreCase("connectors")){
				isConnector=true;	// loop end, connectors are processed later
			}
			// Not the component to be divided, add the node
			if(actualComp!=componentSplit && !isConnector){
				// Add the node
				mutTree.addNode(tree.getNode(i));
			}
		}

		// Add new components and connectors
		searchInterfaces(this.numberOfComponents-1);
		addNewComponent(mutTree, this.numberOfComponents-1);
		searchInterfaces(this.numberOfComponents);
		addNewComponent(mutTree, this.numberOfComponents);
		mutTree.addNode(tree.getNode(i-1));	// The connector node has not been added in the loop
		this.schema.setConnectors(mutTree, numberOfComponents+1);

		return mutTree;
	}

	/**
	 * Search interfaces for the given component.
	 * @param numComponent The number of component.
	 * */
	private void searchInterfaces(int numComponent) {

		TerminalNode [] terminals = this.schema.getTerminals();
		int numOfClasses = this.schema.getNumOfClasses();
		int numOfInterfaces = this.schema.getTerminals().length-numOfClasses;
		String class1, class2;
		int index1, index2;
		String interfaceName;

		this.provInterfaces.clear();
		this.reqInterfaces.clear();

		// Check the interfaces defines in schema
		for(int i=0; i<numOfInterfaces; i++){
			interfaceName = terminals[i+numOfClasses].getSymbol();

			// Get the classes involved in the interface definition
			class1 = interfaceName.substring(0, interfaceName.indexOf("_"));
			class2 = interfaceName.substring(interfaceName.lastIndexOf("_")+1);

			index1 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class1));
			index2 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class2));

			// Check if the class to be moved are included in the interface and the other class belong to other component
			if(this.classDistribution[index1] != this.classDistribution[index2]){

				// A required interface
				if(interfaceName.contains("req") && this.classDistribution[index1]==numComponent)
					this.reqInterfaces.add(i+numOfClasses);

				// A provided interface
				else if(interfaceName.contains("prov") && this.classDistribution[index1]==numComponent)
					this.provInterfaces.add(i+numOfClasses);
			}
		}
	}

	/**
	 * Add a new component in the tree using actual properties
	 * @param mutTree The tree to be modified.
	 * @param numComponent The number of component.
	 * */
	private void addNewComponent(SyntaxTree mutTree, int numComponent) {

		NonTerminalNode [] nonTerminals = this.schema.getNonTerminal("component");
		TerminalNode [] terminals = this.schema.getTerminals();
		mutTree.addNode(nonTerminals[0]);
		String [] symbols = nonTerminals[0].getElements();
		int j;

		for(int i=0; i<symbols.length; i++){
			mutTree.addNode(this.schema.getNonTerminal(symbols[i])[0]);
			// Classes
			if(symbols[i].equalsIgnoreCase("classes"))
				for(j=0; j<this.classesSplitDistr.length; j++){
					if(this.classesSplitDistr[j]==numComponent){
						mutTree.addNode(this.schema.getTerminal(this.classNames[j]));
					}
				}
			// Required interfaces
			else if(symbols[i].equalsIgnoreCase("required-interfaces"))
				for(j=0; j<this.reqInterfaces.size(); j++)
					mutTree.addNode(terminals[this.reqInterfaces.get(j)]);
			// Provided interfaces	
			else if(symbols[i].equalsIgnoreCase("provided-interfaces"))
				for(j=0; j<this.provInterfaces.size(); j++)
					mutTree.addNode(terminals[this.provInterfaces.get(j)]);
		}
	}
}