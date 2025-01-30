package net.sf.jclec.sbse.discovery.imo.mut;

import java.util.ArrayList;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual moving a class from one component to another.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (May 2015)
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class MoveClassMutator extends AbstractComponentMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = -3177846500667341425L;

	/** Origin component */
	private int origComponent;

	/** Destination component */
	private int destComponent;

	/** Class to be moved */
	private int classToBeMoved;

	private ArrayList<Integer> origCandidates;
	private ArrayList<Integer> destCandidates;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public MoveClassMutator(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public IIndividual mutateIndividual(InteractiveMOIndividual ind, IRandGen randgen) {
		this.ind = ind;
		this.numberOfComponents = ind.getNumberOfComponents();
		randomSelection(randgen);

		SyntaxTree mutTree = mutateSyntaxTree();
		//System.out.println("IND: " + ind.toString());
		//System.out.println("MUT: " + mutTree);

		IIndividual mutant = this.species.createIndividual(mutTree);

		/*if(ind.numberFrozenComponents() != 0){
			System.out.println("MOVE CLASS MUTATOR");
			int frozen = -1;
			for(int i=0; frozen==-1 && i<ind.getNumberOfComponents(); i++)
				if(ind.isFrozenComponent(i))
					frozen = i;
			System.out.println("PARENT: frozen=" + frozen + ind);
			System.out.println("SON: ");
			printDistribution();
		}*/

		//System.out.println("MOVE CLASS");
		//printDistribution();
		//System.out.println(mutant.toString());

		// Copy the frozen component from the parent.
		for(int i=0; i<((InteractiveMOIndividual)ind).getNumberOfComponents(); i++){
			((InteractiveMOIndividual)mutant).setFrozenComponent(i, ((InteractiveMOIndividual)ind).isFrozenComponent(i));
		}

		// testing
		/*System.out.println("MOVE CLASS -> origin="+this.origComponent + " destination="+this.destComponent);
		System.out.println("PARENT: ");
		for(int i=0; i<((IMOIndividual)ind).getNumberOfComponents(); i++){
			System.out.println("\t i="+i+" frozen="+((IMOIndividual)ind).isFrozenComponent(i));
		}
		System.out.println("MUTANT: ");
		for(int i=0; i<((IMOIndividual)mutant).getNumberOfComponents(); i++){
			System.out.println("\t i="+i+" frozen="+((IMOIndividual)mutant).isFrozenComponent(i));
		}*/

		return mutant;
	}

	/**
	 * {@inheritDoc}
	 * <p>For this operator:
	 * <ul>
	 * 	<li>Condition 1: One component has, at least, two classes.</li>
	 *  <li>Condition 2: Two components, at least, must not be frozen.</li>
	 * </ul>
	 * */
	@Override
	public boolean isApplicable(InteractiveMOIndividual ind) {
		// check if there exists at least two components that are not frozen
		if((ind.getNumberOfComponents() - ind.numberFrozenComponents()) < 2)
			return false;

		// check if there exist one component that is not frozen and contains, at least, two classes
		origCandidates = new ArrayList<Integer>();
		destCandidates = new ArrayList<Integer>();
		for(int i=0; i<ind.getNumberOfComponents(); i++){
			if(!ind.isFrozenComponent(i)){
				destCandidates.add(i);
				if(ind.getNumberOfClasses(i)>=2)
					origCandidates.add(i);
			}
		}

		if(origCandidates.size() > 0 && destCandidates.size() > origCandidates.size())
			return true;
		else
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

		// Select the origin component
		int index = randgen.choose(0, origCandidates.size());
		this.origComponent = origCandidates.get(index);

		// Select the class
		this.classToBeMoved = randgen.choose(0, this.ind.getNumberOfClasses(this.origComponent));

		// Select the destination component
		if(this.numberOfComponents==2) // Only two components, swap it
			if(this.origComponent==0)
				this.destComponent = 1;
			else
				this.destComponent = 0;

		else{ // Select a random component
			do{
				index = randgen.choose(0, destCandidates.size());
				this.destComponent = destCandidates.get(index);
			}while(this.origComponent==this.destComponent);
		}
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
	 * Set the new distribution of classes
	 * */
	private void setNewDistribution(){
		SyntaxTree tree = this.ind.getGenotype();
		String symbol;
		boolean isClass = false, classFound = false;
		int currentClass = -1, index, currentComponent = -1;

		// Create the new class distribution
		int [] indDistribution = this.ind.getClassesDistribution();
		int numOfClasses = indDistribution.length;
		this.distribution = new int [numOfClasses];

		// Copy the current distribution
		for(int i=0; i<numOfClasses; i++){
			this.distribution[i] = indDistribution[i];
		}

		// Locate the class that will be moved and change the number of components it belongs to
		for(int i=0; !classFound; i++){
			symbol = tree.getNode(i).getSymbol();

			// Non terminal
			if(tree.getNode(i).arity()!=0){
				if(symbol.equalsIgnoreCase("component")){
					currentComponent++; // Increment component's count
				}
				else if(symbol.equalsIgnoreCase("classes")){
					isClass=true;	// The beginning of a set of classes
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass=false;	// The end of a set of classes
					currentClass=-1;
				}
			}
			// Terminal, class
			else if(isClass){
				currentClass++;

				// The class to be moved
				if(this.origComponent==currentComponent && this.classToBeMoved==currentClass){
					index = this.species.getDataset().getIndexOfColumn(this.species.getDataset().getColumnByName(symbol));
					this.distribution[index]=this.destComponent;
					classFound = true;
				}
			}
		}

		////////////////////////
		/*System.out.println("Origin: " + origComponent + " Destination: " + destComponent + " DISTRIBUTION:");
		for(int i=0; i<ind.getClassesDistribution().length; i++){
			System.out.println("Class: " + i + " Initial Component: " + ind.getClassesDistribution()[i] + " Final Component: " + this.distribution[i]);
		}*/
	}
}