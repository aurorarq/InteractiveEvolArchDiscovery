package net.sf.jclec.sbse.discovery.imo.selectors;

import java.util.Collections;

import net.sf.jclec.IIndividual;
import net.sf.jclec.ISystem;

/**
 * This selector orders the population and select
 * individuals at every p/N positions, where p is the population
 * size and N the number of solutions to select.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (April 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class PartialSequentialSelector extends InteractiveSelector {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 6052282359374654303L;

	/** Current position */
	protected int currentPos;
	
	/** Step */
	protected int step;
	
	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public PartialSequentialSelector() {
		super();
		this.currentPos = -1;
		this.step = -1;
	}

	/**
	 * Parameterized constructor
	 * @param context Execution context
	 * */
	public PartialSequentialSelector(ISystem context){
		super(context);
		this.currentPos = -1;
		this.step = -1;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void prepareSelection() {
		// Other the source
		Collections.sort(getIndividualsToSelectFrom(), getIndividualComparator());
		this.currentPos = 0;
		this.step = (int)((double)getIndividualsToSelectFrom().size()/(double)getNumberToSelect());
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IIndividual selectNext() {
		
		// Get the individual in the current position
		IIndividual selected = getIndividualsToSelectFrom().get(currentPos);
		
		// Set the next position
		this.currentPos += this.step;
		
		// Avoid out of range exception
		if(this.currentPos > getIndividualsToSelectFrom().size()){
			this.currentPos = 0;
		}
		return selected;
	}
}
