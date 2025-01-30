package net.sf.jclec.sbse.discovery.mut;

import java.util.ArrayList;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.TerminalNode;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual moving a class from one component to another.
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
public class MoveClassMutator extends AbstractCmpMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = -3177846500667341425L;

	/** The origin components required interfaces (terminal number is saved) */
	private ArrayList<Integer> origReqInterfaces;

	/** The origin components provided interfaces (terminal number is saved) */
	private ArrayList<Integer> origProvInterfaces;

	/** The destination component required interfaces (terminal number is saved) */
	private ArrayList<Integer> destReqInterfaces;

	/** The destination component provided interfaces (terminal number is saved) */
	private ArrayList<Integer> destProvInterfaces;

	/** New classes distribution */
	private int [] classDistribution;

	/** Number of components */
	private int numOfComponents;

	/** Origin component */
	private int origComponent;

	/** Destination component */
	private int destComponent;

	/** Class to be moved */
	private int classToMoved;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public MoveClassMutator(){
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
		this.ind = ind;
		this.numOfComponents = ind.getNumberOfComponents();
		randomSelection(randgen);
		SyntaxTree mutTree = mutateSyntaxTree();
		return this.species.createIndividual(mutTree);	// Copy id from parent
	}

	@Override
	public boolean isApplicable(Individual ind) {
		// The mutation is applicable if the individual 
		// has, at least, one component with 2 classes 
		if(ind.getNumberOfComponents() < this.schema.getNumOfClasses())
			return true;
		return false;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Random selection
	 * @param oRangen Random number generator
	 * */
	private void randomSelection(IRandGen randgen){
		// Select the origin component with, at least, two classes
		do{
			this.origComponent = randgen.choose(0,this.numOfComponents);
		}while(this.ind.getNumberOfClasses(this.origComponent)<2);

		// Select the class
		this.classToMoved = randgen.choose(0, this.ind.getNumberOfClasses(this.origComponent));

		// Select the destination component
		if(this.numOfComponents==2) // Only two components, swap it
			if(this.origComponent==0)
				this.destComponent = 1;
			else
				this.destComponent = 0;

		else // Select a random component
			do{
				this.destComponent = randgen.choose(this.numOfComponents);
			}while(this.origComponent==this.destComponent);
	}

	/**
	 * Create a new genotype with the class moved
	 * @return The mutated genotype
	 * */
	private SyntaxTree mutateSyntaxTree(){

		SyntaxTree tree = this.ind.getGenotype();
		SyntaxTree mutTree = new SyntaxTree();
		int actualComponent = -1;
		String symbol;
		boolean isClass = false, isRequired=false, isProvided=false, isConnector=false;
		int classPos = -1, index, numOfComp = -1;

		int numOfClasses = this.species.getDataset().getColumns().size();
		this.classDistribution = new int [numOfClasses];
		String className = "";
		int actualClass = -1;
		String className2;	// For checking interfaces

		this.destProvInterfaces.clear();
		this.destReqInterfaces.clear();
		this.origProvInterfaces.clear();
		this.origReqInterfaces.clear();

		// First, locate the class to be moved and the interfaces in which it participates
		// Also, save the class distribution
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

				// The class to be moved
				if(this.origComponent==numOfComp && this.classToMoved==actualClass){
					classPos = i;
					className = symbol;
					this.classDistribution[index]=this.destComponent;
				}
				else{
					this.classDistribution[index]=numOfComp;
				}
			}
		}

		// Second, get the new interfaces
		searchInterfaces(className, this.origComponent);

		// Clear control variables
		isClass = isConnector = false;
		actualComponent=-1;
		TerminalNode [] aTerminals = this.schema.getTerminals(); // Used to locate interfaces nodes

		// Copy the individual genotype and perform the class movement
		for(int i=0; !isConnector; i++){
			symbol = tree.getNode(i).getSymbol();

			// Non terminal, identify the subtree of the model decomposition
			if(tree.getNode(i).arity()!=0){

				if(symbol.equalsIgnoreCase("component")){
					actualComponent++;
					isProvided=false;
					isRequired=false;
					isClass=false;
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
				// Add the node
				mutTree.addNode(tree.getNode(i));

				// Check if it is the destination component, in this case, add the moved 
				// class and the interfaces
				if(actualComponent==this.destComponent){

					if(isClass)
						mutTree.addNode(tree.getNode(classPos));

					else if(isRequired)
						for(int j=0; j<this.destReqInterfaces.size(); j++)
							mutTree.addNode(aTerminals[this.destReqInterfaces.get(j)]);

					else if(isProvided)
						for(int j=0; j<this.destProvInterfaces.size(); j++)
							mutTree.addNode(aTerminals[this.destProvInterfaces.get(j)]);
				}

				// Check if it is the origin component, in this case, add the new interfaces
				if(actualComponent==this.origComponent){
					if(isRequired)
						for(int j=0; j<this.origReqInterfaces.size(); j++)
							mutTree.addNode(aTerminals[this.origReqInterfaces.get(j)]);

					else if(isProvided)
						for(int j=0; j<origProvInterfaces.size(); j++)
							mutTree.addNode(aTerminals[this.origProvInterfaces.get(j)]);
				}
			}

			// Terminal symbol
			else{
				// The component's element is not the origin or destination, add the node (class or interface)
				if(actualComponent != this.destComponent && actualComponent != this.origComponent)
					mutTree.addNode(tree.getNode(i));

				// Origin or destination component, add old classes and interfaces
				else{
					// Class
					if(isClass && !symbol.equalsIgnoreCase(className))
						mutTree.addNode(tree.getNode(i));
					// Interface
					else if(!isClass){

						className2 = symbol.substring(symbol.lastIndexOf("_")+1);

						// Check old interfaces in the destination component
						if(isRequired){
							if(actualComponent==this.destComponent && !className2.equalsIgnoreCase(className))
								mutTree.addNode(tree.getNode(i));
						}
						else{
							if(actualComponent==this.destComponent && !className2.equalsIgnoreCase(className))
								mutTree.addNode(tree.getNode(i));
						}
					}
				}
			}
		}

		// Finally, set connectors
		this.schema.setConnectors(mutTree, numOfComp+1);
		return mutTree;
	}

	/**
	 * Search possible new interfaces based on the moved class
	 * @param className The moved class name
	 * @param originComponent The origin class component
	 * */
	private void searchInterfaces(String className, int originComponent) {

		TerminalNode [] terminals = this.schema.getTerminals();
		int numOfClasses = this.classDistribution.length;
		int numberOfIntefaces = this.schema.getTerminals().length-numOfClasses;
		String class1, class2;
		int index1, index2;
		String interfaceName;

		// Check the interfaces defines in schema
		for(int i=0; i<numberOfIntefaces; i++){
			interfaceName = terminals[i+numOfClasses].getSymbol();

			// Get the classes involved in the interface definition
			class1 = interfaceName.substring(0, interfaceName.indexOf("_"));
			class2 = interfaceName.substring(interfaceName.lastIndexOf("_")+1);

			index1 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class1));
			index2 = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(class2));

			// Check if the class to be moved are included in the interface and the other class belong to other component
			if(this.classDistribution[index1] != this.classDistribution[index2]){

				// A required interface
				if(interfaceName.contains("req")){
					if(className.equalsIgnoreCase(class1))	// The moved class required functionality from class2
						this.destReqInterfaces.add(i+numOfClasses);

					else if(this.classDistribution[index1]==originComponent) // The origin component needs class moved
						this.origReqInterfaces.add(i+numOfClasses);
				}
				// A provided interface
				else {
					if(className.equalsIgnoreCase(class1))	// The moved class provide functionality to class2
						this.destProvInterfaces.add(i+numOfClasses);

					else if(this.classDistribution[index1]==originComponent)	// A class in the origin component provided functionality to class2
						this.origProvInterfaces.add(i+numOfClasses);
				}
			}
		}
	}
}