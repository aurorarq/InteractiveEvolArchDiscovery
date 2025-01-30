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
public class SimilarityBestInterface extends ArchitecturalPreference {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** The subtree with the component */
	protected SyntaxTree interfaceSubTree;

	/** The set of operations */
	protected ArrayList<String> setOfOperations;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public SimilarityBestInterface(){
		super();
	}

	/**
	 * Parameterized constructor.
	 * @param component The component
	 * */
	public SimilarityBestInterface(SyntaxTree interfaceSubTree, int interfaceIndex) {
		super();
		setInterfaceSubTree(interfaceSubTree, interfaceIndex);
	}

	/**
	 * Parameterized constructor. For testing purposes.
	 * @param setOfOperations The interface operations.
	 * */
	public SimilarityBestInterface(ArrayList<String> setOfOperations) {
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
		double similarity, maxSimilarity = Double.NEGATIVE_INFINITY;

		// Locate each provided interface in the individual
		for(int i=0; i<numberOfComponents; i++){

			numberOfProvInterfaces = individual.getNumberOfProvided(n);

			if(numberOfProvInterfaces > 0){
				for(int j=0; j<numberOfProvInterfaces; j++){

					// Get the set of operations
					operations = extractOperations(genotype, i, j, false);

					// Check the similarity for this interface
					similarity = similarity(operations);

					// Update most similar component
					if(similarity > maxSimilarity){
						maxSimilarity = similarity;
					}
				}
			}
			else{
				maxSimilarity = 0.0;
			}
		}

		return maxSimilarity;
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Set the subtree that encodes the interface
	 * @param interfaceSubTree The subtree
	 * @param interfaceIndex The index position of the interface
	 * */
	public void setInterfaceSubTree(SyntaxTree interfaceSubTree, int interfaceIndex){
		this.interfaceSubTree = interfaceSubTree.copy();
		this.setOfOperations = extractOperations(interfaceSubTree, 0, interfaceIndex, true);
		/*System.out.println("Operations:");
		for(int i=0; i<setOfOperations.size(); i++){
			System.out.println("\t"+setOfOperations.get(i));
		}*/
	}

	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Private methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Extract the operations from the interface subtree
	 * @param tree The syntax tree that encapsulates the architecture
	 * @param index The index of the component
	 * */
	protected ArrayList<String> extractOperations(SyntaxTree tree, int componentIndex, int interfaceIndex, boolean verbose){
		String symbol;
		int nC=-1, nI=-1;
		ArrayList<String> setOfOperations = new ArrayList<String>();
		boolean finish = false;
		boolean isInterface = true;
		int size = tree.size();

		//System.out.println("cIndex="+componentIndex + " iIndex="+interfaceIndex);

		// Search the component at "componentIndex" position
		for(int i=0; !finish && nC<=componentIndex && i<size; i++){

			symbol = tree.getNode(i).getSymbol();

			// Non terminal node
			if(tree.getNode(i).arity()!=0){
				if(symbol.equalsIgnoreCase("provided-interfaces")){
					nC++;
					nI=-1;
				}
				else if(symbol.equalsIgnoreCase("interface")){
					nI++;
					isInterface = true;
				}
				else if(symbol.equalsIgnoreCase("connector")){
					finish = true;
					isInterface = false;
				}
				else if(symbol.equalsIgnoreCase("component")){
					nI=-1;
					isInterface = false;
				}
			}

			// Terminal node
			else{
				// If the terminal is an interface and it is the n-th component and 
				// the n-th provided interface add the operation
				if(isInterface && nC==componentIndex && nI==interfaceIndex){
					setOfOperations.add(symbol);
				}
			}
			/*if(verbose)
				System.out.println("symbol: " + symbol + " nC="+ nC + " nI="+ nI + " isInterface="+isInterface);
			 */
		}
		return setOfOperations;
	}

	/**
	 * Compute the similarity between the best interface
	 * and another interface. It uses the Jaccard index.
	 * @param otherSetOfOperations The classes belonging to the other component
	 * @return A double value in [0,1] representing the similarity between
	 * the interfaces in terms of their operations.
	 * */
	public double similarity(ArrayList<String> otherSetOfOperations){

		double similarity = 0.0;

		int inCommon = 0;		// Number of classes in common
		int inBestComponent;	// Number of classes in classes1 that are not in classes2
		int inOtherComponent;	// Number of classes in classes2 that are not in classes1
		int nOperationsBest = this.setOfOperations.size();
		int nOperationsOther = otherSetOfOperations.size();
		int nTotalDifferentClasses;

		// Check classes in common
		for(int i=0; i<nOperationsBest; i++){
			if(otherSetOfOperations.contains(this.setOfOperations.get(i))){
				inCommon++;
			}
		}

		// Compute the number of classes that are different in each component
		inBestComponent = nOperationsBest - inCommon;
		inOtherComponent = nOperationsOther - inCommon;
		nTotalDifferentClasses = inCommon + inBestComponent + inOtherComponent;

		// Compute similarity using the Jaccard index
		similarity = (double)inCommon / (double)nTotalDifferentClasses;
		return similarity;
	}

	public void print(){
		System.out.println("Preference: " + this.getClass().getName());
		System.out.println("List of clases: ");
		for(int i=0; i<setOfOperations.size(); i++){
			System.out.print(setOfOperations.get(i)+" ");
		}
		System.out.println();
	}
}
