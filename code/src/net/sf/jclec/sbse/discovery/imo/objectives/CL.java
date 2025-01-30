package net.sf.jclec.sbse.discovery.imo.objectives;

import org.apache.commons.configuration.Configuration;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 * Critical Link Metric. Number of components 
 * exceeding a critical number of links, 
 * i.e. the threshold). Links are the
 * provided interfaces. 
 * From "Some theoretical considerations for 
 * a suite of metrics for the integration 
 * of software components" (2007)
 * 
 * <p>History:
 * <ul>
 *	<li>1.0: Creation (February 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * @see Metric
 * */
public class CL extends Metric {

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -3787656899004741455L;	

	/** Critical threshold */
	protected int threshold;

	/////////////////////////////////////////////////////////////////
	//-------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public CL() {
		super();
		setName("cl");
	}

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * <p>Specific parameter for CritSize is:
	 * <ul>
	 * 	<li>link-threshold (<code>int</code>):
	 * 	Maximum number of links.
	 * </ul>
	 * */
	@Override
	public void configure(Configuration settings) {

		super.configure(settings);

		// Configure link threshold
		Object property = settings.getProperty("objective("+getIndex()+").link-threshold");
		if(property == null){
			throw new IllegalArgumentException("A critical link threshold must be specified");
		}
		else{
			try{
				this.threshold = Integer.parseInt(property.toString());
				if(this.threshold < 0){
					throw new IllegalArgumentException("The critical size threshold must be greater than 0");
				}
			}catch(NumberFormatException e){
				throw new IllegalArgumentException("The critical link threshold must be an integer value");
			}
		}
	}

	@Override
	protected void prepare(InteractiveMOIndividual ind) {
		int numOfComponents = ind.getNumberOfComponents();
		double [] cc_link = new double[numOfComponents];
		for(int i=0; i<numOfComponents; i++){
			if(ind.getNumberOfProvided(i)>this.threshold){
				cc_link[i] = 1.0;
			}
			else{
				cc_link[i] = 0.0;
			}
		}
		setComponentsMeasure(cc_link);
		cc_link=null;
	}

	@Override
	protected IFitness compute(InteractiveMOIndividual ind) {
		double numOfCriticalComponents = 0.0;
		int numOfComponents = ind.getNumberOfComponents();
		double [] cc_link = getComponentsMeasure();
		for(int i=0; i<numOfComponents; i++){
			numOfCriticalComponents+=cc_link[i];
		}
		double cl = (numOfCriticalComponents/(double)numOfComponents);
		return new SimpleValueFitness(cl);
	}
	
	@Override
	public void computeMaxValue(){
		//do nothing
	}
}
