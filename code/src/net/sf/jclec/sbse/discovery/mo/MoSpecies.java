package net.sf.jclec.sbse.discovery.mo;

import net.sf.jclec.sbse.discovery.Species;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.syntaxtree.SyntaxTreeIndividual;

/**
 * Species for the discovery problem
 * formulated as a multi-objective problem.
 * 
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * 
 * @version 1.0
 * History:
 * <ul>
 * 	<li>1.0: Creation (December 2013)
 * </ul>
 * */
public class MoSpecies extends Species {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////
	
	/** Serial ID */
	private static final long serialVersionUID = 6297845260447280385L;

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public SyntaxTreeIndividual createIndividual(SyntaxTree genotype) {
		return new MoIndividual(genotype);
	}
}
