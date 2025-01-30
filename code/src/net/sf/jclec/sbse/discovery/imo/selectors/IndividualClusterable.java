package net.sf.jclec.sbse.discovery.imo.selectors;

import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;

import org.apache.commons.math3.ml.clustering.Clusterable;

public class IndividualClusterable implements Clusterable {

	private double [] points;
	
	private IIndividual individual;
	
	public IndividualClusterable(IIndividual individual){
		
		this.individual = individual;
		MOFitness fitness;
		int n;
		
		if(individual.getFitness() instanceof MOFitness){
			fitness = (MOFitness)individual.getFitness();
			n = fitness.getNumberOfObjectives();
			
			this.points = new double[n];
			for(int i=0; i<n; i++){
				try {
					this.points[i] = fitness.getObjectiveDoubleValue(i);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public IIndividual getIndividual(){
		return individual;
	}
	
	@Override
	public double[] getPoint() {
		return points;
	}

}
