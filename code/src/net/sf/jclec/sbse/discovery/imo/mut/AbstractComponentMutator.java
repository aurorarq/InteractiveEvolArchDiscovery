package net.sf.jclec.sbse.discovery.imo.mut;

import net.sf.jclec.IIndividual;
import net.sf.jclec.IPopulation;
import net.sf.jclec.ISpecies;
import net.sf.jclec.JCLEC;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOSchema;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOSpecies;
import net.sf.jclec.util.random.IRandGen;

/**
 * Abstract class for mutation operations with
 * domain knowledge for the interactive discovery
 * of software architectures.
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (May 2015)</li>
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public abstract class AbstractComponentMutator implements JCLEC{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = 750616693095634501L;

	/** Individual species */
	protected transient InteractiveMOSpecies species;

	/** Individuals schema */
	protected transient InteractiveMOSchema schema;

	/** Mutator weight */
	private double weight;

	/** Random behavior */
	protected boolean isRandom;

	/** Individual to be mutated */
	protected InteractiveMOIndividual ind;
	
	/** New classes distribution */
	protected int [] distribution;
	
	/** New number of components */
	protected int numberOfComponents;

	//////////////////////////////////////////////////////////////////
	//-------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public AbstractComponentMutator(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------- Public methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Contextualize execution
	 * @param context Execution context
	 * */
	public void contextualize(IPopulation context){
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

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Abstract methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Mutate an individual
	 * @param ind: The individual to be mutated
	 * @param rangen: The random generator
	 * @return The mutated individual
	 * */
	public abstract IIndividual mutateIndividual(InteractiveMOIndividual ind, IRandGen randgen);

	/**
	 * The mutator is applicable over the individual. This method must 
	 * be invoked before mutateIndividual() to guarantee correct mutation.
	 * @param The individual to be mutated
	 * @return True if the mutation is applicable, false otherwise
	 * */
	public abstract boolean isApplicable(InteractiveMOIndividual ind);

	/**
	 * Get the mutator weight in the mutator roulette
	 * @return Mutator weight
	 * */
	public double getWeight(){
		return this.weight;
	}

	/**
	 * Set the mutator weight in the mutator roulette
	 * @param weight Mutator weight
	 * */
	public void setWeight(double weight){
		this.weight = weight;
	}

	/**
	 * Return if mutator acts randomly or not
	 * @return Value of isRandom
	 * */
	public boolean isRandom(){
		return this.isRandom;
	}

	/**
	 * Set random behavior
	 * @param isRandom True if mutator is random, false otherwise 
	 * */
	public void setRandom(boolean isRandom){
		this.isRandom = isRandom;
	}
	
	/// VERBOSE
	public void printDistribution(){
		
		for(int i=0; i<distribution.length; i++){
			System.out.println("class: " 
					+ this.species.getDataset().getColumn(i).getName() 
					+ " component: " + this.distribution[i]);
		}
		
	}
}