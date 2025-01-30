package net.sf.jclec.sbse.discovery.imo.mut;

import java.util.ArrayList;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual merging two existing components.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (May 2015)</li>
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class MergeComponentsMutator extends AbstractComponentMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = -6121041039166849383L;

	/** First component */
	private int component1;

	/** Second component */
	private int component2;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public MergeComponentsMutator(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public IIndividual mutateIndividual(InteractiveMOIndividual ind, IRandGen randgen) {

		this.numberOfComponents = ind.getNumberOfComponents();
		this.ind=ind;

		//Choose component selection method
		if(this.isRandom)
			randomSelection(randgen);
		else 
			heuristicSelection(randgen);

		SyntaxTree mutTree = mutateSyntaxTree();
		//System.out.println("MERGE COMPONENTS");
		//System.out.println("IND: " + ind.toString());
		//System.out.println("MUT: " + mutTree);

		IIndividual mutant = this.species.createIndividual(mutTree);

		/*if(ind.numberFrozenComponents() != 0){
			System.out.println("MERGE COMPONENTS MUTATOR");
			int frozen = -1;
			for(int i=0; frozen==-1 && i<ind.getNumberOfComponents(); i++)
				if(ind.isFrozenComponent(i))
					frozen = i;
			System.out.println("PARENT: frozen=" + frozen + ind);
			System.out.println("SON: ");
			printDistribution();
		}*/

		// Set frozen components
		int firstComponent = (this.component1<this.component2) ? this.component1 : this.component2;
		int lastComponent = (this.component1>this.component2) ? this.component1 : this.component2;
		int i=0,index;
		for(i=0; i<((InteractiveMOIndividual)ind).getNumberOfComponents(); i++){
			if(i != firstComponent && i != lastComponent){	
				index=(i>lastComponent ? i-1 : i);
				((InteractiveMOIndividual)mutant).setFrozenComponent(index, ((InteractiveMOIndividual)ind).isFrozenComponent(i));
			}
		}
		// the component that has been merged is not frozen
		((InteractiveMOIndividual)mutant).setFrozenComponent(firstComponent, false);

		// testing
		/*System.out.println("MERGE COMPONENTS -> component1="+this.component1 + " component2=" + this.component2);
		System.out.println("PARENT: ");
		for(i=0; i<((IMOIndividual)ind).getNumberOfComponents(); i++){
			System.out.println("\t i="+i+" frozen="+((IMOIndividual)ind).isFrozenComponent(i));
		}
		System.out.println("MUTANT: ");
		for(i=0; i<((IMOIndividual)mutant).getNumberOfComponents(); i++){
			System.out.println("\t i="+i+" frozen="+((IMOIndividual)mutant).isFrozenComponent(i));
		}*/

		//printDistribution();
		//System.out.println(mutant.toString());
		return mutant;
	}

	/**
	 * {@inheritDoc}
	 * <p>For this operator:
	 * <ul>
	 * 	<li>Condition 1: The current number of components cannot be the minimum configured.</li>
	 *  <li>Condition 2: Two components, at least, must not be frozen.</li>
	 * </ul>
	 * */
	@Override
	public boolean isApplicable(InteractiveMOIndividual ind) {
		// Check conditions
		if(ind.getNumberOfComponents()>this.schema.getMinNumOfComp() &&
				(ind.getNumberOfComponents()-ind.numberFrozenComponents()) >=2)
			return true;
		return false;
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Randomly selection of the components
	 * that will be joined
	 * @param randgen 
	 * */
	private void randomSelection(IRandGen randgen) {
		do{
			this.component1 = randgen.choose(0, this.numberOfComponents);
		}while(ind.isFrozenComponent(this.component1));
		do{
			this.component2 = randgen.choose(0, this.numberOfComponents);
		}while(this.component1==this.component2 || this.ind.isFrozenComponent(this.component2));
	}

	/**
	 * Heuristic selection of the component to be joined.
	 * Two coupled component are selected. The probability
	 * of each pair of coupled components is based on the
	 * strength of coupling
	 * @param randgen Random number generator
	 * */
	private void heuristicSelection(IRandGen randgen) {

		// Select the first component: the most coupled component that is not frozen
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		int [] connectivity = this.ind.getExternalConnections();
		int maxValue = 0;
		for(int i=0; i<this.numberOfComponents; i++){
			if(!this.ind.isFrozenComponent(i) && connectivity[i]>maxValue)
				maxValue=connectivity[i];
		}

		for(int i=0; i<this.numberOfComponents; i++)
			if(!this.ind.isFrozenComponent(i) && connectivity[i]==maxValue)
				candidates.add(i);

		if(candidates.size()>1){
			int iIndex1 = randgen.choose(0, candidates.size());
			int iIndex2; 
			do{
				iIndex2 = randgen.choose(0, candidates.size());
			}while(iIndex1==iIndex2);

			this.component1=candidates.get(iIndex1);
			this.component2=candidates.get(iIndex2);
		}

		else{
			this.component1=candidates.get(0);
			do{
				this.component2=randgen.choose(0, this.numberOfComponents);
			}while(this.component1==this.component2 || this.ind.isFrozenComponent(this.component2) || connectivity[this.component2]==0);
		}
	}

	/**
	 * Create a mutant genotype merging two components.
	 * @return The mutant genotype.
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

		int [] indDistribution = this.ind.getClassesDistribution();
		int numOfClasses = indDistribution.length;
		this.distribution = new int [numOfClasses];

		int firstComponent = (this.component1<this.component2) ? this.component1 : this.component2;
		int lastComponent = (this.component1<this.component2) ? this.component2 : this.component1;

		for(int i=0; i<numOfClasses; i++){
			if(indDistribution[i]!=this.component1 && indDistribution[i]!=this.component2){
				if(indDistribution[i] < lastComponent)
					this.distribution[i] = indDistribution[i];
				else
					this.distribution[i] = indDistribution[i]-1;
			}
			else{
				this.distribution[i] = firstComponent;
			}
		}

		this.numberOfComponents = ind.getNumberOfComponents()-1;

		////////////////////////
		/*System.out.println("Component1: " + component1 + " Component2: " +  component2 + " DISTRIBUTION:");
		for(int i=0; i<indDistribution.length; i++){
			System.out.println("Class: " + i + " Initial Component: " + indDistribution[i] + " Final Component: " + this.distribution[i]);
		}*/
	}
}