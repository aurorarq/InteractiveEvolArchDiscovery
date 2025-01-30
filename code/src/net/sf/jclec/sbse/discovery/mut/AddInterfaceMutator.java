package net.sf.jclec.sbse.discovery.mut;

import java.util.ArrayList;

import es.uco.kdis.datapro.dataset.column.MultiIntegerColumn;
import es.uco.kdis.datapro.datatypes.MultiIntegerValue;
import es.uco.kdis.datapro.datatypes.InvalidValue;
import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.NonTerminalNode;
import net.sf.jclec.sbse.discovery.TerminalNode;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual adding a new interface
 * (Split a component in two components related by an interface)
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (July 2013)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class AddInterfaceMutator extends AbstractCmpMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = 4527709662093710870L;

	/** The origin class index (in the dataset) for the "point-to-split" relation */
	protected int origClassIndex;

	/** The destination class index (in the dataset) for the "point-to-split" relation */
	protected int destClassIndex;

	/** The new classes distribution */
	protected int [] classDistribution;

	/** The names of classes to be moved to a new component */
	protected String [] classNames;

	/** The class-component distribution in the divided component */
	protected int [] classesSplitDistr;

	/** Number of components in the individual */
	protected int numberOfComponents;

	/** Component to split */
	protected int componentSplit;

	/** Candidate relations */
	protected ArrayList<Integer> candRelations;

	/** Internal classes indexes (in the dataset) */
	protected ArrayList<Integer> classIndexes;

	/** Visited classes for the heuristic class distribution */
	protected boolean visited [];

	/** Group distribution inside the selected component */
	protected int classGroup [];

	/** The destination component required interfaces (interface node position on schema is saved) */
	protected ArrayList<Integer> reqInterfaces;

	/** The destination component provided interfaces (interface node position on schema is saved) */
	private ArrayList<Integer> provInterfaces;


	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public AddInterfaceMutator(){
		super();
		this.reqInterfaces = new ArrayList<Integer>();
		this.provInterfaces = new ArrayList<Integer>();
		this.candRelations = new ArrayList<Integer>();
		this.classIndexes = new ArrayList<Integer>();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public IIndividual mutateIndividual(Individual ind, IRandGen randgen) {
		this.numberOfComponents = ind.getNumberOfComponents();
		this.ind = ind;

		// Choose the component
		selectComponent(randgen);

		// Choose the relation that will create the new interface
		selectRelation(randgen);

		// Set the class distribution in the new components
		if(this.isRandom)
			randomSelection(randgen);
		else 
			heuristicSelection(randgen);

		// Mutate syntax tree
		SyntaxTree mutTree = mutateSyntaxTree();
		return this.species.createIndividual(mutTree);	// Copy id from parent
	}

	@Override
	public boolean isApplicable(Individual ind) {
		// The actual number of components can't be the maximum configured
		int numOfComp = ind.getNumberOfComponents();
		if(numOfComp == this.schema.getMaxNumOfComp())
			return false;
		// One component, at least, must have more navigable relationship between its classes
		boolean hasRelations = false;
		this.ind = ind;
		for(int i=0; !hasRelations && i<numOfComp; i++)
			if(hasNavigableRelations(i))
				hasRelations = true;
		return hasRelations;
	}
	
	//////////////////////////////////////////////////////////////////
	//---------------------------------------------- Protected methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Check if the selected component has internal
	 * relations that can be used to define a new interface
	 * (navigable relations)
	 * @param index The number of component
	 * */
	protected boolean hasNavigableRelations(int index) {
		boolean hasNavigableRelations = false;
		boolean isClass = false, exit=false;
		String symbol;
		SyntaxTree genotype = ind.getGenotype();
		int actualCmp = -1, classIndex, otherClassIndex;
		MultiIntegerColumn classCol, otherClassCol;
		MultiIntegerValue relations_ij, relations_ji;
		Object oValue;
		int j;
		String otherClass;
		int nav_ij, nav_ji;

		// Search the component in the genotype
		for(int i=1; !hasNavigableRelations && !exit; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){
				// New set of classes
				if(symbol.equalsIgnoreCase("classes")){
					isClass=true;
					actualCmp++;
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass=false;
					if(index==actualCmp)	// End of specified component classes
						exit=true;
				}
			}
			// Terminal node
			// Check class relations inside the component
			else if(index==actualCmp && isClass){
				// Get the dataset information about the class
				classCol = (MultiIntegerColumn) this.species.getDataset().getColumnByName(symbol);
				classIndex = this.species.getDataset().getIndexOfColumn(classCol);
				// Rest of classes in the component
				j=i+1;
				while(genotype.getNode(j).arity()==0){
					otherClass = genotype.getNode(j).getSymbol();
					otherClassCol = (MultiIntegerColumn) this.species.getDataset().getColumnByName(otherClass);
					otherClassIndex = this.species.getDataset().getIndexOfColumn(otherClassCol);
					
					oValue = classCol.getElement(otherClassIndex);

					// Check relations between both classes
					if(!(oValue instanceof InvalidValue)){// Not invalid

						relations_ij = (MultiIntegerValue)oValue;
						relations_ji = (MultiIntegerValue)otherClassCol.getElement(classIndex);
						
						for(int k=1; k<relations_ij.getSize(); k+=2){ // relation type (k=0,2...) are not important
							nav_ij = relations_ij.getValue(k);
							nav_ji = relations_ji.getValue(k);
							if(nav_ij != nav_ji)
								hasNavigableRelations = true;
						}
					}
					j++;
				}
			}
		}// end of tree route

		return hasNavigableRelations;
	}




	/**
	 * Select the component to be split
	 * @param randgen The random number generator
	 * */
	protected void selectComponent(IRandGen randgen) {
		this.componentSplit=-1;
		// Select a component with internal candidate interface (associations and dependences)
		boolean isValid;
		do{
			this.componentSplit = randgen.choose(0, this.numberOfComponents);
			isValid = hasNavigableRelations(this.componentSplit);
		}while(!isValid);
	}

	/**
	 * Select the relation that will constitute
	 * the new candidate interface
	 * */
	protected void selectRelation(IRandGen randgen){
		boolean hasNavigableAssoc = false;
		boolean isClass = false, exit=false;
		String symbol;
		SyntaxTree genotype = ind.getGenotype();
		int actualCmp = -1, classIndex, otherClassIndex;
		MultiIntegerColumn classCol, otherClassCol;
		Object oValue;
		MultiIntegerValue relations_ij, relations_ji;
		int j;
		String otherClass;
		int nav_ij, nav_ji;

		this.candRelations.clear();
		this.classIndexes.clear();

		// Search the candidate relations
		for(int i=1; !hasNavigableAssoc && !exit; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){
				// New set of classes
				if(symbol.equalsIgnoreCase("classes")){
					isClass=true;
					actualCmp++;
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass=false;
					if(this.componentSplit==actualCmp)	// End of component classes
						exit=true;
				}
			}
			// Terminal node
			// Check class relations inside the component
			else if(this.componentSplit==actualCmp && isClass){
				// Get the dataset information about the class
				classCol = (MultiIntegerColumn) this.species.getDataset().getColumnByName(symbol);
				classIndex = this.species.getDataset().getIndexOfColumn(classCol);
				// Save the index
				this.classIndexes.add(classIndex);

				// Rest of classes in the component
				j=i+1;
				while(genotype.getNode(j).arity()==0){
					otherClass = genotype.getNode(j).getSymbol();
					otherClassCol = (MultiIntegerColumn) this.species.getDataset().getColumnByName(otherClass);
					otherClassIndex = this.species.getDataset().getIndexOfColumn(otherClassCol);
					
					oValue = classCol.getElement(otherClassIndex);

					// Check relations between both classes
					if(!(oValue instanceof InvalidValue)){// Not invalid
						
						relations_ij = (MultiIntegerValue)oValue;
						relations_ji = (MultiIntegerValue)otherClassCol.getElement(classIndex);
						
						for(int k=1; k<relations_ij.getSize(); k+=2){
							nav_ij = relations_ij.getValue(k);
							nav_ji = relations_ji.getValue(k);

							// Navigable relation, add origin an destination class
							if(nav_ij != nav_ji){
								this.candRelations.add(classIndex);
								this.candRelations.add(otherClassIndex);
							}
						}
					}
					j++;
				}
			}
		}// end of tree route

		int rnd = randgen.choose(0,this.candRelations.size());
		if(rnd%2==0){	// Is origin
			this.origClassIndex = this.candRelations.get(rnd);	// Previous class is the origin
			this.destClassIndex = this.candRelations.get(rnd+1);
		}
		else{	// Is destination
			this.origClassIndex = this.candRelations.get(rnd-1);
			this.destClassIndex = this.candRelations.get(rnd);	// Next class is the destination
		}
	}

	/**
	 * Search new groups of classes
	 * without considering the relation to be
	 * split (between <code>orig</code> and <code>dest</code>)
	 * @param orig Origin class 
	 * @param dest Destination class
	 * */
	protected void reassignGroups() {
		int i, numOfGroups = 0;
		int size = this.classIndexes.size();
		this.visited = new boolean[size];
		this.classGroup = new int[size];

		for(i=0; i<size; i++)
			this.visited[i]=false;

		for(i=0; i<size; i++){
			if(!this.visited[i]){
				this.classGroup[i]=numOfGroups; // Set group number
				numOfGroups++;
				graphDepthPath(i, this.classIndexes.get(i));
			}
		}
	}


	/**
	 * Create a new genotype using the component division.
	 * */
	protected SyntaxTree mutateSyntaxTree() {

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

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Random selection of the class distribution
	 * @param randgen Random number generator
	 * */
	private void randomSelection(IRandGen randgen) {
		// New classes distribution in the resultant components
		int numberOfClasses = this.ind.getNumberOfClasses(componentSplit);
		this.classesSplitDistr = new int[numberOfClasses];
		int classIndex;

		// Distribute classes
		for(int i=0; i<numberOfClasses; i++){
			classIndex = this.classIndexes.get(i);

			// Origin class
			if(classIndex == this.origClassIndex)
				this.classesSplitDistr[i] = numberOfComponents-1;
			// Destination class
			else if(classIndex == this.destClassIndex)
				this.classesSplitDistr[i] = numberOfComponents;
			// Other classes
			else
				this.classesSplitDistr[i]=numberOfComponents+randgen.choose(0,2)-1;	
		}
	}

	/**
	 * Set the new class distribution splitting the
	 * relation between <code>origClassIndex</code>
	 * and <code>destClassIndex</code>. Result is
	 * stored in <code>classesSplitDistr</code>.
	 * A class remains in the component if it is connected
	 * with the origin class (recursive search path is executed). 
	 * If class is connected with both origin and destination
	 * class, the component is selected randomly.
	 * @param randgen Random number generator
	 * */
	private void heuristicSelection(IRandGen randgen) {

		// New classes distribution in the resultant components
		int numberOfClasses = this.ind.getNumberOfClasses(componentSplit);
		this.classesSplitDistr = new int[numberOfClasses];

		boolean connectOrig = false;
		boolean connectDest = false;
		int classIndex;

		// Reassign the group distribution in the selected component
		// considering the break point
		reassignGroups();

		for(int i=0; i<numberOfClasses; i++){

			classIndex = this.classIndexes.get(i);
			// Origin class
			if(classIndex == this.origClassIndex)
				this.classesSplitDistr[i] = numberOfComponents-1;

			// Destination class
			else if(classIndex == this.destClassIndex)
				this.classesSplitDistr[i] = numberOfComponents;

			// Other class
			else{

				connectOrig = (this.classGroup[this.classIndexes.indexOf(this.origClassIndex)]==this.classGroup[i]);
				connectDest = (this.classGroup[this.classIndexes.indexOf(this.destClassIndex)]==this.classGroup[i]);

				// Allocate with origin class
				if(connectOrig && !connectDest)
					this.classesSplitDistr[i] = numberOfComponents-1;

				// Allocate with destination class
				else if(connectDest && !connectOrig)
					this.classesSplitDistr[i] = numberOfComponents;

				// Random allocation
				else
					this.classesSplitDistr[i]=numberOfComponents+randgen.choose(0,2)-1;
			}
		}
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
	 * Realize a graph depth search marking 
	 * visited nodes.
	 * @param actualNode The node origin.
	 * @param classIndex The column index in the dataset for the origin node.
	 * */
	private void graphDepthPath(int actualNode, int classIndex){
		int j, size = this.visited.length;
		// Now, the node is visited
		this.visited[actualNode]=true;
		for(j=0; j<size; j++){

			// Not a relation between orig and dest classes (the break point)
			if((classIndex==this.origClassIndex && this.classIndexes.get(j)!=this.destClassIndex) ||
					(classIndex==this.destClassIndex && this.classIndexes.get(j)!=this.origClassIndex) ||
					(classIndex!=this.origClassIndex && classIndex!=this.destClassIndex)){

				// The node origin is connected with the node j, not visited yet, continue recursive depth search with j
				if( !(((MultiIntegerColumn)this.species.getDataset().getColumn(classIndex)).getElement(this.classIndexes.get(j)) instanceof InvalidValue)
						&& !this.visited[j]){

					this.classGroup[j]=this.classGroup[actualNode];	// Set its group number
					graphDepthPath(j, this.classIndexes.get(j));
				}
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
