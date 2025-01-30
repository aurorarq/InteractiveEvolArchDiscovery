package net.sf.jclec.sbse.discovery.imo.objectives;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Dataset;
import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.evaluation.Objective;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 * An abstract objective for the evaluation of
 * a desirable design characteristic in the
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
 * @see Objective
 * */
public abstract class Metric extends Objective {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -5228349782111999472L;

	/** Dataset that contains the problem information (analysis model) */
	protected Dataset dataset;

	/** Objective name */
	protected String name;

	/** Maximum number of components */
	private int maxComponents;
	
	/** Minimum number of components */
	private int minComponents;
	
	/** Maximum number of interfaces */
	private int maxInterfaces;
	
	/** Measure per component */
	private double [] componentsMeasure;
		
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public Metric(){
		super();
	}

	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Get/set methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Get the dataset
	 * @return The dataset
	 * */
	public Dataset getDataset(){
		return this.dataset;
	}

	/**
	 * Set the dataset
	 * @param dataset The dataset
	 * */
	public void setDataset(Dataset dataset){
		this.dataset = dataset;
	}
	
	/**
	 * Get the objective name
	 * @return The objective name
	 * */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Set the objective name
	 * @param name The new name
	 * */
	protected void setName(String name){
		this.name = name;
	}
	
	/**
	 * Get the maximum number
	 * of components
	 * @return maximum number
	 * of components configured
	 * */
	public int getMaxComponents(){
		return this.maxComponents;
	}
	
	/**
	 * Set the maximum number
	 * of components
	 * @param maxComponents The
	 * maximum number of components
	 * */
	public void setMaxComponents(int maxComponents){
		this.maxComponents = maxComponents;
	}
	
	/**
	 * Get the minimum number
	 * of components
	 * @return minimum number
	 * of components configured
	 * */
	public int getMinComponents(){
		return this.minComponents;
	}
	
	/**
	 * Set the minimum number
	 * of components
	 * @param minComponents The
	 * maximum number of components
	 * */
	public void setMinComponents(int minComponents){
		this.minComponents = minComponents;
	}
	
	/**
	 * Get the maximum number
	 * of candidate interfaces
	 * @return maximum number
	 * of interfaces extracted
	 * from the model
	 * */
	public int getMaxInterfaces(){
		return this.maxInterfaces;
	}
	
	/**
	 * Set the maximum number
	 * of candidate interfaces
	 * @param maxInterfaces The
	 * maximum number of components
	 * */
	public void setMaxInterfaces(int maxInterfaces){
		this.maxInterfaces = maxInterfaces;
	}
	
	public double [] getComponentsMeasure(){
		return this.componentsMeasure;
	}
	
	protected void setComponentsMeasure(double [] values){
		int size = values.length;
		this.componentsMeasure = new double[size];
		for(int i=0; i<size; i++){
			this.componentsMeasure[i] = values[i];
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	public void configure(Configuration settings) {
		super.configure(settings);
	}
	
	/**
	 * {@inheritDoc}
	 * The computation of the objective will be
	 * performed in two steps:
	 * <p>Prepare the computation (get auxiliary measures)
	 * <p>Compute the objective value
	 * */
	@Override
	public IFitness evaluate(IIndividual ind){
		// Prepare and compute the objective value
		prepare((InteractiveMOIndividual)ind);
		return compute((InteractiveMOIndividual)ind);
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Abstract methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Prepare the computation. Required characteristics
	 * are taken from the individual.
	 * @param ind The individual
	 * */
	protected abstract void prepare(InteractiveMOIndividual ind);

	/**
	 * Compute the objective value. <code>prepare()</code> 
	 * should be executed before to prepare computation.
	 * @see prepare(IIndividual ind)
	 * @return Result of the metric
	 * */
	protected abstract IFitness compute(InteractiveMOIndividual ind);
	
	/**
	 * Compute the maximum objective value. It should be used
	 * when the maximum depends on the problem instance.
	 * */
	public abstract void computeMaxValue();
}
