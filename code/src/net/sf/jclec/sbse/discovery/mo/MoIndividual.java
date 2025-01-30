package net.sf.jclec.sbse.discovery.mo;

import org.apache.commons.lang.builder.EqualsBuilder;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.IConstrained;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Individual for the multi-objective discovery problem. The main difference between
 * the ranking-based problem is the storage of metrics, since
 * each considered metric is saved in the fitness. It also
 * implements the IConstrained interface.
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * 
 * @version 2.0
 * History:
 * <ul>
 * 	<li>2.0: Adaptation to jclec-moea module (May 2014)
 * 	<li>1.0: Creation (December 2013)
 * </ul>
 * @see IConstrained
 * */
public class MoIndividual extends Individual implements IConstrained {

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -1994897763447657423L;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Parameterized constructor.
	 * @param genotype Individual genotype
	 */
	public MoIndividual(SyntaxTree genotype) {
		super(genotype);
	}

	/**
	 * Parameterized constructor.
	 * @param genotype Individual genotype
	 * @param fitness  Individual fitness
	 */
	public MoIndividual(SyntaxTree genotype, IFitness fitness) {
		super(genotype,fitness);
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/** 
	 * Copy the individual
	 * @return A copy of the individual
	 * */
	@Override
	public IIndividual copy() {
		// Create new individual
		MoIndividual ind = new MoIndividual(this.genotype.copy());

		// Set phenotype
		ind.setPhenotypefromGenotype();

		// Copy properties
		ind.setNumberOfComponents(this.getNumberOfComponents());
		ind.setNumberOfConnectors(this.getNumberOfConnectors());
		ind.setHasIsolatedComponents(this.hasIsolatedComponents());
		ind.setHasMutuallyDepComponents(this.hasMutuallyDepComponents());

		// Copy fitness and metrics
		if(this.fitness != null){
			ind.setFitness(this.getFitness().copy());

			// Copy rest of properties
			ind.setNumberOfClasses(this.getNumberOfClasses());
			ind.setNumberOfGroups(this.getNumberOfGroups());
			ind.setClassesDistribution(this.getClassesDistribution());
			ind.setClassesToGroups(this.getClassesToGroups());
			ind.setExternalConnections(this.getExternalConnections());
			ind.setNumberOfProvided(this.getNumberOfProvided());
			ind.setNumberOfRequired(this.getNumberOfRequired());
		}
		return ind;
	}

	@Override
	public String toString(){

		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		double value;
		int nObjectives;
		if(this.fitness != null && this.fitness instanceof MOFitness){
			nObjectives = ((MOFitness)this.fitness).getNumberOfObjectives();
			buffer.append("\nObjective values: ");
			try {
				for(int i=0; i<nObjectives; i++){
					value = ((MOFitness)this.fitness).getObjectiveDoubleValue(i);
					buffer.append(value + " ");
				}
				buffer.append("\nFitness: " + ((MOFitness)this.fitness).getValue());
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		return buffer.toString();
	}

	@Override
	public boolean equals(Object other){	
		
		if (other instanceof MoIndividual) {
			MoIndividual ind = (MoIndividual) other;
			// Check general properties: type of solution and fitness
			EqualsBuilder eb = new EqualsBuilder();
			eb.append(genotype, ind.genotype);
			eb.append(fitness, ind.fitness);		
			return eb.isEquals();
		}
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	public boolean isFeasible() {
		boolean cond1 = hasIsolatedComponents();
		boolean cond2 = hasMutuallyDepComponents();
		return (!cond1 && !cond2);
	}

	/**
	 * {@inheritDoc}
	 * Return the degree of infeasibility. It count
	 * the number of constraints that the individual
	 * violates. 
	 * @return 0 if individual
	 * is valid, 1 if individual violates 1 constraint, 2 if individual
	 * violates both constraints
	 * */
	@Override
	public double degreeOfInfeasibility() {
		boolean cond1 = hasIsolatedComponents();
		boolean cond2 = hasMutuallyDepComponents();
		if (!cond1 && !cond2)
			return 0.0;
		else if (cond1 && cond2)
			return 2.0;
		else
			return 1.0;
	}

}
