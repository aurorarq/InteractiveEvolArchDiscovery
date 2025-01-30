package net.sf.jclec.sbse.discovery.imo.comparators;

import java.util.Comparator;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOFitness;

public class IndividualMaximinComparator implements Comparator<IIndividual>{
	@Override
	public int compare(IIndividual o1, IIndividual o2) {
		double value1 = ((InteractiveMOFitness)o1.getFitness()).getDominanceValue();
		double value2 = ((InteractiveMOFitness)o2.getFitness()).getDominanceValue();
		if(value1 < value2) // smaller values are better
			return -1;
		else if(value1 > value2)
			return 1;
		return 0;
	}
}
