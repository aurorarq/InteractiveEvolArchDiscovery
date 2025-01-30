package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;
import java.util.List;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.discovery.TreeCreator;


/**
 * Tree creator for the discovery problem for the interactive version. 
 * It controls that every created individual represents
 * a different architectural solution.
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (June 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * @see TreeCreator
 * */
public class InteractiveMOTreeCreator extends TreeCreator {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -1970408597117610590L;

	/////////////////////////////////////////////////////////////////
	// -------------------------------------------....-- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public InteractiveMOTreeCreator(){
		super();
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	public List<IIndividual> provide(int numberOfIndividuals) {
		// Set numberOfIndividuals
		this.numberOfIndividuals = numberOfIndividuals;
		// Result list
		createdBuffer = new ArrayList<IIndividual> (numberOfIndividuals);
		// Prepare process
		prepareCreation();
		// Provide individuals
		createdCounter = 0;
		/*IMOIndividual newInd, ind;
		boolean equivalent;*/

		while (createdCounter<numberOfIndividuals) {
			// add a new individual to the buffer
			createNext();
			
			//////////////////// testing
			/*System.out.println("\nCurrent buffer: size="+createdBuffer.size() + " counter=" + createdCounter);
			for(IIndividual i: createdBuffer){
				System.out.println(i.toString());
			}*/
			
			// check if the added individual is equivalent to other individual within the buffer
			/*
			newInd = (IMOIndividual)createdBuffer.get(createdBuffer.size()-1);
			equivalent = false;
			for(int i=0; !equivalent && i<createdBuffer.size()-1; i++){
				ind = (IMOIndividual)createdBuffer.get(i);
				// an equivalent individual exists, remove the new individual from the buffer
				if(newInd.isEquivalent(ind)){
					createdBuffer.remove(createdBuffer.size()-1);
					equivalent = true;
				}
			}
			// a different individual, the counter is incremented
			if(!equivalent)
			*/
			createdCounter++;
			
			//System.out.println("--------------------------------\nNew individual: equivalent="+equivalent);
			//System.out.println(newInd.toString());
		}
		// Returns result
		return createdBuffer;
	}
}