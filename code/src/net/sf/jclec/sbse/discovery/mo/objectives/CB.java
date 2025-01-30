package net.sf.jclec.sbse.discovery.mo.objectives;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.Individual;

/**
 * Component Balance metric. This metric
 * evaluates the balance between the number
 * of components and their sizes. 
 * From "Quantifying the analyzability
 * of software architectures" (2011)
 * 
 * <p>History:
 * <ul>
 *	<li>1.0: Creation (June 2014)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * @see Metric
 * */
public class CB extends Metric {

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 3339545653904472016L;

	/////////////////////////////////////////////////////////////////
	//-------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public CB() {
		super();
		setName("cb");
	}

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	protected void prepare(Individual solution) {
		int numberOfComponents = solution.getNumberOfComponents();
		double [] numOfClasses = new double[numberOfComponents];
		for(int i=0; i<numberOfComponents; i++){
			numOfClasses[i] = solution.getNumberOfClasses(i);
		}
		setComponentsMeasure(numOfClasses);
		numOfClasses = null;
	}

	@Override
	protected IFitness compute(Individual solution) {
		double res;
		double sb, csu;

		// System breakdown value
		int opt = (int)(((float)getMaxComponents()+(float)getMinComponents())/2.0);
		int n = solution.getNumberOfComponents();
		int min = getMinComponents();
		int max = getMaxComponents();
		if(min!=max){
			if(n<opt){
				sb = ((float)(n-min))/((float)(opt-min));
			}
			else{
				sb = 1-((float)(n-opt)/(float)(max-opt));
			}
		}
		else
			sb = 1;

		// Component size uniformity
		double [] sizes = new double[n];
		for(int i=0; i<n; i++){
			sizes[i] = solution.getNumberOfClasses(i);
		}
		csu = 1.0 - giniCoefficient(sizes);

		// Result (invert to minimization problem)
		res = 1.0-(sb*csu);
		return new SimpleValueFitness(res);
	}

	/**
	 * Calculate the gini coefficient
	 * for a given array of values
	 * @param sizes The array with the size of each component
	 * @return Gini coefficient
	 *  */
	protected double giniCoefficient(double [] sizes){

		// Order the sizes
		double aux;
		for(int i=0; i<sizes.length-1; i++)
			for(int j=i+1; j<sizes.length; j++)
				if(sizes[i] > sizes[j]){
					aux = sizes[j];
					sizes[j] = sizes[i];
					sizes[i] = aux;
				}

		// Accumulate
		double acc [] = new double[sizes.length];
		for(int i=0; i<acc.length; i++){
			acc[i]=sizes[i];
		}
		for(int i=1; i<acc.length;i++){
			acc[i] += acc[i-1];
		}
		
		// Gini coefficient
		double area_ab = (acc[acc.length-1]*acc.length)/2.0;
		double area_b = acc[0]/2.0;	
		for(int i=1; i<acc.length; i++){
			area_b += (acc[i-1]+acc[i])/2.0;
		}
		double gini = (area_ab-area_b)/area_ab;
		return gini;
	}
}