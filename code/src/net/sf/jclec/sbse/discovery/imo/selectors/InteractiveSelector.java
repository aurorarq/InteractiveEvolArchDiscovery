package net.sf.jclec.sbse.discovery.imo.selectors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.IPopulation;
import net.sf.jclec.ISystem;
import net.sf.jclec.base.AbstractSelector;
import net.sf.jclec.sbse.discovery.imo.comparators.IndividualObjectivesComparator;
import net.sf.jclec.util.random.IRandGen;

/**
 * Abstract selector for interactive algorithms.
 * This class represents a generic method to select
 * the set of solutions that will be shown to the
 * expert.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (April 2015)
 * </ul>
 * 
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public abstract class InteractiveSelector extends AbstractSelector {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -6761698048026889543L;
	
	/** Fitness comparator */
	protected Comparator<IFitness> fcomparator;
	
	/** Individual comparator */
	protected Comparator<IIndividual> icomparator;
	
	/** Random number generator */
	protected IRandGen randgen;
	
	/** Number of individuals to select */
	protected int numOfSelect;
	
	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public InteractiveSelector(){
		super();
	}

	/**
	 * Parameterized constructor
	 * */
	public InteractiveSelector(ISystem context){
		super(context);
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Get/set methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get the list of individuals to select from
	 * @return List of individuals
	 * */
	public List<IIndividual> getIndividualsToSelectFrom(){
		return super.actsrc;
	}

	/**
	 * Get the number of individuals to select
	 * @return Number of individuals to select
	 * */
	public int getNumberToSelect(){
		return this.numOfSelect;
	}
	
	/**
	 * Get the fitness comparator
	 * @return Comparator
	 * */
	public Comparator<IFitness> getFitnessComparator(){
		return this.fcomparator;
	}
	
	/**
	 * Get the individual comparator
	 * @return Comparator
	 * */
	public Comparator<IIndividual> getIndividualComparator(){
		return this.icomparator;
	}
	
	/**
	 * Get the random generator
	 * @return Random generation object
	 * */
	public IRandGen getRandgen(){
		return this.randgen;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Contextualize
	 * */
	public void contextualize(ISystem context){
		if(context instanceof IPopulation) {
			this.context = (IPopulation) context;
			this.randgen = this.context.createRandGen();
			this.fcomparator = ((IPopulation)context).getEvaluator().getComparator();
			this.icomparator = new IndividualObjectivesComparator(this.fcomparator);
		}
		else {
			throw new IllegalArgumentException
				("This object uses a population as execution context");
		}
	}
	
	

	/**
	 * This method has been overridden to avoid its use.
	 * @return null
	 * */
	public final List<IIndividual> select(List<IIndividual> src) {
		return null;
	}

	/**
	 * Select a subset of solutions from the source without repetition
	 * @param source Set of individuals to select from
	 * @param nofsel Number of individuals to select
	 * @return Subset with selected individuals
	 */
	public List<IIndividual> select(List<IIndividual> source, int nofsel) {

		// Sets source set and actsrcsz
		this.actsrc = source; 
		this.actsrcsz = source.size();
		this.numOfSelect = nofsel;
		
		// Prepare selection process
		prepareSelection();

		// Performs selection of n individuals
		ArrayList<IIndividual> result = new ArrayList<IIndividual>();
		IIndividual selected;

		for (int i=0; i<nofsel; i++) {
			do{
				selected = selectNext();
			}while(result.contains(selected));
			result.add(selected);		
		}		
		// Returns selection
		return result;
	}

	/**
	 * This method has been overridden to avoid its use.
	 * @return null
	 * */
	public final List<IIndividual> select(List<IIndividual> src, int nofsel, boolean repeat) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected abstract void prepareSelection();

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected abstract IIndividual selectNext();
}
