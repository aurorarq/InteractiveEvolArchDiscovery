package net.sf.jclec.sbse.discovery.mut;

import net.sf.jclec.IIndividual;
import net.sf.jclec.IPopulation;
import net.sf.jclec.ISpecies;
import net.sf.jclec.JCLEC;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.Schema;
import net.sf.jclec.sbse.discovery.Species;
import net.sf.jclec.util.random.IRandGen;

/**
 * Abstract class for mutation operations with
 * domain knowledge in 'Classes to Components' (Cl2Cmp) problem.
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 2.0
 * History:
 * <ul>
 * 	<li>1.0: Creation (February 2013)
 * 	<li>2.0: Refactoring (July 2013)
 * </ul>
 * */
public abstract class AbstractCmpMutator implements JCLEC{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////
	
	/** Serial version */
	private static final long serialVersionUID = 750616693095634501L;

	/** Individual species */
	protected transient Species species;

	/** Individuals schema */
	protected transient Schema schema;
	
	/** Mutator weight */
	private double weight;
	
	/** Random behavior */
	protected boolean isRandom;
	
	/** Individual to be mutated */
	protected Individual ind;
	
	//////////////////////////////////////////////////////////////////
	//-------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public AbstractCmpMutator(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------- Public methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Contextualize execution
	 * @param context Population context execution
	 * */
	public void contextualize(IPopulation context){
		ISpecies species = context.getSpecies();
		if (species instanceof Species) {
			// Type conversion 
			this.species = (Species) species;
			// Sets genotype schema
			this.schema = ((Species) species).getGenotypeSchema();
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
	public abstract IIndividual mutateIndividual(Individual ind, IRandGen randgen);

	/**
	 * The mutator is applicable over the individual. This method must 
	 * be invoked before mutateIndividual() to guarantee correct mutation.
	 * @param The individual to be mutated
	 * @return True if the mutation is applicable, false otherwise
	 * */
	public abstract boolean isApplicable(Individual ind);
	
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
	 * Set random behaviour
	 * @param isRandom True if mutator is random, false otherwise 
	 * */
	public void setRandom(boolean isRandom){
		this.isRandom = isRandom;
	}
}