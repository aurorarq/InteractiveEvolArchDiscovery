package net.sf.jclec.sbse.discovery.mo.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import es.uco.kdis.datapro.dataset.column.IntegerColumn;
import es.uco.kdis.datapro.dataset.column.NumericalColumn;
import es.uco.kdis.datapro.dataset.source.ExcelDataset;
import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.IMOAlgorithm;
import net.sf.jclec.mo.listener.MOReporter;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOEPAlgorithm;

/**
 * Interaction file reporter
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (April 2016)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * @see MOReporter
 * */
public class MOAlgorithmFileReporter extends MOReporter {

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
	public MOAlgorithmFileReporter(){
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
		this.dataset.addColumn(new NumericalColumn("Percentage invalids Pop."));
		this.dataset.addColumn(new IntegerColumn("Archive size"));
		
		InteractiveMOEPAlgorithm alg = (InteractiveMOEPAlgorithm)event.getAlgorithm();
		//InteractiveMOGAlgorithm alg = (InteractiveMOGAlgorithm)event.getAlgorithm();
		int min = alg.getMinNumberOfComponents();
		int max = alg.getMaxNumberOfComponents();
		for(int i=min; i<=max; i++){
			this.dataset.addColumn(new NumericalColumn(i+" components"));
		}

		// Create a directory for storing pareto front files
		File dir = new File(getReportDirectory().getAbsolutePath()+"/evolution/");
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
				String filename = getInteractionDirectory()+ "/" + getReportTitle()+"-"+getNumberOfExecution()+"-evolution.xlsx";
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

		// Infeasible solutions and archive size
		double invalids = alg.getPercentageInvalids();
		this.dataset.getColumn(1).addValue(invalids);
		
		List<IIndividual> archive = alg.getArchive();
		if(archive == null){
			this.dataset.getColumn(2).addValue(0);
		}
		else{
			this.dataset.getColumn(2).addValue(alg.getArchive().size());
		}

		// Distribution of solutions per number of components
		int min = alg.getMinNumberOfComponents();
		int max = alg.getMaxNumberOfComponents();

		double [] distribution = alg.getDistribution();
		for(int i=min; i<=max; i++){
			this.dataset.getColumn(3+(i-min)).addValue(distribution[i-min]);
		}
	}

	@Override
	protected String getName() {
		return "-AlgorithmProgress";
	}
}