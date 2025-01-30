package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;
//import java.util.Random;

import org.apache.commons.lang.builder.EqualsBuilder;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.mo.IConstrained;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Individual for interactive discovery of
 * software architectures.
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (April 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 2.0
 * @see IConstrained
 * */
public class InteractiveMOIndividual extends Individual implements IConstrained {

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 729784600259151497L;

	/** Matrix of measures per component */
	protected double [][] measures;

	/** Number of measures */
	protected String [] names;

	/** Frozen components */
	protected boolean [] frozenComponents;
	
	/** The solution was selected to enter in the archive */
	protected boolean inArchive;
	
	/** The solution was marked to be removed */
	protected boolean toBeRemoved;
	
	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Parameterized constructor.
	 * @param genotype Individual genotype
	 */
	public InteractiveMOIndividual(SyntaxTree genotype) {
		super(genotype);
		initializeFrozenComponents();
		this.inArchive = false;
		this.toBeRemoved = false;
	}

	/**
	 * Parameterized constructor.
	 * @param genotype Individual genotype
	 * @param fitness  Individual fitness
	 */
	public InteractiveMOIndividual(SyntaxTree genotype, IFitness fitness) {
		super(genotype,fitness);
		initializeFrozenComponents();
		this.inArchive = false;
		this.toBeRemoved = false;
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Get/set methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Set the matrix of measures
	 * @param measures The new matrix of measures
	 * */
	protected void setComponentMeasures(double [][] measures){
		int nRows = measures.length;
		int nCols = measures[0].length;
		this.measures = new double[nRows][nCols];
		for(int i=0; i<nRows; i++){
			for(int j=0; j<nCols; j++){
				this.measures[i][j]=measures[i][j];
			}
		}
	}

	/**
	 * Get the component measures
	 * @return Matrix of component measures
	 * */
	protected double [][] getComponentMeasures(){
		return this.measures;
	}

	/**
	 * Set the value of a component measure
	 * @param indexC The index of the component
	 * @param indexM The index of the measure
	 * @param value The new value
	 * */
	public void setComponentMeasure(int indexC, int indexM, double value){
		if(this.measures != null)
			this.measures[indexC][indexM] = value;
	}

	/**
	 * Get the value of a component measure
	 * @param indexM The index of the measure
	 * @param indexC The index of the component
	 * @return The value of the measure for the component specified
	 * */
	public double getComponentMeasure(int indexM, int indexC){
		return this.measures[indexM][indexC];
	}

	/**
	 * Get the names of the measures
	 * @return the names of the measures
	 * */
	public String [] getMeasuresNames(){
		return names;
	}

	/**
	 * Set the names of the measures
	 * @param The names of the measures
	 * */
	protected void setMeasuresNames(String [] names){
		this.names = new String[names.length];
		for(int i=0; i<names.length; i++){
			this.names[i] = names[i]; 
		}
	}

	/**
	 * Get a subtree of the genotype
	 * @param init Starting index
	 * @return The subtree starting in the given index, if it represent a non-terminal component,
	 * an empty tree otherwise
	 * */
	public SyntaxTree getSubtree(int init){
		SyntaxTree subtree = new SyntaxTree();
		int i = init;
		if(genotype.getNode(i).arity()!=0){
			do{
				subtree.addNode(genotype.getNode(i));
				i++;
			}while(genotype.getNode(i).arity()<genotype.getNode(i-1).arity());
		}
		return subtree;
	}
	
	
	public SyntaxTree getComponentTree(int index){
		SyntaxTree subtree = new SyntaxTree();
		int c=-1;
		String symbol;
		boolean isConnector = false;
		for(int i=0; c<=index && !isConnector && i<genotype.size();i++){
			symbol = genotype.getNode(i).getSymbol();
			if(symbol.equalsIgnoreCase("component")){
				c++;
			}
			if(symbol.equalsIgnoreCase("connectors")){
				isConnector=true;
			}
			if(!isConnector && c==index){
				subtree.addNode(genotype.getNode(i));
			}
		}
		return subtree;
	}
	
	public SyntaxTree getInterfaceTree(int indexC, int indexI){
		SyntaxTree subtree = new SyntaxTree();
		int c=-1;
		int inter = -1;
		String symbol;
		boolean isConnector = false, isProvided = true;
		//System.out.println("Extract interface -> component: " + indexC + " interface:" + indexI);
		for(int i=0; c<=indexC && !isConnector && i<genotype.size();i++){
			symbol = genotype.getNode(i).getSymbol();
			if(symbol.equalsIgnoreCase("component")){
				c++;
				inter=-1;
			}
			if(symbol.equalsIgnoreCase("provided-interfaces")){
				isProvided = true;
			}
			if(symbol.equalsIgnoreCase("interface")){
				inter++;
			}
			if(symbol.equalsIgnoreCase("connectors")){
				isConnector=true;
			}
			if(!isConnector && isProvided && c==indexC && inter==indexI){
				subtree.addNode(genotype.getNode(i));
			}
			//System.out.println("symbol: " + symbol + " c="+c + " i="+inter + " indexC="+indexC + " indexI="+indexI);
		}
		return subtree;
	}

	/**
	 * Get if a specific component is frozen
	 * @param index The index of the component
	 * */
	public boolean isFrozenComponent(int index){
		return this.frozenComponents[index];
	}

	/**
	 * Set the 'frozen' flag for a specific component
	 * @param index The index of the component
	 * @param value The value of the flag
	 * */
	public void setFrozenComponent(int index, boolean value){
		this.frozenComponents[index] = value;
	}

	/**
	 * Get the array of frozen components
	 * @return Frozen components array
	 * */
	public boolean[] getFrozenComponents() {
		return frozenComponents;
	}

	/**
	 * Set the array of frozen components
	 * @param frozenComponents New array of values
	 * */
	protected void setFrozenComponents(boolean[] frozenComponents) {
		this.frozenComponents = new boolean[frozenComponents.length];
		for(int i=0; i<frozenComponents.length; i++){
			this.frozenComponents[i] = frozenComponents[i];
		}
	}

	/**
	 * Get the number of frozen components
	 * */
	public int numberFrozenComponents(){
		int n = 0;
		for(int i=0; i<frozenComponents.length; i++){
			if(this.frozenComponents[i])
				n++;
		}
		return n;
	}
	
	public void setSolutionInArchive(boolean inArchive){
		this.inArchive = inArchive;
	}
	
	public boolean getSolutionInArchive(){
		return this.inArchive;
	}
	
	public void setToBeRemoved(boolean toBeRemoved){
		this.toBeRemoved = toBeRemoved;
	}
	
	public boolean getToBeRemoved(){
		return this.toBeRemoved;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/** 
	 * Copy the individual
	 * @return A copy of the individual
	 * */
	@Override
	public IIndividual copy() {
		// Create new individual
		InteractiveMOIndividual ind = new InteractiveMOIndividual(this.genotype.copy());

		// Set phenotype
		ind.setPhenotypefromGenotype();

		// Copy properties
		ind.setNumberOfComponents(this.getNumberOfComponents());
		ind.setNumberOfConnectors(this.getNumberOfConnectors());
		ind.setHasIsolatedComponents(this.hasIsolatedComponents());
		ind.setHasMutuallyDepComponents(this.hasMutuallyDepComponents());
		ind.setFrozenComponents(this.getFrozenComponents());
		ind.setClassesDistribution(this.getClassesDistribution());
		ind.setToBeRemoved(this.toBeRemoved);
		ind.setSolutionInArchive(this.inArchive);
		
		// Copy fitness and metrics
		if(this.fitness != null){
			ind.setFitness(this.getFitness().copy());
			ind.setNumberOfClasses(this.getNumberOfClasses());
			ind.setNumberOfGroups(this.getNumberOfGroups());
			ind.setClassesToGroups(this.getClassesToGroups());
			ind.setExternalConnections(this.getExternalConnections());
			
			ind.setNumberOfProvided(this.getNumberOfProvided());
			ind.setNumberOfRequired(this.getNumberOfRequired());

			ind.setComponentMeasures(this.getComponentMeasures());
			ind.setMeasuresNames(this.getMeasuresNames());
		}
		return ind;
	}

	@Override
	public String toString(){

		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());

		double value;
		int nObjectives, nComponents, nMeasures;

		if(this.fitness != null && fitness instanceof MOFitness){
			nObjectives = ((MOFitness)fitness).getNumberOfObjectives();

			// Fitness value
			if(this.fitness instanceof InteractiveMOFitness)
				buffer.append("\nFitness value: " +  ((MOFitness)fitness).getValue()
					+ " Preferences: " + ((InteractiveMOFitness)fitness).getPreferenceValue() 
					+ " Dominance (maximin): " + ((InteractiveMOFitness)fitness).getDominanceValue());

			// Fitness components
			buffer.append("\nObjective values: ");
			try {
				for(int i=0; i<nObjectives-1; i++){
					value = ((MOFitness)fitness).getObjectiveDoubleValue(i);
					buffer.append(value + " ");
				}
				buffer.append(" " + ((MOFitness)fitness).getObjectiveDoubleValue(nObjectives-1));
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}

			// Measures per component
			nComponents = getNumberOfComponents();
			nMeasures = getComponentMeasures().length;

			buffer.append("\nMeasures per component: ");
			for(int i=0; i<nMeasures; i++){
				buffer.append("\n\t" + names[i] + ":");
				for(int j=0; j<nComponents; j++){
					buffer.append(" " + getComponentMeasure(i, j));
				}
			}

			buffer.append("\nFrozen components: ");
			for(int i=0; i<nComponents; i++){
				if(this.frozenComponents[i]){
					buffer.append(i + " ");
				}
			}
			buffer.append("\n");
			buffer.append("User has included the solution in the archive: " + this.inArchive);
			buffer.append("\nUser has marked the solution to be removed: " + this.toBeRemoved);
			if(this.fitness instanceof InteractiveMOFitness)
				buffer.append("\nRegion: " + ((InteractiveMOFitness)getFitness()).getRegion() + " Territory size: " + Math.abs(((InteractiveMOFitness)getFitness()).getTerritory()));
		}
		return buffer.toString();
	}

	@Override
	public boolean equals(Object other){	

		if (other instanceof InteractiveMOIndividual) {
			InteractiveMOIndividual ind = (InteractiveMOIndividual) other;
			// Check general properties: type of solution and fitness
			EqualsBuilder eb = new EqualsBuilder();
			eb.append(genotype, ind.genotype);
			eb.append(fitness, ind.fitness);		
			return eb.isEquals();
		}
		else
			return false;
	}

	@Override
	protected void setInterfacesDistribution(){
		int actualCmp=-1;
		boolean isReqInterface = false, isConnector = false, isProvInterface = false;
		String symbol;

		// Initialize
		int numOfComponents = getNumberOfComponents();
		int componentNumProvided [] = new int [numOfComponents];
		int componentNumRequired [] = new int [numOfComponents];

		// Search interfaces in the components
		for(int i=1; !isConnector; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){

				// The symbol classes indicates the beginning of a new component
				if(symbol.equalsIgnoreCase("classes")){
					isReqInterface = false;
					isProvInterface = false;
					actualCmp++;
					componentNumRequired[actualCmp] = 0;
					componentNumProvided[actualCmp] = 0;
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isReqInterface = true;
					isProvInterface = false;
				}
				else if(symbol.equalsIgnoreCase("provided-interfaces")){
					isProvInterface = true;
					isReqInterface = false;
				}
				else if(symbol.equalsIgnoreCase("connectors")){
					isConnector=true;
				}

				// A required interface
				if (isReqInterface && symbol.equalsIgnoreCase("interface"))
					componentNumRequired[actualCmp]++;
				// A provided interface
				else if(isProvInterface && symbol.equalsIgnoreCase("interface"))
					componentNumProvided[actualCmp]++;
			}
		}// end of tree route

		setNumberOfProvided(componentNumProvided);
		setNumberOfRequired(componentNumRequired);

		// Check constraint
		boolean hasIsolated = false;
		for(int i=0; !hasIsolated && i<numOfComponents; i++)
			if(componentNumProvided[i]==0 && componentNumRequired[i]==0)
				hasIsolated = true;
		setHasIsolatedComponents(hasIsolated);

		///
		/*System.out.println(toString());
		System.out.println("NUMBER OF INTERFACES");
		for(int i=0; i<numOfComponents; i++){
			System.out.println("Component: i="+i+" req="+ getNumberOfRequired(i) + " prov="+ getNumberOfProvided(i));
		}*/
	}

	@Override
	protected void setInterfaceDependencies(){

		// Get genotype
		int i, j, k;
		boolean isConnector = false;
		String symbol, previous;
		int numOfComponents = getNumberOfComponents();
		ArrayList<Integer> reqComponents = new ArrayList<Integer>();
		ArrayList<Integer> provComponent = new ArrayList<Integer>();
		String aux[];

		// Initialize
		boolean componentIntefaceConnections [][] = new boolean [numOfComponents][numOfComponents];

		for(i=0; i<numOfComponents; i++)
			for(j=0; j<numOfComponents; j++)
				componentIntefaceConnections[i][j] = false;

		// Compute metrics for each component in the individual
		int size = genotype.size();
		int indexReq, indexProv;

		i=1;
		while(i<size){

			symbol = genotype.getNode(i).getSymbol();
			previous = genotype.getNode(i-1).getSymbol();

			//System.out.println("Symbol: " + symbol + " Previous: " + previous);

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){

				if(previous.equalsIgnoreCase("connector")){
					// New connector
					isConnector=true;

					// Save the information of the previous connector
					for(j=0; j<provComponent.size(); j++){
						indexProv = provComponent.get(j);
						for(k=0; k<reqComponents.size(); k++){
							indexReq = reqComponents.get(k);
							componentIntefaceConnections[indexProv][indexReq] = true;
						}
					}

					// Clean the indexes
					reqComponents.clear();
					provComponent.clear();
				}

				if(isConnector){
					// get the index of the component that provides the interface
					i++; // skip the non-terminal nodes "connector" and "provided-interfaces"
					aux = genotype.getNode(i).getSymbol().split("-");
					indexProv = Integer.parseInt(aux[0].substring(aux[0].length()-1, aux[0].length()));
					provComponent.add(indexProv-1);
					//System.out.println("Prov: " + genotype.getNode(i).getSymbol() + " index: " + (indexProv-1));

					// get the indexes of the components that require the interface
					i+=2; // skip the non-terminal node "required-interfaces"
					while(i<size && genotype.getNode(i).arity()==0){
						symbol = genotype.getNode(i).getSymbol();
						aux = symbol.split("-");
						indexReq = Integer.parseInt(aux[0].substring(aux[0].length()-1, aux[0].length()));
						reqComponents.add(indexReq-1);
						//System.out.println("Req: " + genotype.getNode(i).getSymbol() + " index: " + (indexReq-1));
						i++;
					}
					isConnector = false;
				}
				else
					i++;
			}
			else
				i++;

		}// end of tree route

		// Save the last connector
		// Save the information of the previous connector
		for(j=0; j<provComponent.size(); j++){
			indexProv = provComponent.get(j);
			for(k=0; k<reqComponents.size(); k++){
				indexReq = reqComponents.get(k);
				componentIntefaceConnections[indexProv][indexReq] = true;
			}
		}

		//////
		/*System.out.println("INTERFACE CONNECTIONS (PROV)");
		for(i=0; i<componentIntefaceConnections.length; i++){
			System.out.print("\ni="+i + " -> ");
			for(j=0; j<componentIntefaceConnections[i].length; j++){
				System.out.print(componentIntefaceConnections[i][j] + " ");
			}
		}
		System.out.println();*/

		boolean hasPairMutuallyDep = false;
		for(i=0; !hasPairMutuallyDep && i<numOfComponents; i++)
			for(k=i+1; !hasPairMutuallyDep && k<numOfComponents; k++)
				if(componentIntefaceConnections[i][k] && componentIntefaceConnections[k][i])
					hasPairMutuallyDep = true;
		//System.out.println("hasPairMutuallyDep: " + hasPairMutuallyDep);
		setHasMutuallyDepComponents(hasPairMutuallyDep);
		//System.out.println(toString());
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Initialize the flags for frozen components
	 * */
	private void initializeFrozenComponents(){
		int n = getNumberOfComponents();
		this.frozenComponents = new boolean[n];
		for(int i=0; i<n; i++){
			this.frozenComponents[i] = false;
		}
		
		// TESTING one frozen at random
		/*Random rndObject = new Random();
		int rnd = rndObject.nextInt(n);
		this.frozenComponents[rnd] = true;*/
	}
}