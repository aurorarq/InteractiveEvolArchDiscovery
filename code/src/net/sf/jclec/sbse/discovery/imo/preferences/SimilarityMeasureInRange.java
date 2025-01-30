package net.sf.jclec.sbse.discovery.imo.preferences;

import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 *  This preference considers how good is an individual
 *  when comparing an objective value to the preferable
 *  range specified by the architect.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (February 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * 
 * @version 1.0
 * */
public class SimilarityMeasureInRange extends ArchitecturalPreference {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** The index of the selected metric */
	protected int index;

	/** The minimum value allowed */
	protected double minValue;

	/** The maximum value allowed */
	protected double maxValue;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor. For test purposes.
	 * */
	public SimilarityMeasureInRange(){
		super();
	}

	/**
	 * Parameterized constructor.
	 * @param index The index of the metric
	 * @param min The minimum value
	 * @param max The maximum value
	 * */
	public SimilarityMeasureInRange(int index, double min, double max) {
		super();
		setMeasureParameters(index, min, max);
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	public double evaluatePreference(InteractiveMOIndividual individual) {

		// Get the index of the objective function
		/*String [] objsNames = individual.getMeasuresNames();
		int index = -1;
		int nObjs = objsNames.length;

		for(int i=0; index==-1 && i<nObjs; i++){
			if(this.objectiveName.equalsIgnoreCase(objsNames[i]))
				index = i;
		}*/

		// Compare the value
		double value = -1;
		double similarity = -1;
		try {
			value = ((MOFitness)individual.getFitness()).getObjectiveDoubleValue(index);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}

		if(value!=-1){
			similarity = similarity(value);
		}
		return similarity;
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Set the parameters of the preference
	 * @param name The objective function name
	 * @param min The minimum value
	 * @param max The maximum value
	 * */
	public void setMeasureParameters(int index, double min, double max){
		this.index = index;
		this.minValue = min;
		this.maxValue = max;
	}

	/**
	 * Compute the similarity value considering the position
	 * of the value with respect to the range [min,max]
	 * */
	public double similarity(double value){
		double result;
		double mean;
		if(value < this.minValue || value > this.maxValue){
			result = 0;
		}
		// Compute the distance to the mean
		else{
			mean = this.maxValue - this.minValue;
			if(mean!=0)
				result = 1.0 - (((value-mean)*(value-mean))/mean);
			else{
				result = 1.0;
			}
		}
		return result;
	}
	
	public void print(){
		System.out.println(this.getClass().getName());
	}
}