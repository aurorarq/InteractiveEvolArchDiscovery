package net.sf.jclec.sbse.discovery.imo.listeners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.jclec.mo.IMOAlgorithm;
import net.sf.jclec.mo.listener.MOReporter;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOEPAlgorithm;
//import net.sf.jclec.sbse.discovery.imo.InteractiveMOGAlgorithm;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOStrategy;
import net.sf.jclec.sbse.discovery.imo.preferences.ArchitecturalPreference;

/**
 * Interaction reporter
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
public class InteractionReporter extends MOReporter{

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 1L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	public InteractionReporter(){
		super();
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	protected void doReport(IMOAlgorithm algorithm, int generation, boolean finalReport) {

		InteractiveMOEPAlgorithm alg = (InteractiveMOEPAlgorithm)algorithm;
		//InteractiveMOGAlgorithm alg = (InteractiveMOGAlgorithm)algorithm;
		
		List<ArchitecturalPreference> preferences 
				= ((InteractiveMOStrategy)alg.getStrategy()).getPreferences();

		// Architectural preferences
		StringBuffer sb = new StringBuffer("");
		sb.append("Architectural preferences:\n");
		for(ArchitecturalPreference pref: preferences){
			sb.append("\t"+pref.getClass().getSimpleName() + " -> generation=" 
					+ pref.getGeneration() + " priority=" + pref.getPriority() 
					+ " solutions (> threshold)=" + pref.getNumberOfSolutions() + "\n");
		}
		
		// Fitness progress
		double meanFitness = alg.getFitnessProgress().get(alg.getFitnessProgress().size()-2);
		double stdFitness = alg.getFitnessProgress().get(alg.getFitnessProgress().size()-1);
		sb.append("\nFitness: mean="+meanFitness + " stdv=" + stdFitness);
		
		double meanPrefs = alg.getPreferenceFitnessProgress().get(alg.getPreferenceFitnessProgress().size()-2);
		double stdPrefs = alg.getPreferenceFitnessProgress().get(alg.getPreferenceFitnessProgress().size()-1);
		sb.append("\nPreference fitness component: mean="+meanPrefs + " stdv=" + stdPrefs);
		
		double meanDominance = alg.getDominanceFitnessProgress().get(alg.getDominanceFitnessProgress().size()-2);
		double stdDominance = alg.getDominanceFitnessProgress().get(alg.getDominanceFitnessProgress().size()-1);
		sb.append("\nDominance fitness component: mean="+meanDominance + " stdv=" + stdDominance);
		
		// Distribution of solutions and invalids
		double [] distribution = alg.getDistribution();
		double invalids = alg.getPercentageInvalids();
		int min = alg.getMinNumberOfComponents();
		int max = alg.getMaxNumberOfComponents();
		sb.append("\nPercentage infeasible solutions: " + invalids);
		sb.append("\nArchive size: " + alg.getArchive().size());
		sb.append("\nDistribution of solutions per number of components:\n");
		for(int i=min; i<=max; i++){
			sb.append("\t" + i+" comp=" + (int)distribution[i-min] + "\n");
		}
		sb.append("\nInteractions:\n");
		ArrayList<Integer> interactiveGenerations = alg.getInterativeGenerations();
		for(int i=0; i<interactiveGenerations.size(); i+=2){
			sb.append("\tgeneration="+interactiveGenerations.get(i) + " cause="+interactiveGenerations.get(i+1)+"\n");
		}
				
		if(isReportOnConsole()){
			System.out.println(sb.toString());
		}
		if(isReportOnFile()){
			try {
				getReportFileWriter().append(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String getName() {
		return "-Interaction";
	}
}
