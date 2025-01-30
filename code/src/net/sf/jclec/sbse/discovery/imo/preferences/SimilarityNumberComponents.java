package net.sf.jclec.sbse.discovery.imo.preferences;

import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 * This preference considers the similarity between architectures
 * regarding the number of components.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (May 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class SimilarityNumberComponents extends ArchitecturalPreference{

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Preferable number of components */
	protected int ideal;

	/** Minimum number of components */
	protected int minimum;

	/** Maximum number of components */
	protected int maximum;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor. For test purposes.
	 * */
	public SimilarityNumberComponents(){
		super();
	}
	
	/**
	 * Parameterized constructor
	 * @param minimum Minimum number of components
	 * @param maximum Maximum number of components
	 * */
	public SimilarityNumberComponents(int minimum, int maximum) {
		super();
		this.minimum = minimum;
		this.maximum = maximum;
	}

	/**
	 * Parameterized constructor
	 * @param minimum Minimum number of components
	 * @param maximum Maximum number of components
	 * @param ideal Ideal number of components
	 * */
	public SimilarityNumberComponents(int minimum, int maximum, int ideal) {
		super();
		this.minimum = minimum;
		this.maximum = maximum;
		this.ideal = ideal;
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	public double evaluatePreference(InteractiveMOIndividual individual) {
		int n = individual.getNumberOfComponents();
		double result;
		if(this.ideal == n){
			result = 1.0;
		}
		else if(n<this.ideal){
			result = ((double)(n-this.minimum))/((double)(this.ideal-this.minimum));
		}
		else{
			result = 1-((double)(n-this.ideal)/(double)(this.maximum-this.ideal));
		}
		return result;
	}
	
	public void print(){
		System.out.println(this.getClass().getName());
	}
}