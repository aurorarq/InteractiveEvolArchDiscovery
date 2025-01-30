package net.sf.jclec.sbse.discovery.imo;

public class PreferredRegion {

	private int h; // number of interaction
	
	private double [][] weightPreferredRegion; // rows=#objectives, columns=2 (low bound, upper bound)
	
	private double territorySize;
	
	public PreferredRegion(int index, double [][] weights, double territorySize){
		this.h = index;
		this.weightPreferredRegion = weights;
		this.territorySize = territorySize;
	}
	
	public int getIndex(){
		return this.h;
	}
	
	public double [][] getWeights(){
		return this.weightPreferredRegion;
	}
	
	public double [] getWeigths(int index){
		return this.weightPreferredRegion[index];
	}
	
	public double getTerritorySize(){
		return this.territorySize;
	}
	
	public void setTerritorySize(double t){
		this.territorySize = t;
	}
}
