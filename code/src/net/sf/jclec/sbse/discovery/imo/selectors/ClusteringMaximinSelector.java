package net.sf.jclec.sbse.discovery.imo.selectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jclec.IIndividual;
import net.sf.jclec.ISystem;
import net.sf.jclec.mo.distance.EuclideanDistance;
import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.sbse.discovery.imo.comparators.IndividualMaximinComparator;

/**
 * Selector based on clustering
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (June 2015)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class ClusteringMaximinSelector extends InteractiveSelector {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////
	
	/** Serial ID */
	private static final long serialVersionUID = 1112140671329474555L;

	/** Clusters */
	protected ArrayList<ArrayList<IIndividual>> clusters;

	/** Center of each cluster */
	protected double [] centers;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public ClusteringMaximinSelector() {
		super();
	}

	/**
	 * Parameterized constructor
	 * @param context Execution context
	 * */
	public ClusteringMaximinSelector(ISystem context){
		super(context);
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	protected void prepareSelection() {
		this.icomparator = new IndividualMaximinComparator();
		Collections.sort(this.actsrc, icomparator);
	}

	@Override
	public List<IIndividual> select(List<IIndividual> source, int nofsel) {

		EuclideanDistance edist = new EuclideanDistance();
		double distance, minDistance;
		IIndividual ind;
		int index = -1;

		// Sets source set and size
		this.actsrc = source; 
		this.actsrcsz = source.size();

		// Prepare selection process
		prepareSelection();

		// Choose the best 'nofsel' individuals as centers of the clusters
		this.clusters = new ArrayList<ArrayList<IIndividual>>();
		ArrayList<IIndividual> c;
		for(int i=0; i<nofsel; i++){
			c = new ArrayList<IIndividual>();
			c.add(this.actsrc.get(i));
			this.clusters.add(c);
		}

		// Do one iteration of clustering
		for(int i=nofsel; i<actsrcsz; i++){
			ind = actsrc.get(i);
			minDistance = Double.POSITIVE_INFINITY;
			index = -1;
			// get the minimum distance to the centers of the clusters
			for(int j=0; j<nofsel; j++){
				distance = edist.distance(this.clusters.get(j).get(0), ind);
				if(distance < minDistance){
					index = j;
					minDistance = distance;
				}
			}

			// add the individual to the selected cluster
			this.clusters.get(index).add(ind);
		}

		// Obtain the new centers
		this.centers = new double[nofsel];
		double acc;
		int size;
		for(int i=0; i<nofsel; i++){
			size = clusters.get(i).size();
			acc = 0.0;
			for(int j=0; j<size; j++){
				acc += ((InteractiveMOFitness)clusters.get(i).get(j).getFitness()).getDominanceValue();
			}
			this.centers[i] = acc/(double)size;
		}
		
		///////////////////////////////// TESTING
		String [] names = ((InteractiveMOIndividual)source.get(0)).getMeasuresNames();
		int n = names.length;
		int [] metricsFitness = new int[n];
		int metricIndex;
		for(int i=0; i<clusters.size(); i++){
			System.out.println("\nCluster " +i + " center (maximin)=" + centers[i]);
			
			for(int j=0 ; j<metricsFitness.length; j++){
				metricsFitness[j] = 0;
			}
			
			for(int j=0; j<clusters.get(i).size(); j++){
				metricIndex = ((InteractiveMOFitness) ((InteractiveMOIndividual)clusters.get(i).get(j)).getFitness()).getMetricIndex();
				metricsFitness[metricIndex]++;
			}
			
			for(int j=0; j<metricsFitness.length; j++){
				System.out.print(names[j] + " : " + metricsFitness[j] + " ");
			}
		}
		System.out.println();
		////////////////////////////////////////////

		// Performs selection of n individuals
		ArrayList<IIndividual> result = new ArrayList<IIndividual>();
		IIndividual selected;
		double value;

		for (int i=0; i<nofsel; i++) {
			// get the individual closer to the center of the cluster
			size = clusters.get(i).size();
			minDistance = Double.POSITIVE_INFINITY;
			index = -1;
			for(int j=0; j<size; j++){
				value = ((InteractiveMOFitness)clusters.get(i).get(j).getFitness()).getDominanceValue();
				distance = Math.abs(this.centers[i] - value);
				if(distance<minDistance){
					minDistance = distance;
					index = j;
				}
			}
			selected = clusters.get(i).get(index);
			result.add(selected);
		}

		////////////////////////// TESTING
		int nObjs = ((MOFitness) result.get(0).getFitness()).getNumberOfObjectives();
		InteractiveMOFitness fitness;
		try{
			System.out.println();
			for(IIndividual individual: result){
				fitness = (InteractiveMOFitness)individual.getFitness();
				System.out.print("Objectives: ");
				for(int i=0; i<nObjs; i++){
					System.out.print(fitness.getObjectiveDoubleValue(i) + " ");
				}
				System.out.println(" Maximin: " + fitness.getDominanceValue() + " Metric: " 
						+ ((InteractiveMOIndividual)individual).getMeasuresNames()[fitness.getMetricIndex()]
						+ " Number of components: " + ((InteractiveMOIndividual)individual).getNumberOfComponents());
			}
			System.out.println();
		}catch(Exception e){
			e.printStackTrace();
		}

		// Returns selection
		return result;
	}

	@Override
	protected IIndividual selectNext() {
		return null;
	}
}