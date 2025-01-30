package net.sf.jclec.sbse.discovery.imo.preferences;

import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 * An abstract architectural preference in the
 * 'Classes to Components' (CL2CMP) problem.
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
 */
public abstract class ArchitecturalPreference {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Number of solutions that satisfy this preference */
	protected int numOfSolutions;

	/** Generation in which was created */
	protected int generation;
	
	/** Priority */
	protected double priority;
	
	/** Threshold to consider that a preference is satisfied */
	protected double threshold = 0.8;
	
	/** Confidence of the user */
	protected double confidence;
	
	/** Scaled confidence */
	protected double scaledConfidence;
	
	/** Added in the last interaction? */
	protected boolean addedLastInteraction;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public ArchitecturalPreference() {
		this.numOfSolutions = 0;
		this.generation = -1;
		this.priority = 1.0; // default value if priorities are not considered
		this.confidence = 3.0;
		this.scaledConfidence = 1.0; // default value if confidences are not considered
	}

	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Get/Set methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Get the number of solutions that satisfy this preference
	 * @return Number of solutions
	 * */
	public int getNumberOfSolutions(){
		return this.numOfSolutions;
	}
	
	/**
	 * Set the number of solutions that satisfy this preference
	 * @param numOfSolutions New value
	 * */
	public void setNumberOfSolutions(int numOfSolutions){
		this.numOfSolutions = numOfSolutions;
	}
	
	/**
	 * Get the threshold
	 * @return Threshold
	 * */
	public double getThreshold(){
		return this.threshold;
	}
	
	/**
	 * Set the threshold
	 * @param threshold New threshold
	 * */
	protected void setThreshold(double threshold){
		this.threshold = threshold;
	}
		
	/**
	 * Get the generation
	 * @return Generation
	 * */
	public int getGeneration() {
		return generation;
	}

	/**
	 * Set the generation
	 * @param generation New value
	 * */
	public void setGeneration(int generation) {
		this.generation = generation;
	}

	/**
	 * Get the priority
	 * @return priority
	 * */
	public double getPriority() {
		return priority;
	}

	/**
	 * Set the priority
	 * @param priority New priority
	 * */
	public void setPriority(double priority) {
		this.priority = priority;
	}
	
	/**
	 * Get the confidence
	 * @return confidence
	 * */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * Set the confidence
	 * @param confidence New confidence
	 * */
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	/**
	 * Get the scaled confidence
	 * @return scaled confidence
	 * */
	public double getScaledConfidence() {
		return scaledConfidence;
	}

	/**
	 * Set the scaled confidence
	 * @param confidence New scaled confidence
	 * */
	public void setScaledConfidence(double confidence) {
		this.scaledConfidence = confidence;
	}
	
	public void setAddedInLastInteraction(boolean addedLastInteraction){
		this.addedLastInteraction = addedLastInteraction;
	}
	
	public boolean wasAddedInLastInteraction(){
		return this.addedLastInteraction;
	}
	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Abstract methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Check in which degree the individual meet the criterion
	 * established by the preference
	 * @param individual The individual to be evaluated
	 * @return A value representing the quality of the individual
	 * with respect to the property
	 * */
	public abstract double evaluatePreference(InteractiveMOIndividual individual);

	/**
	 * Print information of the preference
	 * For testing purposes.
	 * */
	public abstract void print();
}