package net.sf.jclec.sbse.discovery.imo.exp;

import java.io.File;
import java.io.IOException;

import es.uco.kdis.datapro.algorithm.preprocessing.discretization.MDLPDiscretize;
import es.uco.kdis.datapro.algorithm.preprocessing.instance.RemoveDuplicates;
import es.uco.kdis.datapro.dataset.column.CategoricalColumn;
import es.uco.kdis.datapro.dataset.source.CsvDataset;
import es.uco.kdis.datapro.exception.IllegalFormatSpecificationException;
import es.uco.kdis.datapro.exception.NotAddedValueException;

public class PostProcessDiscretization {

	public static void main(String[] args) {

		String dirname = "";
		String resname = "";
		CsvDataset dataset;
		MDLPDiscretize discrAlg;
		RemoveDuplicates removeAlg;
		CategoricalColumn column;
		File dir = new File(dirname);
		File [] files = dir.listFiles();
		int size;
		
		for(int i=0; i<files.length; i++){
			
			// Open dataset
			dataset = new CsvDataset(files[i].getAbsolutePath());
			try {
				dataset.readDataset("nv","fff"); // three objectives
			} catch (IndexOutOfBoundsException | IOException | NotAddedValueException
					| IllegalFormatSpecificationException e1) {
				e1.printStackTrace();
			}
			
			
			// Add a categorical column at the end (it is necessary in MDLP algorithm)
			size = dataset.getColumn(0).getSize();
			column = new CategoricalColumn("class");
			column.addCategory("a");
			for(int j=0; j<size; j++){
				column.addValue("a");
			}
			dataset.addColumn(column);
			
			// Remove duplicates
			removeAlg = new RemoveDuplicates(dataset);
			removeAlg.initialize();
			removeAlg.execute();
			removeAlg.postexec();
			dataset = (CsvDataset) removeAlg.getResult();
			
			// Apply discretization
			discrAlg = new MDLPDiscretize(dataset);
			discrAlg.initialize();
			discrAlg.execute();
			discrAlg.postexec();
			dataset = (CsvDataset)discrAlg.getResult();
						
			// Write dataset
			try{
			dataset.writeDataset(resname+"/"+files[i].getName()+".csv");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
