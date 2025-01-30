package net.sf.jclec.sbse.discovery.imo.mut;

import java.util.ArrayList;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual adding a new component.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (May 2015)</li>
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class AddComponentMutator extends AbstractComponentMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = 501997763408833174L;

	/** The classes and components to be modified */
	private ArrayList<Integer> indexes;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public AddComponentMutator(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public IIndividual mutateIndividual(InteractiveMOIndividual ind, IRandGen randgen) {

		this.numberOfComponents = ind.getNumberOfComponents();
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
		IIndividual mutant = this.species.createIndividual(mutTree);
		/*if(ind.getNumberOfComponents()==7){
			System.out.println("PARENT: " + ind.getGenotype());
			System.out.println("SON: " + ((IMOIndividual)mutant).getGenotype()+"\n");
		}*/
		//printDistribution();
		//System.out.println(mutant.toString());

		/*if(ind.numberFrozenComponents() != 0){
			System.out.println("ADD COMPONENT MUTATOR");
			int frozen = -1;
			for(int i=0; frozen==-1 && i<ind.getNumberOfComponents(); i++)
				if(ind.isFrozenComponent(i))
					frozen = i;
			System.out.println("PARENT: frozen=" + frozen + ind);
			System.out.println("SON: ");
			printDistribution();
		}*/

		// Copy the frozen component from the parent. The new component is not frozen
		int i=0;
		for(i=0; i<((InteractiveMOIndividual)ind).getNumberOfComponents(); i++){
			((InteractiveMOIndividual)mutant).setFrozenComponent(i, ((InteractiveMOIndividual)ind).isFrozenComponent(i));
		}
		((InteractiveMOIndividual)mutant).setFrozenComponent(i, false);

		// testing
		/*System.out.println("ADD COMPONENT");
		System.out.println("PARENT: ");
		for(i=0; i<((IMOIndividual)ind).getNumberOfComponents(); i++){
			System.out.println("\t i="+i+" frozen="+((IMOIndividual)ind).isFrozenComponent(i));
		}
		System.out.println("MUTANT: ");
		for(i=0; i<((IMOIndividual)mutant).getNumberOfComponents(); i++){
			System.out.println("\t i="+i+" frozen="+((IMOIndividual)mutant).isFrozenComponent(i));
		}*/

		return mutant;	
	}

	/**
	 * {@inheritDoc}
	 * <p>For this operator:
	 * <ul>
	 * 	<li>Condition 1: The current number of components cannot be the maximum configured.</li>
	 *  <li>Condition 2: One component, at least, must have more than one class, and this component cannot be frozen.</li>
	 * </ul>
	 * */
	@Override
	public boolean isApplicable(InteractiveMOIndividual ind) {
		// Condition 1: The actual number of components can't be the maximum configured
		int numOfComp = ind.getNumberOfComponents();
		if(numOfComp == this.schema.getMaxNumOfComp() || numOfComp == this.schema.getNumOfClasses())
			return false;

		// Condition 2: One component, at least, must have more than one class, and this component cannot be frozen
		else{
			for(int i=0; i<numOfComp; i++){
				if(!ind.isFrozenComponent(i) && ind.getNumberOfClasses(i) > 1)
					return true;
			}
		}
		return false;
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
		int [] numSelectComp = new int[this.numberOfComponents]; // Number of selected classes in each component

		// Select some classes for the new component
		do{
			for(int i=0; i<this.numberOfComponents; i++){
				numClasses = this.ind.getNumberOfClasses(i);
				numSelectComp[i]=0;

				// If the component is not frozen and contains more than one class
				if(!this.ind.isFrozenComponent(i) && numClasses>1){
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
	 * based on its number of groups. Group of classes are randomly 
	 * selected when ties occur.
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
		for(int i=0; i<this.numberOfComponents; i++){
			if(!this.ind.isFrozenComponent(i) && this.ind.getNumberOfGroups(i)>maxGroups)
				maxGroups=this.ind.getNumberOfGroups(i);
		}

		// Search classes not linked inside each component
		for(int i=0; i<this.numberOfComponents; i++){

			if(!this.ind.isFrozenComponent(i)){

				// Number of groups in component i
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
		}

		// All components have only one group, deterministic selection is not possible
		if(numSelected==0){
			return false;
		}
		return true;
	}

	/**
	 * Create a new genotype with the class moved
	 * @return The mutated genotype
	 * */
	private SyntaxTree mutateSyntaxTree(){

		// Create the new distribution
		setNewDistribution();

		// Create the mutated tree
		SyntaxTree mutTree = this.schema.createSyntaxTree(this.numberOfComponents, this.distribution);

		return mutTree;
	}

	/**
	 * Set the new class distribution
	 */
	private void setNewDistribution() {

		SyntaxTree tree = this.ind.getGenotype();
		int newComponent = this.numberOfComponents;
		String symbol;
		boolean isClass = false, isConnector=false;
		int index, numOfComp = -1;
		int actualClass = -1;

		int numOfClasses = this.species.getDataset().getColumns().size();
		this.distribution = new int [numOfClasses];

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
				this.distribution[index]=numOfComp;

				// The classes to be moved
				for(int j=0; j<indexes.size(); j+=2){
					if(indexes.get(j)==numOfComp &&
							((this.isRandom && indexes.get(j+1)==actualClass) || 
									(!this.isRandom && indexes.get(j+1)==index))
							){
						this.distribution[index]=newComponent;
						break;
					}
				}
			}
		}

		// Update the number of components
		this.numberOfComponents = ind.getNumberOfComponents()+1;
		//System.out.println("New number components="+this.numberOfComponents);
		////////////////////////
		/*System.out.println("DISTRIBUTION:");
		for(int i=0; i<ind.getClassesDistribution().length; i++){
			System.out.println("Class: " + i + " Initial Component: " + ind.getClassesDistribution()[i] + " Final Component: " + this.distribution[i]);
		}*/

		// Update frozen components

	}
}