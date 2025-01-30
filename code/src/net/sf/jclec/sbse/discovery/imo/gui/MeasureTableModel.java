package net.sf.jclec.sbse.discovery.imo.gui;

import javax.swing.table.AbstractTableModel;

import net.sf.jclec.mo.evaluation.fitness.MOFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;

public class MeasureTableModel extends AbstractTableModel {

	/** Serial ID */
	private static final long serialVersionUID = -6721557154229122227L;

	private String [] columnNames;
	
	private Object [][] data;
	
	public MeasureTableModel(InteractiveMOIndividual individual){
		setColumnNames(individual.getMeasuresNames());
		setTableData(individual);
	}
	
	/////////////////////////////////////////////////////////
	
	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int getRowCount() {
		return this.data.length;
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		return this.data[arg0][arg1];
	}

	/////////////////////////////////////////////////////////
	
	public String [] getColumnNames(){
		return this.columnNames;
	}
	
	public Object[][] getData(){
		return this.data;
	}
	/////////////////////////////////////////////////////////
	
	private void setColumnNames(String [] columnNames){
		int length = columnNames.length;
		//this.columnNames = new String [length];
		this.columnNames = new String [length+1];
		this.columnNames[0] = "Element";
		String suffix; // suffix (type of measure for component: min, max, equal)
		for(int i=0; i<length; i++){
			
			if(columnNames[i].equals("icd") || columnNames[i].equals("abs") || columnNames.equals("enc"))
				suffix = " (max)";
			else if(columnNames[i].equals("cb"))
				suffix = " (equal)";
			else
				suffix = " (min)";
	
			this.columnNames[i+1] = columnNames[i] + suffix;
					
			//this.columnNames[i] = columnNames[i];
		}
		//this.columnNames = new String[2];
		//this.columnNames[0] = "column 1";
		//this.columnNames[1] = "column 2";
	}
	
	private void setTableData(InteractiveMOIndividual individual){
		
		int nrows = individual.getNumberOfComponents()+1;
		int ncols = individual.getMeasuresNames().length+1;
		int i,j;
		
		this.data = new Object [nrows][ncols];
		
		for(i=0; i<nrows-1; i++){
			this.data[i][0] = "Component-"+(i+1);
			for(j=1; j<ncols; j++){
				this.data[i][j] = individual.getComponentMeasure(j-1, i);
			}
		}
		this.data[i][0] = "Architecture";
		for(j=1; j<ncols; j++){
			try{
				this.data[i][j] = ((MOFitness)individual.getFitness()).getObjectiveDoubleValue(j-1);
			}catch(Exception e){
				this.data[i][j] = -1;
			}
		}
	}
}
