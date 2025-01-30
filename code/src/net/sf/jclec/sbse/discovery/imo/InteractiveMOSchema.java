package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.column.MultiIntegerColumn;
import es.uco.kdis.datapro.datatypes.MultiIntegerValue;
import es.uco.kdis.datapro.datatypes.InvalidValue;
import es.uco.kdis.dss.databuilders.info.UMLClass;
import es.uco.kdis.dss.databuilders.info.UMLOperation;
import net.sf.jclec.sbse.discovery.NonTerminalNode;
import net.sf.jclec.sbse.discovery.Schema;
import net.sf.jclec.sbse.discovery.TerminalNode;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Tree schema for the discovery problem
 * formulated as a multi-objective interactive problem.
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (May 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class InteractiveMOSchema extends Schema {

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 1361117505618817889L;

	/** Dataset that stores the relationships between the classes */
	protected Dataset relationshipsDataset;

	/** List that stores de information of the classes within the analysis model */
	protected ArrayList<UMLClass> classesList;

	/** Number of components to be created in a tree */
	protected int numberOfComponents;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Parameterized constructor
	 * @param relationshipsDataset The dataset
	 * @param classesList The list of classes
	 * */
	public InteractiveMOSchema(Dataset relationshipsDataset, ArrayList<UMLClass> classesList){
		super();
		this.relationshipsDataset = relationshipsDataset;
		this.classesList = classesList;
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Recursive generation of tree branches
	 * @param tree The tree
	 * @param symbol The actual symbol
	 * @param n The number of components to generate. For classes symbol, it represent the number of his component.
	 * */
	@Override
	public void fillSyntaxBranch(SyntaxTree tree, String symbol, int n) {

		// Terminal, add node
		if (isTerminal(symbol)) {
			tree.addNode(getTerminal(symbol));
		}

		// Non terminal
		else {

			// Get the element decomposition
			NonTerminalNode nonTerminal = this.nonTerminalsMap.get(symbol)[0]; 

			if (nonTerminal != null) {

				// Add this node
				tree.addNode(nonTerminal);

				// Continue the element decomposition
				int numberOfElements = nonTerminal.arity();
				if(numberOfElements>0){
					for (int i=0; i<numberOfElements; i++)
						fillSyntaxBranch(tree, nonTerminal.getElements()[i], n);
				}

				// The non terminal requires a dynamically decomposition
				else{

					// If the symbol is "components", expand the number of components
					if(nonTerminal.getSymbol().equalsIgnoreCase("components")){
						this.numberOfComponents = n;
						for(int i=0; i<n; i++)
							fillSyntaxBranch(tree, "component", i);
					}

					// If the symbol is "classes", add the classes assigned to component n
					else if(nonTerminal.getSymbol().equalsIgnoreCase("classes")){
						setClassesInComponent(tree, n, this.distribution);
					}

					// If the symbol is "required-interfaces", add the services required 
					// by classes of the current component and specify the required interfaces
					else if (nonTerminal.getSymbol().equalsIgnoreCase("required-interfaces")){
						setRequiredInterfaces(tree, n, this.distribution, this.numberOfComponents);
					}

					// If the symbol is "provided-interfaces", add the services provided 
					// by classes of the current component and specify the provided interfaces
					else if (nonTerminal.getSymbol().equalsIgnoreCase("provided-interfaces")){
						setProvidedInterfaces(tree, n, this.distribution, this.numberOfComponents);
					}

					// If the symbol is "connectors", generate the required connectors 
					// using the information of the created interfaces
					else if(nonTerminal.getSymbol().equalsIgnoreCase("connectors")){
						setConnectors(tree, this.numberOfComponents);
					}
				}
			}
		}
	}

	/**
	 * Add the nodes that represent the classes belonging to a component
	 * @param tree The tree structure that stores the genotype
	 * @param component The current component
	 * @param distribution The distribution of classes into components
	 * */
	public void setClassesInComponent(SyntaxTree tree, int component, int [] distribution) {
		for(int i=0; i<distribution.length; i++){
			if(distribution[i]==component){
				tree.addNode(this.terminals[i]);
			}
		}
	}

	/**
	 * Add the nodes that represent the required interfaces of a given component
	 * @param tree The tree structure that stores the genotype
	 * @param component The current component
	 * @param distribution The distribution of classes into components
	 * @param numberOfComponents The number of components in the architectural solution
	 * */
	public void setRequiredInterfaces(SyntaxTree tree, int component, int [] distribution, int numberOfComponents) {

		// Number of candidate services
		int numOfServices = this.terminals.length-this.numberOfClasses;
		String class1name;
		MultiIntegerColumn column;
		Object value;
		MultiIntegerValue multi_class1, multi_class2;
		int size, index_class1;
		ArrayList<TerminalNode> candidateServices = new ArrayList<TerminalNode>();
		ArrayList<Integer> provComponent = new ArrayList<Integer>();
		//ArrayList<ArrayList<Integer>> providerComponent = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> reqComponents = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<String>> classesReqComponents = new ArrayList<ArrayList<String>>();

		// For each service, check if it should be part of a required interface of this component
		for(int i=0; i<numOfServices; i++){

			// get the information of the class that specify the operation
			class1name = this.terminals[this.numberOfClasses+i].getSymbol().split(":")[0];
			column = (MultiIntegerColumn) this.relationshipsDataset.getColumnByName(class1name);
			index_class1 = this.relationshipsDataset.getIndexOfColumn(column);

			// for all the other classes, check if there exists a navigable relationships between them
			for(int j=0; j<this.numberOfClasses; j++){

				value = column.getElement(j);

				if(!(value instanceof InvalidValue)){
					multi_class1 = (MultiIntegerValue)value;
					multi_class2 = (MultiIntegerValue)this.relationshipsDataset.getColumn(j).getElement(index_class1);
					size = multi_class1.getSize();

					for(int k=0; k<size; k+=2){
						if(distribution[j] == component // the class belongs to the current component
								&& distribution[index_class1] != distribution[j] // class1 and class2 are allocated in different components
										&& multi_class1.getValue(k+1)==1 && multi_class2.getValue(k+1)==0){ // and class1 is invoked by class2

							// add the candidate service and the providers
							candidateServices.add(this.terminals[this.numberOfClasses+i]);
							provComponent.add(distribution[index_class1]);
							ArrayList<String> aux = new ArrayList<String>();
							aux.add(this.relationshipsDataset.getColumn(j).getName());

							// search other components (and its classes) that require the same service
							reqComponents.add(searchReqComponents(index_class1, distribution, aux));
							classesReqComponents.add(aux);

							/*candidateServices.add(this.terminals[this.numberOfClasses+i]);
							targetComponent.add(new Integer(distribution[index_class1]));
							 */

							/*System.out.println("Class1: " + class1name + " Component: " + distribution[index_class1]);
							System.out.println("Class2: " + this.relationshipsDataset.getColumn(j).getName() + " Component: " + distribution[j]);
							System.out.println("Relation1: type=" + multi_class1.getValue(k) + " nav=" + multi_class1.getValue(k+1));
							System.out.println("Relation2: type=" + multi_class2.getValue(k) + " nav=" + multi_class2.getValue(k+1));*/
						}
					}
				}
			}
		}



		// Add related operations
		addRelatedOperations(candidateServices, reqComponents, provComponent, distribution, classesReqComponents, false);
		addRelatedOperationsByClass(candidateServices, reqComponents, provComponent, distribution, component, classesReqComponents, false);

		////////////////////////////////////////////////////////////////////
		/*System.out.println("REQUIRED -- BEFORE REMOVAL...");
		for(int i=0; i<candidateServices.size(); i++){
			System.out.print("Service: " + candidateServices.get(i).getSymbol() + " provider components: ");
			for(int j=0; j<reqComponents.get(i).size(); j++){
				System.out.print(reqComponents.get(i).get(j) + " ");
			}
			System.out.print("\nClasses: ");
			for(int j=0; j<classesReqComponents.get(i).size(); j++){
				System.out.print(classesReqComponents.get(i).get(j) + " ");
			}
			System.out.println();
		}*/
		///////////////////////////////////////////////////////////////////

		// Remove duplicated operations
		removeDuplicatedOperations(candidateServices, reqComponents, provComponent, classesReqComponents, false);

		////////////////////////////////////////////////////////////////////
		/*System.out.println("REQUIRED -- AFTER REMOVAL...");
		for(int i=0; i<candidateServices.size(); i++){
			//if((candidateServices.get(i).getSymbol().startsWith("Dataset:") ||
			//		candidateServices.get(i).getSymbol().startsWith("NumericalColumn:")) && provComponent.get(i)==2){
			System.out.print("Service: " + candidateServices.get(i).getSymbol() + "provider: " + provComponent.get(i) + " req components: ");
			for(int j=0; j<reqComponents.get(i).size(); j++){
				System.out.print(reqComponents.get(i).get(j) + " ");
			}
			System.out.print("\nClasses: ");
			for(int j=0; j<classesReqComponents.get(i).size(); j++){
				System.out.print(classesReqComponents.get(i).get(j) + " ");
			}
			System.out.println("\n\n");
			//}
		}*/
		///////////////////////////////////////////////////////////////////

		/*System.out.println("Component: " + component + " Candidate services: ");
		for(int i=0; i<candidateServices.size(); i++){
			System.out.println("\t"+ candidateServices.get(i).getSymbol() + " target: " + targetComponent.get(i));
		}*/

		// Create one interface for each set of operations that are required by the same component
		size = candidateServices.size();
		//boolean createInterface;

		int s1, s2;
		s1 = 0;
		String reqClasses;

		// For each candidate service, create a new interface considering all the services
		// that have the same target components
		while(s1<reqComponents.size()){
			//if(reqComponents.get(s1).contains(component)){
			//System.out.println("REQ INTERFACE");
			tree.addNode(this.nonTerminalsMap.get("interface")[0]);
			//tree.addNode(candidateServices.get(s1));
			reqClasses = searchRequiredClasses(classesReqComponents.get(s1), component, distribution);
			tree.addNode(new TerminalNode(candidateServices.get(s1).getSymbol() + reqClasses));
			//System.out.println("\t"+candidateServices.get(s1).getSymbol() + reqClasses);
			s2 = s1+1;
			while(s2<reqComponents.size()){
				// Both services share the provider component and the set of required components, 
				// remove the candidate service to avoid considering it later
				if(provComponent.get(s1) == provComponent.get(s2) 
						&& equalsSets(reqComponents.get(s1), reqComponents.get(s2))){

					reqClasses = searchRequiredClasses(classesReqComponents.get(s2), component, distribution);
					//tree.addNode(candidateServices.get(s2));
					tree.addNode(new TerminalNode(candidateServices.get(s2).getSymbol() + reqClasses));
					//System.out.println("\t"+candidateServices.get(s2).getSymbol() + reqClasses);
					candidateServices.remove(s2);
					reqComponents.remove(s2);
					provComponent.remove(s2);
				}
				else
					s2++;
			}
			//}
			s1++;
		}

		/*for(int i=0; i<numberOfComponents; i++){
			createInterface = true;
			for(int j=0; j<size; j++){
				//if(targetComponent.get(j) == i){
				if(targetComponent.get(j).contains(i)){
					if(createInterface){
						tree.addNode(this.nonTerminalsMap.get("interface")[0]);
						createInterface = false;
					}
					tree.addNode(candidateServices.get(j));
				}
			}
		}*/
	}

	/**
	 * Add the nodes that represent the provided interfaces of a given component
	 * @param tree The tree structure that stores the genotype
	 * @param component The current component
	 * @param distribution The distribution of classes into components
	 * @param numberOfComponents The number of components in the architectural solution
	 * */
	public void setProvidedInterfaces(SyntaxTree tree, int component, int [] distribution, int numberOfComponents) {

		// Number of candidate services
		int numOfServices = this.terminals.length-this.numberOfClasses;
		String class1name;
		MultiIntegerColumn column;
		Object value;
		MultiIntegerValue multi_class1, multi_class2;
		int size, index_class1;
		ArrayList<TerminalNode> candidateServices = new ArrayList<TerminalNode>();
		ArrayList<ArrayList<Integer>> targetComponent = new ArrayList<ArrayList<Integer>>();

		// For each service, check if it should be part of a provided interface of this component
		for(int i=0; i<numOfServices; i++){

			// get the information of the class that specify the operation
			class1name = this.terminals[this.numberOfClasses+i].getSymbol().split(":")[0];
			column = (MultiIntegerColumn) this.relationshipsDataset.getColumnByName(class1name);
			index_class1 = this.relationshipsDataset.getIndexOfColumn(column);

			// The class belongs to the component
			if(distribution[index_class1]==component){

				// for all the other classes, check if there exists a navigable relationship between them
				for(int j=0; j<this.numberOfClasses; j++){

					value = column.getElement(j);

					if(!(value instanceof InvalidValue)){
						multi_class1 = (MultiIntegerValue)value;
						multi_class2 = (MultiIntegerValue)this.relationshipsDataset.getColumn(j).getElement(index_class1);
						size = multi_class1.getSize();

						for(int k=0; k<size; k+=2){
							if(distribution[index_class1] != distribution[j] // class1 and class2 are allocated in different components
									&& multi_class1.getValue(k+1)==1 && multi_class2.getValue(k+1)==0){ // and class1 is invoked by class2

								/*System.out.println("Class1: " + class1name + " Component: " + distribution[index_class1]);
								System.out.println("Class2: " + this.relationshipsDataset.getColumn(j).getName() + " Component: " + distribution[j]);
								System.out.println("Relation1: type=" + multi_class1.getValue(k) + " nav=" + multi_class1.getValue(k+1));
								System.out.println("Relation2: type=" + multi_class2.getValue(k) + " nav=" + multi_class2.getValue(k+1));
								 */

								// Save the service and the index of the component that require it
								int index = candidateServices.indexOf(this.terminals[this.numberOfClasses+i]);
								if(index!=-1){
									if(!targetComponent.get(index).contains(distribution[j]))
											targetComponent.get(index).add(distribution[j]);
								}
								else{
									candidateServices.add(this.terminals[this.numberOfClasses+i]);
									targetComponent.add(new ArrayList<Integer>());
									targetComponent.get(candidateServices.size()-1).add(distribution[j]);
								}

								/*
								 * candidateServices.add(this.terminals[this.numberOfClasses+i]);
									targetComponent.add(new Integer(distribution[j]));
								 * */
							}
						}
					}
				}
			}
		}

		////////////////////////////////////////////////////////////////////
		/*for(int i=0; i<candidateServices.size(); i++){
			System.out.print("Service: " + candidateServices.get(i).getSymbol() + " target components: ");
			for(int j=0; j<targetComponent.get(i).size(); j++){
				System.out.print(targetComponent.get(i).get(j) + " ");
			}
			System.out.println();
		}*/
		///////////////////////////////////////////////////////////////////

		// Add related operations
		addRelatedOperations(candidateServices, targetComponent, null, distribution, null, true);

		//System.out.println(tree);
		addRelatedOperationsByClass(candidateServices, targetComponent, null, distribution, component, null, true);

		// Remove duplicated operations
		removeDuplicatedOperations(candidateServices, targetComponent, null, null, true);

		////////////////////////////////////////////////////////////////////
		/*System.out.println("PROVIDED -- AFTER REMOVAL...");
		for(int i=0; i<candidateServices.size(); i++){
			//if(candidateServices.get(i).getSymbol().contains("Missing")){
				System.out.print("Service: " + candidateServices.get(i).getSymbol() + " target components: ");

				for(int j=0; j<targetComponent.get(i).size(); j++){
					System.out.print(targetComponent.get(i).get(j) + " ");
				}
				System.out.println();
			//}
		}*/
		///////////////////////////////////////////////////////////////////

		/*	System.out.println("Component: " + component + " Candidate services: ");
		for(int i=0; i<candidateServices.size(); i++){
			System.out.println("\t"+ candidateServices.get(i).getSymbol() + " target: " + targetComponent.get(i));
		}*/

		// Create one interface for each set of operations that are required by the same set of components
		size = candidateServices.size();
		//boolean createInterface;

		int s1, s2;
		s1 = 0;
		// For each candidate service, create a new interface considering all the services
		// that have the same target components
		while(s1<targetComponent.size()){
			tree.addNode(this.nonTerminalsMap.get("interface")[0]);
			tree.addNode(candidateServices.get(s1));
			s2 = s1+1;
			while(s2<targetComponent.size()){
				// Same target components, remove the candidate service to avoid visiting it again
				if(equalsSets(targetComponent.get(s1), targetComponent.get(s2))){
					tree.addNode(candidateServices.get(s2));
					candidateServices.remove(s2);
					targetComponent.remove(s2);
				}
				else
					s2++;
			}
			s1++;
		}

		/*for(int i=0; i<numberOfComponents; i++){
			createInterface = true;
			for(int j=0; j<size; j++){
				//if(targetComponent.get(j) == i){
				if(targetComponent.get(j).contains(i)){
					if(createInterface){
						tree.addNode(this.nonTerminalsMap.get("interface")[0]);
						createInterface = false;
					}
					tree.addNode(candidateServices.get(j));
				}
			}
		}*/
	}

	/**
	 * Add the nodes that represent the connectors within the architecture
	 * @param tree The tree structure that stores the genotype
	 * @param numberOfComponents The number of components in the architectural solution
	 * */
	public void setConnectors(SyntaxTree tree, int numberOfComponents) {

		String actualSymbol;
		int [] compPos = new int[numberOfComponents];
		int [] reqPos = new int[numberOfComponents];
		int [] provPos = new int[numberOfComponents];
		int j, nProvInterface, k;
		boolean finish;
		ArrayList<String> provServices;
		ArrayList<String> reqInterfaces;

		// Get the positions of component and interface symbols
		locateComponentInterfaces(tree, compPos, reqPos, provPos);

		// For each component
		for(int i=0; i<numberOfComponents; i++){

			// Search each provided interface until the next component is reached
			j=provPos[i];

			nProvInterface = 0;
			finish = false;
			while(!finish){
				actualSymbol = tree.getNode(j).getSymbol();

				if(actualSymbol.equalsIgnoreCase("interface")){
					nProvInterface++;

					// Extract the services
					provServices = new ArrayList<String>();
					for(k=j+1; tree.getNode(k).arity()==0; k++){
						provServices.add(tree.getNode(k).getSymbol());
					}
					j = k;

					// Search if any required interface in the rest of the components matches with the provided interface
					/*System.out.println("\nBuscar interfaces requeridas... ");
					System.out.println("Servicios de la interfaz: ");
					for(String s: provServices){
						System.out.println("\t"+s);
					}*/

					reqInterfaces = searchRequiredInterfaces(tree, i, provServices, compPos, reqPos, provPos);

					//System.out.println("Num req interfaces: " + reqInterfaces.size());

					if(reqInterfaces.size() > 0){
						tree.addNode(this.nonTerminalsMap.get("connector")[0]);	// new connector		
						tree.addNode(this.nonTerminalsMap.get("provided-interface")[0]); // new provided interface
						tree.addNode(new TerminalNode("component"+(i+1)+"-interface"+nProvInterface));
						tree.addNode(this.nonTerminalsMap.get("required-interfaces")[0]);
						for(String s: reqInterfaces){
							tree.addNode(new TerminalNode(s));
						}
						reqInterfaces.clear();
					}					
				}
				else{
					j++;
				}

				if(tree.getNode(j).getSymbol().equalsIgnoreCase("connectors")
						|| ((i+1)<numberOfComponents && j>=compPos[i+1])){
					finish = true;
				}
			}
		}
	}

	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Private methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Search for component and interfaces symbols and 
	 * save the positions in the respective array.
	 * @param tree The tree
	 * @param compPos The array for component positions
	 * @param reqPos The array for required interfaces positions
	 * @param provPos The array for provided interfaces positions
	 * */
	private void locateComponentInterfaces(SyntaxTree tree, int [] compPos, int [] reqPos, int [] provPos){
		int size = tree.size();
		int actualComp=-1;
		String symbol;
		for(int i=0; i<size; i++){
			symbol = tree.getNode(i).getSymbol();
			if(symbol.equalsIgnoreCase("component")){
				actualComp++;
				compPos[actualComp] = i;
			}
			else if(symbol.equalsIgnoreCase("required-interfaces"))
				reqPos[actualComp] = i;
			else if(symbol.equalsIgnoreCase("provided-interfaces"))
				provPos[actualComp] = i;
		}
	}

	/**
	 * Remove duplicated candidate operations.
	 * @param candidateOperations Set of candidate services
	 * @param targetComponent Set of target components
	 * */
	//private void removeDuplicatedOperations(ArrayList<TerminalNode> candidateOperations, ArrayList<Integer> targetComponent){
	private void removeDuplicatedOperations(ArrayList<TerminalNode> candidateOperations, 
			ArrayList<ArrayList<Integer>> targetComponent, ArrayList<Integer> provComponent, 
			ArrayList<ArrayList<String>> classesReqComponent, boolean isProvided){
		// Remove duplicated operations
		int c1 = 0, c2;
		String oper1, oper2;
		boolean duplicated;
		String classname;

		while(c1<candidateOperations.size()){
			c2 = c1+1;
			oper1 = candidateOperations.get(c1).getSymbol();
			duplicated = false;
			while(c2<candidateOperations.size()){
				oper2 = candidateOperations.get(c2).getSymbol();
				//if(oper1.equalsIgnoreCase(oper2)){

				///////////////////////////////////////////////
				/*if(targetComponent.get(c1).size() == targetComponent.get(c2).size() && targetComponent.get(c1).size()>1){
					System.out.println("EQUAL SETS result="+equalsSets(targetComponent.get(c1), targetComponent.get(c2)));
					System.out.print("\t");
					for(int i=0; i<targetComponent.get(c1).size(); i++){
						System.out.print(targetComponent.get(c1).get(i) + " ");
					}
					System.out.print("\n\t");
					for(int i=0; i<targetComponent.get(c2).size(); i++){
						System.out.print(targetComponent.get(c2).get(i) + " ");
					}
					System.out.println();
				}*/
				///////////////////////////////////////////////

				if(oper1.equalsIgnoreCase(oper2)){// && equalsSets(targetComponent.get(c1), targetComponent.get(c2))){	
					candidateOperations.remove(c2);
					targetComponent.remove(c2);
					if(!isProvided){
						provComponent.remove(c2);
						// Copy the classes in the list of the second operation into the list of the first operation
						for(int i=0; i<classesReqComponent.get(c2).size(); i++){
							classname = classesReqComponent.get(c2).get(i);
							if(!classesReqComponent.get(c1).contains(classname))
								classesReqComponent.get(c1).add(classname);
						}

						classesReqComponent.remove(c2);
					}
					duplicated = true;
				}
				else{
					c2++;
				}
			}
			if(!duplicated){
				c1++;
			}
		}
	}

	/**
	 * Add operations to the set of candidate operations considering
	 * those classes that are related by composition and generalization
	 * and provide public methods.
	 * @param candidateOperations Current set of candidate operations
	 * @param targetComponents Set of target component of each operation
	 * @param distribution The current distribution of classes into components
	 * */
	private void addRelatedOperations(ArrayList<TerminalNode> candidateOperations, 
			ArrayList<ArrayList<Integer>> targetComponents, ArrayList<Integer> provComponent, int [] distribution, 
			ArrayList<ArrayList<String>> classesReqComponent, boolean isProvided){
		//private void addRelatedOperations(ArrayList<TerminalNode> candidateOperations, ArrayList<Integer> targetComponent, int [] distribution){
		int numberOfClasses = this.classesList.size();
		String className, relatedClassName, operation2;
		String [] aux;
		int indexClass1, indexClass2;
		int numberOfServices = candidateOperations.size();
		MultiIntegerColumn column1, column2;
		int type;
		MultiIntegerValue values1;

		// For each candidate operation
		for(int i=0; i<numberOfServices; i++){

			// Get the class that implement the operation
			aux = candidateOperations.get(i).getSymbol().split(":");
			className = aux[0];
			column1 = (MultiIntegerColumn)this.relationshipsDataset.getColumnByName(className);
			indexClass1 = this.relationshipsDataset.getIndexOfColumn(column1);

			// For every operation extracted from the analysis model, check if it should be added to the interface
			for(int j=numberOfClasses; j<this.terminals.length; j++){

				aux = this.terminals[j].getSymbol().split(":");
				relatedClassName = aux[0];
				operation2 = aux[1];
				column2 = (MultiIntegerColumn)this.relationshipsDataset.getColumnByName(relatedClassName);
				indexClass2 = this.relationshipsDataset.getIndexOfColumn(column2);

				// Check if class1 and class2 have a relationship and they also belong to the same component
				if(distribution[indexClass1] == distribution[indexClass2] &&
						!(column1.getElement(indexClass2) instanceof InvalidValue)){

					// Get the set of relationships between them
					values1 = (MultiIntegerValue)column1.getElement(indexClass2);

					for(int k=0; k<values1.getSize(); k+=2){
						// Get the type of relationship
						type = values1.getValue(k);

						// For compositions and generalizations, add the public operation 
						// defined by class1 in the set of candidate operations
						if(type==4 || (type==5 && !isOperationDefinedByClass(indexClass1, operation2))){

							//	System.out.println("type: " + type + " class1: " + className + 
							//			" op1: " + operation1 + "class2: " + relatedClassName + " op2: " + operation2);
							candidateOperations.add(this.terminals[j]);
							targetComponents.add(targetComponents.get(i));
							if(!isProvided){
								provComponent.add(provComponent.get(i));
								ArrayList<String> auxList = new ArrayList<String>();
								for(int c=0; c<classesReqComponent.get(i).size(); c++){
									auxList.add(classesReqComponent.get(i).get(c));
								}
								classesReqComponent.add(auxList);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Add operations to the candidate set considering those operations
	 * that are provided by subclasses when the superclass has not public methods
	 * but it has external relationships.
	 * @param candidateOperations Set of candidate operations
	 * @param targetComponent Set of target components for the operations
	 * @param distribution The current distribution of classes into components
	 * @param component The current component for which this method should search new operations
	 * @param isProvided A flag indicating whether we are looking for provided or required services of the component
	 * */
	/*private void addRelatedOperationsByClass(ArrayList<TerminalNode> candidateOperations, 
			ArrayList<Integer> targetComponent, int [] distribution, int component, boolean isProvided){*/
	private void addRelatedOperationsByClass(ArrayList<TerminalNode> candidateOperations, 
			ArrayList<ArrayList<Integer>> targetComponent, ArrayList<Integer> provComponent, int [] distribution, 
			int component, ArrayList<ArrayList<String>> classesReqComponent, boolean isProvided){	
		UMLClass umlclass;
		ArrayList<String> subclasses;
		String classname1, aux;
		int classIndex;
		int nClasses = distribution.length;
		MultiIntegerValue values_ij, values_ji;
		Object oValue;

		// Check if a class that have external invocations does not provide any operation 
		// but it has subclasses that define public methods
		for(int i=0; i<nClasses; i++){
			if((isProvided && distribution[i] == component) || (!isProvided && distribution[i]!=component)){
				umlclass = this.classesList.get(i);
				classIndex = this.relationshipsDataset.getIndexOfColumn(this.relationshipsDataset.getColumnByName(umlclass.getName()));

				//System.out.println("Class: " + umlclass.getName() + " hasOper: " + umlclass.hasOperations()+
				//		" hasExternalRel: " + hasExternalRelationships(i, distribution, isProvided));

				if(!umlclass.hasOperations() && hasExternalRelationships(i, distribution, isProvided)){
					subclasses = findSubclassesInComponent(i, distribution);

					for(int j=0; j<subclasses.size(); j++){
						//System.out.println("\t Subclase: " + subclasses.get(j));
						classname1 = subclasses.get(j);

						for(int k=nClasses; k<this.terminals.length; k++){
							aux = this.terminals[k].getSymbol().split(":")[0];

							//System.out.println("\t\t Method: " + this.terminals[k].getSymbol());

							// An operation of the subclass has been found
							if(classname1.equalsIgnoreCase(aux)){

								// Provided operation, add the operation and the index of all the components that require it
								if(isProvided){

									// search all the classes that could require this operation
									for(int c=0; c<nClasses; c++){

										oValue = this.relationshipsDataset.getColumn(classIndex).getElement(c);
										if(!(oValue instanceof InvalidValue)){

											values_ij = (MultiIntegerValue)oValue;
											values_ji = (MultiIntegerValue)this.relationshipsDataset.getColumn(c).getElement(classIndex);

											for(int l=1; l<values_ij.getSize(); l+=2){
												//System.out.println("value_ji: " + values_ji.getValue(l) + " value_ij: " + values_ij.getValue(l));
												if(values_ij.getValue(l)==1 && values_ji.getValue(l)==0){

													int index = candidateOperations.indexOf(this.terminals[k]);
													if(index!=-1){
														targetComponent.get(index).add(distribution[c]);
													}
													else{
														candidateOperations.add(this.terminals[k]);
														targetComponent.add(new ArrayList<Integer>());
														targetComponent.get(candidateOperations.size()-1).add(distribution[c]);
													}

													/*candidateOperations.add(this.terminals[k]);
													targetComponent.add(distribution[c]);
													 */
													//System.out.println("class1: " + classname1 + " dist: " + distribution[subclassIndex] + " class2: " + this.relationshipsDataset.getColumn(c).getName() 
													//		+ " dist: "+ distribution[c] + " isProv: " + isProvided + " target: " + targetComponent.get(targetComponent.size()-1));
													break;

												}
											}
										}
									}
								}

								// Required operation, add the index of the component that requires it
								else{

									// search all the classes that could require this operation
									for(int c=0; c<nClasses; c++){

										oValue = this.relationshipsDataset.getColumn(classIndex).getElement(c);
										if(!(oValue instanceof InvalidValue)){

											values_ij = (MultiIntegerValue)oValue;
											values_ji = (MultiIntegerValue)this.relationshipsDataset.getColumn(c).getElement(classIndex);

											for(int l=1; l<values_ij.getSize(); l+=2){
												//System.out.println("value_ji: " + values_ji.getValue(l) + " value_ij: " + values_ij.getValue(l));
												// if the class c provides an operation of the other class and the latter belongs to the
												// current component, add the required service
												if(values_ij.getValue(l)==1 && values_ji.getValue(l)==0 && distribution[c] == component){

													int index = candidateOperations.indexOf(this.terminals[k]);
													if(index!=-1){
														targetComponent.get(index).add(distribution[classIndex]);
														classesReqComponent.get(index).add(this.relationshipsDataset.getColumn(c).getName());
													}
													else{
														// If it is the first time that the service appears, add the candidate service,
														// the required component and the provider
														candidateOperations.add(this.terminals[k]);
														targetComponent.add(new ArrayList<Integer>());
														targetComponent.get(candidateOperations.size()-1).add(distribution[classIndex]);
														provComponent.add(component);
														ArrayList<String> auxList = new ArrayList<String>();
														auxList.add(this.relationshipsDataset.getColumn(c).getName());
														classesReqComponent.add(auxList);
													}
												}
											}
										}
									}
									/*candidateOperations.add(this.terminals[k]);
										targetComponent.add(distribution[classIndex]);*/
								}

							}
						}
					}
				}
			}
		}
	}

	/**
	 * For a given class, check if it has external relationships that can be candidate services
	 * @param classIndex The index of the class in the dataset
	 * @param distribution The current distribution of classes into components
	 * @param isProvided A flag indicating whether we are looking for provided or required services of the component
	 * */
	private boolean hasExternalRelationships(int classIndex, int[] distribution, boolean isProvided){
		boolean result = false;

		MultiIntegerColumn column = (MultiIntegerColumn)this.relationshipsDataset.getColumn(classIndex);
		MultiIntegerValue value, value2;
		for(int i=0; i<column.getSize(); i++){
			if(distribution[classIndex] != distribution[i] && !(column.getElement(i) instanceof InvalidValue)){

				value = (MultiIntegerValue)column.getElement(i);
				value2 = (MultiIntegerValue)this.relationshipsDataset.getColumn(i).getElement(classIndex);

				for(int j=1; j<value.getSize(); j+=2){
					if((isProvided && value.getValue(j)==1 && value2.getValue(j)==0) ||
							(!isProvided && value.getValue(j)==1 && value2.getValue(j)==0)
							){ // not unidirectional
						result = true;
					}
				}
			}
		}
		return result;
	}

	/**
	 * For a given class, check if it has subclasses allocated in the same component
	 * @param classIndex The index of the class in the dataset
	 * @param distribution The current distribution of classes into components
	 * @return An array with the names of the subclasses
	 * */
	private ArrayList<String> findSubclassesInComponent(int classIndex, int[] distribution) {
		ArrayList<String> subclasses = new ArrayList<String>();
		MultiIntegerColumn column = (MultiIntegerColumn)this.relationshipsDataset.getColumn(classIndex);

		for(int i=0; i<column.getSize(); i++){
			if(!(column.getElement(i) instanceof InvalidValue)){
				if( ((MultiIntegerValue)column.getElement(i)).contains(5)
						&& distribution[classIndex] == distribution[i] ){
					subclasses.add(this.relationshipsDataset.getColumn(i).getName());
				}
			}
		}
		return subclasses;
	}

	/**
	 * Search required interfaces that match with the provided interface
	 * specified by a set of operations
	 * @param tree The tree structure that encodes the genotype
	 * @param index The index of the component that specifies the provided interface
	 * @param provOperations The set of operations in the provided interface
	 * @param compPos The positions of component symbols in the tree
	 * @param reqPos The positions of required interface symbols in the tree
	 * @param provPos The positions of provided interface symbols in the tree
	 * */
	private ArrayList<String> searchRequiredInterfaces(SyntaxTree tree, int index, ArrayList<String> provOperations, int [] compPos, int [] reqPos, int [] provPos){
		ArrayList<String> requiredInterfaces = new ArrayList<String>();
		int l,m;
		String comparedSymbol;
		int nReqInterface;
		ArrayList<String> reqOperations;
		int numberOfComponents = compPos.length;
		String aux;
		// Check the required interfaces in the rest of components to find
		// any required interface that matches with the provided interface
		for(int c=0; c<numberOfComponents; c++){
			if(c!=index){
				l=reqPos[c]+1;
				nReqInterface = 0;
				while(l<provPos[c]){
					comparedSymbol = tree.getNode(l).getSymbol();

					if(comparedSymbol.equalsIgnoreCase("interface")){
						nReqInterface++;
						reqOperations = new ArrayList<String>();
						for(m=l+1; tree.getNode(m).arity() == 0; m++){
							aux = tree.getNode(m).getSymbol();
							// remove the information of the set of classes that require the operation, which is different from one component to another
							aux = aux.substring(0,aux.indexOf("[")-1); 
							reqOperations.add(aux);
						}
						l = m;

						if(isSubset(reqOperations,provOperations) || isSubset(provOperations,reqOperations)){
							requiredInterfaces.add("component"+(c+1)+"-interface"+nReqInterface);
						}
					}
					else{
						l++;
					}
				}
			}
		}		
		return requiredInterfaces;
	}


	/**
	 * Check if a list is a subset of another list
	 * @param list1 the list that might be the subset
	 * @param list2 the list that might be the superset
	 * @return True if list1 is a subset of list2, false otherwise
	 * */
	private boolean isSubset(ArrayList<String> list1, ArrayList<String> list2){
		boolean result = true;
		boolean containsElement = false;
		String elem1;
		for(int i=0; result && i<list1.size(); i++){
			containsElement = false;
			elem1 = list1.get(i);
			for(int j=0; !containsElement && j<list2.size(); j++){
				if(elem1.equalsIgnoreCase(list2.get(j))){
					containsElement = true;
				}
			}
			if(!containsElement)
				result = false;
		}
		return result;
	}

	/**
	 * Check if an operation is defined by a class
	 * @param indexClass The index of the class
	 * @param operationName The name of the operation
	 * @return True if the class specifies this operation, false otherwise.
	 * */
	private boolean isOperationDefinedByClass(int indexClass, String operationName){
		boolean exists = false;

		ArrayList<UMLOperation> operations = this.classesList.get(indexClass).getOperations();
		for(int i=0; !exists && i<operations.size(); i++){
			if(operations.get(i).getName().equalsIgnoreCase(operationName)){
				exists = true;
			}
		}
		return exists;
	}

	/**
	 * 
	 * */
	private boolean equalsSets(ArrayList<Integer> set1, ArrayList<Integer> set2){
		boolean equals = true;
		int size1 = set1.size();
		int size2 = set2.size();
		int i,j, pos;
		ArrayList<Integer> aux = new ArrayList<Integer>();
		for(j=0; j<set2.size(); j++){
			aux.add(set2.get(j));
		}
		if(size1 != size2){
			equals = false;
		}
		else{
			i=0;
			while(equals && i<size1){
				pos = aux.indexOf(set1.get(i));
				if(pos==-1){
					equals = false;
				}
				else{
					aux.remove(pos);
					i++;
				}
			}
			/*for(int i=0; equals && i<size1; i++){
				if(!set2.contains(set1.get(i)))
					equals = false;
			}*/
		}
		//System.out.println("Set1: " + set1 + " Set2: " + set2 + " Equals: " + equals);
		return equals;
	}

	private ArrayList<Integer> searchReqComponents(int provClass, int [] distribution, ArrayList<String> classesReqOperation){
		ArrayList<Integer> components = new ArrayList<Integer>();
		int nClasses = distribution.length;
		MultiIntegerColumn column;
		MultiIntegerValue multi_class1, multi_class2;
		Object value;
		int size;
		String classname;

		column = (MultiIntegerColumn)this.relationshipsDataset.getColumn(provClass);
		for(int i=0; i<nClasses; i++){
			value = column.getElement(i);
			if(!(value instanceof InvalidValue)){
				multi_class1 = (MultiIntegerValue)value;
				multi_class2 = (MultiIntegerValue)this.relationshipsDataset.getColumn(i).getElement(provClass);
				size = multi_class1.getSize();

				for(int k=0; k<size; k+=2){
					if(distribution[provClass] != distribution[i] // class1 and class2 are allocated in different components
							&& multi_class1.getValue(k+1)==1 && multi_class2.getValue(k+1)==0){ // and class1 is invoked by class2
						if(!components.contains(distribution[i])){
							components.add(distribution[i]);
							classname = this.relationshipsDataset.getColumn(i).getName();
							if(!classesReqOperation.contains(classname))
								classesReqOperation.add(classname);
						}
					}
				}
			}
		}
		return components;
	}

	private String searchRequiredClasses(ArrayList<String> classesReqOperation, int currentComponent, int [] distribution){
		StringBuffer sb = new StringBuffer(" [ ");
		int index;
		String classname;
		for(int i=0; i<classesReqOperation.size(); i++){
			classname = classesReqOperation.get(i);
			index = this.relationshipsDataset.getIndexOfColumn(this.relationshipsDataset.getColumnByName(classname));
			if(distribution[index] == currentComponent){
				sb.append(classname + " ");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
