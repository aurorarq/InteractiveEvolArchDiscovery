package net.sf.jclec.sbse.discovery.imo.comparators;

import java.util.Comparator;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOFitness;

public class IndividualPreferenceComparator implements Comparator<IIndividual>{
	@Override
	public int compare(IIndividual o1, IIndividual o2) {
		double value1 = ((InteractiveMOFitness)o1.getFitness()).getPreferenceValue();
		double value2 = ((InteractiveMOFitness)o2.getFitness()).getPreferenceValue();
		if(value1 > value2) // greater values are better
			return -1;
		else if(value1 < value2)
			return 1;
		return 0;
	}
}
