package net.sf.jclec.sbse.discovery.imo.rec;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMORecombinator;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOSpecies;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Crossover for the discovery problem. It interchanges
 * the distribution of the classes into components given a
 * pair of individuals.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (September 2015)</li>
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class OnePointCrossover extends InteractiveMORecombinator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -7329281254637724043L;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public OnePointCrossover() {
		super();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	protected void recombineNext() {

		SyntaxTree tree;
		IIndividual son;
		int [] distSon;
		int component1, component2;
		int nComponents = -1;
		
		// Get the class distribution of each parent
		int [] distParent1 = ((InteractiveMOIndividual)parentsBuffer.get(parentsCounter)).getClassesDistribution();
		int [] distParent2 = ((InteractiveMOIndividual)parentsBuffer.get(parentsCounter+1)).getClassesDistribution();
		int nClasses = distParent1.length;

		// Get the frozen components in each parent
		/*boolean [] frozenParent1 = ((IMOIndividual)parentsBuffer.get(parentsCounter)).getFrozenComponents();
		boolean [] frozenParent2 = ((IMOIndividual)parentsBuffer.get(parentsCounter+1)).getFrozenComponents();

		System.out.println("\nPARENT 1");
		for(int i=0; i<nClasses; i++){
			System.out.print(distParent1[i] + "("+frozenParent1[distParent1[i]]+") ");
		}
		System.out.println("\nPARENT 2");
		for(int i=0; i<nClasses; i++){
			System.out.print(distParent2[i] + "("+frozenParent2[distParent2[i]]+") ");
		}*/

		// Generate the cut point
		int nCompParent1 = ((InteractiveMOIndividual)parentsBuffer.get(parentsCounter)).getNumberOfComponents();
		int nCompParent2 = ((InteractiveMOIndividual)parentsBuffer.get(parentsCounter+1)).getNumberOfComponents();
		int limit;
		if(nCompParent1 > nCompParent2){
			limit = nCompParent2;
		}
		else{
			limit = nCompParent1;
		}
		int cutPoint = randgen.choose(0, limit-1); // at least one component will change
		//System.out.println("\nPUNTO DE CORTE: " + cutPoint);

		// Create son 1
		// Assign the new distribution of classes
		distSon = new int[nClasses];

		for(int i=0; i<nClasses; i++){
			component1 = distParent1[i];
			component2 = distParent2[i];
			if(component1 <= cutPoint)
				distSon[i] = component1;
			else
				distSon[i] = component2;
		}
		/*System.out.println("SON 1");
		for(int i=0; i<nClasses; i++){
			System.out.print(distSon[i] + " ");
		}*/

		// Update the component indexes
		distSon = fixComponentIndexes(distSon);
		for(int i=0; i<distSon.length; i++){
			if(distSon[i]>nComponents)
				nComponents=distSon[i];
		}
		nComponents++; // increment the number of the upper index
		
		/*System.out.println("\nFINAL SON 1");
		for(int i=0; i<nClasses; i++){
			System.out.print(distSon[i] + " ");
		}

		System.out.println("Son 1: #comp=" + nComponents);*/
		// If the number of components is valid, add the son
		if(nComponents >= this.schema.getMinNumOfComp() && nComponents <= this.schema.getMaxNumOfComp()){
			// Create the individual
			tree = this.schema.createSyntaxTree(nComponents, distSon);
			son = ((InteractiveMOSpecies)species).createIndividual(tree);
			//System.out.println("Add Son 1: #comp="+ ((IMOIndividual)son).getNumberOfComponents());
			sonsBuffer.add(son);
			//System.out.println(((IMOIndividual)sonsBuffer.get(sonsBuffer.size()-1)));
		}
		// TODO add the parent to maintain the number of descendants
		else{
			sonsBuffer.add((InteractiveMOIndividual)parentsBuffer.get(parentsCounter));
		}

		// Create son 2
		// Assign the new distribution of classes
		for(int i=0; i<nClasses; i++){
			component1 = distParent1[i];
			component2 = distParent2[i];
			if(component2 <= cutPoint){
				distSon[i] = component2;
			}
			else{
				distSon[i] = component1;
			}
		}
		/*System.out.println("\nSON 2");
		for(int i=0; i<nClasses; i++){
			System.out.print(distSon[i] + " ");
		}*/

		// Obtain the resulting number of components

		/*System.out.println("\nFINAL SON 2");
		for(int i=0; i<nClasses; i++){
			System.out.print(distSon[i] + " ");
		}*/

		// Update the component indexes
		distSon = fixComponentIndexes(distSon);
		nComponents = -1;
		for(int i=0; i<distSon.length; i++){
			if(distSon[i]>nComponents)
				nComponents=distSon[i];
		}
		nComponents++; // increment the number of the upper index
		
		// If the number of components is valid, add the son
		//System.out.println("Son 2: #comp=" + nComponents);
		if(nComponents >= this.schema.getMinNumOfComp() && nComponents <= this.schema.getMaxNumOfComp()){
			// Create the individual
			tree = this.schema.createSyntaxTree(nComponents, distSon);
			son = ((InteractiveMOSpecies)species).createIndividual(tree);
			//System.out.println("Add Son 2: #comp="+ ((IMOIndividual)son).getNumberOfComponents());
			sonsBuffer.add(son);
			//System.out.println(((IMOIndividual)sonsBuffer.get(sonsBuffer.size()-1)));
		}
		// TODO add the parent to maintain the number of descendants
		else{
			sonsBuffer.add((InteractiveMOIndividual)parentsBuffer.get(parentsCounter+1));
		}
		//System.out.println("Gen: " + this.context.getGeneration() + " New sons: " + addedSons);
	}
}
