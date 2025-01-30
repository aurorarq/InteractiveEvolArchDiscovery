package net.sf.jclec.sbse.discovery.imo.objectives;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 * Groups/components ratio (GCR) metric for the
 * 'Classes to Components' (CL2CMP) problem.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (February 2015)
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class GCR extends Metric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////
	
	/** Serial ID */
	private static final long serialVersionUID = -8339765927385885414L;

	/** Maximum value */
	double maxValue;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public GCR(){
		super();
		setName("gcr");
	}
	
	/////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void prepare(InteractiveMOIndividual ind) {
		int numberOfComponents = ind.getNumberOfComponents();
		double [] groups = new double[numberOfComponents];
		for(int i=0; i<numberOfComponents; i++){
			groups[i] = ind.getNumberOfGroups(i);
		}
		setComponentsMeasure(groups);
		groups = null;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IFitness compute(InteractiveMOIndividual ind) {
		int numberOfComponents = ind.getNumberOfComponents();
		double numberOfGroups = 0.0;
			
		// Total number of groups
		for(int i=0; i<numberOfComponents; i++){
			numberOfGroups += ind.getNumberOfGroups(i);
		}
		double aux = (double)numberOfGroups/(double)numberOfComponents;
		double maxValue = getDataset().getColumns().size() / (double) numberOfComponents;
		double gcr = ((aux-1.0)/(maxValue-1.0)); // normalize
		return new SimpleValueFitness(gcr);
	}
	
	@Override
	public void computeMaxValue(){
		// do nothing
		// it will be computed for each individual
	}
}