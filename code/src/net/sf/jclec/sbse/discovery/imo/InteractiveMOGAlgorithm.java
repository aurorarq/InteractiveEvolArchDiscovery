package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import es.uco.kdis.datapro.dataset.source.CsvDataset;
import es.uco.kdis.dss.databuilders.DiscoveryDataBuilder;
import es.uco.kdis.dss.databuilders.DiscoveryDataBuilderFromXMI;
import es.uco.kdis.dss.databuilders.info.UMLClass;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.algorithm.MOGeneticAlgorithm;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.sbse.discovery.control.InteractionController;
import net.sf.jclec.sbse.discovery.imo.preferences.ArchitecturalPreference;

public class InteractiveMOGAlgorithm extends MOGeneticAlgorithm {

	/** Serial ID */
	private static final long serialVersionUID = 5857091434075955474L;

	/** Minimum number of components */
	private int minOfComp;

	/** Maximum number of components */
	private int maxOfComp;

	/** Interaction frequency */
	private int interactionFrequency;

	/** Interaction controller */
	private InteractionController interactionController;

	/** Distribution of solutions per number of components */
	private double [] distribution;

	/** Percentage of invalids in the current population */
	private double percentageInvalids;

	/** Mean and standard Deviation fitness value in population */
	private ArrayList<Double> fitnessProgress;

	/** Mean and standard Deviation preference fitness value in population */
	private ArrayList<Double> preferenceFitnessProgress;

	/** Mean and standard deviation dominance fitness value in population */
	private ArrayList<Double> dominanceFitnessProgress;

	/** Current generation needs interaction? */
	private boolean interaction;

	/** Store the generation and cause of each interactive generation */
	private ArrayList<Integer> interactionSteps;

	/** Minimum evolution without interaction */
	private double minEvolutionThreshold = 0.1;

	/** Number of generations without improvements */
	//private int generationStep = 10;

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Constructor
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public InteractiveMOGAlgorithm() {
		super();
		this.fitnessProgress = new ArrayList<Double>();
		this.preferenceFitnessProgress = new ArrayList<Double>();
		this.dominanceFitnessProgress = new ArrayList<Double>();
		this.interactionSteps = new ArrayList<Integer>();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Get/Set methods
	//////////////////////////////////////////////////////////////////
	/**
	 * Get minimum number of components
	 * @return The minimum number of components
	 * */
	public int getMinNumberOfComponents() {
		return this.minOfComp;
	}

	/**
	 * Set minimum number of components
	 * @param minOfComp: the new minimum limit
	 * */
	protected void setMinNumberOfComponents(int minOfComp) {
		this.minOfComp = minOfComp;
	}

	/**
	 * Get maximum number of components
	 * @return The maximum number of components
	 * */
	public int getMaxNumberOfComponents() {
		return this.maxOfComp;
	}

	/**
	 * Set maximum number of components
	 * @param maxOfComp: the new maximum limit
	 * */
	protected void setMaxNumberOfComponents(int maxOfComp) {
		this.maxOfComp = maxOfComp;
	}

	/**
	 * Get the interaction frequency
	 * @return Interaction frequency
	 * */
	public int getInteractionFrequency() {
		return this.interactionFrequency;
	}

	/**
	 * Set the interaction frequency
	 * @param interactionFrequency The new interaction frequency
	 * */
	protected void setInteractionFrequency(int interactionFrequency) {
		this.interactionFrequency = interactionFrequency;
	}

	/**
	 * Get the distribution of the solutions
	 * per number of components
	 * @return Distribution array
	 * */
	public double[] getDistribution() {
		return this.distribution;
	}

	/**
	 * Get the percentage of invalid solutions
	 * @return Percentage of invalid solutions within
	 * the population
	 * */
	public double getPercentageInvalids() {
		return this.percentageInvalids;
	}

	/**
	 * Get the fitness evolution
	 * @return Array with mean and standard deviation
	 * of the fitness
	 * */
	public ArrayList<Double> getFitnessProgress() {
		return this.fitnessProgress;
	}

	/**
	 * Get the preference fitness evolution
	 * @return Array with mean and standard deviation
	 * of the preferences values
	 * */
	public ArrayList<Double> getPreferenceFitnessProgress() {
		return this.preferenceFitnessProgress;
	}

	/**
	 * Get the dominance evolution
	 * @return Array with mean and standard deviation
	 * of the dominance values
	 * */
	public ArrayList<Double> getDominanceFitnessProgress() {
		return this.dominanceFitnessProgress;
	}

	/**
	 * Get the information about interactive generations
	 * @return Array with generations and causes of each interaction
	 * */
	public ArrayList<Integer> getInterativeGenerations(){
		return this.interactionSteps;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * <p>Specific parameter for IMOAlgorithm are:
	 * <ul>
	 * 	<li>model (<code>Complex</code>):
	 * 	<p>Information about the design model (XMI file)</p>
	 * 	</li>
	 * 	
	 * <li>min-of-components (<code>int</code>):
	 * <p>Minimum number of components</p>
	 * </li>
	 * 
	 * 	<li>max-of-components (<code>int</code>):
	 * <p>Maximum number of components</p>
	 * </li>
	 * 
	 * 	<li>interaction-frequency (<code>int</code>):
	 * <p>Frequency of interaction with the architect</p>
	 * </li>
	 * </ul>
	 * */
	@Override
	public void configure(Configuration settings){

		super.configure(settings);

		// Configure datasets from the analysis model
		int numOfClasses = -1;
		try {
			numOfClasses = setDatasetsFromAnalysisModel(settings.subset("model"));
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}

		// Set algorithm parameters
		int min = settings.getInt("min-of-components", 2);
		int max = settings.getInt("max-of-components", numOfClasses);

		// Check the compatibility between classes and components
		if(min<2 || max>numOfClasses){
			throw new IllegalArgumentException("Illegal limits for number of components");
		}

		setMinNumberOfComponents(min);
		setMaxNumberOfComponents(max);

		// Interaction parameters --> TODO Not only number of generations ?
		int interactionFrequency = settings.subset("interaction").getInt("interaction-frequency", (int)(maxOfGenerations/10));
		setInteractionFrequency(interactionFrequency);

		// Configure problem parameters in species and evaluator
		((InteractiveMOSpecies)this.getSpecies()).setConstraints(min, max);

		// Configure problem instance dependencies
		// If all navigable relationships is considered as candidate interface...
		int maxInterfaces = (((InteractiveMOSpecies)this.getSpecies()).getGenotypeSchema().getTerminals().length 
				- ((InteractiveMOSpecies)this.getSpecies()).getGenotypeSchema().getNumOfClasses())/2;
		((InteractiveMOEvaluator)this.getEvaluator()).setProblemCharacteristics(min, max, maxInterfaces);
		((InteractiveMOEvaluator)this.getEvaluator()).setObjectiveMaxValues();

		// Configure the controller
		this.interactionController = new InteractionController(this.strategy.getContext());
		this.interactionController.configure(settings.subset("interaction"));

		// Contextualize mutator
		if(getMutator() instanceof InteractiveMOMutator){
			((InteractiveMOMutator)getMutator()).specificContextualize(maxOfGenerations);
		}
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void doInit() {
		//System.out.println("DO INIT");
		super.doInit();

		// statistics
		updatePopulationStatistics();
		interaction = false;	// interaction is not used yet

		/*double [] statistics = this.averageFitnessAndFitnessVariance();
		// First interaction ?
		interactionController.interactiveInteraction(bset, 
				super.getExternalPopulation(), ((IMOStrategy)this.strategy).getPreferences(),
				generation, statistics[0], statistics[1]);*/
		//System.out.println("END DO INIT");
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void doGeneration() {

		/*if(this.generation%10==0)
			System.out.println("Generation: " + this.generation);
*/
		/////////////////////////////////////////////
		/*System.out.println("\n\n----------------------POPULATION");
		for(IIndividual ind: this.bset){
			System.out.println(ind.toString());
		}
		System.out.println("\n\n");*/
		////////////////////////////////////////////

		// TESTING
		// TODO
		// If it is an interactive iteration, call the controller
		//if(generation%interactionFrequency == 0){

		checkInteractionNecessities();
		if(interaction){
			double [] statistics = this.meanAndStandardDeviationInPopulation(0);
			
			boolean noPreferencesYet;
			if(((InteractiveMOStrategy)this.strategy).getPreferences().size()==0){
				noPreferencesYet=true;
			}
			else{
				noPreferencesYet=false;
			}
			
			// Call the interaction controller
			List<ArchitecturalPreference> preferences = interactionController.interactiveInteraction(bset, 
					super.getArchive(), ((InteractiveMOStrategy)this.strategy).getArchiveMaxSize(), ((InteractiveMOStrategy)this.strategy).getPreferences(), 
					generation, statistics[0], statistics[1], distribution, percentageInvalids,noPreferencesYet,this.minOfComp,this.maxOfComp);

			// Get the new preferences established by the architect
			//List<ArchitecturalPreference> preferences = this.interactionController.updatePreferences(selected,((IMOStrategy)this.strategy).getPreferences());

			// Update the architectural preferences in the multi-objective strategy
			((InteractiveMOStrategy)this.strategy).setPreferences(preferences);

			interaction = false;
		}

		// Clean preference count
		for(ArchitecturalPreference ap: ((InteractiveMOStrategy)this.strategy).getPreferences()){
			ap.setNumberOfSolutions(0);
			if(ap.getGeneration()==-1)
				ap.setGeneration(generation);
		}

		// Call super method
		if(!interactionController.userStopSearch()){
			super.doGeneration();
			updatePopulationStatistics();
		}

	//	System.out.println("END DO GENERATION");
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void doReplacement() {
		//System.out.println("DO REPLACEMENT");
		if(!interactionController.userStopSearch())
			super.doReplacement();
		//System.out.println("END DO REPLACEMENT");
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void doUpdate() {
		//System.out.println("DO UPDATE");
		if(!interactionController.userStopSearch())
			super.doUpdate();
		//System.out.println("END DO UPDATE");
	}

	@Override
	public void doControl(){
		//System.out.println("DO CONTROL");
		super.doControl();
		if(interactionController.userStopSearch()){
			state = FINISHED;
			interactionController.stopInteraction();
		}
		//System.out.println("END DO CONTROL");
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Average and variance fitness in a population
	 * @param index Index to indicate the fitness value to be considered (0=fitness, 1=preferences, 2=dominance)
	 * @return Average and variance value for the desired fitness function
	 * */
	private double [] meanAndStandardDeviationInPopulation(int index) {
		double result [] = {0.0, 0.0};
		int nOfInds = 0;
		double tmp = 0;
		for (IIndividual ind : bset) {
			if(((InteractiveMOIndividual)ind).isFeasible()){
				switch(index){
				case 0:
					tmp = ((MOFitness) ind.getFitness()).getValue(); break;
				case 1:
					tmp = ((InteractiveMOFitness) ind.getFitness()).getPreferenceValue();
					//System.out.println("temp="+tmp);
					break;
				case 2: // normalized value
					tmp = (1.0+((InteractiveMOFitness) ind.getFitness()).getDominanceValue())/2.0; break;
				}

				result[0] += tmp;
				result[1] += tmp*tmp;
				nOfInds++;
			}
		}
		//System.out.println("nOfInds="+nOfInds);
		// Fitness average
		result[0] /= nOfInds;
		// Standard deviation of fitness
		result[1] = Math.sqrt((result[1]/nOfInds) - result[0]*result[0]);
		return result;
	}

	/**
	 * Update internal statistics and progress measures
	 * */
	private void updatePopulationStatistics(){

		// distribution of solutions
		this.distribution = new double[maxOfComp-minOfComp+1];
		int n;
		this.percentageInvalids = 0;
		InteractiveMOIndividual ind;

		for(int i=0; i<bset.size(); i++){
			ind = ((InteractiveMOIndividual)bset.get(i));
			n = ind.getNumberOfComponents();
			this.distribution[n-minOfComp]++;
			if(!ind.isFeasible())
				this.percentageInvalids++;
		}
		this.percentageInvalids = 100*(this.percentageInvalids/(double)this.populationSize);

		// fitness progress
		double [] meanStd = meanAndStandardDeviationInPopulation(0);
		this.fitnessProgress.add(meanStd[0]);
		this.fitnessProgress.add(meanStd[1]);

		// preferences progress
		meanStd = meanAndStandardDeviationInPopulation(1);
		this.preferenceFitnessProgress.add(meanStd[0]);
		this.preferenceFitnessProgress.add(meanStd[1]);

		// dominance progress
		meanStd = meanAndStandardDeviationInPopulation(2);
		this.dominanceFitnessProgress.add(meanStd[0]);
		this.dominanceFitnessProgress.add(meanStd[1]);
	}

	/**
	 * Check if an interaction is required. The interactive
	 * flag will be updated.
	 * */
	private void checkInteractionNecessities(){

		//double initValue, endValue;

		// Less than a percentage of the evolution, do not interact
		double ratioEvolution = (double)generation / (double)maxOfGenerations;
		if(ratioEvolution < minEvolutionThreshold){
			interaction = false;
		}

		// Otherwise, check progress in the population
		else{

			// Case 0: fixed frecuency
			if(generation%interactionFrequency == 0){
				interaction = true;
				this.interactionSteps.add(generation);
				this.interactionSteps.add(0);
			}
			/*
			// Case 1: fitness has not improved in X generations
			if(generation>generationStep){
				initValue = this.fitnessProgress.get((generation-generationStep)*2);
				endValue = this.fitnessProgress.get(generation*2);
				if((endValue-initValue)<0.001){
					interaction = true;
					this.interactionSteps.add(generation);
					this.interactionSteps.add(1);
				}
			}

			// Case 2: preferences have been achieved
			if(generation>generationStep){
				initValue = this.preferenceFitnessProgress.get((generation-generationStep)*2);
				endValue = this.preferenceFitnessProgress.get(generation*2);
				if((endValue-initValue)<0.001){
					interaction = true;
					this.interactionSteps.add(generation);
					this.interactionSteps.add(2);
				}
			}

			// Case 3: dominance progress (e-progress in the archive??)
			if(generation>generationStep){
				initValue = this.dominanceFitnessProgress.get((generation-generationStep)*2);
				endValue = this.dominanceFitnessProgress.get(generation*2);
				if((endValue-initValue)<0.001){
					interaction = true;
					this.interactionSteps.add(generation);
					this.interactionSteps.add(3);
				}
			}*/
		}
	}

	/**
	 * Build the datasets of the problem
	 * @param settings Configuration object
	 * */
	private int setDatasetsFromAnalysisModel(Configuration settings) throws ConfigurationException{

		// Configure datasets in species and evaluator
		String analysisModelFileName = settings.getString("path");
		CsvDataset relationshipsDataset;
		int numberOfClasses = -1;

		DiscoveryDataBuilder builder = null;
		if(analysisModelFileName.contains(".xmi"))
			builder = new DiscoveryDataBuilderFromXMI(analysisModelFileName);

		builder.buildDatasets();
		relationshipsDataset = builder.getRelationshipsDataset();
		ArrayList<UMLClass> classesList = builder.getClasses();

		if(relationshipsDataset!=null){
			// Set in Species
			((InteractiveMOSpecies)this.getSpecies()).setDataset(relationshipsDataset);
			((InteractiveMOSpecies)this.getSpecies()).setClassesList(classesList);

			// Set in Evaluator
			((InteractiveMOEvaluator)this.getEvaluator()).setRelationshipsDataset(relationshipsDataset);
			((InteractiveMOEvaluator)this.getEvaluator()).setClassesList(classesList);

			numberOfClasses = relationshipsDataset.getColumns().size();
		}
		else{
			throw new ConfigurationException("Problems building the datasets");
		}
		return numberOfClasses;
	}
}
