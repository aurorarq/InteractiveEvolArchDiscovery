package net.sf.jclec.sbse.discovery.mo.objectives;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.Individual;

/**
 * Abstractness Metric. Number of components 
 * exceeding a critical size (threshold). 
 * From "Reconstruction of Software Component 
 * Architectures and Behaviour Models using 
 * Static and Dynamic Analysis" (2010)
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
 * @see Metric
 * */
public class ABS extends Metric {

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -8739015307154669949L;

	/////////////////////////////////////////////////////////////////
	//-------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public ABS() {
		super();
		setName("abs");
	}

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	protected void prepare(Individual solution) {
		int numOfComponents = solution.getNumberOfComponents();
		double [] abs = new double[numOfComponents];
		int [] numOfAbstractClasses = solution.getNumberOfAbstractClasses();
		for(int i=0; i<numOfComponents; i++){
			abs[i] = (double)numOfAbstractClasses[i]/(double)solution.getNumberOfClasses(i);
		}
		setComponentsMeasure(abs);
		abs=null;
	}

	@Override
	protected IFitness compute(Individual solution) {
		// Abstractness of each component
		int numOfComponents = solution.getNumberOfComponents();
		double avg = 0.0;
		double [] abs = getComponentsMeasure();
		for(int i=0; i<numOfComponents; i++){
			avg += abs[i];
		}	
		// Mean abstractness in the architecture
		avg /= (double)numOfComponents;
		// Invert the value (minimization problem)
		avg = 1-avg;
		return new SimpleValueFitness(avg);
	}
}