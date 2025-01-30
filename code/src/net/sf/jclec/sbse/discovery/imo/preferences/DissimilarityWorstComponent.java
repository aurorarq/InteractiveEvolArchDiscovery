package net.sf.jclec.sbse.discovery.imo.preferences;

import java.util.ArrayList;

import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 *  This preference considers how good is an individual
 *  when comparing its interfaces to the worst component
 *  identified by the architect. It uses the Jaccard index
 *  to compute the inverted similarity between two sets of classes.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (May 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class DissimilarityWorstComponent extends SimilarityBestComponent {

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor.
	 * */
	public DissimilarityWorstComponent(){
		super();
	}

	/**
	 * Parameterized constructor.
	 * @param component The component
	 * */
	public DissimilarityWorstComponent(SyntaxTree component) {
		super(component);
	}

	/**
	 * Parameterized constructor. For testing purposes.
	 * @param setOfClasses The classes belonging to the component.
	 * */
	public DissimilarityWorstComponent(ArrayList<String> setOfClasses) {
		super(setOfClasses);
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * */
	@Override
	public double evaluatePreference(InteractiveMOIndividual individual) {

		SyntaxTree genotype = individual.getGenotype();
		int numberOfComponents = individual.getNumberOfComponents();
		ArrayList<String> classes;
		double dissimilarity, maxDissimilarity = Double.POSITIVE_INFINITY;

		// Locate each component in the individual
		for(int i=0; i<numberOfComponents; i++){
			// Get the set of classes
			classes = extractClasses(genotype, i);

			// Check the dissimilarity for this component
			dissimilarity = 1.0 - similarity(classes);

			// Update the worst case
			if(dissimilarity < maxDissimilarity){
				maxDissimilarity = dissimilarity;
			}
		}
		return maxDissimilarity;
	}
}
