package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.column.MultiIntegerColumn;
import es.uco.kdis.datapro.datatypes.MultiIntegerValue;
import es.uco.kdis.datapro.datatypes.InvalidValue;
import es.uco.kdis.dss.databuilders.info.UMLClass;
import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.evaluation.MOEvaluator;
//import net.sf.jclec.mo.evaluation.MOParallelEvaluator;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.mo.evaluation.Objective;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.imo.objectives.Metric;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Evaluator for the interactive multiobjective
 * discovery of software architectures.
 * 
 * <p>HISTORY:
 * <ul>
 *  <li>1.0: Creation (February 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * @see MOEvaluator
 * */
public class InteractiveMOEvaluator extends MOEvaluator { //extends MOParallelEvaluator { 

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -4455739218134419846L;

	/** Dataset that stores the information about relations */
	private Dataset relationshipsDataset;

	/** List of classes */
	private ArrayList<UMLClass> classesList;

	/** Number of classes */
	//private int numberOfClasses;

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
	public InteractiveMOEvaluator() {
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
	 * Set the problem information
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
		}
	}

	/**
	 * Set the relationships dataset
	 * @param The dataset
	 * */
	public void setRelationshipsDataset(Dataset dataset){ 
		this.relationshipsDataset = dataset;
		//this.numberOfClasses = this.relationshipsDataset.getColumns().size();

		// Set dataset in the metrics
		for(Objective obj: this.objectives){
			((Metric)obj).setDataset(this.relationshipsDataset);
		}
	}

	/**
	 * Set the list of classes
	 * @param List of classes
	 * */
	public void setClassesList(ArrayList<UMLClass> classesList){
		this.classesList = classesList;
	}

	/**
	 * Update the objective list considering the maximum
	 * value of those objective that depends on the problem instance.
	 * */
	public void setObjectiveMaxValues(){
		if(this.relationshipsDataset!=null){
			int numOfObjs = getObjectives().size();
			for(int i=0; i<numOfObjs; i++){
				((Metric)this.objectives.get(i)).computeMaxValue();
			}
		}
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

		InteractiveMOIndividual emoInd = (InteractiveMOIndividual)ind;
		int nObjs = this.numberOfObjectives();
		MOFitness fitness;// = new InteractiveMOFitness();
		IFitness [] components = new IFitness[nObjs];

		int nComps = emoInd.getNumberOfComponents();
		double [][] measures = new double[nObjs][nComps];
		String [] names = new String[nObjs];
		Metric obj;

		// The individual has not been evaluated yet
		if (emoInd.getFitness() == null){

			try {
				fitness = (MOFitness)fitnessPrototype.clone();

				// Measures needed for objectives evaluation
				computeMeasures(emoInd);	

				// Evaluate the individual for each objective
				for(int i=0; i<nObjs; i++){
					components[i] = this.objectives.get(i).evaluate(ind);
				}

				// Set components in the composite fitness
				fitness.setObjectiveValues(components);

				// Set the fitness in the individual
				ind.setFitness(fitness);

				// Copy the measures per component
				for(int i=0;i<nObjs;i++){
					obj = ((Metric)getObjectives().get(i));
					measures[i] = obj.getComponentsMeasure();
					names[i] = obj.getName();
				}

				//////////////////////////////////////////////////////

				/*for(int i=0; i<nObjs; i++){
					try {
						System.out.print("\n\nObjective: " + names[i] + " Objective value: " + ((InteractiveMOFitness)emoInd.getFitness()).getObjectiveDoubleValue(i));
						System.out.print(" components: ");
						for(int j=0; j<nComps; j++){
							System.out.print(measures[i][j] + " ");
						}
					} catch (IllegalAccessException | IllegalArgumentException e) {
						e.printStackTrace();
					}
				}*/

				/////////////////////////////////////////////////////

				emoInd.setComponentMeasures(measures);
				emoInd.setMeasuresNames(names);

			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
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
	private void computeMeasures(InteractiveMOIndividual ind){
		// Set required information for evaluation objectives
		this.setClassesDistribution(ind);
		this.setClassesInGroupsDistribution(ind);
		this.setNumberExternalRelations(ind);
		this.setAbstractClasses(ind);
	}

	/** 
	 * Set the correspondence between each class and its component 
	 * @param ind: The individual
	 * */
	private void setClassesDistribution(Individual ind){

		int actualCmp=-1;//, index;
		boolean isClass = false, isConnector = false;
		String symbol;
		// Number of classes in each component
		int componentNumClasses [] = new int[ind.getNumberOfComponents()];

		// Component at each class belong
		//	int distribution [] = new int[this.numberOfClasses];
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

				// Set the component at which the class belongs to
				//			index = this.relationshipsDataset.getIndexOfColumn(this.relationshipsDataset.getColumnByName(symbol));
				//			distribution[index] = actualCmp;
			}
		}// end of tree route

		// Set distribution in the individual
		//	ind.setClassesDistribution(distribution); // it is stored by species when the individual is created
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

		//System.out.println(ind.getGenotype());

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
						// TODO FALLA SI 'CLASSES' ESTÁ VACÍO!!!
						if((genotype.getNode(j-1).getSymbol().equalsIgnoreCase("classes"))){
							isOtherClass=true;
							otherCmp++;
						}

						if(isOtherClass){
							// Get relations between classes
							classIndex = this.relationshipsDataset.getIndexOfColumn(this.relationshipsDataset.getColumnByName(genotype.getNode(j).getSymbol()));

							//System.out.println(genotype.getNode(j).getSymbol());
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

		// Check whether a class is abstract or not
		int numOfClasses = this.classesList.size();
		UMLClass analysisClass;
		boolean isAbstract;
		for(int i=0; i<numOfClasses; i++){
			analysisClass = this.classesList.get(i);
			className = analysisClass.getName();
			isAbstract = analysisClass.isAbstract();

			if(isAbstract){
				// Get the index of the class
				index = this.relationshipsDataset.getIndexOfColumn(this.relationshipsDataset.getColumnByName(className));

				// Get the corresponding component
				component = classDistribution[index];
				numOfAbstractClasses[component]++;
			}	
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
	 * Make a graph depth search marking 
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
