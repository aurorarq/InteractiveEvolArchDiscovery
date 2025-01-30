package net.sf.jclec.sbse.discovery.imo.preferences;

import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

/**
 * This preference considers the distance between the solution
 * and a reference point. It computes an achievement scalarizing function
 * f=max(w_i*(f_i-g_i)), where w are weights associated to the objective
 * funtions, f are the objective values and g is the reference point.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (April 2016)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class SimilarityReferencePoint extends ArchitecturalPreference {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Reference point (aspiration levels) */
	protected double [] referencePoint;

	/** Weights associated to each objective function */
	protected double [] weights;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor. For test purposes.
	 * */
	public SimilarityReferencePoint(){
		super();
	}

	/**
	 * Parameterized constructor
	 * @param point the reference point (aspiration levels)
	 * @param weights the weights for each objective
	 * */
	public SimilarityReferencePoint(double [] point, double [] weights) {
		super();
		this.referencePoint = point;
		this.weights = weights;

	}

	@Override
	public double evaluatePreference(InteractiveMOIndividual individual) {
		double max = Double.NEGATIVE_INFINITY;
		double distance;

		///////////////////////////////////
		/*System.out.print("Reference point: ");
		for(int i=0; i<referencePoint.length; i++){
			System.out.print(referencePoint[i] + " ");
		}
		System.out.print("\nWeights: ");
		for(int i=0; i<this.weights.length; i++){
			System.out.print(weights[i] + " ");
		}
		System.out.print("\nObjective values:");
		for(int i=0; i<this.weights.length; i++){
			try{
				System.out.print((((MOFitness)individual.getFitness()).getObjectiveDoubleValue(i)) + " ");
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nComputing ASF...");*/
		// compute the achievement scalarizing function
		for(int i=0; i<referencePoint.length; i++){
			try {
				distance = this.weights[i]*(((MOFitness)individual.getFitness()).getObjectiveDoubleValue(i)-this.referencePoint[i]); // minimization formulation
				//System.out.println("\ti=" + i+" distance="+distance + " max=" + max);
				if(distance > 0 && distance > max)
					max = distance;
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		// Invert the preference value (minimize the achievement scalarizing function)
		if(max>0)
			max = 1.0 - max;
		else
			max = 1.0; // the point is better in all the objective values
			
		return max;
	}

	@Override
	public void print() {
		System.out.println(this.getClass().getName());
	}
}
