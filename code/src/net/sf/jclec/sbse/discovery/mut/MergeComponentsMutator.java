package net.sf.jclec.sbse.discovery.mut;

import java.util.ArrayList;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.Individual;
import net.sf.jclec.sbse.discovery.NonTerminalNode;
import net.sf.jclec.sbse.discovery.TerminalNode;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.util.random.IRandGen;

/**
 * Mutate an individual merging two existing components.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (February 2013)
 * 	<li>2.0: Refactoring (July 2013)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 2.0
 * */
public class MergeComponentsMutator extends AbstractCmpMutator {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial version */
	private static final long serialVersionUID = -6121041039166849383L;

	/** Number of component */
	private int numberOfComponents;

	/** First component */
	private int component1;
	
	/** Second component */
	private int component2;
	
	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public MergeComponentsMutator(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public IIndividual mutateIndividual(Individual ind, IRandGen randgen) {

		this.numberOfComponents = ind.getNumberOfComponents();
		this.ind=ind;
		//Choose component selection method
		if(this.isRandom)
			randomSelection(randgen);
		else 
			heuristicSelection(randgen);

		SyntaxTree mutTree = mutateSyntaxTree();
		return this.species.createIndividual(mutTree);	// Copy id from parent
	}

	@Override
	public boolean isApplicable(Individual ind) {
		// The actual number of components must be greater than
		// the minimum number of components configured. 
		if(ind.getNumberOfComponents()>this.schema.getMinNumOfComp())
			return true;
		return false;
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Randomly selection of component
	 * that will be joined
	 * @param randgen 
	 * */
	private void randomSelection(IRandGen randgen) {
		this.component1 = randgen.choose(0, this.numberOfComponents);
		do{
			this.component2 = randgen.choose(0, this.numberOfComponents);
		}while(this.component1==this.component2);
	}

	/**
	 * Heuristic selection of the component to be joined.
	 * Two coupled component are selected. The probability
	 * of each pair of coupled components is based on the
	 * strength of coupling
	 * @param randgen Random number generator
	 * @return true if heuristic selection can be realized, false otherwise
	 * */
	private void heuristicSelection(IRandGen randgen) {

		// Select the first component: the most coupled component
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		int [] connectivity = this.ind.getExternalConnections();
		int maxValue = 0;
		for(int i=0; i<this.numberOfComponents; i++){
			if(connectivity[i]>maxValue)
				maxValue=connectivity[i];
		}

		for(int i=0; i<this.numberOfComponents; i++)
			if(connectivity[i]==maxValue)
				candidates.add(i);

		if(candidates.size()>1){
			int iIndex1 = randgen.choose(0, candidates.size());
			int iIndex2; 
			do{
				iIndex2 = randgen.choose(0, candidates.size());
			}while(iIndex1==iIndex2);

			this.component1=candidates.get(iIndex1);
			this.component2=candidates.get(iIndex2);
		}

		else{
			this.component1=candidates.get(0);
			do{
				this.component2=randgen.choose(0, this.numberOfComponents);
			}while(this.component1==this.component2 || connectivity[this.component2]==0);
		}
	}

	/**
	 * Create a mutant genotype merging two components.
	 * @return The mutant genotype.
	 * */
	private SyntaxTree mutateSyntaxTree(){
		SyntaxTree tree = this.ind.getGenotype();
		SyntaxTree mutTree = new SyntaxTree();
		int actualComp = -1;
		String symbol;
		boolean isClass = false, isRequired=false, isProvided=false, isConnector=false;

		// The new component classes
		ArrayList<String> classes = new ArrayList<String>();

		// The new component required interfaces
		ArrayList<String> reqInterfaces = new ArrayList<String>();

		// The new component provided interfaces
		ArrayList<String> provInterfaces = new ArrayList<String>();

		// Locate the component to be divided and save the class and interfaces implied
		// Copy the rest of components
		int i=0;
		for(i=0; !isConnector; i++){
			symbol = tree.getNode(i).getSymbol();
			if(tree.getNode(i).arity()!=0){
				if(tree.getNode(i).getSymbol().equalsIgnoreCase("component")){
					actualComp++;
					isProvided=false;
					isRequired=false;
					isClass=false;
				}
				else if(symbol.equalsIgnoreCase("classes")){
					isClass=true;	// The beginning of a set of classes
				}
				else if(symbol.equalsIgnoreCase("provided-interfaces")){
					isProvided=true;
					isRequired=false;
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass=false;	// The end of a set of classes
					isRequired=true;
				}
				else if(symbol.equalsIgnoreCase("connectors")){
					isConnector=true;	// The end of classes and interfaces information
				}
				if(actualComp != this.component1 && actualComp != this.component2 && !isConnector)
					mutTree.addNode(tree.getNode(i));

			}
			// Terminal node
			else{
				// One of the components to be merged, save his specification
				if(actualComp == this.component1 || actualComp == this.component2){
					if(isClass)
						classes.add(symbol);
					else if(isRequired)
						reqInterfaces.add(symbol);
					else if(isProvided)
						provInterfaces.add(symbol);
				}
				// Other component, add it
				else
					mutTree.addNode(tree.getNode(i));
			}
		}

		// Add the merged component
		addNewComponent(mutTree, classes, reqInterfaces, provInterfaces);

		// Add connectors
		mutTree.addNode(tree.getNode(i-1));	// Add connectors symbol
		this.schema.setConnectors(mutTree, actualComp+1);

		return mutTree;
	}

	/**
	 * Add a new component with the given specification.
	 * @param mutTree The genotype where component will be added.
	 * @param classes The classes belonging to the component.
	 * @param reqInterfaces The component's required interfaces.
	 * @param provInterfaces The component's provided interfaces.
	 * */
	private void addNewComponent(SyntaxTree mutTree,
			ArrayList<String> classes, ArrayList<String> reqInterfaces,
			ArrayList<String> provInterfaces) {

		NonTerminalNode [] nonTerminals = this.schema.getNonTerminal("component");
		TerminalNode terminal;
		String interfaceName, class1, class2;

		mutTree.addNode(nonTerminals[0]);
		String [] symbols = nonTerminals[0].getElements();
		int j;
		for(int i=0; i<symbols.length; i++){
			mutTree.addNode(this.schema.getNonTerminal(symbols[i])[0]);

			// Classes
			if(symbols[i].equalsIgnoreCase("classes"))
				for(j=0; j<classes.size(); j++){
					mutTree.addNode(this.schema.getTerminal(classes.get(j)));
				}

			// Required interfaces
			else if(symbols[i].equalsIgnoreCase("required-interfaces"))
				for(j=0; j<reqInterfaces.size(); j++){
					terminal = this.schema.getTerminal(reqInterfaces.get(j));
					interfaceName = terminal.getSymbol();
					class1 = interfaceName.substring(0, interfaceName.indexOf("_"));
					class2 = interfaceName.substring(interfaceName.lastIndexOf("_")+1);
					if(!(classes.contains(class1) && classes.contains(class2)))
						mutTree.addNode(terminal);
				}

			// Provided interfaces	
			else if(symbols[i].equalsIgnoreCase("provided-interfaces"))
				for(j=0; j<provInterfaces.size(); j++){
					terminal = this.schema.getTerminal(provInterfaces.get(j));
					interfaceName = terminal.getSymbol();
					class1 = interfaceName.substring(0, interfaceName.indexOf("_"));
					class2 = interfaceName.substring(interfaceName.lastIndexOf("_")+1);
					if(!(classes.contains(class1) && classes.contains(class2)))
						mutTree.addNode(terminal);
				}
		}
	}
}