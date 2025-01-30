package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;

import net.sf.jclec.ISpecies;
import net.sf.jclec.base.AbstractRecombinator;

/**
 * Abstract crossover for the discovery problem.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (September 2015)</li>
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public abstract class InteractiveMORecombinator extends AbstractRecombinator {

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -3689593503893285375L;

	/** Individual species */
	protected transient InteractiveMOSpecies species;

	/** Individuals schema */
	protected transient InteractiveMOSchema schema;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public InteractiveMORecombinator() {
		super();
	}
	
	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	protected void setPpl() {
		this.ppl = 2; // two parents
	}

	@Override
	protected void setSpl() {
		this.spl = 2; // two descendants
	}

	@Override
	protected void prepareRecombination() {
		ISpecies species = context.getSpecies();
		if (species instanceof InteractiveMOSpecies) {
			// Type conversion 
			this.species = (InteractiveMOSpecies) species;
			// Sets genotype schema
			this.schema = (InteractiveMOSchema)((InteractiveMOSpecies) species).getGenotypeSchema();
		}
		else {
			throw new IllegalStateException("Invalid species in context");
		}
	}

	@Override
	protected abstract void recombineNext();

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------- Protected methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Correct the index of the components (remove gaps)
	 * @param distribution Initial distribution
	 * @return New distribution with consecutive indexes
	 * */
	protected int [] fixComponentIndexes(int [] distribution){
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		int nClasses = distribution.length;
		int [] newDistribution = new int[nClasses];

		for(int i=0; i<nClasses; i++){
			if(!indexes.contains(distribution[i])){
				indexes.add(distribution[i]);
			}
		}
		int nComponents = indexes.size();
		int min, aux;

		// Sort the indexes
		for(int i=0; i<nComponents-1; i++){
			min = i;
			for(int j=i+1; j<nComponents; j++){
				if(indexes.get(j)<indexes.get(min)){
					min = j;
				}
			}
			aux = indexes.get(i);
			indexes.set(i, indexes.get(min));
			indexes.set(min, aux);
		}

		// Reassign the component indexes (remove gaps)
		for(int i=0; i<nClasses; i++){
			newDistribution[i] = indexes.indexOf(distribution[i]);
		}
		
		///////////////////////////
		/*for(int i=0; i<nClasses; i++){
			System.out.println(distribution[i] + " --> " + newDistribution[i]);
		}
		System.out.println("--------");*/
		
		return newDistribution;
	}
}
