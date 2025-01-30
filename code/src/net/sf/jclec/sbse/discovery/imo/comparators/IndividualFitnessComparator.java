package net.sf.jclec.sbse.discovery.imo.comparators;

import java.util.Comparator;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOFitness;

public class IndividualFitnessComparator implements Comparator<IIndividual>{
	@Override
	public int compare(IIndividual o1, IIndividual o2) {
		double value1 = ((InteractiveMOFitness)o1.getFitness()).getValue();
		double value2 = ((InteractiveMOFitness)o2.getFitness()).getValue();
		if(value1 < value2) // minimization problem
			return -1;
		else if(value1 > value2)
			return 1;
		return 0;
	}

}
