package net.sf.jclec.sbse.discovery.imo.objectives;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 * Abstractness Metric. Number of components 
 * exceeding a critical size (threshold). 
 * From "Reconstruction of Software Component 
 * Architectures and Behaviour Models using 
 * Static and Dynamic Analysis" (2010)
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (February 2015)
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
	protected void prepare(InteractiveMOIndividual ind) {
		int numOfComponents = ind.getNumberOfComponents();
		double [] abs = new double[numOfComponents];
		int [] numOfAbstractClasses = ind.getNumberOfAbstractClasses();
		for(int i=0; i<numOfComponents; i++){
			abs[i] = (double)numOfAbstractClasses[i]/(double)ind.getNumberOfClasses(i);
		}
		setComponentsMeasure(abs);
		abs=null;
	}

	@Override
	protected IFitness compute(InteractiveMOIndividual ind) {
		// Abstractness of each component
		int numOfComponents = ind.getNumberOfComponents();
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
	
	@Override
	public void computeMaxValue(){
		//do nothing
	}
}