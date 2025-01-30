package net.sf.jclec.sbse.discovery.imo.mut;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual removing a component.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (May 2015)</li>
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class RemoveComponentMutator extends AbstractComponentMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = -6206551014598508699L;

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
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public IIndividual mutateIndividual(InteractiveMOIndividual ind, IRandGen randgen) {

		// Select a component with more than one class
		this.numberOfComponents = ind.getNumberOfComponents();
		this.ind=ind;

		if(this.isRandom)
			randomSelection(randgen);
		else{ 
			heuristicSelection();
			if(this.component==-1){
				this.isRandom=true;
				randomSelection(randgen);
			}
		}

		SyntaxTree mutTree = mutateSyntaxTree(randgen);
		//System.out.println("IND: " + ind.toString());
		//System.out.println("MUT: " + mutTree);

		IIndividual mutant = this.species.createIndividual(mutTree);

		/*if(ind.numberFrozenComponents() != 0){
			System.out.println("REMOVE COMPONENT MUTATOR");
			int frozen = -1;
			for(int i=0; frozen==-1 && i<ind.getNumberOfComponents(); i++)
				if(ind.isFrozenComponent(i))
					frozen = i;
			System.out.println("PARENT: frozen=" + frozen + ind);
			System.out.println("SON: ");
			printDistribution();
		}*/

		//System.out.println("REMOVE COMPONENT");
		//printDistribution();
		//System.out.println(mutant.toString());

		// Set frozen components
		int i=0,index;
		for(i=0; i<((InteractiveMOIndividual)ind).getNumberOfComponents(); i++){
			if(i != component){	
				index=(i<component ? i : i-1);
				((InteractiveMOIndividual)mutant).setFrozenComponent(index, ((InteractiveMOIndividual)ind).isFrozenComponent(i));
			}
		}

		// testing
		/*System.out.println("REMOVE COMPONENT -> componentToRemove="+this.component);
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
	 * 	<li>Condition 1: The current number of components cannot be the minimum configured.</li>
	 *  <li>Condition 2: At least two components must not be frozen.</li>
	 * </ul>
	 * */
	@Override
	public boolean isApplicable(InteractiveMOIndividual ind) {
		if(ind.getNumberOfComponents() > this.schema.getMinNumOfComp() &&
				(ind.getNumberOfComponents()-ind.numberFrozenComponents())>=2)
			return true;
		return false;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Randomly selection of component
	 * that will be joined
	 * @param randgen 
	 * */
	private void randomSelection(IRandGen randgen) {
		do{
			this.component = randgen.choose(0, this.numberOfComponents);
		}while(this.ind.isFrozenComponent(this.component));
	}

	/**
	 * Select the component to be removed.
	 * The most coupled component will
	 * be selected.
	 * @return The component index
	 * */
	private void heuristicSelection() {
		this.component = -1;
		int maxCoupling = 0;
		int [] connectivity = this.ind.getExternalConnections();
		for(int i=0; i<this.numberOfComponents; i++){
			if(!this.ind.isFrozenComponent(i) && connectivity[i]>maxCoupling){
				this.component=i;
				maxCoupling=connectivity[i];
			}
		}
	}

	/**
	 * Create the mutated genotype
	 * @param randgen Random generator
	 * */
	private SyntaxTree mutateSyntaxTree(IRandGen randgen) {

		// Create the new distribution
		setNewDistribution(randgen);

		// Create the mutated tree
		SyntaxTree mutTree = this.schema.createSyntaxTree(this.numberOfComponents, this.distribution);
		return mutTree;
	}

	/**
	 * New distribution of the classes
	 * @param randgen Random generator
	 * */
	private void setNewDistribution(IRandGen randgen){
		// New classes distribution in the resultant components
		int newComponent;
		int [] indDistribution = this.ind.getClassesDistribution();
		int iNumClasses = indDistribution.length;
		this.distribution = new int[iNumClasses];

		for(int i=0; i<iNumClasses; i++){

			if(indDistribution[i]!=this.component){
				this.distribution[i]=(indDistribution[i]>this.component ? indDistribution[i]-1 : indDistribution[i]);
			}

			else{
				do{
					newComponent = randgen.choose(0, this.numberOfComponents);
				}while(!this.ind.isFrozenComponent(newComponent) && newComponent==this.component);

				// Set the new component, if it is posterior component, the component number will decrease after removal
				this.distribution[i]=(newComponent>this.component ? newComponent-1 : newComponent);
			}
		}

		// Update the number of components
		this.numberOfComponents = ind.getNumberOfComponents()-1;

		////////////////////////
		/*System.out.println("Component to be removed: " + component + " DISTRIBUTION:");
		for(int i=0; i<indDistribution.length; i++){
			System.out.println("Class: " + i + " Initial Component: " + indDistribution[i] + " Final Component: " + this.distribution[i]);
		}*/
	}
}
