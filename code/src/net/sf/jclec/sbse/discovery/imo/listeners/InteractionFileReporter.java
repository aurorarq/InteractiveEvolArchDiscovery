package net.sf.jclec.sbse.discovery.imo.listeners;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.column.IntegerColumn;
import es.uco.kdis.datapro.dataset.column.NumericalColumn;
import es.uco.kdis.datapro.dataset.source.ExcelDataset;
import es.uco.kdis.datapro.datatypes.NullValue;
import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.mo.IMOAlgorithm;
import net.sf.jclec.mo.listener.MOReporter;
//import net.sf.jclec.sbse.discovery.imo.InteractiveMOGAlgorithm;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOEPAlgorithm;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOStrategy;

/**
 * Interaction file reporter
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (June 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * @see MOReporter
 * */
public class InteractionFileReporter extends MOReporter {

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -8037499252549376993L;

	/** Dataset */
	private ExcelDataset dataset;

	/** Directory for saving interaction data */
	protected File reportDirInteraction;

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Constructor
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public InteractionFileReporter(){
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

	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void algorithmStarted(AlgorithmEvent event) {
		super.algorithmStarted(event);

		// Create an empty dataset
		this.dataset = new ExcelDataset();
		this.dataset.addColumn(new IntegerColumn("Generation"));
		this.dataset.addColumn(new NumericalColumn("Mean fitness Pop."));
		this.dataset.addColumn(new NumericalColumn("Std. fitness Pop."));
		this.dataset.addColumn(new NumericalColumn("Mean preference fitness Pop."));
		this.dataset.addColumn(new NumericalColumn("Std. preference fitness Pop."));
		this.dataset.addColumn(new NumericalColumn("Mean dominance fitness Pop."));
		this.dataset.addColumn(new NumericalColumn("Std. dominance fitness Pop."));
		this.dataset.addColumn(new NumericalColumn("Percentage invalids Pop."));
		this.dataset.addColumn(new IntegerColumn("Archive size"));
		this.dataset.addColumn(new NumericalColumn("Archive updates"));
		this.dataset.addColumn(new NumericalColumn("Mean fitness Arch."));
		this.dataset.addColumn(new NumericalColumn("Std. fitness Arch."));
		this.dataset.addColumn(new NumericalColumn("Mean preference fitness Arch."));
		this.dataset.addColumn(new NumericalColumn("Std. preference fitness Arch."));
		this.dataset.addColumn(new NumericalColumn("Mean dominance fitness Arch."));
		this.dataset.addColumn(new NumericalColumn("Std. dominance fitness Arch."));

		InteractiveMOEPAlgorithm alg = (InteractiveMOEPAlgorithm)event.getAlgorithm();
		//InteractiveMOGAlgorithm alg = (InteractiveMOGAlgorithm)event.getAlgorithm();
		int min = alg.getMinNumberOfComponents();
		int max = alg.getMaxNumberOfComponents();
		for(int i=min; i<=max; i++){
			this.dataset.addColumn(new NumericalColumn(i+" components"));
		}

		// Create a directory for storing pareto front files
		File dir = new File(getReportDirectory().getAbsolutePath()+"/interaction/");
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
				String filename = getInteractionDirectory()+ "/" + getReportTitle()+"-"+getNumberOfExecution()+"-interaction.xlsx";
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

		//InteractiveMOGAlgorithm alg = (InteractiveMOGAlgorithm)algorithm;
		InteractiveMOEPAlgorithm alg = (InteractiveMOEPAlgorithm)algorithm;

		// Generation
		this.dataset.getColumn(0).addValue(generation);

		// Fitness progress in population
		double value = alg.getFitnessProgress().get(alg.getFitnessProgress().size()-4);

		if(Double.isNaN(value))
			this.dataset.getColumn(1).addValue(NullValue.getNullValue());
		else	
			this.dataset.getColumn(1).addValue(value);

		value = alg.getFitnessProgress().get(alg.getFitnessProgress().size()-3);
		if(Double.isNaN(value))
			this.dataset.getColumn(2).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(2).addValue(value);

		// Preference fitness component in population
		value = alg.getPreferenceFitnessProgress().get(alg.getPreferenceFitnessProgress().size()-4);
		if(Double.isNaN(value))
			this.dataset.getColumn(3).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(3).addValue(value);

		value = alg.getPreferenceFitnessProgress().get(alg.getPreferenceFitnessProgress().size()-3);
		if(Double.isNaN(value))
			this.dataset.getColumn(4).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(4).addValue(value);

		// Maximin fitness component in population
		value = alg.getDominanceFitnessProgress().get(alg.getDominanceFitnessProgress().size()-4);
		if(Double.isNaN(value))
			this.dataset.getColumn(5).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(5).addValue(value);

		value = alg.getDominanceFitnessProgress().get(alg.getDominanceFitnessProgress().size()-3);
		if(Double.isNaN(value))
			this.dataset.getColumn(6).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(6).addValue(value);

		// infeasible solutions and archive
		double invalids = alg.getPercentageInvalids();
		this.dataset.getColumn(7).addValue(invalids);
		this.dataset.getColumn(8).addValue(alg.getArchive().size());
		int archiveUpdates = ((InteractiveMOStrategy)alg.getStrategy()).getNumberArchiveUpdates();
		this.dataset.getColumn(9).addValue((double)archiveUpdates);

		// Fitness progress in archive
		value = alg.getFitnessProgress().get(alg.getFitnessProgress().size()-2);

		if(Double.isNaN(value))
			this.dataset.getColumn(10).addValue(NullValue.getNullValue());
		else	
			this.dataset.getColumn(10).addValue(value);

		value = alg.getFitnessProgress().get(alg.getFitnessProgress().size()-1);
		if(Double.isNaN(value))
			this.dataset.getColumn(11).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(11).addValue(value);

		// Preference fitness component in archive
		value = alg.getPreferenceFitnessProgress().get(alg.getPreferenceFitnessProgress().size()-2);
		if(Double.isNaN(value))
			this.dataset.getColumn(12).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(12).addValue(value);

		value = alg.getPreferenceFitnessProgress().get(alg.getPreferenceFitnessProgress().size()-1);
		if(Double.isNaN(value))
			this.dataset.getColumn(13).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(13).addValue(value);

		// Maximin fitness component in archive
		value = alg.getDominanceFitnessProgress().get(alg.getDominanceFitnessProgress().size()-2);
		if(Double.isNaN(value))
			this.dataset.getColumn(14).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(14).addValue(value);

		value = alg.getDominanceFitnessProgress().get(alg.getDominanceFitnessProgress().size()-1);
		if(Double.isNaN(value))
			this.dataset.getColumn(15).addValue(NullValue.getNullValue());
		else
			this.dataset.getColumn(15).addValue(value);

		// Distribution of solutions per number of components
		int min = alg.getMinNumberOfComponents();
		int max = alg.getMaxNumberOfComponents();

		double [] distribution = alg.getDistribution();
		for(int i=min; i<=max; i++){
			this.dataset.getColumn(16+(i-min)).addValue(distribution[i-min]);
		}
	}

	@Override
	protected String getName() {
		return "-AlgorithmProgress";
	}
}