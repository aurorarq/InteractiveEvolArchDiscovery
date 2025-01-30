package net.sf.jclec.sbse.discovery.imo.preferences;

import java.util.ArrayList;

import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 *  This preference considers how good is an individual
 *  when comparing its components to the best component
 *  identified by the architect. It uses the Jaccard index
 *  to compute the similarity between two sets of classes.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (February 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * 
 * @version 1.0
 * */
public class SimilarityBestComponent extends ArchitecturalPreference {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** The subtree with the component */
	protected SyntaxTree component;

	/** The set of classes */
	protected ArrayList<String> setOfClasses;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor.
	 * */
	public SimilarityBestComponent(){
		super();
	}
	
	/**
	 * Parameterized constructor.
	 * @param component The component
	 * */
	public SimilarityBestComponent(SyntaxTree component) {
		super();
		setComponentSubTree(component);
	}
	
	/**
	 * Parameterized constructor. For testing purposes.
	 * @param setOfClasses The classes belonging to the component.
	 * */
	public SimilarityBestComponent(ArrayList<String> setOfClasses) {
		super();
		this.setOfClasses = setOfClasses;
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	public double evaluatePreference(InteractiveMOIndividual individual) {

		SyntaxTree genotype = individual.getGenotype();
		int numberOfComponents = individual.getNumberOfComponents();
		ArrayList<String> classes;
		double similarity, maxSimilarity = Double.NEGATIVE_INFINITY;

		//System.out.println("INDIVIDUAL: " + individual.toString());
		
		// Locate each component in the individual
		for(int i=0; i<numberOfComponents; i++){
			// Get the set of classes
			classes = extractClasses(genotype, i);

			// Check the similarity for this component
			similarity = similarity(classes);

			// Update best case
			if(similarity > maxSimilarity){
				maxSimilarity = similarity;
			}
		}
		//System.out.println("Max similarity: " + maxSimilarity);
		return maxSimilarity;
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Set the subtree that encodes the component
	 * @param component The component
	 * */
	public void setComponentSubTree(SyntaxTree component){
		this.component = component.copy();
		this.setOfClasses = extractClasses(component, 0);
	}

	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Private methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Extract the classes from the component subtree
	 * @param tree The syntax tree that encapsulates the architecture
	 * @param index The index of the component
	 * */
	protected ArrayList<String> extractClasses(SyntaxTree tree, int index){
		String symbol;
		int n=-1;
		ArrayList<String> setOfClasses = new ArrayList<String>();
		boolean finish = false, isClass = false;
		
		// Search the n-th component
		for(int i=0; !finish; i++){

			symbol = tree.getNode(i).getSymbol();

			// Non terminal node
			if(tree.getNode(i).arity()!=0){	
				if(symbol.equalsIgnoreCase("classes")){
					n++;
					isClass = true;
				}
				if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass = false;
					if(n==index)// the component was found, force the finish
						finish=true;
				}
				if(symbol.equalsIgnoreCase("connector")){
					finish = true;
				}
			}

			// Terminal node
			else{
				// If the terminal is a class and it is the n-th component, add the class
				if(n==index && isClass){
					setOfClasses.add(symbol);
				}
			}
		}
		return setOfClasses;
	}

	/**
	 * Compute the similarity between the best component
	 * and another component. It uses the Jaccard index.
	 * @param otherSetOfClasses The classes belonging to the other component
	 * @return A double value in [0,1] representing the similarity between
	 * the components in terms of their operations.
	 * */
	public double similarity(ArrayList<String> otherSetOfClasses){

		double similarity = 0.0;

		int inCommon = 0;		// Number of classes in common
		int inBestComponent;	// Number of classes in classes1 that are not in classes2
		int inOtherComponent;	// Number of classes in classes2 that are not in classes1
		int nClassesBest = this.setOfClasses.size();
		int nClassesOther = otherSetOfClasses.size();
		int nTotalDifferentClasses;

		/*System.out.println("BEST COMPONENT: ");
		for(int i=0; i<this.setOfClasses.size(); i++){
			System.out.print(this.setOfClasses.get(i) + " ");
		}
		System.out.println();
		
		System.out.println("CURRENT COMPONENT: ");
		for(int i=0; i<otherSetOfClasses.size(); i++){
			System.out.print(otherSetOfClasses.get(i) + " ");
		}
		System.out.println();*/
		
		// Check classes in common
		for(int i=0; i<nClassesBest; i++){
			if(otherSetOfClasses.contains(this.setOfClasses.get(i))){
				inCommon++;
			}
		}
		
		// Compute the number of classes that are different in each component
		inBestComponent = nClassesBest - inCommon;
		inOtherComponent = nClassesOther - inCommon;
		nTotalDifferentClasses = inCommon + inBestComponent + inOtherComponent;
		
		
		// Compute similarity using the Jaccard index
		similarity = (double)inCommon / (double)nTotalDifferentClasses;
		//System.out.println("InCommon="+inCommon + " inBest="+inBestComponent + " inOther="+inOtherComponent + " similarity=" +similarity+"\n");
		
		return similarity;
	}
	
	public void print(){
		System.out.println("Preference: " + this.getClass().getName());
		System.out.println("List of clases: ");
		for(int i=0; i<setOfClasses.size(); i++){
			System.out.print(setOfClasses.get(i)+" ");
		}
		System.out.println();
	}
}