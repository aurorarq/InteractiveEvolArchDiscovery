package net.sf.jclec.sbse.discovery.imo.objectives;

import es.uco.kdis.datapro.dataset.column.MultiIntegerColumn;
import es.uco.kdis.datapro.datatypes.MultiIntegerValue;
import es.uco.kdis.datapro.datatypes.InvalidValue;
import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Intra-Modular Coupling Density (ICD) Metric
 * inspired by "Optimization Model of COTS Selection Based 
 * on Cohesion and Coupling for Modular Software Systems 
 * under Multiple Applications Environment" (2012)
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
 * */
public class ICD extends Metric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -609119681606726064L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public ICD(){
		super();
		setName("icd");
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void prepare(InteractiveMOIndividual ind) {

		// Get genotype
		SyntaxTree genotype = ind.getGenotype();
		int j, otherClassIndex, actualCmp=-1;
		boolean isClass = false, isConnector = false;
		MultiIntegerColumn column, otherColumn;
		String symbol;

		// Initialize
		int numberOfComponents = ind.getNumberOfComponents();
		double [] c_in = new double [numberOfComponents];
		double [] c_out = new double [numberOfComponents];

		for(int i=0; i<numberOfComponents; i++){
			c_in[i] = c_out[i] = 0;
		}

		// Compute c_in for each component in the individual
		for(int i=1; !isConnector; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){
				// The symbol classes indicates the beginning of a 
				// group of classes in a component
				if(symbol.equalsIgnoreCase("classes")){
					isClass=true;
					actualCmp++;
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass=false;
				}
				else if(symbol.equalsIgnoreCase("connectors")){
					isConnector=true;
				}
			}

			// Terminal node
			else if(isClass){

				// Get the dataset information about the class
				column = (MultiIntegerColumn) this.dataset.getColumnByName(symbol);

				// Check the relations with the rest of class in the component
				j=i+1;	
				while(genotype.getNode(j).arity()==0){
					otherColumn = (MultiIntegerColumn)this.dataset.getColumnByName(genotype.getNode(j).getSymbol());
					otherClassIndex = this.dataset.getIndexOfColumn(otherColumn);

					Object relations = column.getElement(otherClassIndex);

					// Not an invalid value
					if(!(relations instanceof InvalidValue)){
						c_in[actualCmp] += ((double)((MultiIntegerValue)relations).getSize()/2);
					}
					j++;
				}
			}
		}// end of tree route

		// Compute c_out
		for(int i=0; i<numberOfComponents; i++){
			c_out[i] = ind.getNumberOfProvided(i) + ind.getNumberOfRequired(i);
		}

		// Now, compute icd for each component
		int nClasses = getDataset().getColumns().size();
		double [] icd = new double[numberOfComponents];
		double ratio, classesRatio;
		for(int i=0; i<numberOfComponents; i++){
			if(c_out[i]!=0){
				ratio = c_in[i]/(c_in[i]+c_out[i]);
				classesRatio = ((double)nClasses - (double)ind.getNumberOfClasses(i))/(double)nClasses;
				icd[i] = ratio*classesRatio;
			}
			else{
				icd[i] = 0.0;
			}
		}

		setComponentsMeasure(icd);
		icd = null;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IFitness compute(InteractiveMOIndividual ind) {
		int nComponents = ind.getNumberOfComponents();
		double avg = 0.0;
		double [] icd = getComponentsMeasure();
		// Average ICD
		for(int i=0; i<nComponents; i++){
			avg += icd[i];
		}
		avg /= (double)nComponents;
		avg = 1.0 - avg; // normalize
		return new SimpleValueFitness(avg);
	}

	@Override
	public void computeMaxValue(){
		//do nothing
	}
}
