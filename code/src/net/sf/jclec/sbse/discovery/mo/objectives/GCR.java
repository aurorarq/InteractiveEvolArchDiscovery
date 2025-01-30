package net.sf.jclec.sbse.discovery.mo.objectives;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.Individual;

/**
 * Groups/components ratio (GCR) metric for the
 * discovery problem.
 * 
 * <p>History:
 * <ul>
 * 	<li>2.0: Now extending Metric (June 2014)
 * 	<li>1.0: Creation (December 2013)
 * </ul>
 * 
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
	protected void prepare(Individual solution) {
		int numberOfComponents = solution.getNumberOfComponents();
		double [] groups = new double[numberOfComponents];
		for(int i=0; i<numberOfComponents; i++){
			groups[i] = solution.getNumberOfGroups(i);
		}
		setComponentsMeasure(groups);
		groups = null;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IFitness compute(Individual solution) {
		int numberOfComponents = solution.getNumberOfComponents();
		double numberOfGroups = 0.0;
			
		// Total number of groups
		for(int i=0; i<numberOfComponents; i++){
			numberOfGroups += solution.getNumberOfGroups(i);
		}
		double aux = (double)numberOfGroups/(double)numberOfComponents;
		double maxValue = getDataset().getColumns().size() / (double) numberOfComponents;
		double gcr = ((aux-1.0)/(maxValue-1.0)); // normalize
		return new SimpleValueFitness(gcr);
	}
}