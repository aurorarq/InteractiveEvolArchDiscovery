package net.sf.jclec.sbse.discovery.imo.objectives;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Encapsulation Metric. Maximize the average
 * encapsulation of the components in the
 * architecture. Encapsulation is measured
 * as the ratio of internal classes and
 * total number of classes inside a component.
 * 
 * Inspired by Data Access Metric defined in: 
 * "A hierarchical model for object-oriented 
 * design quality assessment" (2002)
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (February 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * @see Metric
 * */

public class ENC extends Metric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -5537465199904996515L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public ENC(){
		super();
		setName("enc");
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void prepare(InteractiveMOIndividual ind) {
		// Get genotype
		SyntaxTree genotype;
		int numOfComponents = ind.getNumberOfComponents();

		int k;
		String symbol, otherSymbol;
		boolean isInternal = true, isReqInterface = false;
		String [] aux;
		double [] internalClasses = new double[numOfComponents];

		//System.out.println(ind.getGenotype());

		for(int i=0; i<numOfComponents; i++){
			internalClasses[i] = 0; // initialize

			// get the subtree for this component
			genotype = ind.getComponentTree(i);

			// count the number of internal classes in this component
			// slip nodes 0 (component) and 1 (classes)
			boolean lastClass = false;
			for(int j=2; !lastClass; j++){
				symbol = genotype.getNode(j).getSymbol();

				if(symbol.equalsIgnoreCase("required-interfaces"))
					lastClass=true;
				if(!lastClass){
					
					k = 2+ind.getNumberOfClasses(i); // slip the set of classes
					isInternal = true;
					isReqInterface = false;
					
					while(isInternal && k<genotype.size()){
						otherSymbol = genotype.getNode(k).getSymbol();

						if(genotype.getNode(k).arity()!=0){
							if(otherSymbol.equalsIgnoreCase("required-interfaces"))
								isReqInterface = true;
							else if(otherSymbol.equalsIgnoreCase("provided-interfaces")){
								isReqInterface = false;
							}
						}

						else{
							// required operation
							if(isReqInterface){
								
								aux = otherSymbol.split(" "); // separate the classes that required this operation
								// elements 1,2 and length-1 are omitted: (provClass:operation) ([) (])
								for(int l=2; isInternal && l<aux.length-1; l++){ 
									if(symbol.equalsIgnoreCase(aux[l]))
										isInternal = false;
								}
							}

							// provided operation
							else{
								aux = otherSymbol.split(":");
								// the class specifies that operation
								if(symbol.equalsIgnoreCase(aux[0]))
									isInternal = false;
							}
						}
						k++;
					}

					if(isInternal)
						internalClasses[i]++;
				}
			}
		}

		// Now, obtain the encapsulation for each component
		double [] enc = new double[numOfComponents];
		for(int i=0; i<numOfComponents; i++){
			enc[i] = ((double)internalClasses[i])/((double)ind.getNumberOfClasses(i));
		}
		setComponentsMeasure(enc);
		enc=null;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IFitness compute(InteractiveMOIndividual ind) {
		int numOfComponents = ind.getNumberOfComponents();
		double [] enc = getComponentsMeasure();
		double avg = 0.0;
		for(int i=0; i<numOfComponents; i++){
			avg += enc[i];
		}
		avg /= (double)numOfComponents;
		// invert the value for minimization problem
		avg = 1.0 - avg;
		return new SimpleValueFitness(avg);
	}

	@Override
	public void computeMaxValue(){
		//do nothing
	}
}