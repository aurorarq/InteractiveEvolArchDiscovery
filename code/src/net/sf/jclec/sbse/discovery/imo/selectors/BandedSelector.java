package net.sf.jclec.sbse.discovery.imo.selectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jclec.IIndividual;
import net.sf.jclec.ISystem;
import net.sf.jclec.util.random.IRandGen;

/**
 * This selector divides the population in bands
 * and one individual from each band is selected
 * as random.
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

public class BandedSelector extends InteractiveSelector {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -5533472032291469238L;

	/** Number of bands */
	protected int nbands;

	/** Bands of individuals */
	protected List<ArrayList<IIndividual>> bands;

	/** Current band */
	protected int currentBand;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public BandedSelector() {
		super();
		this.nbands = -1;
		this.currentBand = -1;
	}

	/**
	 * Parameterized constructor
	 * @param context Execution context
	 * */
	public BandedSelector(ISystem context){
		super(context);
		this.nbands = -1;
		this.currentBand = -1;
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Get/set methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get number of bands
	 * @return Number of bands
	 * */
	public int getNumberOfBands(){
		return this.nbands;
	}

	/**
	 * Set number of bands
	 * @param nbands Number of bands
	 * */
	public void setNumberOfBands(int nbands){
		this.nbands = nbands;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void prepareSelection() {

		// Sort the individuals
		List<IIndividual> orderedIndividuals = getIndividualsToSelectFrom();
		Collections.sort(orderedIndividuals, getIndividualComparator());

		// Prepare the bands
		this.bands = new ArrayList<ArrayList<IIndividual>>();

		for(int i=0; i<nbands; i++){
			this.bands.add(new ArrayList<IIndividual>());
		}

		// Fill the bands
		int band = 0;
		int size = orderedIndividuals.size()/nbands;
		int nInds = 0;
		for(int i=0; i<orderedIndividuals.size(); i++){

			this.bands.get(band).add(orderedIndividuals.get(i));
			nInds++;

			if(nInds == size){
				band++;
				nInds = 0;
			}
		}

		this.currentBand = 0;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IIndividual selectNext() {
		IIndividual selected = null;
		int size = this.bands.get(this.currentBand).size();
		IRandGen randgen = getRandgen();
		
		// Select one individual from the current band
		selected = this.bands.get(this.currentBand).get(randgen.choose(0, size));

		// Increment the current band for the next selection
		this.currentBand++;
		return selected;
	}
}