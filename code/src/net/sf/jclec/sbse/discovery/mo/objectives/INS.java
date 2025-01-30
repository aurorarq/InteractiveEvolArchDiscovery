package net.sf.jclec.sbse.discovery.mo.objectives;

import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Instability Metric. Minimize the average
 * instability of the components in the
 * architecture. Instability is computed
 * as the ratio of afferent and efferent
 * coupling between components.
 * 
 * <p>From: 
 * "On the Modularity of Software Architectures:
 * A Concern-Driven Measurement Framework" (2007)
 * 
 * <p>http://www.ndepend.com/Metrics.aspx#MetricsOnAssemblies
 * 
 * <p>History:
 * <ul>
 * 	<li>2.0: Now extending Metric (June 2014)
 * 	<li>1.0: Creation (December 2013)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class INS extends Metric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////
	
	/** Serial ID */
	private static final long serialVersionUID = -8893162707879855079L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor.
	 * */
	public INS(){
		super();
		setName("ins");
	}

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void prepare(Individual solution) {
		// Get genotype
		SyntaxTree genotype = solution.getGenotype();
		int numOfComponents = solution.getNumberOfComponents();

		int j;
		String symbol;

		// Initialize
		boolean [][] afferentRelations = new boolean[numOfComponents][numOfComponents];
		boolean [][] efferentRelations = new boolean[numOfComponents][numOfComponents];

		for(int i=0; i<numOfComponents; i++){
			for(int k=0; k<numOfComponents; k++){
				afferentRelations[i][k] = false;
				efferentRelations[i][k] = false;
			}
		}
		
		// search the node where the connectors begin
		int i=0;
		while(!genotype.getNode(i).getSymbol().equalsIgnoreCase("connectors")){
			i++;
		}
		
		// For each connector, extract the afferent and efferent relations for all the component
		int provComponent = -1, reqComponent = -1;
		String [] aux;
		while(i<genotype.size()){
			
			symbol = genotype.getNode(i).getSymbol();

			if(symbol.equalsIgnoreCase("provided-interface")){
				// get the index of the component that provides the interface
				aux = genotype.getNode(i+1).getSymbol().split("-");
				provComponent = Integer.parseInt(aux[0].substring(aux[0].lastIndexOf("t")+1,aux[0].length())) -1;
				i+=2;
				
			}
			else if(symbol.equalsIgnoreCase("required-interfaces")){
				
				j=i+1;
				while(j<genotype.size() && !genotype.getNode(j).getSymbol().equalsIgnoreCase("connector")){
					
					// get the index of the component that requires the interface
					aux = genotype.getNode(j).getSymbol().split("-");
					reqComponent = Integer.parseInt(aux[0].substring(aux[0].lastIndexOf("t")+1,aux[0].length())) -1;
					
					// update the matrix
					afferentRelations[provComponent][reqComponent]=true;
					efferentRelations[reqComponent][provComponent]=true;
					j++;
				}
				i=j+1;
			}
			else 
				i++;
		}
		
		// Instability of each component
		double [] ins = new double[numOfComponents];
		int numAfferent, numEfferent;

		for(i=0; i<numOfComponents; i++){
			numAfferent=0;
			numEfferent=0;
			// Get the total number of afferent and efferent relations
			for(j=0; j<numOfComponents; j++){
				if(afferentRelations[i][j])
					numAfferent++;
				if(efferentRelations[i][j])
					numEfferent++;
			}
			if(numAfferent!=0)
				ins[i] = ((double)numEfferent)/((double)(numAfferent+numEfferent));
			else
				ins[i] = 1.0;
		}
		setComponentsMeasure(ins);
		ins = null;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IFitness compute(Individual solution) {
		int numOfComponents = solution.getNumberOfComponents();
		double avg=0.0;
		double [] ins = getComponentsMeasure();

		// Average instability in the architecture
		for(int i=0; i<numOfComponents; i++){
			avg += ins[i];
		}
		avg = avg/(double)numOfComponents;
		return new SimpleValueFitness(avg);
	}

}