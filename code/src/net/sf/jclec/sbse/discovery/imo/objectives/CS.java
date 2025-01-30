package net.sf.jclec.sbse.discovery.imo.objectives;

import org.apache.commons.configuration.Configuration;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 * Critical Size Metric. Number of components 
 * exceeding a critical size (threshold). 
 * From "Some theoretical considerations for 
 * a suite of metrics for the integration 
 * of software components" (2007)
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
public class CS extends Metric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 5067486815392489048L;

	/** Threshold of critically as a percentage of the number
	 * of classes in the problem */
	protected double threshold;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public CS(){
		super();
		setName("cs");
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * <p>Specific parameter for CritSize is:
	 * <ul>
	 * 	<li>size-threshold (<code>double</code>):
	 * 	Maximum percentage of classes contained
	 *  inside of a component in relation to the 
	 *  total number of classes in the initial 
	 *  analysis model.
	 * </ul>
	 * */
	@Override
	public void configure(Configuration settings) {

		super.configure(settings);

		// Configure size threshold
		Object property = settings.getProperty("objective("+getIndex()+").size-threshold");
		if(property == null){
			throw new IllegalArgumentException("A critical size threshold must be specified");
		}
		else{
			try{
				this.threshold = Double.parseDouble(property.toString());
				if(this.threshold < 0.0 || this.threshold > 1.0){
					throw new IllegalArgumentException("The critical size threshold varies between 0.0 and 1.0");
				}
			}catch(NumberFormatException e){
				throw new IllegalArgumentException("The critical size threshold must be a double value");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void prepare(InteractiveMOIndividual ind) {
		int numOfComponents = ind.getNumberOfComponents();
		int maxNumOfClasses = (int)Math.round(getDataset().getColumns().size()*this.threshold);
		double [] cc_size = new double[numOfComponents];
		for(int i=0; i<numOfComponents; i++){
			if(ind.getNumberOfClasses(i)>maxNumOfClasses){
				cc_size[i] = 1.0;
			}
			else{
				cc_size[i] = 0.0;
			}
		}
		setComponentsMeasure(cc_size);
		cc_size=null;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	public IFitness compute(InteractiveMOIndividual ind) {
		double numOfCriticalComponents = 0.0;
		int numOfComponents = ind.getNumberOfComponents();
		double [] cc_size = getComponentsMeasure();
		for(int i=0; i<numOfComponents; i++){
			numOfCriticalComponents+=cc_size[i];
		}
		double cs = (numOfCriticalComponents/(double)numOfComponents);
		return new SimpleValueFitness(cs);
	}
	
	@Override
	public void computeMaxValue(){
		//do nothing
	}
}
