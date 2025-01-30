package net.sf.jclec.sbse.discovery.imo.selectors;

import net.sf.jclec.IIndividual;
import net.sf.jclec.ISystem;
import net.sf.jclec.util.random.IRandGen;

/**
 * This selector chooses solutions at random.
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
public class PartialRandomSelector extends InteractiveSelector {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** SerialID */
	private static final long serialVersionUID = 8136591849422493269L;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public PartialRandomSelector() {
		super();
	}

	/**
	 * Parameterized constructor
	 * @param context Execution context
	 * */
	public PartialRandomSelector(ISystem context){
		super(context);
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void prepareSelection() {
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IIndividual selectNext() {
		IRandGen randgen = getRandgen();
		int size = getNumberToSelect();
		// Select one individual at random
		IIndividual selected = getIndividualsToSelectFrom().get(randgen.choose(0, size));
		return selected;
	}
}
