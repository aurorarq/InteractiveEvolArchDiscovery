package net.sf.jclec.sbse.discovery.mo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.InstanceIterator;
import es.uco.kdis.datapro.dataset.column.MultiIntegerColumn;
import es.uco.kdis.datapro.datatypes.MultiIntegerValue;
import es.uco.kdis.datapro.datatypes.InvalidValue;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.evaluation.MOEvaluator;
import net.sf.jclec.mo.evaluation.Objective;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.mo.objectives.Metric;

/**
 * Evaluator for multi/many-objective algorithms
 * in the discovery problem.
 * 
 * <p>History:
 * <ul>
 *  <li>2.1: New dataset with abstract classes and new method (setAbstractClasses) (June 2014)
 * 	<li>2.0: Now extending from MOEvaluator (May 2014)
 * 	<li>1.0: Creation (December 2013)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 2.0
 * @see MOEvaluator
 * */
public class MoEvaluator extends MOEvaluator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -4279911861830083711L;

	/** Dataset that stores the information about relations */
	private Dataset relationshipsDataset;

	/** Dataset that stores the information about classes */
	private Dataset classesDataset;

	/** Number of classes */
	private int numberOfClasses;

	/** Array with the indexes of classes in a component  */
	private transient ArrayList<Integer> indexes = new ArrayList<Integer>();

	/** Array of nodes visited used the graph depth function  */
	private transient boolean visited [];

	/** Classes and its correspondent group inside the component */
	private transient int classGroup [];

	/** Maximum number of components */
	private int maxComponents;

	/** Minimum number of components */
	private int minComponents;

	/** Maximum number of interfaces */
	private int maxInterfaces;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public MoEvaluator(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Get/Set methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get the maximum number
	 * of components
	 * @return maximum number
	 * of components configured
	 * */
	public int getMaxComponents(){
		return this.maxComponents;
	}

	/**
	 * Set the minimum number
	 * of components
	 * @param minComponents The
	 * maximum number of components
	 * */
	public void setMaxComponents(int minComponents){
		this.minComponents = minComponents;
	}

	/**
	 * Get the minimum number
	 * of components
	 * @return minimum number
	 * of components configured
	 * */
	public int getMinComponents(){
		return this.minComponents;
	}

	/**
	 * Set the minimum number
	 * of components
	 * @param minComponents The
	 * minimum number of components
	 * */
	public void setMinComponents(int minComponents){
		this.minComponents = minComponents;
	}

	/**
	 * Get the maximum number
	 * of candidate interfaces
	 * @return maximum number
	 * of interfaces extracted
	 * from the model
	 * */
	public int getMaxInterfaces(){
		return this.maxInterfaces;
	}

	/**
	 * Set the maximum number
	 * of candidate interfaces
	 * @param maxInterfaces The
	 * maximum number of components
	 * */
	public void setMaxInterfaces(int maxInterfaces){
		this.maxInterfaces = maxInterfaces;
	}

	/**
	 * Set the problem parameters
	 * @param minComponents Minimum number of components
	 * @param maxComponents Maximum number of components
	 * @param maxInterfaces Maximum number of interfaces
	 * */
	public void setProblemCharacteristics(int minComponents, int maxComponents, int maxInterfaces){
		setMinComponents(minComponents);
		setMaxComponents(maxComponents);
		setMaxInterfaces(maxInterfaces);

		// Configure in the objectives
		for(Objective obj: this.objectives){
			((Metric)obj).setDataset(relationshipsDataset);
			((Metric)obj).setMaxComponents(maxComponents);
			((Metric)obj).setMinComponents(minComponents);
			((Metric)obj).setMaxInterfaces(maxInterfaces);
			//((Metric)obj).computeBounds();
		}
	}
	
	/**
	 * Set the relationships dataset
	 * */
	public void setRelationshipsDataset(Dataset dataset){ 
		this.relationshipsDataset = dataset;
		this.numberOfClasses = this.relationshipsDataset.getColumns().size();
		
		// Set dataset in the metrics
		for(Objective obj: this.objectives){
			((Metric)obj).setDataset(this.relationshipsDataset);
		}
	}
	
	/**
	 * Set the relationships dataset
	 * */
	public void setClassesDataset(Dataset dataset){ 
		this.classesDataset = dataset;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public void configure(Configuration settings) {
		// Call super class configuration method
		super.configure(settings);
	}

	@Override
	protected void evaluate(IIndividual ind) {
		Individual myInd = (Individual)ind;

		// The individual has not been evaluated yet
		if (myInd.getFitness() == null){
			computeMeasures(myInd);	// Measures needed for objectives evaluation
			super.evaluate(myInd);		// Evaluate the individual
		}
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	// --------------------------- Private evaluation methods

	/**
	 * Compute general measures over an individual
	 * @param ind: The individual
	 * */
	private void computeMeasures(Individual ind){
		// Set required information for evaluation objectives
		this.setClassesDistribution(ind);
		this.setClassesInGroupsDistribution(ind);
		this.setNumberExternalRelations(ind);
		//this.setNumberOfDataTypes(ind);
		this.setAbstractClasses(ind);
	}

	/** 
	 * Set the correspondence between each class and its component 
	 * @param ind: The individual
	 * */
	private void setClassesDistribution(Individual ind){

		int actualCmp=-1, index;
		boolean isClass = false, isConnector = false;
		String symbol;
		// Number of classes in each component
		int componentNumClasses [] = new int[ind.getNumberOfComponents()];

		// Component at each class belong
		int distribution [] = new int[this.numberOfClasses];
		SyntaxTree genotype = ind.getGenotype();

		for(int i=1; !isConnector; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){
				// New set of classes
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

				// Increment the component number of classes
				componentNumClasses[actualCmp]++;

				// Set the component at which the class belongs
				index = this.relationshipsDataset.getIndexOfColumn(this.relationshipsDataset.getColumnByName(symbol));
				distribution[index] = actualCmp;
			}
		}// end of tree route

		// Set distribution in the individual
		ind.setClassesDistribution(distribution);
		ind.setNumberOfClasses(componentNumClasses);
	}

	/**
	 * Set the classes/group correspondence in each component
	 * @param ind: The individual 
	 * */
	private void setClassesInGroupsDistribution(Individual ind){
		// Get genotype
		SyntaxTree genotype = ind.getGenotype();

		int numberOfComponents = ind.getNumberOfComponents();
		int numberOfClasses = this.relationshipsDataset.getColumns().size();

		int classIndex, actualCmp=-1;
		boolean isClass = false, isConnector = false;
		MultiIntegerColumn column;
		String symbol;

		// Initialize
		this.classGroup = new int [numberOfClasses];
		int componentNumGroups[] = new int[numberOfComponents];

		// Compute metrics for each component in the individual
		for(int i=1; !isConnector; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){
				// The symbol classes indicates the beginning of a 
				// group of classes in a component
				if(symbol.equalsIgnoreCase("classes")){
					isClass=true;
					actualCmp++;
					// New set of classes
					this.indexes.clear();
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass=false;
					// End of component classes, compute the number of groups
					componentNumGroups[actualCmp] = this.numberOfGroups();
				}
				else if(symbol.equalsIgnoreCase("connectors")){
					isConnector=true;
				}
			}
			// Terminal node
			else if(isClass){

				// Get the dataset information about the class
				column = (MultiIntegerColumn) this.relationshipsDataset.getColumnByName(symbol);
				classIndex = this.relationshipsDataset.getIndexOfColumn(column);

				// Add the class index in the array of indexes
				this.indexes.add(classIndex);
			}
		}// end of tree route

		// Set on individual
		ind.setClassesToGroups(this.classGroup);
		ind.setNumberOfGroups(componentNumGroups);
	}

	/**
	 * Counts the number of external relations on each component
	 * @param ind: The individual
	 * */ 
	private void setNumberExternalRelations(Individual ind) {
		// Get genotype
		SyntaxTree genotype = ind.getGenotype();
		int numberOfComponents = ind.getNumberOfComponents();

		int j, actualIndex, classIndex, actualCmp=-1, otherCmp=-1;
		boolean isClass = false, isOtherClass = false, isConnector = false;
		MultiIntegerColumn column;
		int nav_ij, nav_ji;
		String symbol;
		Object oValue;
		MultiIntegerValue relations_ij, relations_ji;
		
		// Initialize
		int componentNumberExternalConnections [] = new int[numberOfComponents];

		for(int i=0; i<numberOfComponents; i++){
			componentNumberExternalConnections[i] = 0;
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
					column = (MultiIntegerColumn) this.relationshipsDataset.getColumnByName(symbol);
					actualIndex = this.relationshipsDataset.getIndexOfColumn(column);

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
							classIndex = this.relationshipsDataset.getIndexOfColumn(this.relationshipsDataset.getColumnByName(genotype.getNode(j).getSymbol()));
							
							oValue = column.getElement(classIndex);
							
							// Not an invalid value
							if(!(oValue instanceof InvalidValue)){

								relations_ij = (MultiIntegerValue) oValue;
								relations_ji = (MultiIntegerValue)((MultiIntegerColumn)this.relationshipsDataset.getColumn(classIndex)).getElement(actualIndex);

								// Check type and navigation of each relation
								for(int k=1; k<relations_ij.getSize(); k+=2){

									nav_ij = relations_ij.getValue(k);
									nav_ji = relations_ji.getValue(k);

									// Not a candidate interface, because its a bidirectional relation
									if(nav_ij==nav_ji){
										componentNumberExternalConnections[actualCmp]++;
										componentNumberExternalConnections[otherCmp]++;
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

		ind.setExternalConnections(componentNumberExternalConnections);
	}

	/**
	 * Set the number of abstract classes per component
	 * @param ind The individual
	 * */
	private void setAbstractClasses(Individual ind){
		String className;
		int index;
		int component;
		int numOfComponents = ind.getNumberOfComponents();
		int numOfAbstractClasses [] = new int [numOfComponents];
		int classDistribution [] = ind.getClassesDistribution();
		for(int i=0; i<numOfComponents; i++){
			numOfAbstractClasses[i] = 0;
		}

		InstanceIterator it = new InstanceIterator(classesDataset);
		List<Object> instance;
		
		// Clases no ordenadas
		while(!it.isDone()){
			instance = it.currentInstance();

			className = instance.get(0).toString();
		
			// The class is abstract
			if((Boolean)instance.get(1)){

				// Get the index of the class
				index = relationshipsDataset.getIndexOfColumn(relationshipsDataset.getColumnByName(className));

				// Get the corresponding component
				component = classDistribution[index];
				numOfAbstractClasses[component]++;
			}
			it.next();
		}

		ind.setNumberOfAbstractClasses(numOfAbstractClasses);
	}

	/**
	 * Compute the number of connected component
	 * in the graph formed with the actual 
	 * <code>indexes</code>.
	 * @return Number of connected component
	 * */
	private int numberOfGroups() {
		int i, numOfGroups = 0;
		int size = this.indexes.size();
		this.visited = new boolean[size];

		for(i=0; i<size; i++)
			this.visited[i]=false;

		for(i=0; i<size; i++){
			if(!this.visited[i]){
				this.classGroup[this.indexes.get(i)]=numOfGroups; // Set group number
				numOfGroups++;
				graphDepthPath(i, this.indexes.get(i));
			}
		}
		return numOfGroups;
	}

	/**
	 * Realize a graph depth search marking 
	 * visited nodes.
	 * @param actualNode The node origin.
	 * @param classIndex The column index in the dataset for the origin node.
	 * */
	private void graphDepthPath(int actualNode, int classIndex){
		int j, size = this.visited.length;
		// Now, the node is visited
		this.visited[actualNode]=true;
		for(j=0; j<size; j++){
			// The node origin is connected with the node j, not visited yet, continue recursive depth search with j
			if( !(((MultiIntegerColumn)this.relationshipsDataset.getColumn(classIndex)).getElement(this.indexes.get(j)) instanceof InvalidValue)
					&& !this.visited[j]){

				this.classGroup[this.indexes.get(j)]=this.classGroup[classIndex];	// Set its group number
				graphDepthPath(j, this.indexes.get(j));
			}
		}
	}
}