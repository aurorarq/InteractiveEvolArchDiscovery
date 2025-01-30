package net.sf.jclec.sbse.discovery.imo.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.column.IntegerColumn;
import es.uco.kdis.datapro.dataset.column.NumericalColumn;
import es.uco.kdis.datapro.dataset.source.ExcelDataset;
import es.uco.kdis.datapro.datatypes.NullValue;
import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.IMOAlgorithm;
import net.sf.jclec.mo.evaluation.MOEvaluator;
import net.sf.jclec.mo.evaluation.Objective;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.mo.listener.MOReporter;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOEPAlgorithm;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOEvaluator;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.sbse.discovery.imo.objectives.Metric;

/**
 * Reporter that stores the convergence of each measure (population and archive)
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (April 2016)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class MeasureEvolutionReporter extends MOReporter{

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -6133065804742557254L;

	/** Dataset */
	private ExcelDataset dataset;

	/** Directory for saving interaction data */
	protected File reportDirInteraction;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public MeasureEvolutionReporter(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Get/set methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get report directory for interaction data
	 * @return report directory
	 * */
	public File getInteractionDirectory() {
		return this.reportDirInteraction;
	}

	/**
	 * Set report directory for interaction data
	 * @param reportDirectory New report directory
	 * */
	protected void setInteractionDirectory(File reportDirectory) {
		this.reportDirInteraction = reportDirectory;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void configure(Configuration settings) {
		// Call super method
		super.configure(settings);

		// Set appropriate flags
		setReportOnConsole(false);
		setReportOnFile(true);
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	public void algorithmStarted(AlgorithmEvent event) {
		super.algorithmStarted(event);

		// Extract the information from the algorithm
		InteractiveMOEPAlgorithm alg = (InteractiveMOEPAlgorithm)event.getAlgorithm();
		List<Objective> metrics = ((InteractiveMOEvaluator)alg.getContext().getEvaluator()).getObjectives();
		int size = metrics.size();

		// Create the dataset
		this.dataset = new ExcelDataset();

		// Column for the current generation
		this.dataset.addColumn(new IntegerColumn("Generation"));

		// Columns for the metric values in the population
		for(int i=0; i<size; i++){
			this.dataset.addColumn(new NumericalColumn("Avg-"+((Metric)metrics.get(i)).getName()+"-Pop"));
			this.dataset.addColumn(new NumericalColumn("Std-"+((Metric)metrics.get(i)).getName()+"-Pop"));
		}

		// Columns for the metric values in the archive
		for(int i=0; i<size; i++){
			this.dataset.addColumn(new NumericalColumn("Avg-"+((Metric)metrics.get(i)).getName()+"-Arch"));
			this.dataset.addColumn(new NumericalColumn("Std-"+((Metric)metrics.get(i)).getName()+"-Arch"));
		}

		// Create a directory for storing pareto front files
		File dir = new File(getReportDirectory().getAbsolutePath()+"/measures/");
		setInteractionDirectory(dir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// Do an iteration report
		doIterationReport((IMOAlgorithm)alg,false);
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void algorithmFinished(AlgorithmEvent event) {

		// Do last generation report (if it has not been made before)
		IMOAlgorithm alg = (IMOAlgorithm)event.getAlgorithm();
		doIterationReport(alg,true);

		// Close report file if necessary
		if (this.reportOnFile  && this.reportFile != null) {
			try {
				this.reportFileWriter.append("Time (ms): " + alg.executionTime());
				this.reportFileWriter.close();

				// Store dataset
				String filename = getInteractionDirectory()+ "/" + getReportTitle()+"-"+getNumberOfExecution()+"-measures.xlsx";
				this.dataset.setNullValue("-");
				((ExcelDataset)this.dataset).setNumberOfDecimals(4);
				((ExcelDataset)this.dataset).writeDataset(filename);

			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void doReport(IMOAlgorithm algorithm, int generation, boolean finalReport) {

		// Generation
		this.dataset.getColumn(0).addValue(generation);

		// Average and standard deviation in the population
		List<IIndividual> set = algorithm.getContext().getInhabitants();
		int nMetrics = ((MOEvaluator)algorithm.getContext().getEvaluator()).numberOfObjectives();
		double [] values = metricAvgStdValueInPopulation(set, nMetrics);
		int i,j;
		for(i=0, j=1; i<values.length; i+=2, j+=2){
			this.dataset.getColumn(j).addValue(values[i]); // avg
			this.dataset.getColumn(j+1).addValue(values[i+1]); // std
		}

		// Average and standard deviation in the archive
		set = algorithm.getArchive();
		if(set==null || set.isEmpty()){
			for(i=0; i<nMetrics*2; i+=2, j+=2){
				this.dataset.getColumn(j).addValue(NullValue.getNullValue()); // avg
				this.dataset.getColumn(j+1).addValue(NullValue.getNullValue()); // std
			}
		}
		else{
			values  = metricAvgStdValueInPopulation(set, nMetrics);
			for(i=0; i<values.length; i+=2, j+=2){
				this.dataset.getColumn(j).addValue(values[i]); // avg
				this.dataset.getColumn(j+1).addValue(values[i+1]); // std
			}
		}
	}

	@Override
	protected String getName() {
		return "-MeasureProgress";
	}

	/**
	 * Compute the average value of each metric within the population
	 * @param population The population of individuals
	 * @param nMetrics Number of metrics
	 * @return Average values
	 * */
	protected double [] metricAvgStdValueInPopulation(List<IIndividual> population, int nMetrics){
		double [] avg = new double [nMetrics];
		double [] std = new double [nMetrics];
		int size = population.size();
		double temp;
		MOFitness fitness;
		for(int i=0; i<nMetrics; i++){
			avg[i] = 0;
			for(int j=0; j<size; j++){
				fitness = (MOFitness) ((InteractiveMOIndividual)population.get(j)).getFitness();
				try {
					temp = fitness.getObjectiveDoubleValue(i);
					avg[i] += temp;
					std[i] += temp*temp;
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			avg[i] /= size;
			std[i] = (std[i]/size) - avg[i]*avg[i];
		}

		double [] res = new double[nMetrics*2];
		for(int i=0, j=0; j<nMetrics; i+=2, j++){
			res[i]=avg[j];
			res[i+1]=std[j];
		}
		return res;
	}
}