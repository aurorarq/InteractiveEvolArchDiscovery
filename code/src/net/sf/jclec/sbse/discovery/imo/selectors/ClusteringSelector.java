package net.sf.jclec.sbse.discovery.imo.selectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import net.sf.jclec.IIndividual;
import net.sf.jclec.ISystem;


/**
 * Selector based on KMeans++ algorithm
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
 * 
 * */

public class ClusteringSelector extends InteractiveSelector{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -3022956424115533019L;

	/** Algorithm */
	protected KMeansPlusPlusClusterer<IndividualClusterable> algorithm;

	/** Input in the correct format */
	protected List<IndividualClusterable> input;

	/** Clusters found*/
	protected List<CentroidCluster<IndividualClusterable>> output;

	/** Number of iterations */
	protected int numberIterations = 10;

	/** Cluster to select from */
	protected int currentCluster;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public ClusteringSelector() {
		super();
	}

	/**
	 * Parameterized constructor
	 * @param context Execution context
	 * */
	public ClusteringSelector(ISystem context){
		super(context);
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	protected void prepareSelection() {

		// Copy the individuals
		this.input = new ArrayList<IndividualClusterable>(getIndividualsToSelectFrom().size());
		for(IIndividual ind: getIndividualsToSelectFrom()){
			this.input.add(new IndividualClusterable(ind));
		}

		// Initialize the algorithm
		this.algorithm = new KMeansPlusPlusClusterer<IndividualClusterable>(getNumberToSelect(),numberIterations);

		// Execute the algorithm
		this.output = this.algorithm.cluster(this.input);

		// Current cluster
		this.currentCluster = 0;
	}

	@Override
	protected IIndividual selectNext() {

		double [] center;
		double distance, minDistance;
		int index, size;
		IndividualClusterable ind;
		IIndividual selected = null;

		/* CASE 1: SELECT THE INDIVIDUAL CLOSER TO THE CENTER */

		// get the center
		center = this.output.get(currentCluster).getCenter().getPoint();

		// search the individual closer to the center
		minDistance = Double.POSITIVE_INFINITY;
		index = -1;
		size = this.output.get(this.currentCluster).getPoints().size();

		// TODO
		if(size > 0){
			for(int j=0; j<size; j++){
				ind = this.output.get(this.currentCluster).getPoints().get(j);
				distance = euclideanDistance(center, ind.getPoint());
				if(distance<minDistance){
					minDistance = distance;
					index = j;
				}
			}

			selected = ((IndividualClusterable)this.output.get(this.currentCluster).getPoints().get(index)).getIndividual();


			/* CASE 2: ONE INDIVIDUAL AT RANDOM FOR EACH CLUSTER */
			/*index = this.randgen.choose(this.output.get(currentCluster).getPoints().size());
			selected = this.output.get(currentCluster).getPoints().get(index).getIndividual();
			 */
		}
		// prepare next cluster
		this.currentCluster++;
		return selected;
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Euclidean distance between two points
	 * @param point1 First point
	 * @param point2 Second point
	 * @return Euclidean distance
	 * */
	private double euclideanDistance(double [] point1, double [] point2){
		double distance = 0;
		int nc = point1.length;
		for(int i=0; i<nc; i++){
			distance += (point1[i] - point2[i])*(point1[i] - point2[i]);
		}
		distance = Math.sqrt(distance);
		return distance;
	}
}