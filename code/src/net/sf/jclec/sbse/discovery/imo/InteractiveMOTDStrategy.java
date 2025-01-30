package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.imo.preferences.ArchitecturalPreference;
import net.sf.jclec.util.random.IRandGen;
import net.sf.jclec.mo.IConstrained;
import net.sf.jclec.mo.IMOEvaluator;
import net.sf.jclec.mo.comparator.MOSolutionComparator;
import net.sf.jclec.mo.comparator.fcomparator.MOValueFitnessComparator;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;

/**
 * Multi-objective strategy for the discovery of software architectures
 * formulated as a multi-objective interactive problem. Based on iTDEA.
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (January 2016)
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class InteractiveMOTDStrategy extends InteractiveMOStrategy {

	/** Serial ID */
	private static final long serialVersionUID = -6714183811581211436L;

	/** Initial territory size */
	protected double initialTerritory;

	/** Final territory size */
	protected double finalTerritory;

	protected double currentTerritorySize;

	protected int nextInteraction;

	protected int maxGenerations;

	protected int interactionCounter;

	/** Number of interactions */
	protected int nInteractions;

	protected int interactionStep;

	/** Reduction factor for territories (r) */
	protected double reductionFactor;

	/** Territory size decrease factor */
	protected double rho;

	protected double lambda;

	protected double [] idealPoint;

	protected double [] nadirPoint;

	protected List<PreferredRegion> regions;
	//protected double [][] weightPreferredRegion; // rows=#objectives, columns=2 (low bound, upper bound)

	protected IIndividual preferredSolution;

	protected boolean [] offspringSurvive;

	protected double weightPreferences;

	protected double weightDominance;

	protected boolean usePriority;

	protected boolean useConfidence;

	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Get/set methods
	/////////////////////////////////////////////////////////////////

	public void setNumberOfInteractions(int n){
		this.nInteractions = n;
	}

	public void setNumberOfSolutionsToBeShown(int n){
		this.nSolutions = n;
	}

	public int getNumberOfSolutionsToBeShown(){
		return this.nSolutions;
	}

	public void setMaximumNumberGenerations(int n){
		this.maxGenerations = n;
	}

	public int getNextInteractiveGeneration(){
		return this.nextInteraction;
	}

	public void setPreferredSolution(IIndividual solution){
		this.preferredSolution = solution;
	}

	public void incrementInteractionCounter(){
		this.interactionCounter++;
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * 
	 * */
	@Override
	public void configure(Configuration settings){

		super.configure(settings);

		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();

		// Initial territory
		this.initialTerritory = settings.getDouble("initial-t", 0.1);
		// Final territory
		this.finalTerritory = settings.getDouble("final-t", 0.005);
		// Lambda
		this.lambda = settings.getDouble("lambda",1.0/(double)nObjs);

		this.weightPreferences = settings.getDouble("weight-preferences",0.5);
		this.weightDominance = settings.getDouble("weight-dominance",1-this.weightPreferences);

		this.usePriority = settings.getBoolean("preference-priority",false);
		this.useConfidence = settings.getBoolean("use-confidence", true);

		////////////////////////////////////////////
		//System.out.println("CONFIGURE STRATEGY");
		//System.out.println("Interaction: nInteractions="+ this.nInteractions + " nSolutions=" + this.nSolutions + " current="+this.interactionCounter + " next=" + this.nextInteraction + " step=" + this.interactionStep);
		//System.out.println("Territory: current=" + this.currentTerritorySize + " initial=" + this.initialTerritory + " final=" + this.finalTerritory);
		////////////////////////////////////////////
	}

	@Override
	public List<IIndividual> initialize(List<IIndividual> population) {

		// Empty preferences
		this.preferences = new ArrayList<ArchitecturalPreference>();

		// Initialize iTDEA parameters
		this.interactionCounter = 0;												// Number of current interaction
		if(this.nInteractions>0){
			this.nextInteraction = (int)((double)maxGenerations/3.0);					// Number of generation for the next interaction
			this.interactionStep = (int)((double)maxGenerations/(2.0*(nInteractions-1)));	// interaction frequency
		}
		else{
			this.nextInteraction=maxGenerations+1; // not interaction
		}
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();
		this.currentTerritorySize = this.initialTerritory;							// territory size
		this.rho = Math.log(initialTerritory/finalTerritory)/(double)nInteractions;	// rho (territory reduction)
		this.idealPoint = new double[nObjs];										// ideal point
		this.nadirPoint = new double[nObjs];										// nadir point
		double [][] weights = new double[nObjs][2];							// rows=#objectives, columns=2 (low bound, upper bound)
		for(int i=0; i<nObjs; i++){
			this.idealPoint[i] = 0.0;
			this.nadirPoint[i] = 1.0;
			weights[i][0] = 0.0;
			weights[i][1] = 1.0;
		}
		this.regions = new ArrayList<PreferredRegion>();
		this.regions.add(new PreferredRegion(0,weights,initialTerritory));

		// Reduction factor
		if(this.nInteractions > 0){
			double min = Math.pow((1.0/(double)nSolutions), 1.0/(double)nObjs);
			double max = Math.pow(lambda, 1.0/(double)(nInteractions-1));
			this.reductionFactor = (Math.abs(max-min))/2.0;
		}
		//System.out.println("Lambda: " + lambda + " r-min=" + min + " r-max=" + max + " r=" + reductionFactor);

		// Initialize archive with the set of non-dominated solutions
		List<IIndividual> archive = new ArrayList<IIndividual>();
		int size = population.size();
		IIndividual ind;

		// Assign the first fitness value for all the solutions only considering the maximin function
		boolean equals;

		fitnessAssignment(population, null);

		for(int i=0; i<size; i++){
			ind = population.get(i);
			// Add only feasible solutions
			if(((InteractiveMOIndividual)ind).isFeasible()){

				// check if the maximin value is < 0 (non-dominated)
				if(((InteractiveMOFitness)ind.getFitness()).getDominanceValue()<0){

					// check if there exists an equivalent solution in the current archive
					equals = false;
					for(int j=0; !equals && j<archive.size(); j++){
						if(((InteractiveMOIndividual)ind).isEquivalent((InteractiveMOIndividual)archive.get(j))){
							equals=true;
						}
					}
					// non equal solution, add to the archive if it does not belong to other territory
					if(!equals){

						if(archive.size()==0){
							((InteractiveMOFitness)ind.getFitness()).setTerritory(this.initialTerritory);
							((InteractiveMOFitness)ind.getFitness()).setRegion(0);
							archive.add(ind.copy());
						}
						else{
							for(int j=0; j<archive.size(); j++){
								if(!((InteractiveMOIndividual)ind).isEquivalent((InteractiveMOIndividual)archive.get(j))){
									double threshold = calculateMaximumThreshold(ind, archive.get(j));
									if(threshold>=this.initialTerritory){
										((InteractiveMOFitness)ind.getFitness()).setTerritory(this.initialTerritory);
										archive.add(ind.copy());
									}
								}
							}
						}
					}
				}
			}
		}

		////////////////////////////////////////////
		//System.out.println("INITIALIZE STRATEGY");
		//System.out.println("Interaction: nInteractions="+ this.nInteractions + " nSolutions=" + this.nSolutions + " current="+this.interactionCounter + " next=" + this.nextInteraction + " step=" + this.interactionStep);
		//System.out.println("Territory: current=" + this.currentTerritorySize + " initial=" + this.initialTerritory + " final=" + this.finalTerritory);
		//System.out.println("Archive: size=" + archive.size());
		///////////////////////////////////////////

		/*System.out.println("INITIAL ARCHIVE");
		for(int i=0; i<archive.size(); i++){
			InteractiveMOFitness f = (InteractiveMOFitness)archive.get(i).getFitness();
			try {
				System.out.println("f1="+f.getObjectiveDoubleValue(0) + " f2=" + f.getObjectiveDoubleValue(1) + " pref=" 
						+ f.getPreferenceValue() + " dom=" + f.getDominanceValue() + " territory-size=" +f.getTerritory());
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}*/

		this.nUpdates=archive.size();

		return archive;
	}

	@Override
	public void update() {
		int generation = getContext().getGeneration();

		if(generation == nextInteraction){	
			// Update interaction counter
			//interactionCounter++;

			// Decrease the territory size
			currentTerritorySize = finalTerritory*Math.exp((nInteractions-interactionCounter)*rho);

			// Update the preferred region
			updatePreferredRegion();

			if(this.interactionCounter < this.nInteractions){
				// Next interaction
				nextInteraction += interactionStep;
			}
			
			////////////////////////////////////////////
			//System.out.println("UPDATE STRATEGY");
			//System.out.println("Current="+this.interactionCounter + " next=" + this.nextInteraction + " step=" + this.interactionStep);
			//System.out.println("Territory: current=" + this.currentTerritorySize + " initial=" + this.initialTerritory + " final=" + this.finalTerritory);
			////////////////////////////////////////////
		}
	}

	@Override
	public void createSolutionComparator(Comparator<IFitness>[] components) {
		super.createSolutionComparator(components);
	}

	@Override
	public List<IIndividual> matingSelection(List<IIndividual> population, List<IIndividual> archive) {
		//System.out.println("STRATEGY -- MATING SELECTION");

		List<IIndividual> parents = new ArrayList<IIndividual>();
		int nSelectFromPopulation = 1;
		IIndividual ind1, ind2;
		InteractiveMOFitness fitness1, fitness2;
		IRandGen randgen = getContext().createRandGen();
		int rndIndex;

		// Interactive iteration, update preference confidence
		int generation = getContext().getGeneration();
		if(generation == nextInteraction && this.useConfidence){
			updatePreferenceConfidences();
		}

		// Update fitness values
		fitnessAssignment(population, archive);

		// Binary tournament between two individuals in the archive
		if(archive.size() > 0){
			if(archive.size() >1){
				rndIndex = randgen.choose(0, archive.size());
				ind1 = archive.get(rndIndex);
				rndIndex = randgen.choose(0, archive.size());
				ind2 = archive.get(rndIndex);

				// compare by overall fitness
				fitness1 = (InteractiveMOFitness)ind1.getFitness();
				fitness2 = (InteractiveMOFitness)ind2.getFitness();
				if(fitness1.getValue() < fitness2.getValue()){
					parents.add(ind1);
				}
				else if(fitness1.getValue() > fitness2.getValue()){
					parents.add(ind2);
				}
				else{
					if(randgen.coin()){
						parents.add(ind1);
					}
					else{
						parents.add(ind2);
					}
				}

			}
			else{
				parents.add(archive.get(0));
			}
		}


		else{
			nSelectFromPopulation = 2;
		}

		// Binary tournament between two individuals in the population
		for(int i=0; i<nSelectFromPopulation; i++){
			rndIndex = randgen.choose(0, population.size());
			ind1 = population.get(rndIndex);
			rndIndex = randgen.choose(0, population.size());
			ind2 = population.get(rndIndex);

			// compare by overall fitness
			fitness1 = (InteractiveMOFitness)ind1.getFitness();
			fitness2 = (InteractiveMOFitness)ind2.getFitness();
			if(fitness1.getValue() < fitness2.getValue()){
				parents.add(ind1);
			}
			else if(fitness1.getValue() > fitness2.getValue()){
				parents.add(ind2);
			}
			else{
				if(randgen.coin()){
					parents.add(ind1);
				}
				else{
					parents.add(ind2);
				}
			}
		}
		return parents;
	}

	@Override
	public List<IIndividual> environmentalSelection(List<IIndividual> population, 
			List<IIndividual> offspring, List<IIndividual> archive) {

		// Use the overall fitness value, offspring replace worst individuals if they are feasible solutions
		List<IIndividual> survivors = new ArrayList<IIndividual>();
		for(int i=0; i<population.size(); i++){
			survivors.add(population.get(i).copy());
		}
		MOValueFitnessComparator fcomparator = new MOValueFitnessComparator();
		MOSolutionComparator comparator = new MOSolutionComparator(fcomparator);
		Collections.sort(survivors,comparator);

		boolean isFeasible1=((IConstrained)offspring.get(0)).isFeasible();
		boolean isFeasible2=((IConstrained)offspring.get(1)).isFeasible();
		this.offspringSurvive = new boolean[]{false,false};
		if(isFeasible1 && isFeasible2){
			survivors.remove(survivors.size()-1);
			survivors.remove(survivors.size()-1);
			survivors.addAll(offspring);
			this.offspringSurvive[0]=true;
			this.offspringSurvive[1]=true;
		}
		else if(isFeasible1 || isFeasible2){
			survivors.remove(survivors.size()-1);
			if(isFeasible1){
				survivors.add(offspring.get(0).copy());
				this.offspringSurvive[0]=true;
			}
			else{
				survivors.add(offspring.get(1).copy());
				this.offspringSurvive[1]=true;
			}	
		}

		// Evaluate the population and the archive
		fitnessAssignment(survivors, archive);

		// Update the weights and fitness of offspring that survive
		for(int i=0, j=offspring.size(); i<offspring.size(); i++, j--){
			if(this.offspringSurvive[i]){
				InteractiveMOFitness f = (InteractiveMOFitness)survivors.get(population.size()-j).getFitness();
				offspring.get(i).setFitness(f.copy());
			}
		}

		return survivors;
	}

	@Override
	public List<IIndividual> updateArchive(List<IIndividual> population, 
			List<IIndividual> offspring, List<IIndividual> archive) {

		// Option 1: original method (TDEA)
		//List<IIndividual> newArchive = updateArchive1(offspring, archive);

		// Option 2: allow changing dominated solutions
		//List<IIndividual> newArchive = updateArchive2(offspring, archive);

		// Option 3: allow changing dominated solutions or solutions with low preference value
		//List<IIndividual> newArchive = updateArchive3(offspring, archive);

		// Option 4: control overlapping between territories
		//List<IIndividual> newArchive = updateArchive4(offspring, archive);

		List<IIndividual> newArchive = updateArchive5(population, archive);

		// If this generation has included an interaction, update the preferred solution 
		// of the archive for the next generation
		int generation = getContext().getGeneration();
		if(generation == nextInteraction){
			this.preferredSolution = choosePreferredSolution(newArchive);


			/////////////////////
			/*InteractiveMOFitness f;
			InteractiveMOIndividual ind;

			System.out.println("SURVIVORS");
			for(int i=0; i<population.size(); i++){
				ind = (InteractiveMOIndividual)population.get(i);
				f=(InteractiveMOFitness)ind.getFitness();
				try {
					System.out.println("\t"+f.getObjectiveDoubleValue(0) + " " + f.getObjectiveDoubleValue(1) + " " + f.getObjectiveDoubleValue(2) + " -> user=" 
							+ ind.getSolutionInArchive() + " dominance=" + f.getDominanceValue() + " region=" + f.getRegion() + " territory=" + f.getTerritory());
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}

			System.out.println("INITIAL ARCHIVE size=" + archive.size());
			for(int i=0; i<archive.size(); i++){
				ind = (InteractiveMOIndividual)archive.get(i);
				f=(InteractiveMOFitness)ind.getFitness();
				try {
					System.out.println("\t"+f.getObjectiveDoubleValue(0) + " " + f.getObjectiveDoubleValue(1) + " " + f.getObjectiveDoubleValue(2) + " -> user=" 
							+ ind.getSolutionInArchive() + " dominance=" + f.getDominanceValue() + " region=" + f.getRegion() + " territory=" + f.getTerritory());
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			System.out.println("FINAL ARCHIVE size=" + newArchive.size());
			for(int i=0; i<newArchive.size(); i++){
				ind = (InteractiveMOIndividual)newArchive.get(i);
				f=(InteractiveMOFitness)newArchive.get(i).getFitness();
				try {
					System.out.println("\t"+f.getObjectiveDoubleValue(0) + " " + f.getObjectiveDoubleValue(1) + " " + f.getObjectiveDoubleValue(2) + " -> user=" 
							+ ind.getSolutionInArchive() + " dominance=" + f.getDominanceValue() + " region=" + f.getRegion() + " territory=" + f.getTerritory());
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}*/
		}

		return newArchive;
	}

	@Override
	protected void fitnessAssignment(List<IIndividual> population, List<IIndividual> archive) {

		int generation = getContext().getGeneration();
		//System.out.println("STRATEGY -- FITNESS ASSIGMENT: gen=" + generation);

		// Join the populations
		int populationSize = population.size();
		List<IIndividual> allIndividuals = new ArrayList<IIndividual>();
		allIndividuals.addAll(population);
		if(archive!=null)
			allIndividuals.addAll(archive);

		// 1 - Evaluate dominance for all the individuals (it changes after 
		// each generation only if at least one offspring survives)

		int size = allIndividuals.size();		
		InteractiveMOIndividual individual;
		double fitnessP, fitness;						// preference value
		double [] fitnessD = new double[2];		// dominance value + metric
		int nSurvivors = 0;
		boolean update = false;
		// check if update is necessary
		if(generation==0 || offspringSurvive == null){ // after initialization or before the first mating selection
			update = true;
		}
		else if(offspringSurvive[0] || offspringSurvive[1]){
			update = true;
			if(offspringSurvive[0] && offspringSurvive[1]){
				nSurvivors = 2;
			}
			else
				nSurvivors = 1;
		}

		// Get the set of non dominated solutions
		if(update){
			List<IIndividual> nonDominated = extractNonDominatedIndividuals(population, archive);

			// Evaluate all the solutions
			for(int i=0; i<size; i++){
				individual = (InteractiveMOIndividual)allIndividuals.get(i);
				fitnessD[0] = 0.0;
				fitnessD[1] = -1.0;

				// Compute the maximin fitness value
				fitnessD = maximinValue(individual, nonDominated);
				((InteractiveMOFitness)individual.getFitness()).setDominanceValue(fitnessD[0]);
				((InteractiveMOFitness)individual.getFitness()).setMetricIndex((int)fitnessD[1]);
			}
		}

		// 2 - Evaluate preferences only for offspring or after the update of the preferences

		if(interactionCounter==0){	// preference information is not provided yet
			for(int i=0; i<size; i++){
				((InteractiveMOFitness)allIndividuals.get(i).getFitness()).setPreferenceValue(0.0);
			}
			// 3- Assign favored weights
			assignFavoredWeights(allIndividuals);
		}
		else if(generation == nextInteraction){ // after an interaction, update the preference information
			for(int i=0; i<size; i++){
				individual = (InteractiveMOIndividual)allIndividuals.get(i);
				evaluateNewPreferences(individual);
			}
			// 3- Assign favored weights to offspring
			assignFavoredWeights(allIndividuals.subList(populationSize-nSurvivors, populationSize));
			//assignFavoredWeights(allIndividuals);
		}
		else{ // only offspring should be evaluated
			//System.out.println("Population size: " + populationSize + " nSurvivors: " + nSurvivors);
			for(int i=populationSize-nSurvivors; i<populationSize; i++){
				individual = (InteractiveMOIndividual)allIndividuals.get(i);
				evaluateNewPreferences(individual);
			}
			// 3- Assign favored weights to offspring
			assignFavoredWeights(allIndividuals.subList(populationSize-nSurvivors, populationSize));
			//assignFavoredWeights(allIndividuals);
		}

		// 4 - Update the priority of each preference
		if(usePriority)
			updatePreferencePriorities(allIndividuals);

		// 5 - Update the overall fitness value
		for(int i=0; i<size; i++){

			individual = (InteractiveMOIndividual)allIndividuals.get(i);
			if(interactionCounter>0){
				fitnessP = preferenceValue(individual); // average preference value with updated priority
				((InteractiveMOFitness)allIndividuals.get(i).getFitness()).setPreferenceValue(fitnessP);
			}
			else
				fitnessP = 0.0;
			fitnessD[0] = ((InteractiveMOFitness)individual.getFitness()).getDominanceValue();

			// Set the fitness value, the first term (preferences) is inverted 
			// and the second term (maximin) is scaled to [0,1]
			fitness = this.weightPreferences*(1.0-fitnessP) + this.weightDominance*((1.0+fitnessD[0])/2.0);

			//System.out.println("Pref. value="+fitnessP + " Maximim value="+fitnessA + " Fitness: " + fitness);
			if(individual.toBeRemoved)
				((MOFitness)individual.getFitness()).setValue(3.0); // worse than infeasible solutions
			else if(individual.isFeasible())
				((MOFitness)individual.getFitness()).setValue(fitness);
			else
				((MOFitness)individual.getFitness()).setValue(2.0); // invalid fitness value
		}

		//////////////////////////////////////////////////////////////////
		/*System.out.println("GENERATION: " + generation);
		System.out.println("SOLUTIONS");
		for(int i=0; i<allIndividuals.size(); i++){
			InteractiveMOFitness f = (InteractiveMOFitness)allIndividuals.get(i).getFitness();
			List<Double> values = f.getPreferenceValues();
			System.out.print("i="+i+ " avg pref=" + f.getPreferenceValue() + " -> pref. values: ");
			for(int j=0; j<values.size(); j++){
				System.out.print(values.get(j) + " ");
			}
			System.out.println();
		}

		System.out.println("\nPREFERENCES");
		for(int i=0; i<this.preferences.size(); i++){
			ArchitecturalPreference p = this.preferences.get(i);
			System.out.println("Pref. i="+ i + " #sol=" + p.getNumberOfSolutions() + " threshold=" + p.getThreshold() 
			+ " priority=" + p.getPriority() + " conf=" + p.getConfidence() + " scaled conf=" + p.getScaledConfidence());
		}*/
		//////////////////////////////////////////////////////////////////

		/////////////////////////////

		// TESTING
		/*try {

			System.out.println("Population");
			for(int i=0; i<population.size()-nSurvivors; i++){
				System.out.print("\t obj0=" + ((InteractiveMOFitness) population.get(i).getFitness()).getObjectiveDoubleValue(0) +
						" obj1=" + ((InteractiveMOFitness) population.get(i).getFitness()).getObjectiveDoubleValue(1) + " feasible="
						+ ((Individual)population.get(i)).isFeasible());
				System.out.print(" w0=" + ((InteractiveMOFitness) population.get(i).getFitness()).getWeights()[0] +
						" w1=" + ((InteractiveMOFitness) population.get(i).getFitness()).getWeights()[1]);
				System.out.println(" dom=" + ((InteractiveMOFitness) population.get(i).getFitness()).getDominanceValue() +
						" pref=" + ((InteractiveMOFitness) population.get(i).getFitness()).getPreferenceValue());
			}

			if(generation>0){
				System.out.println("\nOffspring");
				for(int i=populationSize-nSurvivors; i<populationSize; i++){
					//if(Double.isNaN(((InteractiveMOFitness) allIndividuals.get(i).getFitness()).getWeights()[0]) 
					//		|| Double.isNaN(((InteractiveMOFitness) allIndividuals.get(i).getFitness()).getWeights()[1])){
						System.out.print("\t obj0=" + ((InteractiveMOFitness) allIndividuals.get(i).getFitness()).getObjectiveDoubleValue(0) +
								" obj1=" + ((InteractiveMOFitness) allIndividuals.get(i).getFitness()).getObjectiveDoubleValue(1) + " feasible="
								+ ((Individual)allIndividuals.get(i)).isFeasible());
						System.out.print(" w0=" + ((InteractiveMOFitness) allIndividuals.get(i).getFitness()).getWeights()[0] +
								" w1=" + ((InteractiveMOFitness) allIndividuals.get(i).getFitness()).getWeights()[1]);
						System.out.println(" dom=" + ((InteractiveMOFitness) allIndividuals.get(i).getFitness()).getDominanceValue() +
								" pref=" + ((InteractiveMOFitness) allIndividuals.get(i).getFitness()).getPreferenceValue());
					}

				}

				/*System.out.println("\nArchive");
				for(int i=0; i<archive.size(); i++){
					System.out.print("\t obj0=" + ((InteractiveMOFitness) archive.get(i).getFitness()).getObjectiveDoubleValue(0) +
							" obj1=" + ((InteractiveMOFitness) archive.get(i).getFitness()).getObjectiveDoubleValue(1) + " feasible="
							+ ((Individual)archive.get(i)).isFeasible());
					System.out.print(" w0=" + ((InteractiveMOFitness) archive.get(i).getFitness()).getWeights()[0] +
							" w1=" + ((InteractiveMOFitness) archive.get(i).getFitness()).getWeights()[1]);
					System.out.println(" dom=" + ((InteractiveMOFitness) archive.get(i).getFitness()).getDominanceValue() +
							" pref=" + ((InteractiveMOFitness) archive.get(i).getFitness()).getPreferenceValue());
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Set the scaled confidence values for 
	 * the preferences added in the last interaction.
	 * */
	protected void updatePreferenceConfidences(){
		int size = this.preferences.size();
		double acc = 0.0;
		if(size>0){

			int k=size-1;
			while(k>=0 && preferences.get(k).wasAddedInLastInteraction()){
				acc += this.preferences.get(k).getConfidence();
				k--;
			}
			k=size-1;
			while(k>=0 && preferences.get(k).wasAddedInLastInteraction()){
				this.preferences.get(k).setScaledConfidence(this.preferences.get(k).getConfidence()/acc);
				k--;
			}
		}
	}

	/**
	 * Update the priority of each preference
	 * */
	protected void updatePreferencePriorities(List<IIndividual> population){

		double value;
		int nSolutionsThreshold;
		int size = this.preferences.size();
		ArchitecturalPreference preference;
		int size2 = population.size();
		for(int i=0; i<size; i++){
			preference = this.preferences.get(i);
			nSolutionsThreshold = 0;
			for(int j=0; j<size2; j++){
				value = ((InteractiveMOFitness)population.get(j).getFitness()).getPreferenceValueAt(i);
				if(value > preference.getThreshold()){
					nSolutionsThreshold++;
				}
			}
			preference.setNumberOfSolutions(nSolutionsThreshold);
			value = 1.0 - ((double)nSolutionsThreshold / (double)size2);
			preference.setPriority(value);
		}
	}

	protected void evaluateNewPreferences(InteractiveMOIndividual individual){
		int size = this.preferences.size();
		int numberPrefsInd;
		double value;

		if(size>0){

			numberPrefsInd = ((InteractiveMOFitness)individual.getFitness()).getPreferenceValues().size();

			// a new individual, evaluate all preferences
			if(numberPrefsInd == 0){
				for(int i=0; i<size; i++){
					value = this.preferences.get(i).evaluatePreference(individual);
					((InteractiveMOFitness)individual.getFitness()).addPreferenceValue(value);
				}
			}

			// an existing individual, only the last preferences should be evaluated
			else if(numberPrefsInd < size){
				List<Double> values = new ArrayList<Double>();
				int k=size-1;
				while(k>=0 && preferences.get(k).wasAddedInLastInteraction()){
					values.add(this.preferences.get(k).evaluatePreference(individual));
					k--;
				}
				// save in the opposite order
				for(int i=values.size()-1; i>=0; i--){
					((InteractiveMOFitness)individual.getFitness()).addPreferenceValue(values.get(i));
				}
			}
		}
	}

	protected double preferenceValue(InteractiveMOIndividual individual){
		double avg = 0.0;
		double weight;

		List<Double> values = ((InteractiveMOFitness)individual.getFitness()).getPreferenceValues();
		int size = this.preferences.size();

		// Compute the preference value
		if(size>0){
			for(int i=0; i<size; i++){
				// compute the weight
				weight = this.preferences.get(i).getPriority()*this.preferences.get(i).getScaledConfidence();
				// accumulate considering the preference weight
				avg += weight*values.get(i);
			}
			avg /= size;
		}
		return avg;
	}

	/**
	 * 
	 * */
	protected void assignFavoredWeights(List<IIndividual> population){

		Individual solution;
		InteractiveMOFitness fitness;
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();
		double [] objValues = new double[nObjs];
		double [] weights = new double[nObjs];

		int size = population.size();
		// For all solutions
		for(int i=0; i<size; i++){
			solution = (Individual)population.get(i);
			fitness = (InteractiveMOFitness)solution.getFitness();
			// Get the objective values
			for(int j=0; j<nObjs; j++){
				try {
					objValues[j] = fitness.getObjectiveDoubleValue(j);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}

			// Check if the solution has some objective equal to the ideal point (case 1-3)
			boolean allEquals = true, oneEqual = false;
			for(int j=0; j<nObjs; j++){
				if((objValues[j]-idealPoint[j])<0.000000000001){
					oneEqual = true;
				}
				else{
					allEquals = false;
				}
			}

			// Compute the weight for each objective
			double total = 0.0;
			double acc = 0.0;

			// Compute acc (constant value for case 1)
			if(!allEquals && !oneEqual){

				for(int j=0; j<nObjs; j++){
					acc += 1.0/(objValues[j]-idealPoint[j]);
				}
				acc = Math.pow(acc,-1.0);
			}

			for(int j=0; j<nObjs; j++){
				if(allEquals){ // Case 2: the solution is equal to the ideal point
					weights[j] = 1.0;
				}
				else if(oneEqual){ // Case 3: the solution has at least one objective value equal to the ideal point
					weights[j] = 0.0;
				}
				else{ 
					// Case 1: the solution does not have any value equal to the ideal point
					weights[j] = (1.0/(objValues[j]-idealPoint[j])) * acc;
				}
				total +=weights[j];
			}

			// Scale the weights in [0,1]
			if(total!=0){
				for(int j=0; j<nObjs; j++){
					weights[j] /= total;
				}
			}
			// Set in the fitness
			fitness.setWeights(weights);
		}
	}

	/**
	 * 
	 * */
	protected void updatePreferredRegion(){

		double [] weights = ((InteractiveMOFitness)preferredSolution.getFitness()).getWeights();
		int nObjs = weights.length;
		double threshold1, threshold2;
		double [][] weightPreferredRegion = new double[nObjs][2];

		/*System.out.println("UPDATE PREFERRED REGION");
		System.out.println("Weight best solution: " + weights[0] + " " + weights[1]);
		System.out.println("Reduction factor: " + reductionFactor);
		 */
		for(int i=0; i<nObjs; i++){
			threshold1 = (weights[i]-reductionFactor/2.0);
			threshold2 = (weights[i]+reductionFactor/2.0);

			//System.out.println("threshold1" + threshold1);
			//System.out.println("threshold2: " + threshold2);

			if(threshold1<=0){
				//System.out.println("Case 1");
				weightPreferredRegion[i][0] = 0.0;					// low bound
				weightPreferredRegion[i][1] = reductionFactor; 		// upper bound
			}
			else if(threshold2>=1){
				//System.out.println("Case 2");
				weightPreferredRegion[i][0] = 1.0-reductionFactor;	// low bound
				weightPreferredRegion[i][1] = 1.0; 					// upper bound
			}
			else{
				//System.out.println("Case 3");
				weightPreferredRegion[i][0] = threshold1;			// low bound
				weightPreferredRegion[i][1] = threshold2; 			// high bound
			}
		}

		this.currentTerritorySize = this.finalTerritory*Math.exp((nInteractions-interactionCounter)*rho);

		this.regions.add(new PreferredRegion(interactionCounter, weightPreferredRegion, this.currentTerritorySize));
	}

	/**
	 * 
	 * */
	protected double calculateDistance(IIndividual solution1, IIndividual solution2){
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();
		double distance = 0.0;
		MOFitness fitness1 = (MOFitness)solution1.getFitness();
		MOFitness fitness2 = (MOFitness)solution2.getFitness();
		for(int i=0; i<nObjs; i++){
			try {
				distance += Math.abs(fitness1.getObjectiveDoubleValue(i)-fitness2.getObjectiveDoubleValue(i));
			} catch (IllegalAccessException | IllegalArgumentException e) {
				distance = Double.POSITIVE_INFINITY;
				break;
			}
		}
		return distance;
	}

	/**
	 * 
	 * */
	protected double calculateMaximumThreshold(IIndividual solution1, IIndividual solution2){
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();
		double distance;
		double max = Double.NEGATIVE_INFINITY;
		MOFitness fitness1 = (MOFitness)solution1.getFitness();
		MOFitness fitness2 = (MOFitness)solution2.getFitness();
		for(int i=0; i<nObjs; i++){
			try {
				distance = Math.abs(fitness1.getObjectiveDoubleValue(i)-fitness2.getObjectiveDoubleValue(i));
				if(distance > max){
					max = distance;
				}
			} catch (IllegalAccessException | IllegalArgumentException e) {
				max = Double.NEGATIVE_INFINITY;
				break;
			}
		}
		return max;
	}

	/**
	 * 
	 * */
	protected IIndividual choosePreferredSolution(List<IIndividual> archive){
		// the solution with the maximum value of preferences achievement
		double max = Double.NEGATIVE_INFINITY;
		int index = 0;
		double preferencesValue;
		for(int i=0; i<archive.size(); i++){
			preferencesValue = ((InteractiveMOFitness)archive.get(i).getFitness()).getPreferenceValue();
			if(preferencesValue > max){
				max = preferencesValue;
				index = i;
			}
		}
		return archive.get(index);
	}


	protected List<IIndividual> updateArchive1(List<IIndividual> offspring, List<IIndividual> archive){
		//System.out.println("STRATEGY -- UPDATE ARCHIVE 1");

		int numOfOffspring = offspring.size();
		IIndividual archiveMember, individual;
		boolean isDominated;
		int j=0;
		List<IIndividual> newArchive = new ArrayList<IIndividual>();
		for(int i=0; i<archive.size(); i++){
			newArchive.add(archive.get(i).copy());
		}

		List<Integer> dominatedInds = new ArrayList<Integer>();
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();

		// The offspring replaces dominated solutions in the archive
		boolean isEquivalent;

		for(int i=0; i<numOfOffspring; i++){

			if(this.offspringSurvive[i]){ // only an offspring that survives can enter in the archive

				individual =  offspring.get(i);

				// First, check that an equivalent solution doesn't exist in the archive
				isEquivalent = false;
				for(j=0; !isEquivalent && j<newArchive.size(); j++){
					archiveMember = newArchive.get(j);
					if(((InteractiveMOIndividual)individual).isEquivalent((InteractiveMOIndividual)archiveMember)){
						isEquivalent = true;
					}
				}

				// Next, check dominance
				if(!isEquivalent){
					isDominated = false;
					j=0;
					dominatedInds.clear();

					// store the index of the archive members that are dominated by the individual
					for(j=0; !isDominated && j<newArchive.size(); j++){
						archiveMember = newArchive.get(j);
						switch(getSolutionComparator().compare(individual, archiveMember)){
						case 1: dominatedInds.add(j); break;
						case -1: isDominated = true; break;
						}
					}

					// if the offspring dominates all the archive, remove it and add the offspring
					if(dominatedInds.size()==newArchive.size()){
						newArchive.clear();
						((InteractiveMOFitness)individual.getFitness()).setTerritory(this.initialTerritory);
						((InteractiveMOFitness)individual.getFitness()).setRegion(0);
						newArchive.add(individual.copy());
						nUpdates++;
					}

					else if(isDominated == false){ // the offspring has opportunities to enter in the archive

						// get the favorable weights for the offspring (already computed)
						double [] weights = ((InteractiveMOFitness)individual.getFitness()).getWeights();
						//System.out.println("offspring weights = [" + weights[0] + "," + weights[1] + "]");
						int index = 0;
						double [][] weightsR;

						// get the preferred region for this individual
						for(j=0; j<this.regions.size(); j++){
							weightsR = this.regions.get(j).getWeights();
							boolean inside = true;
							for(int k=0; inside && k<nObjs; k++){
								if(!(weights[k] >= weightsR[k][0] && weights[k] <= weightsR[k][1])){
									inside = false;
								}
							}
							if(inside){ // the weight is inside the bounds of region 'j' for all the objectives
								index = j;
							}
						}

						// get the territory size of the preferred region
						double t = this.regions.get(index).getTerritorySize();

						// calculate rectilinear distance between the individual and the non-dominated members of the archive
						// and find the archive member that is closer to the offspring
						double minDistance = Double.POSITIVE_INFINITY;
						double distance;
						//System.out.println("DISTANCES: ");
						for(j=0; j<newArchive.size(); j++){
							if(!dominatedInds.contains(j)){
								distance = calculateDistance(individual, newArchive.get(j));
								if(distance < minDistance){
									minDistance = distance;
									index = j;
								}
							}
						}

						// compute the threshold
						double threshold = calculateMaximumThreshold(individual, newArchive.get(index));

						// check the threshold and the territory size
						if(threshold >= t){
							// remove solutions that are dominated by the individual
							for(j=0; j<dominatedInds.size(); j++){
								newArchive.remove(dominatedInds.get(j).intValue());
								if(j+1<dominatedInds.size())
									dominatedInds.set(j+1, dominatedInds.get(j+1)-(j+1)); // decrement the original index
							}

							// accept the individual
							((InteractiveMOFitness)individual.getFitness()).setTerritory(t);
							((InteractiveMOFitness)individual.getFitness()).setRegion(index);
							newArchive.add(individual);
							nUpdates++;
						}
					}
				}
			}
		}
		return newArchive;
	}

	protected List<IIndividual> updateArchive2(List<IIndividual> offspring, List<IIndividual> archive){
		//System.out.println("STRATEGY -- UPDATE ARCHIVE 2");

		int numOfOffspring = offspring.size();
		IIndividual archiveMember, individual;
		boolean isDominated;
		int j=0;
		List<IIndividual> newArchive = new ArrayList<IIndividual>();
		for(int i=0; i<archive.size(); i++){
			newArchive.add(archive.get(i).copy());
		}

		List<Integer> dominatedInds = new ArrayList<Integer>();
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();

		// The offspring replaces dominated solutions in the archive
		boolean isEquivalent;

		for(int i=0; i<numOfOffspring; i++){

			if(this.offspringSurvive[i]){ // only an offspring that survives can enter in the archive

				individual =  offspring.get(i);

				// First, check that an equivalent solution doesn't exist in the archive
				isEquivalent = false;
				for(j=0; !isEquivalent && j<newArchive.size(); j++){
					archiveMember = newArchive.get(j);
					if(((InteractiveMOIndividual)individual).isEquivalent((InteractiveMOIndividual)archiveMember)){
						isEquivalent = true;
					}
				}

				// Next, check dominance
				if(!isEquivalent){
					isDominated = false;
					j=0;
					dominatedInds.clear();

					// store the index of the archive members that are dominated by the individual
					for(j=0; !isDominated && j<newArchive.size(); j++){
						archiveMember = newArchive.get(j);
						switch(getSolutionComparator().compare(individual, archiveMember)){
						case 1: dominatedInds.add(j); break;
						case -1: isDominated = true; break;
						}
					}

					// if the offspring dominates all the archive, remove it and add the offspring
					if(dominatedInds.size()==newArchive.size()){
						newArchive.clear();
						((InteractiveMOFitness)individual.getFitness()).setTerritory(this.initialTerritory);
						((InteractiveMOFitness)individual.getFitness()).setRegion(0);
						newArchive.add(individual.copy());
						nUpdates++;
					}

					else if(isDominated == false){ // the offspring has opportunities to enter in the archive
						// get the favorable weights for the offspring (already computed)
						double [] weights = ((InteractiveMOFitness)individual.getFitness()).getWeights();
						//System.out.println("offspring weights = [" + weights[0] + "," + weights[1] + "]");
						int index = 0;
						double [][] weightsR;

						// get the preferred region for this individual
						for(j=0; j<this.regions.size(); j++){
							weightsR = this.regions.get(j).getWeights();
							boolean inside = true;
							for(int k=0; inside && k<nObjs; k++){
								if(!(weights[k] >= weightsR[k][0] && weights[k] <= weightsR[k][1])){
									inside = false;
								}
							}
							if(inside){ // the weight is inside the bounds of region 'j' for all the objectives
								index = j;
							}
						}
						// get the territory size of the preferred region
						double t = this.regions.get(index).getTerritorySize();

						// calculate rectilinear distance between the individual and the non-dominated members of the archive
						// and find the archive member that is closer to the offspring
						double minDistance = Double.POSITIVE_INFINITY;
						double distance;
						for(j=0; j<newArchive.size(); j++){
							if(!dominatedInds.contains(j)){
								distance = calculateDistance(individual, newArchive.get(j));
								if(distance < minDistance){
									minDistance = distance;
									index = j;
								}
							}
						}

						// compute the threshold
						double threshold = calculateMaximumThreshold(individual, newArchive.get(index));

						// check the threshold and the territory size
						if(threshold >= t){
							// remove solutions that are dominated by the individual
							for(j=0; j<dominatedInds.size(); j++){
								newArchive.remove(dominatedInds.get(j).intValue());
								if(j+1<dominatedInds.size())
									dominatedInds.set(j+1, dominatedInds.get(j+1)-(j+1)); // decrement the original index
							}

							// accept the individual
							((InteractiveMOFitness)individual.getFitness()).setTerritory(t);
							((InteractiveMOFitness)individual.getFitness()).setRegion(index);
							newArchive.add(individual);
							nUpdates++;
						}

						// consider another opportunity: replace the closest dominated solution 
						// or a solution in the same territory if the offspring has better preference value
						else{

							if(dominatedInds.size()>0){
								index = -1;
								minDistance = Double.POSITIVE_INFINITY;
								for(j=0; j<dominatedInds.size(); j++){
									distance = calculateDistance(individual, newArchive.get(dominatedInds.get(j)));
									if(distance < minDistance){
										minDistance = distance;
										index = dominatedInds.get(j);
									}
								}
								if(index!=-1){
									((InteractiveMOFitness)individual.getFitness()).setTerritory(-1*t); // TODO
									((InteractiveMOFitness)individual.getFitness()).setRegion(((InteractiveMOFitness)newArchive.get(index).getFitness()).getRegion());
									newArchive.set(index, individual);
									nUpdates++;
								}
							}
						}
					}
				}
			}
		}
		return newArchive;
	}

	protected List<IIndividual> updateArchive3(List<IIndividual> offspring, List<IIndividual> archive){
		//System.out.println("STRATEGY -- UPDATE ARCHIVE 3");

		int numOfOffspring = offspring.size();
		IIndividual archiveMember, individual;
		boolean isDominated;
		int j=0;
		List<IIndividual> newArchive = new ArrayList<IIndividual>();
		for(int i=0; i<archive.size(); i++){
			newArchive.add(archive.get(i).copy());
		}

		List<Integer> dominatedInds = new ArrayList<Integer>();
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();

		// The offspring replaces dominated solutions in the archive
		boolean isEquivalent;

		for(int i=0; i<numOfOffspring; i++){

			if(this.offspringSurvive[i]){ // only an offspring that survives can enter in the archive

				individual =  offspring.get(i);

				// First, check that an equivalent solution doesn't exist in the archive
				isEquivalent = false;
				for(j=0; !isEquivalent && j<newArchive.size(); j++){
					archiveMember = newArchive.get(j);
					if(((InteractiveMOIndividual)individual).isEquivalent((InteractiveMOIndividual)archiveMember)){
						isEquivalent = true;
					}
				}

				// Next, check dominance
				if(!isEquivalent){
					isDominated = false;
					j=0;
					dominatedInds.clear();

					// store the index of the archive members that are dominated by the individual
					for(j=0; !isDominated && j<newArchive.size(); j++){
						archiveMember = newArchive.get(j);
						switch(getSolutionComparator().compare(individual, archiveMember)){
						case 1: dominatedInds.add(j); break;
						case -1: isDominated = true; break;
						}
					}

					// if the offspring dominates all the archive, remove it and add the offspring
					if(dominatedInds.size()==newArchive.size()){
						newArchive.clear();
						((InteractiveMOFitness)individual.getFitness()).setTerritory(this.initialTerritory);
						((InteractiveMOFitness)individual.getFitness()).setRegion(0);
						newArchive.add(individual.copy());
						nUpdates++;
					}

					else if(isDominated == false){ // the offspring has opportunities to enter in the archive
						// get the favorable weights for the offspring (already computed)
						double [] weights = ((InteractiveMOFitness)individual.getFitness()).getWeights();
						int index = 0;
						double [][] weightsR;

						// get the preferred region for this individual
						for(j=0; j<this.regions.size(); j++){
							weightsR = this.regions.get(j).getWeights();
							boolean inside = true;
							for(int k=0; inside && k<nObjs; k++){
								if(!(weights[k] >= weightsR[k][0] && weights[k] <= weightsR[k][1])){
									inside = false;
								}
							}
							if(inside){ // the weight is inside the bounds of region 'j' for all the objectives
								index = j;
							}
						}

						// get the territory size of the preferred region
						double t = this.regions.get(index).getTerritorySize();

						// calculate rectilinear distance between the individual and the non-dominated members of the archive
						// and find the archive member that is closer to the offspring
						double minDistance = Double.POSITIVE_INFINITY;
						double distance;
						for(j=0; j<newArchive.size(); j++){
							if(!dominatedInds.contains(j)){
								distance = calculateDistance(individual, newArchive.get(j));
								if(distance < minDistance){
									minDistance = distance;
									index = j;
								}
							}
						}

						// compute the threshold
						double threshold = calculateMaximumThreshold(individual, newArchive.get(index));

						// check the threshold and the territory size
						if(threshold >= t){
							// remove solutions that are dominated by the individual
							for(j=0; j<dominatedInds.size(); j++){
								newArchive.remove(dominatedInds.get(j).intValue());
								if(j+1<dominatedInds.size())
									dominatedInds.set(j+1, dominatedInds.get(j+1)-(j+1)); // decrement the original index
							}

							// accept the individual
							((InteractiveMOFitness)individual.getFitness()).setTerritory(t); //TODO
							((InteractiveMOFitness)individual.getFitness()).setRegion(index);
							newArchive.add(individual);
							nUpdates++;
						}

						// consider another opportunity: replace the closest dominated solution 
						// or a solution in the same territory if the offspring has better preference value
						else{

							if(dominatedInds.size()>0){
								index = -1;
								minDistance = Double.POSITIVE_INFINITY;
								for(j=0; j<dominatedInds.size(); j++){
									distance = calculateDistance(individual, newArchive.get(dominatedInds.get(j)));
									if(distance < minDistance){
										minDistance = distance;
										index = dominatedInds.get(j);
									}
								}
								if(index!=-1){
									((InteractiveMOFitness)individual.getFitness()).setTerritory(-1*t); // TODO
									((InteractiveMOFitness)individual.getFitness()).setRegion(((InteractiveMOFitness)newArchive.get(index).getFitness()).getRegion());
									newArchive.set(index, individual);
									nUpdates++;
								}
							}

							else{
								double offspringPrefValue = ((InteractiveMOFitness)individual.getFitness()).getPreferenceValue();
								double archiveMemberPrefValue = ((InteractiveMOFitness)newArchive.get(index).getFitness()).getPreferenceValue();
								if(offspringPrefValue > archiveMemberPrefValue){
									((InteractiveMOFitness)individual.getFitness()).setTerritory(-1.0); // TODO Check overlapping
									((InteractiveMOFitness)individual.getFitness()).setRegion(((InteractiveMOFitness)newArchive.get(index).getFitness()).getRegion());
									newArchive.set(index, individual);
									nUpdates++;
								}
							}
						}
					}
				}
			}
		}
		return newArchive;
	}

	protected List<IIndividual> updateArchive4(List<IIndividual> offspring, List<IIndividual> archive){
		//System.out.println("STRATEGY -- UPDATE ARCHIVE 4");

		int numOfOffspring = offspring.size();
		IIndividual archiveMember, individual;
		boolean isDominated;
		int j=0;
		List<IIndividual> newArchive = new ArrayList<IIndividual>();
		for(int i=0; i<archive.size(); i++){
			newArchive.add(archive.get(i).copy());
		}

		List<Integer> dominatedInds = new ArrayList<Integer>();
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();

		// The offspring replaces dominated solutions in the archive
		boolean isEquivalent;

		for(int i=0; i<numOfOffspring; i++){

			if(this.offspringSurvive[i]){ // only an offspring that survives can enter in the archive

				individual =  offspring.get(i);

				// First, check that an equivalent solution doesn't exist in the archive
				isEquivalent = false;
				for(j=0; !isEquivalent && j<newArchive.size(); j++){
					archiveMember = newArchive.get(j);
					if(((InteractiveMOIndividual)individual).isEquivalent((InteractiveMOIndividual)archiveMember)){
						isEquivalent = true;
					}
				}

				// Next, check dominance
				if(!isEquivalent){
					isDominated = false;
					j=0;
					dominatedInds.clear();

					// store the index of the archive members that are dominated by the individual
					for(j=0; !isDominated && j<newArchive.size(); j++){
						archiveMember = newArchive.get(j);
						switch(getSolutionComparator().compare(individual, archiveMember)){
						case 1: dominatedInds.add(j); break;
						case -1: isDominated = true; break;
						}
					}

					/////////////////////////////////////
					/*System.out.println("Archive members that are dominated by offspring: ");
								for(int k=0; k<dominatedInds.size(); k++){
									System.out.print(dominatedInds.get(k) + " ");
								}
								System.out.println();*/
					/////////////////////////////////////

					// if the offspring dominates all the archive, remove it and add the offspring
					if(dominatedInds.size()==newArchive.size()){
						newArchive.clear();
						((InteractiveMOFitness)individual.getFitness()).setTerritory(this.initialTerritory);
						((InteractiveMOFitness)individual.getFitness()).setRegion(0);
						newArchive.add(individual.copy());
						nUpdates++;
					}

					else if(isDominated == false){ // the offspring has opportunities to enter in the archive

						/////////////////////////////////////
						/*try {
							System.out.println("Current archive members: ");
							for(int k=0; k<newArchive.size(); k++){
								System.out.println("f1=" + ((MOFitness)newArchive.get(k).getFitness()).getObjectiveDoubleValue(0) + " f2=" + 
										((MOFitness)newArchive.get(k).getFitness()).getObjectiveDoubleValue(1) + " pref=" + ((InteractiveMOFitness)newArchive.get(k).getFitness()).getPreferenceValue() +
										" dom=" + ((InteractiveMOFitness)newArchive.get(k).getFitness()).getDominanceValue() + 
										" territory-size=" + ((InteractiveMOFitness)newArchive.get(k).getFitness()).getTerritory());
							}
							System.out.println("Offspring: f1=" + ((MOFitness)individual.getFitness()).getObjectiveDoubleValue(0) + " f2=" + 
									((MOFitness)individual.getFitness()).getObjectiveDoubleValue(1) + " pref=" + ((InteractiveMOFitness)individual.getFitness()).getPreferenceValue() +
									" dom=" + ((InteractiveMOFitness)individual.getFitness()).getDominanceValue());
						} catch (IllegalAccessException | IllegalArgumentException e) {
							e.printStackTrace();
						}*/
						/////////////////////////////////////

						// get the favorable weights for the offspring (already computed)
						double [] weights = ((InteractiveMOFitness)individual.getFitness()).getWeights();
						//System.out.println("offspring weights = [" + weights[0] + "," + weights[1] + "]");
						int index = 0;
						double [][] weightsR;

						// get the preferred region for this individual
						for(j=0; j<this.regions.size(); j++){
							weightsR = this.regions.get(j).getWeights();
							/*System.out.println("Weights in Region h="+j);
										for(int k=0; k<weightsR.length; k++){
											System.out.println("\t obj " + k + " -> [" + weightsR[k][0] + "," + weightsR[k][1] + "]");
										}*/
							boolean inside = true;
							for(int k=0; inside && k<nObjs; k++){
								if(!(weights[k] >= weightsR[k][0] && weights[k] <= weightsR[k][1])){
									inside = false;
								}
							}
							if(inside){ // the weight is inside the bounds of region 'j' for all the objectives
								index = j;
							}
						}
						//System.out.println("Selected region: " + index);


						// get the territory size of the preferred region
						double t = this.regions.get(index).getTerritorySize();
						//System.out.println("selected region: index="+index + " territory size=" + t);

						// calculate rectilinear distance between the individual and the non-dominated members of the archive
						// and find the archive member that is closer to the offspring
						double minDistance = Double.POSITIVE_INFINITY;
						double distance;
						//System.out.println("DISTANCES: ");
						for(j=0; j<newArchive.size(); j++){
							if(!dominatedInds.contains(j)){
								distance = calculateDistance(individual, newArchive.get(j));
								//System.out.println("\t i="+j+" dist=" + distance);
								if(distance < minDistance){
									minDistance = distance;
									index = j;
								}
							}
						}

						// compute the threshold
						double threshold = calculateMaximumThreshold(individual, newArchive.get(index));
						//System.out.println("threshold="+threshold);

						// check the threshold and the territory size
						if(threshold >= t){
							// remove solutions that are dominated by the individual
							for(j=0; j<dominatedInds.size(); j++){
								//System.out.println("\n--remove index="+dominatedInds.get(j) + " archive.size=" + newArchive.size());
								newArchive.remove(dominatedInds.get(j).intValue());
								if(j+1<dominatedInds.size())
									dominatedInds.set(j+1, dominatedInds.get(j+1)-(j+1)); // decrement the original index
								/*for(int k=0; k<dominatedInds.size(); k++){
												System.out.print(dominatedInds.get(k) + " ");
											}*/
							}

							// accept the individual
							((InteractiveMOFitness)individual.getFitness()).setTerritory(t);
							((InteractiveMOFitness)individual.getFitness()).setRegion(index);
							newArchive.add(individual);
							nUpdates++;
							//System.out.println("Offspring is accepted (threshold >= t)");
						}

						// consider another opportunity: if the solution does not belong to any other territory
						// then replace the individual if it has a better preference value, and also remove
						// the dominated solutions
						else{

							//System.out.println("Offspring can survive... Check distances to other territories (considering dominated solutions)");

							/////////////////////////////////////
							/*try {
								System.out.println("Current archive members: ");
								for(int k=0; k<newArchive.size(); k++){
									System.out.println("f1=" + ((MOFitness)newArchive.get(k).getFitness()).getObjectiveDoubleValue(0) + " f2=" + 
											((MOFitness)newArchive.get(k).getFitness()).getObjectiveDoubleValue(1) + " pref=" + ((InteractiveMOFitness)newArchive.get(k).getFitness()).getPreferenceValue() +
											" dom=" + ((InteractiveMOFitness)newArchive.get(k).getFitness()).getDominanceValue() + 
											" territory-size=" + ((InteractiveMOFitness)newArchive.get(k).getFitness()).getTerritory());
								}
								System.out.println("Offspring: f1=" + ((MOFitness)individual.getFitness()).getObjectiveDoubleValue(0) + " f2=" + 
										((MOFitness)individual.getFitness()).getObjectiveDoubleValue(1) + " pref=" + ((InteractiveMOFitness)individual.getFitness()).getPreferenceValue() +
										" dom=" + ((InteractiveMOFitness)individual.getFitness()).getDominanceValue());
							} catch (IllegalAccessException | IllegalArgumentException e) {
								e.printStackTrace();
							}*/
							/////////////////////////////////////

							// Check the distance to other solutions
							//boolean otherTerritory = false;
							int nTerritories = 0;
							for(j=0; j<newArchive.size(); j++){
								//if(j!=index){
								threshold = calculateMaximumThreshold(individual, newArchive.get(j));
								//System.out.println("\tj="+ j + " Threshold="+threshold + " territory-size=" + ((InteractiveMOFitness)newArchive.get(j).getFitness()).getTerritory());
								if(threshold < ((InteractiveMOFitness)newArchive.get(j).getFitness()).getTerritory()){
									nTerritories++;
								}
								//}
							}
							//System.out.println("Offspring is inside " + nTerritories + " territories");

							if(nTerritories==1){ // the solution does not belong to other territory
								double offspringPrefValue = ((InteractiveMOFitness)individual.getFitness()).getPreferenceValue();
								double archiveMemberPrefValue = ((InteractiveMOFitness)newArchive.get(index).getFitness()).getPreferenceValue();
								if(offspringPrefValue > archiveMemberPrefValue){

									// Replace the individual
									double territorySize = Math.abs(((InteractiveMOFitness)newArchive.get(index).getFitness()).getTerritory());
									((InteractiveMOFitness)individual.getFitness()).setTerritory(-1.0*territorySize);
									((InteractiveMOFitness)individual.getFitness()).setRegion(((InteractiveMOFitness)newArchive.get(index).getFitness()).getRegion());
									newArchive.set(index, individual);
									nUpdates++;

									//System.out.println("Offspring is accepted (greater preferences values)");
									// Remove every dominated solution (excluding the replaced individual if it was dominated)
									for(j=0; j<dominatedInds.size(); j++){
										//System.out.println("\n--remove index="+dominatedInds.get(j) + " archive.size=" + newArchive.size());
										if(dominatedInds.get(j).intValue()!=index){
											newArchive.remove(dominatedInds.get(j).intValue());
											if(j+1<dominatedInds.size())
												dominatedInds.set(j+1, dominatedInds.get(j+1)-(j+1)); // decrement the original index
											/*for(int k=0; k<dominatedInds.size(); k++){
															System.out.print(dominatedInds.get(k) + " ");
														}*/
										}
									}
								}
							}
						}
					}
				}
			}
		}

		/*
		System.out.println("NEW ARCHIVE");
		for(int i=0; i<newArchive.size(); i++){
			InteractiveMOFitness f = (InteractiveMOFitness)newArchive.get(i).getFitness();
			try {
				System.out.println("f1="+f.getObjectiveDoubleValue(0) + " f2=" + f.getObjectiveDoubleValue(1) + " pref=" 
						+ f.getPreferenceValue() + " dom=" + f.getDominanceValue() + " territory-size=" +f.getTerritory());
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}*/

		return newArchive;
	}

	protected List<IIndividual> updateArchive5(List<IIndividual>population, List<IIndividual> archive){

		IIndividual archiveMember, individual;
		boolean isDominated;
		int j=0;
		List<IIndividual> newArchive = new ArrayList<IIndividual>();
		for(int i=0; i<archive.size(); i++){
			newArchive.add(archive.get(i).copy());
		}
		List<Integer> dominatedInds = new ArrayList<Integer>();
		int nObjs = ((IMOEvaluator)getContext().getEvaluator()).numberOfObjectives();
		boolean isEquivalent, inside;
		double distance, minDistance, territory, threshold;
		int region, index, nTerritories;
		double [] weights;
		double [][] weightsR;
		
		for(int i=0; i<population.size(); i++){
			individual = population.get(i);
			
			// Solution won't be removed in the future
			if(!((InteractiveMOIndividual)individual).getToBeRemoved()){
				
				// First case: add solutions that has been selected by the user
				if(((InteractiveMOIndividual)individual).getSolutionInArchive()){

					// First, check that an equivalent solution doesn't exist in the archive
					isEquivalent = false;
					for(j=0; !isEquivalent && j<newArchive.size(); j++){
						archiveMember = newArchive.get(j);
						if(((InteractiveMOIndividual)individual).isEquivalent((InteractiveMOIndividual)archiveMember)){
							isEquivalent = true;
							((InteractiveMOIndividual)archiveMember).setSolutionInArchive(true);
						}
					}
					if(!isEquivalent){
						// Remove the archive members that are dominated by the individual
						j=0;
						while(j<newArchive.size()){
							archiveMember = newArchive.get(j);
							switch(getSolutionComparator().compare(individual, archiveMember)){
							case 1:
								if(!((InteractiveMOIndividual)archiveMember).getSolutionInArchive()) // archive members added by the user cannot be removed
									newArchive.remove(j);
								else
									j++;
								break;
							case -1: j++; break;
							case 0: j++; break;
							}
						}
						
						// Check if the solution belongs to a territory
						// get the favorable weights for the offspring (already computed)
						weights = ((InteractiveMOFitness)individual.getFitness()).getWeights();
						index = 0;

						// get the preferred region for this individual
						for(j=0; j<this.regions.size(); j++){
							weightsR = this.regions.get(j).getWeights();
							inside = true;
							for(int k=0; inside && k<nObjs; k++){
								if(!(weights[k] >= weightsR[k][0] && weights[k] <= weightsR[k][1])){
									inside = false;
								}
							}
							if(inside){ // the weight is inside the bounds of region 'j' for all the objectives
								index = j;
							}
						}
						
						// get the territory size of the preferred region
						territory = this.regions.get(index).getTerritorySize();
						region = index;
						
						// calculate rectilinear distance between the individual and archive members
						// and find the archive member that is closer to the individual
						minDistance = Double.POSITIVE_INFINITY;
						for(j=0; j<newArchive.size(); j++){
							distance = calculateDistance(individual, newArchive.get(j));
							if(distance < minDistance){
								minDistance = distance;
								index = j;
							}
						}

						// compute the threshold
						threshold = calculateMaximumThreshold(individual, newArchive.get(index));
						
						// check the threshold and the territory size => no overlapping, add the individual
						if(threshold >= territory){
							
							// accept the individual
							((InteractiveMOFitness)individual.getFitness()).setTerritory(territory);
							((InteractiveMOFitness)individual.getFitness()).setRegion(region);
							newArchive.add(individual);
							nUpdates++;
						}
						else{ // decrement the territory size in this region
							
							territory = territory - threshold; // update the territory
							if(territory<this.finalTerritory)
								territory = this.finalTerritory;
							((InteractiveMOFitness)individual.getFitness()).setTerritory(-1.0*territory);
							((InteractiveMOFitness)individual.getFitness()).setRegion(region);
							newArchive.add(individual);
							nUpdates++;

							// decrement the territory size in the region
							this.regions.get(region).setTerritorySize(territory);
							// update in all the archive members belonging to the same region
							for(int k=0; k<newArchive.size(); k++){
								if(((InteractiveMOFitness)newArchive.get(k).getFitness()).getRegion()==region){
									((InteractiveMOFitness)newArchive.get(k).getFitness()).setTerritory(-1.0*territory);
								}
							}
						}
					}
				}

				// Second case: survivors can replace solutions in the archive
				else{ 
					// First, check that an equivalent solution doesn't exist in the archive
					isEquivalent = false;
					for(j=0; !isEquivalent && j<newArchive.size(); j++){
						archiveMember = newArchive.get(j);
						if(((InteractiveMOIndividual)individual).isEquivalent((InteractiveMOIndividual)archiveMember)){
							isEquivalent = true;
						}
					}

					// Next, check dominance
					if(!isEquivalent){
						isDominated = false;
						j=0;
						dominatedInds.clear();

						// store the index of the archive members that are dominated by the individual
						for(j=0; !isDominated && j<newArchive.size(); j++){
							archiveMember = newArchive.get(j);
							switch(getSolutionComparator().compare(individual, archiveMember)){
							case 1:
								if(!((InteractiveMOIndividual)archiveMember).getSolutionInArchive()) // archive members added by the user cannot be removed
									dominatedInds.add(j); 
								break;
							case -1: isDominated = true; break;
							}
						}

						// if the offspring dominates all the archive, remove it and add the offspring
						if(dominatedInds.size()==newArchive.size()){
							newArchive.clear();
							((InteractiveMOFitness)individual.getFitness()).setTerritory(this.initialTerritory);
							((InteractiveMOFitness)individual.getFitness()).setRegion(0);
							newArchive.add(individual.copy());
							nUpdates++;
						}

						else if(isDominated == false){ // the offspring has opportunities to enter in the archive

							// get the favorable weights for the offspring (already computed)
							weights = ((InteractiveMOFitness)individual.getFitness()).getWeights();
							index = 0;

							// get the preferred region for this individual
							for(j=0; j<this.regions.size(); j++){
								weightsR = this.regions.get(j).getWeights();
								inside = true;
								for(int k=0; inside && k<nObjs; k++){
									if(!(weights[k] >= weightsR[k][0] && weights[k] <= weightsR[k][1])){
										inside = false;
									}
								}
								if(inside){ // the weight is inside the bounds of region 'j' for all the objectives
									index = j;
								}
							}


							// get the territory size of the preferred region
							territory = this.regions.get(index).getTerritorySize();
							region = index;

							// calculate rectilinear distance between the individual and the non-dominated members of the archive
							// and find the archive member that is closer to the offspring
							minDistance = Double.POSITIVE_INFINITY;
							for(j=0; j<newArchive.size(); j++){
								if(!dominatedInds.contains(j)){
									distance = calculateDistance(individual, newArchive.get(j));
									if(distance < minDistance){
										minDistance = distance;
										index = j;
									}
								}
							}

							// compute the threshold
							threshold = calculateMaximumThreshold(individual, newArchive.get(index));
							
							// check the threshold and the territory size
							if(threshold >= territory){
								// remove solutions that are dominated by the individual
								for(j=0; j<dominatedInds.size(); j++){
									newArchive.remove(dominatedInds.get(j).intValue());
									if(j+1<dominatedInds.size())
										dominatedInds.set(j+1, dominatedInds.get(j+1)-(j+1)); // decrement the original index
								}

								// accept the individual
								((InteractiveMOFitness)individual.getFitness()).setTerritory(territory);
								((InteractiveMOFitness)individual.getFitness()).setRegion(region);
								newArchive.add(individual);
								nUpdates++;
							}

							// consider another opportunity: if the solution does not belong to any other territory
							// then replace the individual if it has a better preference value, and also remove
							// the dominated solutions
							else{

								// Check the distance to other solutions
								nTerritories = 0;
								for(j=0; j<newArchive.size(); j++){
									threshold = calculateMaximumThreshold(individual, newArchive.get(j));
									if(threshold < ((InteractiveMOFitness)newArchive.get(j).getFitness()).getTerritory()){
										nTerritories++;
									}
								}

								// If the solution does not belong to other territory and the closer individual is not a permanent solution, check the preference value
								if(nTerritories==1 && !((InteractiveMOIndividual)newArchive.get(index)).getSolutionInArchive()){ 
									double offspringPrefValue = ((InteractiveMOFitness)individual.getFitness()).getPreferenceValue();
									double archiveMemberPrefValue = ((InteractiveMOFitness)newArchive.get(index).getFitness()).getPreferenceValue();
									if(offspringPrefValue > archiveMemberPrefValue){

										// Replace the individual
										territory = Math.abs(((InteractiveMOFitness)newArchive.get(index).getFitness()).getTerritory());
										((InteractiveMOFitness)individual.getFitness()).setTerritory(-1.0*territory);
										((InteractiveMOFitness)individual.getFitness()).setRegion(((InteractiveMOFitness)newArchive.get(index).getFitness()).getRegion());
										newArchive.set(index, individual);
										nUpdates++;

										// Replace any dominated solution
										j=0;
										while(j<newArchive.size()){
											archiveMember = newArchive.get(j);
											if(getSolutionComparator().compare(individual, archiveMember) == 1){
												if(!((InteractiveMOIndividual)archiveMember).getSolutionInArchive()) // archive members added by the user cannot be removed
													newArchive.remove(j);
											}
											else
												j++;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return newArchive;
	}
}
