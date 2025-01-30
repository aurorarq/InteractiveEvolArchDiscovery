package net.sf.jclec.sbse.discovery.mo.objectives;

import java.util.List;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.column.ColumnAbstraction;
import es.uco.kdis.datapro.dataset.column.MultiIntegerColumn;
import es.uco.kdis.datapro.datatypes.MultiIntegerValue;
import es.uco.kdis.datapro.datatypes.InvalidValue;
import net.sf.jclec.IFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * External Relations Penalty (ERP) Metric
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
public class ERP extends Metric {
	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -5817726652462459403L;

	/** Total relations weights between pairs of components */
	private double componentSumWeightedExternalConnections [][];

	/** UML relation weights */
	protected double umlWeights [];
	
	/** Max value for the current dataset */
	protected double maxValue =-1;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public ERP(){
		super();
		setName("erp");
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 * <p>Parameters for ERP are:
	 * <ul>
	 * 	<li>uml-relation-weights (Complex): 
	 * 	Associated weight to each UML relationship:
	 * 		<ul>
	 * 			<li>assoc-weight (<code>double</code>): 
	 * 			Weight for associations. Default value is 1.0.
	 * 			<li>aggreg-weight (<code>double</code>): 
	 * 			Weight for aggregations. Default value is 1.0.
	 * 			<li>compos-weight (<code>double</code>): 
	 * 			Weight for compositions. Default value is 1.0.
	 * 			<li>gener-weight (<code>double</code>): 
	 * 			Weight for generalizations. Default value is 1.0.
	 * 		</ul>
	 * </ul>
	 * */
	@Override
	public void configure(Configuration settings) {
		
		super.configure(settings);
		
		// Configure weights for UML relations
		this.umlWeights = new double[4];
		String names [] = new String[]{"assoc", "aggreg", "compos", "gener"};
		Object property;
		for(int i=0; i<4; i++){
			property = settings.getProperty("objective("+getIndex()+")."+names[i]+"-weight");
			if(property == null){
				this.umlWeights[i] = 1.0;
			}
			else{
				try{
					this.umlWeights[i] = Double.parseDouble(property.toString());
					if(this.umlWeights[i] < 0.0){
						throw new IllegalArgumentException("The " + names[i] + " weight must be greater than 0");
					}
				}catch(NumberFormatException e){
					throw new IllegalArgumentException("The " + names[i] + " weight must be a number");
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected void prepare(Individual solution) {
		// Get genotype
		SyntaxTree genotype = solution.getGenotype();
		int numberOfComponents = solution.getNumberOfComponents();

		int j, actualIndex, classIndex, actualCmp=-1, otherCmp=-1;
		boolean isClass = false, isOtherClass = false, isConnector = false;
		MultiIntegerColumn column;
		int nav_ij, nav_ji, relationType;
		String symbol;
		MultiIntegerValue relations_ij, relations_ji;
		Object oValue;
		
		// Initialize
		this.componentSumWeightedExternalConnections = new double [numberOfComponents][numberOfComponents];

		for(int i=0; i<numberOfComponents; i++){
			for(j=0; j<numberOfComponents; j++){
				this.componentSumWeightedExternalConnections[i][j] = 0.0;
			}
		}

		// Compute needed metrics for each component
		for(int i=1; !isConnector; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){
				// The symbol classes indicates the beginning of a new component
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
			else{
				// If the terminal is a class
				if(isClass){

					// Get the dataset information about the class
					column = (MultiIntegerColumn) this.dataset.getColumnByName(symbol);
					actualIndex = this.dataset.getIndexOfColumn(column);

					// Check the relations with classes belonging to other components
					otherCmp=actualCmp;			// Start in the actual component
					j=i+1;

					while(!(genotype.getNode(j).getSymbol().equalsIgnoreCase("connectors"))){

						// Search a class in the other component
						if((genotype.getNode(j-1).getSymbol().equalsIgnoreCase("classes"))){
							isOtherClass=true;
							otherCmp++;
						}

						if(isOtherClass){
							// Get relations between classes
							classIndex = this.dataset.getIndexOfColumn(this.dataset.getColumnByName(genotype.getNode(j).getSymbol()));
							
							oValue = ((MultiIntegerColumn)column).getElement(classIndex);

							// Not an invalid value
							if(!(oValue instanceof InvalidValue)){
								relations_ij = (MultiIntegerValue)oValue;
								relations_ji = (MultiIntegerValue)((MultiIntegerColumn)this.dataset.getColumn(classIndex)).getElement(actualIndex);

								// Check type and navigation of each relation
								for(int k=1; k<relations_ij.getSize(); k+=2){

									relationType = relations_ij.getValue(k-1);
									nav_ij = relations_ij.getValue(k);
									nav_ji = relations_ji.getValue(k);

									// Not a candidate interface, because its a bidirectional relation
									if(nav_ij==nav_ji){
										
										// Check the type of relation (dependences are not possible)
										double sumTerm = 0.0;
										switch(relationType){
										case 1: sumTerm = this.umlWeights[0]; break; // Association
										case 3: sumTerm = this.umlWeights[1]; break; // Aggregation
										case 4: sumTerm = this.umlWeights[2]; break; // Composition	
										case 5: sumTerm = this.umlWeights[3]; break; // Generalization
										}

										// Update total sum
										this.componentSumWeightedExternalConnections[actualCmp][otherCmp] += sumTerm;
										this.componentSumWeightedExternalConnections[otherCmp][actualCmp] += sumTerm;
									}
								}
							}
							// End of classes in the other component
							if(genotype.getNode(j+1).arity()!=0){
								isOtherClass=false;
							}
						}
						j++;
					}
				}
			}
		}// end of tree route
		
		// ERP for each component
		double [] erp = new double[numberOfComponents];
		for(int i=0; i<numberOfComponents; i++){
			erp[i] = 0.0;
			for(j=0; j<numberOfComponents; j++){
				erp[i] += this.componentSumWeightedExternalConnections[i][j];
			}
		}
		setComponentsMeasure(erp);
		erp = null;
	}

	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected IFitness compute(Individual solution) {
		int numberOfComponents = solution.getNumberOfComponents();
		double result = 0.0;
		// Total weighted external relations
		for(int i=0; i<numberOfComponents; i++)
			for(int j=i; j<numberOfComponents; j++)
				result += this.componentSumWeightedExternalConnections[i][j];
		if(maxValue==-1)
			computeMaxValue();
		result = result/maxValue; // normalize
		return new SimpleValueFitness(result);
	}
	
	/**
	 * Calculate the minimum value using
	 * the information in the dataset. It
	 * sums the weight of each possible external
	 * relation (those relationships which does
	 * not explicitly specifies its navigability)
	 * */
	public void computeMaxValue(){
		Object values, values2;
		List<ColumnAbstraction> cols = dataset.getColumns();
		int totalRel = 0;
		int size = cols.size(), size2;
		MultiIntegerColumn col;
		int type, nav_ij, nav_ji;
		int nRelationships;
		
		for(int i=0; i<size; i++){
			col = (MultiIntegerColumn) cols.get(i);
			size2 = col.getSize();
			for(int j=i; j<size2; j++){
				values = col.getElement(j);
				values2 = ((MultiIntegerColumn)this.dataset.getColumn(j)).getElement(i);
				
				if(!(values instanceof InvalidValue)){//Not an invalid value
					nRelationships = ((MultiIntegerValue)values).getSize();
					for(int k=0; k<nRelationships; k+=2){
						// Get the type of relationship
						type = ((MultiIntegerValue)values).getValue(k);
						nav_ij = ((MultiIntegerValue)values).getValue(k+1);
						nav_ji = ((MultiIntegerValue)values2).getValue(k+1);

						// Not a candidate interface, because its a bidirectional relation
						if(nav_ij==nav_ji){
							// Add the correspondent weight to the total sum
							switch(type){
							case 1: totalRel += this.umlWeights[0]; break; // Association
							case 3: totalRel += this.umlWeights[1]; break; // Aggregation
							case 4: totalRel += this.umlWeights[2]; break; // Composition	
							case 5: totalRel += this.umlWeights[3]; break; // Generalization
							}
						}
					}
				}
			}
		}
		this.maxValue = totalRel;
	}
}