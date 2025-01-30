package net.sf.jclec.sbse.discovery.mo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import es.uco.kdis.datapro.dataset.source.CsvDataset;
import es.uco.kdis.dss.databuilders.DiscoveryDataBuilder;
import es.uco.kdis.dss.databuilders.DiscoveryDataBuilderFromCSV;
import es.uco.kdis.dss.databuilders.DiscoveryDataBuilderFromXMI;
import net.sf.jclec.IConfigure;
import net.sf.jclec.IIndividual;
import net.sf.jclec.base.AbstractMutator;
import net.sf.jclec.mo.algorithm.MOECAlgorithm;
import net.sf.jclec.mo.comparator.MOSolutionComparator;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.Species;

/**
 * Multi-Objective Evolutionary
 * Algorithm for the discovery problem.
 * 
 * It implements the general configuration,
 * initialization process and the generation 
 * of descendants by mutation.
 * 
 * @author Aurora Ramírez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * 
 * @version 1.0
 * History:
 * <ul>
 * 	<li>2.0: Now extending from MOAlgorithm (May 2014)
 * 	<li>1.0: Creation (November 2013)
 * </ul>
 * @see MOAlgorithm
 * */
public class MoAlgorithm extends MOECAlgorithm {

	/** Serial ID */
	private static final long serialVersionUID = 1091571505216439237L;

	/** Mutator */
	private AbstractMutator mutator;

	/** Frequency of solution sizes */
	private int [] frequency;

	/** Minimum number of components */
	private int minOfComp;

	/** Maximum number of components */
	private int maxOfComp;

	/** Number of invalids in the current generation */
	private int invalids;

	/** Number of non dominated solutions in the current population */
	private int numberOfNonDominated;

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Constructor
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public MoAlgorithm(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Get/Set methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get mutator
	 * @return the configured mutator
	 * */
	public AbstractMutator getMutator() {
		return mutator;
	}

	/**
	 * Set mutator
	 * @param mutator: the new mutator
	 * */
	private void setMutator(AbstractMutator mutator) {
		this.mutator = mutator;
	}

	/**
	 * Get minimum number of components
	 * @return The minimum number of components
	 * */
	public int getMinNumberOfComponents() {
		return minOfComp;
	}

	/**
	 * Set minimum number of components
	 * @param minOfComp: the new minimum limit
	 * */
	private void setMinNumberOfComponents(int minOfComp) {
		this.minOfComp = minOfComp;
	}

	/**
	 * Get maximum number of components
	 * @return The maximum number of components
	 * */
	public int getMaxNumberOfComponents() {
		return maxOfComp;
	}

	/**
	 * Set maximum number of components
	 * @param maxOfComp: the new maximum limit
	 * */
	private void setMaxNumberOfComponents(int maxOfComp) {
		this.maxOfComp = maxOfComp;
	}

	/**
	 * Get number of invalid solutions
	 * in the current population
	 * @return Number of invalid solutions
	 * */
	public int getNumberOfInvalids() {
		return invalids;
	}

	/**
	 * Set number of invalid individuals
	 * @param invalids: the number of invalid individuals
	 * */
	private void setNumberOfInvalids(int invalids) {
		this.invalids = invalids;
	}

	/**
	 * Get number of non dominated solutions
	 * in the current population
	 * @return Number of non dominated solutions
	 * */
	public int getNumberOfNonDominated() {
		return numberOfNonDominated;
	}

	/**
	 * Set number of non dominated individuals
	 * @param nonDominated The number of non dominated individuals
	 * */
	private void setNumberOfNonDominated(int nonDominated) {
		this.numberOfNonDominated = nonDominated;
	}

	/**
	 * Get the frequency of each type
	 * of solution (number of components) in
	 * the population
	 * @return Array with the size frequency
	 * */
	public int[] getComponentFrequency() {
		return frequency;
	}

	/**
	 * Get the frequency of each type
	 * of solution (number of components) in
	 * the population
	 * @param frequency: Array with the size frequency
	 * */
	private void setComponentFrequency(int [] frequency) {
		this.frequency = frequency;
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------- Protected override methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Do initialization
	 * */
	@Override
	protected void doInit(){

		super.doInit();

		// Verbose
		updateStatistics();
	}

	/**
	 * Do generation. Only mutation is performed.
	 * */
	@Override
	protected void doGeneration() {
		// Mutate individuals and evaluate them
		this.cset = this.mutator.mutate(this.pset);
		this.evaluator.evaluate(this.cset);
	}

	/**
	 * Do update
	 * */
	@Override
	protected void doUpdate() {
		// Verbose
		updateStatistics();

		// Update current population
		super.doUpdate();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------ Configuration methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Configure
	 * @param settings: the configuration object
	 * */
	@Override
	public void configure(Configuration settings){
		super.configure(settings);
		int numOfClasses = -1;
		// Configure datasets from the analysis model
		try {
			numOfClasses = setDatasetsFromAnalysisModel(settings.subset("model"));
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}

		// Set algorithm parameters
		int min = settings.getInt("min-of-components", 2);
		int max = settings.getInt("max-of-components", numOfClasses);
		this.frequency = new int [max-min+1];

		// Check the compatibility between classes and components
		if(min<2 || max>numOfClasses){
			throw new IllegalArgumentException("Illegal limits for number of components");
		}
		setMinNumberOfComponents(min);
		setMaxNumberOfComponents(max);

		// Configure problem parameters in species and evaluator
		((Species)this.getSpecies()).setConstraints(min, max);

		// If all navigable relationships is considered as candidate interface...
		int maxInterfaces = (((Species)this.getSpecies()).getGenotypeSchema().getTerminals().length 
				- ((Species)this.getSpecies()).getGenotypeSchema().getNumOfClasses())/2;

		((MoEvaluator)this.getEvaluator()).setProblemCharacteristics(min, max, maxInterfaces);

		// Configure mutator
		try {
			setMutatorSettings(settings.subset("base-mutator"));
		} catch (InstantiationException| IllegalAccessException | ClassNotFoundException e){
			System.err.println("Configuration error: " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Build the datasets of the problem
	 * @param settings Configuration object
	 * */
	private int setDatasetsFromAnalysisModel(Configuration settings) throws ConfigurationException{

		CsvDataset relationshipsDataset, classesDataset;
		DiscoveryDataBuilder builder = null;
		int numberOfClasses = -1;

		// Configure datasets in species and evaluator
		String analysisModelFileName = settings.getString("path");
		if(analysisModelFileName != null){
			// XMI format
			if(analysisModelFileName.contains(".xmi")){
				builder = new DiscoveryDataBuilderFromXMI(analysisModelFileName);
			}
			// TODO JSUML format
		}
		else{
			// CSV format (already pre-processed) // TODO it doesn't work! (revise multi format in files...)
			String classesFileName = settings.getString("path-classes");
			String relationshipsFileName = settings.getString("path-relationships");
			numberOfClasses = settings.getInt("number-of-classes");
			builder = new DiscoveryDataBuilderFromCSV(classesFileName, relationshipsFileName, numberOfClasses);
		}

		builder.buildDatasets();
		relationshipsDataset = builder.getRelationshipsDataset();
		classesDataset = builder.getClassesDataset();
		
		if(relationshipsDataset!=null && classesDataset!=null){

			// Set in Species
			((Species)this.getSpecies()).setDataset(relationshipsDataset);

			// Set in Evaluator
			((MoEvaluator)this.getEvaluator()).setRelationshipsDataset(relationshipsDataset);
			((MoEvaluator)this.getEvaluator()).setClassesDataset(classesDataset);

			numberOfClasses = relationshipsDataset.getColumns().size();
		}
		else{
			throw new ConfigurationException("Problems building the datasets");
		}

		return numberOfClasses;
	}

	/**
	 * Set mutation configuration
	 * @param settings: the configuration object
	 * @throws InstantiationException
	 * @throws IllegalAccessExceptionl
	 * @throws ClassNotFoundException
	 * */
	@SuppressWarnings("unchecked")
	private void setMutatorSettings(Configuration settings) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException{

		// Mutation class instantiation
		String classname = settings.getString("[@type]");
		Class<? extends AbstractMutator> mutatorClass;

		// Create
		mutatorClass = (Class<? extends AbstractMutator>) Class.forName(classname);
		AbstractMutator mutator = mutatorClass.newInstance();

		// Contextualize
		mutator.contextualize(this);

		// Configure
		if (mutator instanceof IConfigure)
			((IConfigure) mutator).configure(settings);

		// Set in algorithm
		setMutator(mutator);
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	private void updateStatistics(){

		// Individuals frequency (types of architectural solutions)
		countFrequency();

		// Number of invalids
		countInvalids();

		// Number of non dominated solutions
		int n = this.getNonDominatedSolutions().size();
		setNumberOfNonDominated(n);
	}

	/**
	 * Count frequency of each size of solution
	 * and save it in <code>frequency</code>
	 * */
	private void countFrequency(){
		int index;
		int min = getMinNumberOfComponents();
		int size = getMaxNumberOfComponents()-min+1;
		int frequency [] = new int[size];

		for(int i=0; i<frequency.length; i++)
			frequency[i]=0;
		for(IIndividual ind: this.bset){
			index = ((Individual)ind).getNumberOfComponents();
			frequency[index-min]++;
		}
		setComponentFrequency(frequency);
	}

	/**
	 * Count number of invalid solutions
	 * and save it in <code>invalids</code>
	 * */
	private void countInvalids(){
		int count = 0;
		for(IIndividual ind: this.bset){
			if(!((Individual)ind).isFeasible())
				count++;
		}
		setNumberOfInvalids(count);
	}

	/**
	 * Get final individuals, i.e. the solutions
	 * to be presented to the user. It removes
	 * equivalent individuals from the final
	 * set of solution of each specific algorithm
	 * @return Final set of individuals
	 * */
	public List<IIndividual> getFinalIndividuals(){

		// Get all the solutions found by the algorithm
		List<IIndividual> all = new ArrayList<IIndividual>();
		// If the algorithm uses external population, it is the set of final solutions
		if(this.archive!=null)
			for(IIndividual ind: this.archive)
				all.add(ind.copy());
		// Otherwise, get current population
		else
			for(IIndividual ind: this.bset)
				all.add(ind.copy());

		List<IIndividual> finalSolutions = new ArrayList<IIndividual>();

		// Remove equivalent solutions, invalid individuals and dominated solutions
		int j;
		MOSolutionComparator indComparator = this.strategy.getSolutionComparator();
		for(int i=0; i<all.size(); i++){
			Individual ind1 = (Individual) all.get(i);
			j=i+1;
			while(j<all.size()){
				Individual ind2 = (Individual) all.get(j);
				if(ind1.isEquivalent(ind2)
						|| indComparator.compare(ind1, ind2)==1){
					all.remove(j);
				}
				else
					j++;
			}
			if(ind1.isFeasible())
				finalSolutions.add(ind1);
		}
		// Return final solutions
		return finalSolutions;
	}
}