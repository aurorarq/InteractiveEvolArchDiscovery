package net.sf.jclec.sbse.discovery.imo.rec;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.sbse.discovery.imo.InteractiveMORecombinator;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOSpecies;
import net.sf.jclec.syntaxtree.SyntaxTree;


/**
 * Crossover for the discovery problem. It swaps
 * one component of each parent.
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

public class OneComponentCrossover extends InteractiveMORecombinator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 777970860649416796L;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor.
	 * */
	public OneComponentCrossover() {
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

		// Generate the index of the component to maintain to interchange
		int nCompParent1 = ((InteractiveMOIndividual)parentsBuffer.get(parentsCounter)).getNumberOfComponents();
		int nCompParent2 = ((InteractiveMOIndividual)parentsBuffer.get(parentsCounter+1)).getNumberOfComponents();
		int indexCompParent1 = randgen.choose(0, nCompParent1);
		int indexCompParent2 = randgen.choose(0, nCompParent2);

		/*System.out.println("\n\nPARENT 1");
		for(int i=0; i<nClasses; i++){
			System.out.print(distParent1[i] + " ");
		}
		System.out.println("\nPARENT 2");
		for(int i=0; i<nClasses; i++){
			System.out.print(distParent2[i] + " ");
		}
		
		System.out.println("\nPUNTO DE CORTE: i=" + indexCompParent1 + " j=" + indexCompParent2);*/
		
		// Create son 1
		// Assign the new distribution of classes
		distSon = new int[nClasses];

		for(int i=0; i<nClasses; i++){
			component1 = distParent1[i];
			component2 = distParent2[i];
			if(component1==indexCompParent1){
				distSon[i] = distParent2[i];
			}
			else{
				distSon[i] = distParent1[i];
			}
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
				
		/*System.out.println("\nFIX SON 1");
		for(int i=0; i<nClasses; i++){
			System.out.print(distSon[i] + " ");
		}*/
		
		// If the number of components is valid, add the son
		//System.out.println("Son 1: #comp=" + nComponents);
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
		for(int i=0; i<nClasses; i++){
			component1 = distParent1[i];
			component2 = distParent2[i];
			if(component2==indexCompParent2){
				distSon[i] = distParent1[i];
			}
			else{
				distSon[i] = distParent2[i];
			}
		}
		
		/*System.out.println("\nSON 2");
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
			
		/*System.out.println("\nFIX SON 2");
		for(int i=0; i<nClasses; i++){
			System.out.print(distSon[i] + " ");
		}*/

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
	}
}
