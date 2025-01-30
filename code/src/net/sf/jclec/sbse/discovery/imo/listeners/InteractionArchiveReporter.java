package net.sf.jclec.sbse.discovery.imo.listeners;

import java.io.IOException;
import java.util.List;

import es.uco.kdis.datapro.dataset.column.NumericalColumn;
import es.uco.kdis.datapro.dataset.source.CsvDataset;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.IMOAlgorithm;
import net.sf.jclec.mo.IMOEvaluator;
import net.sf.jclec.mo.evaluation.Objective;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.mo.listener.MOParetoFrontReporter;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOFitness;

/**
 * Reporter that stores the Pareto front and the specific
 * fitness measures of the interactive algorithm
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (January 2016)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class InteractionArchiveReporter extends MOParetoFrontReporter {

	/** Serial ID */
	private static final long serialVersionUID = -2428342595160116875L;

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void doReport(IMOAlgorithm algorithm, int generation, boolean finalReport) {

		String algName = algorithm.getStrategy().getClass().getSimpleName().toLowerCase();
		String filename;
		if(finalReport)
			filename = getParetoDirectory() + "/" + algName +"-"+getNumberOfExecution()+"-final-ipf.csv";
		else
			filename = getParetoDirectory() + "/" + algName +"-"+getNumberOfExecution()+"-gener"+generation+"-ipf.csv";
		String name;

		// Create the dataset
		List<Objective> objectives = ((IMOEvaluator)algorithm.getContext().getEvaluator()).getObjectives();
		int nObjectives = objectives.size();
		this.dataset = new CsvDataset();
		for(int i=0; i<nObjectives; i++){ // one column for each objective function
			name = objectives.get(i).getClass().getSimpleName();
			this.dataset.addColumn(new NumericalColumn(name));
		}
		// additional columns
		this.dataset.addColumn(new NumericalColumn("Fitness"));
		this.dataset.addColumn(new NumericalColumn("Preferences"));
		this.dataset.addColumn(new NumericalColumn("Maximin"));
		this.dataset.addColumn(new NumericalColumn("Territory"));

		// Extract the pareto set according to the population sets used by the algorithm
		// and the specific configuration of this listener
		List<IIndividual> paretoSet = algorithm.getArchive();
		if(paretoSet == null){
			this.command.setPopulation(algorithm.getContext().getInhabitants());
			this.command.execute();
			paretoSet = this.command.getNonDominatedSolutions();
		}
		else if(isFilterFromArchive()){
			this.command.setPopulation(algorithm.getArchive());
			this.command.execute();
			paretoSet = this.command.getNonDominatedSolutions();
		}

		// Store the objective values of non dominated individuals
		MOFitness fitness;
		try {
			if(paretoSet != null){
				for(IIndividual ind: paretoSet){
					fitness = ((MOFitness)ind.getFitness());
					for(int i=0; i<nObjectives; i++){
						this.dataset.getColumn(i).addValue(fitness.getObjectiveDoubleValue(i));
					}
					// additional values
					this.dataset.getColumn(nObjectives).addValue(((InteractiveMOFitness)fitness).getValue());
					this.dataset.getColumn(nObjectives+1).addValue(((InteractiveMOFitness)fitness).getPreferenceValue());
					this.dataset.getColumn(nObjectives+2).addValue(((InteractiveMOFitness)fitness).getDominanceValue());
					this.dataset.getColumn(nObjectives+3).addValue(((InteractiveMOFitness)fitness).getTerritory());
				}
			}
			this.dataset.setNumberOfDecimals(8);
			((CsvDataset)this.dataset).writeDataset(filename);

		} catch (IllegalAccessException | IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}	
		// Clean the dataset
		this.dataset = null;
	}
}
