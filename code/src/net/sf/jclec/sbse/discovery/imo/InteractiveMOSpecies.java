package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;

import es.uco.kdis.dss.databuilders.info.UMLClass;
import es.uco.kdis.dss.databuilders.info.UMLOperation;
import es.uco.kdis.dss.databuilders.info.Visibility;
import net.sf.jclec.sbse.discovery.NonTerminalNode;
import net.sf.jclec.sbse.discovery.Species;
import net.sf.jclec.sbse.discovery.TerminalNode;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.syntaxtree.SyntaxTreeIndividual;

/**
 * Species for discovery problem
 * formulated as a multi-objective interactive problem.
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (February 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class InteractiveMOSpecies extends Species {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -8409097015375628189L;

	/** Classes and methods information */
	protected ArrayList<UMLClass> classesList;

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------------- Constructor
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public InteractiveMOSpecies(){
		super();
	}
	
	//////////////////////////////////////////////////////////////////
	//------------------------------------------------- Public methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get the list of classes
	 * @return A list with the information of the classes.
	 * */
	public ArrayList<UMLClass> getClassesList(){
		return this.classesList;
	}

	/**
	 * Set the list of classes
	 * @param classesList List of classes
	 * */
	public void setClassesList(ArrayList<UMLClass> classesList) {
		this.classesList = classesList;
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public void setConstraints(int minNumOfComp, int maxNumOfComp){

		// Create and configure the genotype schema
		this.schema = new InteractiveMOSchema(this.dataset, this.classesList);
		this.schema.setRootSymbol("architecture");
		this.schema.setTerminals(this.generateTerminalSymbols());
		this.schema.setNonTerminals(this.generateNonTerminalSymbols());
		this.schema.setMinNumOfComp(minNumOfComp);
		this.schema.setMaxNumOfComp(maxNumOfComp);
		this.schema.setNumOfClasses(this.dataset.getColumns().size());
	}

	@Override
	public SyntaxTreeIndividual createIndividual(SyntaxTree genotype) {
		InteractiveMOIndividual ind = new InteractiveMOIndividual(genotype);
		ind.setClassesDistribution(this.schema.getClassesDistribution());
		return ind;
	}

	@Override
	protected TerminalNode [] generateTerminalSymbols(){

		int numClasses = this.classesList.size();
		int numOperations;
		ArrayList<TerminalNode> terminalsList = new ArrayList<TerminalNode>();
		ArrayList<UMLOperation> operations;
		UMLOperation umlOperation;
		UMLClass umlClass;

		// Set the class names as terminal nodes
		for(int i=0; i<numClasses; i++){
			umlClass = this.classesList.get(i);
			terminalsList.add(new TerminalNode(umlClass.getName()));
		}

		// Set candidate services as terminal nodes
		for(int i=0; i<numClasses; i++){
			umlClass = this.classesList.get(i);
			operations = umlClass.getOperations();
			numOperations = operations.size();
			for(int j=0; j<numOperations; j++){
				umlOperation = operations.get(j);

				if(addOperationAsTerminal(umlOperation, umlClass)){
					terminalsList.add(new TerminalNode(umlClass.getName() + ":" + umlOperation.getName()));
				}
			}
		}

		TerminalNode [] arrayOfTerminals = new TerminalNode[terminalsList.size()];
		terminalsList.toArray(arrayOfTerminals);

		/*for(TerminalNode n: arrayOfTerminals){
			System.out.println(n);
		}*/

		return arrayOfTerminals;
	}

	@Override
	protected NonTerminalNode [] generateNonTerminalSymbols(){

		// Set non terminal symbols and their decomposition symbols
		NonTerminalNode [] nonTerminals = new NonTerminalNode[12];

		// <components-model> := <components> U <conectors>
		nonTerminals[0] = new NonTerminalNode("architecture", new String [] {"components", "connectors"});

		// <components> := <component> <component>+ (created later)
		nonTerminals[1] = new NonTerminalNode("components", null);

		// <connectors> := <connector>+ (created later)
		nonTerminals[2] = new NonTerminalNode("connectors", null);

		// <component> := <classes> U <requiered-interfaces> U <provided-interfaces>
		nonTerminals[3] = new NonTerminalNode("component", new String [] {"classes", "required-interfaces", "provided-interfaces"});

		// <connector> := <requiered-interface> U <provided-interface>
		nonTerminals[4] = new NonTerminalNode("connector", new String [] {"required-interface", "provided-interface"});

		// <classes> := <class>+ (created later)
		nonTerminals[5] = new NonTerminalNode("classes", null);

		// <requiered-interfaces> := null|<interface>+ (created later)
		nonTerminals[6] = new NonTerminalNode("required-interfaces", null);

		nonTerminals[7] = new NonTerminalNode("interface", null);

		// <provided-interfaces> := null|<interface>+ (created later)
		nonTerminals[8] = new NonTerminalNode("provided-interfaces", null);

		nonTerminals[9] = new NonTerminalNode("interface", null);

		// <requiered-interface> := <interface> (created later)
		nonTerminals[10] = new NonTerminalNode("required-interface", null);

		// <provided-interface> := <interface> (created later)
		nonTerminals[11] = new NonTerminalNode("provided-interface", null);

		return nonTerminals;
	}

	/**
	 * Decide if a given operation of a class can be a candidate service
	 * of an interface
	 * @param umlOperation The operation
	 * @param umlClass The class that defines the operation
	 * @return True if the operation should be added, false otherwise
	 * */
	private boolean addOperationAsTerminal(UMLOperation umlOperation, UMLClass umlClass){
		boolean result = false;
		String name = umlOperation.getName();
		String namePair1, namePair2 = null;
		ArrayList<UMLOperation> classOperations;
		int size;

		// A public operation and not a constructor
		if(umlOperation.getVisibility().equals(Visibility.publicMethod) && !name.equals(umlClass.getName())){
			// A getter or setter operation
			if(name.startsWith("get") || name.startsWith("set")){

				result = true;

				// Check if the class has an pair operation
				if(name.startsWith("g")){
					namePair1 = name.replace("get", "set"); // getX -> setX
					namePair2 = null;
				}
				else{
					namePair1 = name.replace("set", "get"); // setX -> getX
					namePair2 = name.replace("set", "is"); // setX -> isX (boolean properties)
				}
				classOperations = umlClass.getOperations();
				size = classOperations.size();

				//System.out.println("pair1: " + namePair1 + " pair2: " + namePair2);

				for(int i=0; result && i<size; i++){
					if(classOperations.get(i).getName().equalsIgnoreCase(namePair1) || 
							(namePair2!=null && classOperations.get(i).getName().equalsIgnoreCase(namePair2))){
						result = false;
					}
				}
			}

			else
				result = true;
		}
		//if(umlClass.getName().equalsIgnoreCase("MissingValue"))
		//System.out.println("class: " + umlClass.getName() + " method: " + umlOperation.getName() + " res: " + result);

		return result;
	}
}
