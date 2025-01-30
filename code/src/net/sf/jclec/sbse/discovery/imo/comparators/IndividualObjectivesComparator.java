package net.sf.jclec.sbse.discovery.imo.comparators;

import java.util.Comparator;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;


public class IndividualObjectivesComparator implements Comparator<IIndividual> {

	Comparator<IFitness> fcomparator;
	
	public IndividualObjectivesComparator(Comparator<IFitness> fcomparator) {
		super();
		this.fcomparator = fcomparator;
	}
	
	@Override
	public int compare(IIndividual o1, IIndividual o2) {
		return this.fcomparator.compare(o1.getFitness(), o2.getFitness());
	}
}
