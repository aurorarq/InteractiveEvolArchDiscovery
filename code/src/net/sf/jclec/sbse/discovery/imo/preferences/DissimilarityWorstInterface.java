package net.sf.jclec.sbse.discovery.imo.preferences;

import java.util.ArrayList;

import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * This preference considers how good is an individual
 *  when comparing its interfaces to the best interface
 *  identified by the architect. It uses the Jaccard index
 *  to compute the similarity between two sets of operations.
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

public class DissimilarityWorstInterface extends SimilarityBestInterface {

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor.
	 * */
	public DissimilarityWorstInterface(){
		super();
	}

	/**
	 * Parameterized constructor.
	 * @param component The component
	 * */
	public DissimilarityWorstInterface(SyntaxTree interfaceSubTree, int interfaceIndex) {
		super(interfaceSubTree, interfaceIndex);
	}

	/**
	 * Parameterized constructor. For testing purposes.
	 * @param setOfOperations The interface operations.
	 * */
	public DissimilarityWorstInterface(ArrayList<String> setOfOperations) {
		super();
		this.setOfOperations = setOfOperations;
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
		int numberOfProvInterfaces;
		int n=0;
		ArrayList<String> operations;
		double dissimilarity, maxDissimilarity = Double.POSITIVE_INFINITY;

		// Locate each provided interface in the individual
		for(int i=0; i<numberOfComponents; i++){

			numberOfProvInterfaces = individual.getNumberOfProvided(n);

			if(numberOfProvInterfaces>0){
				for(int j=0; j<numberOfProvInterfaces; j++){

					// Get the set of operations
					operations = extractOperations(genotype, i, j, false);

					// Check the similarity for this interface
					dissimilarity = 1 - similarity(operations);

					// Update worst case
					if(dissimilarity < maxDissimilarity){
						maxDissimilarity = dissimilarity;
					}
				}
			}
			else{
				maxDissimilarity = 0.0;
			}
		}
		return maxDissimilarity;
	}
}
