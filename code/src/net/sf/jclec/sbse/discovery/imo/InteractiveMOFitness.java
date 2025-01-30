package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;

import net.sf.jclec.IFitness;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;

/**
 * Fitness for the interactive multiobjective
 * discovery of software architectures.
 * 
 * <p>HISTORY:
 * <ul>
 *  <li>0.2: Add territory of iTDEA strategy (January 2016)
 *  <li>0.1: Creation (May 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * @see MOFitness
 * */
public class InteractiveMOFitness extends MOFitness{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -1092356445112432898L;

	/** The component associated with the architectural preferences */
	protected double preferenceValue;

	/** The component associated with the dominance */
	protected double dominance;

	/** Index of the metric that is used in the maximin function */
	protected int metricIndex;

	/** The territory of each solution */
	protected double territory;

	/** The favorable weight vector */
	protected double [] weights;
	
	/** The index of the region */
	protected int region;
	
	/** The value of each preference */
	protected List<Double> preferenceValues;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor.
	 */
	public InteractiveMOFitness() {
		super();
		preferenceValues = new ArrayList<>();
	}

	/**
	 * Parameterized constructor.
	 * @param components Fitness components
	 */
	public InteractiveMOFitness(IFitness [] components) {
		super();
		setObjectiveValues(components);
		preferenceValues = new ArrayList<>();
	}

	/**
	 * Parameterized constructor.
	 * @param components Fitness components
	 * @param valued Fitness value
	 */
	public InteractiveMOFitness(IFitness [] components, double value) {
		super();
		setObjectiveValues(components);
		setValue(value);
		preferenceValues = new ArrayList<>();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Get/set methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get the preference value
	 * @return Fitness component for the preferences
	 * */
	public double getPreferenceValue() {
		return this.preferenceValue;
	}

	/**
	 * Set the preference value
	 * @param preferenceValue New value
	 * */
	public void setPreferenceValue(double preferenceValue) {
		this.preferenceValue = preferenceValue;
	}

	/**
	 * Get the dominance value
	 * @return Fitness component for the dominance
	 * */
	public double getDominanceValue() {
		return this.dominance;
	}

	/**
	 * Set the dominance value
	 * @param dominanceValue New value
	 * */
	public void setDominanceValue(double dominanceValue) {
		this.dominance = dominanceValue;
	}

	/**
	 * Get the metric index
	 * @return Metric index
	 * */
	public int getMetricIndex(){
		return this.metricIndex;
	}

	/**
	 * Set the metric index
	 * @param index New metric index
	 * */
	public void setMetricIndex(int index){
		this.metricIndex = index;
	}

	/**
	 * Get the territory
	 * @return The territory
	 * */
	public double getTerritory(){
		return this.territory;
	}

	/**
	 * Set the territory
	 * @param territory The territory
	 * */
	public void setTerritory(double territory){
		this.territory = territory;
	}

	public double [] getWeights(){
		return this.weights;
	}

	public void setWeights(double [] weights){
		this.weights = new double[weights.length];
		for(int i=0; i<weights.length; i++){
			this.weights[i] = weights[i];
		}
		/*System.out.print("InteractiveMOFitness --> ");
		for(int i=0; i<this.weights.length; i++){
			System.out.print(this.weights[i] + " ");
		}
		System.out.println();*/
	}
	
	public void setRegion(int region){
		this.region = region;
	}
	
	public int getRegion(){
		return this.region;
	}
	
	public List<Double> getPreferenceValues(){
		return this.preferenceValues;
	}
	
	public void addPreferenceValue(double value){
		this.preferenceValues.add(value);
	}
	
	public double getPreferenceValueAt(int index){
		return this.preferenceValues.get(index);
	}
	
	public void setPreferenceValueAt(int index, double value){
		this.preferenceValues.add(index, value);
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	public IFitness copy() {
		// New fitness
		InteractiveMOFitness result = new InteractiveMOFitness();
		// Copy components
		if(this.components!=null){
			int cl = this.components.length;
			result.components = new IFitness[cl];
			for (int i=0; i<cl; i++)
				result.components[i] = this.components[i].copy();
		}
		// Copy value
		result.setValue(this.value);

		// Copy specific parameters
		result.preferenceValue = this.preferenceValue;
		result.dominance = this.dominance;
		result.metricIndex = this.metricIndex;
		result.territory = this.territory;
		result.region = this.region;

		if(this.weights != null){
			result.weights = new double[this.weights.length];
			for(int i=0; i<this.weights.length; i++){
				result.weights[i] = this.weights[i];
			}
		}
		
		for(int i=0; i<this.preferenceValues.size(); i++){
			result.addPreferenceValue(this.preferenceValues.get(i));
		}

		// Returns result
		return result;
	}

	/**
	 * Compare the fitness with other object
	 * @param other Object to compare with.
	 * @return True if objects are equals, false otherwise.
	 * */
	@Override
	public boolean equals(Object other) {
		if (other instanceof InteractiveMOFitness) {
			InteractiveMOFitness coth = (InteractiveMOFitness) other;

			// Comparing simple properties
			int cl  = this.components.length;
			int ocl = coth.components.length;
			EqualsBuilder eb = new EqualsBuilder();
			eb.append(this.value, coth.value);
			eb.append(this.dominance, coth.dominance);
			eb.append(this.preferenceValue, coth.preferenceValue);
			
			//System.out.println("\t value1= " + this.value + " value2= " +coth.value + " equals value => " + (this.value == coth.value) );
			//System.out.println("\t dom1= " + this.dominance + " dom2= " +coth.dominance + "equals dom => " + (this.dominance == coth.dominance) );
			//System.out.println("\t pref1= " + this.preferenceValue + " pref2= " +coth.preferenceValue + " equals pref => " + (this.preferenceValue == coth.preferenceValue));
			//System.out.println("\t equals builder => " + eb.isEquals());
			
			if(eb.isEquals()){
				if (cl == ocl ){//&&) this.value == coth.value &&
				//this.dominance == coth.dominance && this.preferenceValue == coth.preferenceValue) {
					//EqualsBuilder 
				eb = new EqualsBuilder();
					for (int i=0; i<cl; i++) 
						eb.append(this.components[i], coth.components[i]);
					return eb.isEquals();
				}
				else {
					return false;
				}
			}
			else{
				return false;
			}
		}
		else {
			return false;
		}
	}
}