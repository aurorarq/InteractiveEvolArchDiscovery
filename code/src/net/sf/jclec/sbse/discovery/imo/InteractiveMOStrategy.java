package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.comparator.MOSolutionComparator;
import net.sf.jclec.mo.comparator.NSGA2ConstrainedComparator;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.mo.comparator.fcomparator.MOValueFitnessComparator;
import net.sf.jclec.mo.comparator.fcomparator.ParetoComparator;
import net.sf.jclec.mo.strategy.MOStrategy;
import net.sf.jclec.sbse.discovery.imo.comparators.IndividualPreferenceComparator;
import net.sf.jclec.sbse.discovery.imo.preferences.ArchitecturalPreference;
//import net.sf.jclec.sbse.discovery.imo.selectors.ClusteringMaximinSelector;
import net.sf.jclec.sbse.discovery.imo.selectors.ClusteringSelector;
import net.sf.jclec.util.random.IRandGen;

/**
 * Multi-objective strategy for the discovery of software architectures
 * formulated as a multi-objective interactive problem.
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (February 2015)
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class InteractiveMOStrategy extends MOStrategy {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 994858565541027685L;

	/** Set of architectural preferences */
	protected List<ArchitecturalPreference> preferences;

	/** Archive size */
	protected int archiveMaxSize;

	/** k-nearest neighbor */
	protected int kValue;
	
	/** Number of solutions to be shown */
	protected int nSolutions;
	
	/** Number of archive updates */
	protected int nUpdates;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public InteractiveMOStrategy() {
		super();
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Set the architectural preferences to evaluate individuals
	 * @param preferences The new set of Preferences
	 * */
	public void setPreferences(List<ArchitecturalPreference> preferences){
		this.preferences = preferences;
	}

	/**
	 * Get the architectural preferences
	 * @return The current set of architectural preferences
	 * */
	public List<ArchitecturalPreference> getPreferences(){
		return this.preferences;
	}

	/**
	 * Get the maximum size of the archive.
	 * @return Maximum size of the archive.
	 */
	public int getArchiveMaxSize() {
		return archiveMaxSize;
	}

	/**
	 * Set the maximum size of the archive.
	 * @param archiveMaxSize New maximum size.
	 */
	public void setArchiveMaxSize(int archiveMaxSize) {
		this.archiveMaxSize = archiveMaxSize;
	}

	/**
	 * Get the k value, which represents the k-neighbor
	 * in proximity for the kNN approach.
	 * @return k value
	 */
	public int getKValue(){
		return this.kValue;
	}

	/**
	 * Set the k value, which represents the k-neighbor
	 * in proximity for the kNN approach.
	 * @param kValue New k value
	 */
	public void setKValue(int kValue){
		this.kValue = kValue;
	}
	
	public int getNumberArchiveUpdates(){
		return this.nUpdates;
	}
	

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * <p>Parameters:
	 * <ul>
	 * <li>archive-size <code>(int)</code>: 
	 * <p>Size of the external population. By default,
	 * the population size.</p></li> 
	 * */
	@Override
	public void configure(Configuration settings){

		super.configure(settings);

		// External population size
		try{
			int size = settings.getInt("archive-size", getContext().getPopulationSize());
			setArchiveMaxSize(size);
		}catch(Exception e){
			System.err.println("The archive size should be specified");
			e.printStackTrace();
		}

		// k-value
		/*try{
			int kValue = settings.getInt("k-value", (int)Math.sqrt(getContext().getPopulationSize()+getArchiveMaxSize()));
			if(kValue > getArchiveMaxSize())
				throw new IllegalArgumentException();
			setKValue(kValue);
		}catch(IllegalArgumentException e){
			System.err.println("The k-value should be smaller than the archive size");	
		}catch(Exception e){
			System.err.println("The k-value should be specified");
			e.printStackTrace();
		}*/
	}

	@Override
	public List<IIndividual> initialize(List<IIndividual> population) {

		// Empty preferences
		this.preferences = new ArrayList<ArchitecturalPreference>();

		// Initialize archive with the set of non-dominated solutions
		List<IIndividual> archive = new ArrayList<IIndividual>();
		int size = population.size();
		IIndividual ind;

		/*IIndividual individual1, individual2;
		boolean isDominated;
		for(int i=0; i<size; i++){
			individual1 =  population.get(i);
			isDominated = false;
			for(int j=0; !isDominated && j<size; j++){
				individual2 = population.get(j);
				switch(getIndividualsComparator().compare(individual1, individual2)){
				// Individual1 is dominated by individual2
				case -1:
					isDominated = true;
					break;
					// Both solutions are non dominated
				case 0:
					j++;
					break;
					// Individual1 dominates individual2
				case 1:
					break;
				}
			}
			if(!isDominated){
				archive.add(individual1);
			}
		}*/

		// Assign the first fitness value for all the solutions only considering the maximin function
		//double fitness;
		//double [] maximinValue = new double[2];
		boolean equals;

		fitnessAssignment(population, null);

		for(int i=0; i<size; i++){
			ind = population.get(i);
			/*maximinValue = maximinValue(ind, population);
			((IMOFitness)ind.getFitness()).setDominanceValue(maximinValue[0]);
			((IMOFitness)ind.getFitness()).setMetricIndex((int)maximinValue[1]);
			((IMOFitness)ind.getFitness()).setPreferenceValue(0.0);

			// Set the fitness value, the maximin value is scaled to [0,1]
			fitness = (1.0+(maximinValue[0]))/2.0;*/

			//System.out.println("Pref. value="+fitnessP + " Maximim value="+fitnessA + " Fitness: " + fitness);
			// Add only feasible solutions
			if(((InteractiveMOIndividual)ind).isFeasible()){

				// check if the maximin value is < 0 (non-dominated)
				//((MOFitness)ind.getFitness()).setValue(fitness);
				if(((InteractiveMOFitness)ind.getFitness()).getDominanceValue()<0){

					// check if there exists an equivalent solution in the current archive
					equals = false;
					for(int j=0; !equals && j<archive.size(); j++){
						if(((InteractiveMOIndividual)ind).isEquivalent((InteractiveMOIndividual)archive.get(j))){
							equals=true;
						}
					}
					// non equal solution, add to the archive
					if(!equals){
						archive.add(ind);
					}
				}
			}
			//else
			//	((MOFitness)ind.getFitness()).setValue(2.0);//invalid fitness value
		}

		// TODO
		// Decrement the archive if required
		//ClusteringMaximinSelector selector = new ClusteringMaximinSelector(getContext());
		ClusteringSelector selector = new ClusteringSelector(getContext());
		if(archive.size()>archiveMaxSize){
			/*SPEA2 spea2 = new SPEA2();
			spea2.setExternalPopulationSize(archiveMaxSize);
			spea2.setKValue(kValue);
			archive = spea2.decrementPopulation(archive, archive.size());*/

			archive = selector.select(archive, archiveMaxSize);
		}

		////////////////// VERBOSE
		/*System.out.println("POPULATION size=" + population.size());
		for(IIndividual p: population){
			System.out.println("isFeasible: "+((IMOIndividual)p).isFeasible() + " objectives: ");
			printObjectivesAndFitness((IMOIndividual)p);
		}

		System.out.println("ARCHIVE size=" + archive.size());
		for(IIndividual p: archive){
			printObjectivesAndFitness((IMOIndividual)p);
		}*/
		
		nUpdates = archive.size(); // initialize number of added solutions
		return archive;
	}

	@Override
	public void update() {
		// do nothing
	}

	@Override
	public void createSolutionComparator(Comparator<IFitness>[] components) {
		// Fitness comparator
		ParetoComparator fcomparator = new ParetoComparator(components);
		// Individuals comparator
		NSGA2ConstrainedComparator comparator = new NSGA2ConstrainedComparator(fcomparator);
		setSolutionComparator(comparator);
	}

	@Override
	public List<IIndividual> matingSelection(List<IIndividual> population, List<IIndividual> archive) {

		List<IIndividual> parents = new ArrayList<IIndividual>(2);
		IRandGen randgen = getContext().createRandGen();
		int rndIndex;

		assert(population.size()==getContext().getPopulationSize());

		// One parent from the current population

		rndIndex = randgen.choose(0, population.size());
		parents.add(population.get(rndIndex));

		// Another parent from the archive (if it is not empty)
		if(archive.size() > 0){
			rndIndex = randgen.choose(0, archive.size());
			parents.add(archive.get(rndIndex).copy());
		}
		else{
			rndIndex = randgen.choose(0, population.size());
			parents.add(population.get(rndIndex).copy());
		}
		return parents;
	}

	@Override
	public List<IIndividual> environmentalSelection(List<IIndividual> population, 
			List<IIndividual> offspring, List<IIndividual> archive) {

		/*int numOfOffspring = offspring.size();
		int size, worstIndex;
		double worstFitness, currentFitness;
		boolean isInvalidOffspring, isInvalidIndividual;*/

		// Evaluate the population and the offspring
		List<IIndividual> allIndividuals = new ArrayList<IIndividual>();
		allIndividuals.addAll(population);
		allIndividuals.addAll(offspring);
		fitnessAssignment(allIndividuals, archive);

		// Select survivors, the two offspring replaces the worst parents
		/*int i,j;
		boolean finish;
		int nonDominated;
		IRandGen randgen = getContext().createRandGen();
		int numOfOffspring = offspring.size();
		int indexToRemove = -1;
		IMOIndividual offspringInd;
		IMOIndividual populationInd;

		MOIndividualsComparator comparator = super.getIndividualsComparator();*/

		// Add current population
		List<IIndividual> survivors = new ArrayList<IIndividual>();
		survivors.addAll(population);

		/////////// Customized strategy --> strongly promotes preferences
		/*List<IIndividual> candidatesUnfeasibility = new ArrayList<IIndividual>();
		List<IIndividual> candidatesDominated = new ArrayList<IIndividual>();
		List<IIndividual> candidatesNonDominated = new ArrayList<IIndividual>();
		double worstValue, value;

		for(i=0; i<numOfOffspring; i++){
			offspringInd = (IMOIndividual)offspring.get(i);
			j=0;
			finish=false;

			// 1. The offspring should be a different architectural solution
			while(!finish && j<survivors.size()){
				populationInd = (IMOIndividual)survivors.get(j);
				if(offspringInd.isEquivalent(populationInd)){
					finish = true;
				}
				j++;
			}

			if(!finish){

				candidatesUnfeasibility.clear();
				candidatesDominated.clear();
				candidatesNonDominated.clear();

				// Search current invalid solutions
				for(j=0; j<survivors.size(); j++){
					populationInd = (IMOIndividual)survivors.get(j);
					if(!populationInd.isFeasible()){
						candidatesUnfeasibility.add(populationInd);
					}
				}

				// 3. If the individual is feasible, candidate parents to be replaced are: 
				// 1) invalids, 2) dominated individuals with poor fitness, 3) non-dominated individuals with poor fitness
				if(offspringInd.isFeasible()){

					// Create the different set of candidates
					for(j=0; j<survivors.size(); j++){
						populationInd = (IMOIndividual)survivors.get(j);
						if(populationInd.isFeasible()){
							switch(comparator.compare(offspringInd, populationInd)){
							case 1: // offspring dominates the population member
								candidatesDominated.add(populationInd);
								break;
							case -1: // offspring is dominated by population member
								break;
							case 0: // non-dominated solutions
								candidatesNonDominated.add(populationInd);
								break;
							}
						}
					}

					// Choose the individual to be replaced

					// Population contains invalids, remove the solution with worst preference value.
					if(candidatesUnfeasibility.size() > 0){
						worstValue = 1.0;
						indexToRemove = -1;
						for(j=0; j<candidatesUnfeasibility.size(); j++){
							value = ((IMOFitness)candidatesUnfeasibility.get(j).getFitness()).getPreferenceValue();
							if(value!=-1.0 && value<worstValue){
								worstValue = value;
								indexToRemove = j;
							}
						}
						if(indexToRemove==-1){ // all the individual has the same preference value
							indexToRemove = randgen.choose(0, candidatesUnfeasibility.size());
						}

						survivors.remove(candidatesUnfeasibility.get(indexToRemove));
					}

					// The offspring will replace a dominated individuals
					else if(candidatesDominated.size() > 0){
						worstValue = Double.MIN_VALUE;
						indexToRemove = -1;
						for(j=0; j<candidatesDominated.size(); j++){
							value = ((IMOFitness)candidatesDominated.get(j).getFitness()).getValue();
							if(value>worstValue){
								worstValue = value;
								indexToRemove = j;
							}
						}
						survivors.remove(candidatesDominated.get(indexToRemove));
					}

					// The offspring will replace a non-dominated individual
					else if(candidatesNonDominated.size() > 0){
						worstValue = Double.MIN_VALUE;
						indexToRemove = -1;
						for(j=0; j<candidatesNonDominated.size(); j++){
							value = ((IMOFitness)candidatesNonDominated.get(j).getFitness()).getValue();
							if(value>worstValue){
								worstValue = value;
								indexToRemove = j;
							}
						}
						survivors.remove(candidatesNonDominated.get(indexToRemove));
					}

					// The offspring is dominated by all the population members, replace one individual (not offspring) at random
					else{
						indexToRemove = randgen.choose(0, survivors.size()-i);
					}

					// Add the offspring
					survivors.add(offspringInd);
				}
				// 2. If the individual is not feasible, candidate parents to be replaced are:
				// 1) invalids (random or ordered by preference value?)
				else{

					if(candidatesUnfeasibility.size()>0){
						worstValue = 1.0;
						indexToRemove = -1;
						for(j=0; j<candidatesUnfeasibility.size(); j++){
							value = ((IMOFitness)candidatesUnfeasibility.get(j).getFitness()).getPreferenceValue();
							if(value!=-1.0 && value<worstValue){
								worstValue = value;
								indexToRemove = j;
							}
						}
						if(indexToRemove==-1){ // all the individual has the same preference value
							indexToRemove = randgen.choose(0, candidatesUnfeasibility.size());
						}

						survivors.remove(candidatesUnfeasibility.get(indexToRemove));
						// Add the offspring
						survivors.add(offspringInd);
					}
				}			
			}
		}*/

		/////////// eMOEA replacement method --> promotes non-dominated solutions

		// For each offspring
		/*for(i=0; i<numOfOffspring; i++){
			offspringInd = (IMOIndividual) offspring.get(i);
			j=0;
			finish=false;
			nonDominated=0;

			while(!finish && j<survivors.size()){
				populationInd = (IMOIndividual) survivors.get(j);

				// If the offspring dominates one individual in 
				// the current population, replace it
				switch(comparator.compare(offspringInd, populationInd)){
				case 1:
					survivors.remove(j);
					finish=true;
					break;
				case -1:
					j++;
					break;
				case 0:
					nonDominated++;
					j++;
					break;
				}
			}
			// The offspring dominates any individual
			if(finish){
				survivors.add(offspringInd.copy());
			}
			// The offspring is non dominated by any individual in the population
			// Replace one randomly chosen member
			else if(nonDominated==survivors.size()){
				indexToRemove = randgen.choose(0, survivors.size());
				survivors.remove(indexToRemove);
				survivors.add(offspringInd.copy());
			}
		}*/

		///////////////////////////////////// Comparison by fitness
		/*System.out.println("OFFSPRING");
		for(IIndividual ind: offspring){
			System.out.println(ind.getFitness());
		}

		System.out.println("POPULATION");
		for(IIndividual ind: population){
			System.out.println(ind.getFitness());
		}*/

		MOValueFitnessComparator fcomparator = new MOValueFitnessComparator();
		MOSolutionComparator comparator = new MOSolutionComparator(fcomparator);
		Collections.sort(survivors,comparator);
		survivors.remove(survivors.size()-1);
		survivors.remove(survivors.size()-1);
		survivors.addAll(offspring);

		assert(survivors.size()==getContext().getPopulationSize());

		/*System.out.println("SURVIVORS");
		for(IIndividual ind: survivors){
			System.out.println(ind.getFitness());
		}*/

		return survivors;
	}

	@Override
	public List<IIndividual> updateArchive(List<IIndividual> population, 
			List<IIndividual> offspring, List<IIndividual> archive) {

		int numOfOffspring = offspring.size();
		IIndividual archiveMember, individual;
		boolean isDominated;
		int j=0;
		List<IIndividual> newArchive = new ArrayList<IIndividual>();
		newArchive.addAll(archive);
		int size = newArchive.size();

		// The offspring replaces dominated solutions in the archive
		for(int i=0; i<numOfOffspring; i++){
			individual =  offspring.get(i);
			isDominated = false;
			j=0;
			size = newArchive.size();
			//System.out.println("offspring="+i);
			while(j<size && !isDominated){
				archiveMember = newArchive.get(j);
				//System.out.println("archive member="+j);
				//System.out.println("equivalent=" + ((IMOIndividual)individual).isEquivalent((IMOIndividual)archiveMember));
				if(!((InteractiveMOIndividual)individual).isEquivalent((InteractiveMOIndividual)archiveMember)){ // check that the two individuals are different
					switch(getSolutionComparator().compare(individual, archiveMember)){ // check dominance
					// The individual is dominated by the archive member
					case -1:
						isDominated = true;
						break;
						// Both solutions are non dominated
					case 0:
						j++;
						break;
						// The individual dominates the archive member
					case 1:
						newArchive.remove(j);
						size--;
						break;
					}
				}
				// An equivalent solution exists, stop the search
				else
					isDominated = true;
			}
			if(!isDominated){
				newArchive.add(individual);
				nUpdates++;
			}

			// Decrement the archive if required
			// TODO
			// Option 1: use the clustering approach or SPEA2

			/*if(archive.size()>archiveMaxSize){
			SPEA2 spea2 = new SPEA2();
			spea2.setExternalPopulationSize(archiveMaxSize);
			spea2.setKValue(kValue);
			newArchive = spea2.decrementPopulation(newArchive, archive.size());
			}*/

			/*ClusteringMaximinSelector selector = new ClusteringMaximinSelector(getContext());
			if(newArchive.size()>archiveMaxSize){
				newArchive = selector.select(newArchive, archiveMaxSize);
			}*/

			// Option 2: use the value of the preferences (or fitness?)
			IndividualPreferenceComparator comparator = new IndividualPreferenceComparator();
			Collections.sort(newArchive,comparator);
			while(newArchive.size()>archiveMaxSize){
				newArchive.remove(newArchive.size()-1);
			}
		}
		return newArchive;
	}

	@Override
	protected void fitnessAssignment(List<IIndividual> population, List<IIndividual> archive) {

		int numOfPrefs = this.preferences.size();
		int populationSize = population.size();
		
		InteractiveMOIndividual individual;
		double fitnessP;
		double [] fitnessA = new double[2];
		ArchitecturalPreference preference;
		
		// Get the set of non dominated solutions
		List<IIndividual> nonDominated = extractNonDominatedIndividuals(population, archive);

		// Evaluate all the solutions
		List<IIndividual> allIndividuals = new ArrayList<IIndividual>();
		allIndividuals.addAll(population);
		if(archive!=null)
			allIndividuals.addAll(archive);

		//System.out.println("Strategy (parent): allInds.size()="+allIndividuals.size() + " gen=" + getContext().getGeneration());

		// empty the count of solution in the list of preferences
		for(int i=0; i<numOfPrefs; i++){
			preference = this.preferences.get(i);
			preference.setNumberOfSolutions(0);
		}

		for(int i=0; i<populationSize; i++){
			individual = (InteractiveMOIndividual)allIndividuals.get(i);
			fitnessP = 0.0;
			fitnessA[0] = 0.0;
			fitnessA[1] = -1.0;

			// Compute the preference value
			fitnessP = preferenceValue(individual);

			// Compute the maximin fitness value
			fitnessA = maximinValue(individual, nonDominated);

			// Set the fitness components
			((InteractiveMOFitness)individual.getFitness()).setPreferenceValue(fitnessP);
			((InteractiveMOFitness)individual.getFitness()).setDominanceValue(fitnessA[0]);
			((InteractiveMOFitness)individual.getFitness()).setMetricIndex((int)fitnessA[1]);

			// Set the fitness value, the first term (preferences) is inverted 
			// and the second term (maximin) is scaled to [0,1]
			double fitness = 0.5*(1.0-fitnessP) + 0.5*((1.0+(fitnessA[0]))/2.0);

			//System.out.println("Pref. value="+fitnessP + " Maximim value="+fitnessA + " Fitness: " + fitness);
			if(individual.isFeasible())
				((MOFitness)individual.getFitness()).setValue(fitness);
			else
				((MOFitness)individual.getFitness()).setValue(2.0);//invalid fitness value
		}
	}

	protected double preferenceValue(InteractiveMOIndividual individual){
		double preferenceValue = 0.0;
		double avg = 0.0;
		int size = this.preferences.size();
		ArchitecturalPreference preference;
		double weight;
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
			
			// PRINT PREFERENCES
			/*for(int i=0; i<preferences.size(); i++){
				System.out.println("gen="+this.preferences.get(i).getGeneration() + " last=" + this.preferences.get(i).wasAddedInLastInteraction() 
						+ " conf=" + this.preferences.get(i).getConfidence() + " sca conf=" + this.preferences.get(i).getScaledConfidence());
			}*/
			
			// Scale the confidence of each preference (only for the last set == #solutions shown)
			/*int firstAdded = size-this.nSolutions;
			for(int j=size-1; j>=firstAdded; j--){
				acc += this.preferences.get(j).getConfidence();
			}
			for(int j=size-1; j>=firstAdded; j--){
				this.preferences.get(j).setScaledConfidence(this.preferences.get(j).getConfidence()/acc);
			}*/
						
			// Compute the preference value
			for(int j=0; j<size; j++){

				// compute preference value
				preference = this.preferences.get(j);
				preferenceValue = preference.evaluatePreference(individual);
				weight = preference.getPriority()*preference.getScaledConfidence();
				// update the number of solutions if required
				if(preferenceValue > preference.getThreshold()){
					preference.setNumberOfSolutions(preference.getNumberOfSolutions()+1);
				}

				// accumulate considering the preference weight
				avg += weight*preferenceValue;
				//avg += preferenceValue;
			}
			avg /= size;
		}
		return avg;
	}

	/**
	 * Compute the maximin function. This function is defined in the range [-1,1]
	 * and quantifies the maximum value of the minimum distances between the individual
	 * and the Pareto set.
	 * @param individual The individual
	 * @param nonDominated The set of non dominated individuals to compare with
	 * @return An array containing the maximin value and the index of the metric
	 * that has been used to compute that value.
	 * */
	protected double [] maximinValue(IIndividual individual, List<IIndividual> nonDominated){

		int size = nonDominated.size();
		int nObjs = ((MOFitness)individual.getFitness()).getNumberOfObjectives();
		IIndividual other;
		double min=1.0, max = -1.0;
		double objValueInd, objValueOther;
		double dif;
		double [] res = new double[2];
		int indexMin = -1, indexMax = -1;

		// For each solution, compute the minimum distance for each objective,
		// then, save the maximum of these distances
		try{
			for(int i=0; i<size; i++){

				other = nonDominated.get(i);
				// a different individual
				if(!individual.equals(other)){

					// minimum distance for all the objectives
					min = 1.0;
					for(int j=0; j<nObjs; j++){
						objValueInd = ((MOFitness)individual.getFitness()).getObjectiveDoubleValue(j);
						objValueOther = ((MOFitness)other.getFitness()).getObjectiveDoubleValue(j);

						dif = (objValueInd - objValueOther);
						if(dif < min){
							min = dif;
							indexMin = j;
						}
					}

					// update max distance among all the other individuals
					if(min > max){
						max = min;
						indexMax = indexMin;
					}
				}				
			}
		}
		catch(Exception e){
			max = Double.NaN;
		}

		res[0] = max;
		res[1] = indexMax;
		return res;
	}

	/**
	 * FOR TESTING PURPOSES
	 * */
	protected double maximinValue(double [] individual, double [][] nonDominated){

		int size = nonDominated.length;
		int nObjs = individual.length;
		double [] other;
		double min=1.0, max = -1.0;
		double objValueInd, objValueOther;
		double dif;

		// For each solution, compute the minimum distance for each objective,
		// then, save the maximum of these distances

		for(int i=0; i<size; i++){

			other = nonDominated[i];
			// a different individual
			//if(individual.equals(other)){

			// min distance for all the objectives
			min = 1.0;
			for(int j=0; j<nObjs; j++){
				objValueInd = individual[j];
				objValueOther = other[j];

				dif = (objValueInd - objValueOther);
				if(dif < min){
					min = dif;
				}
			}

			// update max distance among all the other individuals
			if(min > max){
				max = min;
			}
			//}				
		}
		return max;
	}


	/**
	 * Extract the set of non-dominated individuals. It uses the
	 * the individual comparator, so it should provide a kind of dominance
	 * comparison.
	 * @param population The population of individuals
	 * @param archive The archive of solutions
	 * */
	protected List<IIndividual> extractNonDominatedIndividuals(List<IIndividual> population, List<IIndividual> archive){
		List<IIndividual> nonDominated = new ArrayList<IIndividual>();

		// Unite current population and the archive
		List<IIndividual> allInds = new ArrayList<IIndividual>();
		allInds.addAll(population);
		if(archive!=null){
			for(IIndividual ind: archive){
				if(!allInds.contains(ind)){
					allInds.add(ind);
				}
			}
		}

		// Compare the individuals
		int size = allInds.size();
		boolean dominated;
		IIndividual ind;
		for(int i=0; i<size; i++) {
			dominated = false;
			ind = allInds.get(i);
			for(int j=0; !dominated && j<size; j++)	{
				// Check pareto dominance
				switch(getSolutionComparator().compare(ind, allInds.get(j))){
				case 1: // i dominates j
					break;
				case -1: //j dominates i
					dominated = true;
					break;

				case 0: //non dominated solutions
					break;
				}
			}
			if(!dominated){
				nonDominated.add(ind);
			}
		}
		return nonDominated;
	}

	/*private void printObjectivesAndFitness(IMOIndividual ind){
	IMOFitness fitness = (IMOFitness)ind.getFitness();
	int n = fitness.getNumberOfObjectives();
	for(int i=0; i<n; i++){
		try {
			System.out.print(fitness.getObjectiveDoubleValue(i)+" ");
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	System.out.println("\npreferences= " + fitness.getPreferenceValue() 
			+ " maximin=" + fitness.getDominanceValue()
			+ " fitness= " + fitness.getValue()+"\n");
	}*/

	/*private void printPreferences(){
		System.out.println("PREFERENCES generation= " + getContext().getGeneration() + " size="+preferences.size());
		for(int i=0; i<this.preferences.size(); i++){
			preferences.get(i).print();
		}
	}*/
}