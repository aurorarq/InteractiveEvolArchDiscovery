package net.sf.jclec.sbse.discovery.control;

import java.io.File;
/*import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
*/
import net.sf.jclec.ExperimentBuilder;
import net.sf.jclec.IAlgorithm;
import net.sf.jclec.IConfigure;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * This class overrides the launch of experiments
 * in JCLEC, allowing to initialize it with a 
 * directory of XML configurations. For experimentation
 * purposes.
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class LaunchExecutions {

	/**
	 * Main method
	 * 
	 * @param args Configuration File
	 */
	public static void main(String[] args) 
	{
		// Expand base configuration with mutation combinations
		/*String dir = "cfg";
		File directory = new File(dir);
		if(!directory.exists()){
			directory.mkdirs();
		}
		else{
			for(File f: directory.listFiles()){
				f.delete();
			}
		}
		createConfigurations(args[0]);
		*/
		
		// First arg must be a directory name
		ExperimentBuilder builder = new ExperimentBuilder();

		System.out.println("Initializing job");

		String files [] = new File(args[0]).list();

		// Expand the processes and execute them
		for(String f: files ){
			System.out.println("File: " + f);
			for(String experiment : builder.buildExperiment(args[0]+"/"+f))
			{
				System.out.println("Algorithm started");
				executeJob(experiment);
				System.out.println("Algorithm finished");
			}
		}
		System.out.println("Job finished");
	}

	/**
	 * Execute experiment
	 * 
	 * @param jobFilename
	 */

	@SuppressWarnings("unchecked")
	private static void executeJob(String jobFilename) 
	{	
		// Try open job file
		File jobFile = new File(jobFilename);		
		if (jobFile.exists()) {
			try {
				// Job configuration
				XMLConfiguration jobConf = new XMLConfiguration(jobFile);
				// Process header
				String header = "process";
				// Create and configure algorithms
				String aname = jobConf.getString(header+"[@algorithm-type]");
				Class<IAlgorithm> aclass = (Class<IAlgorithm>) Class.forName(aname);
				IAlgorithm algorithm = aclass.newInstance();
				// Configure runner
				if (algorithm instanceof IConfigure) {
					((IConfigure) algorithm).configure(jobConf.subset(header));
				}
				// Execute algorithm runner
				algorithm.execute();
			}
			catch (ConfigurationException e) {
				System.out.println("Configuration exception ");
			}			
			catch (Exception e) {
				e.printStackTrace();
			}			
		}
		else {
			System.out.println("Job file not found");
			System.exit(1);			
		}
	}
/*
	@SuppressWarnings("resource")
	private static void createConfigurations(String cfgBaseName){

		String ext = ".xml";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(cfgBaseName+ext));
			String line = reader.readLine();
			StringBuffer bufferInit = new StringBuffer();
			StringBuffer bufferEnd = new StringBuffer();
			StringBuffer buffer;
			FileWriter writer;
			//System.out.println(line);
			// Copy first part of configuration
			while(!line.contains("<listener")){
				bufferInit.append(line + "\n");
				line=reader.readLine();
			}

			// Copy listener configuration
			while(line!=null){
				bufferEnd.append(line + "\n");
				line=reader.readLine();
			}

			// Set mutation configuration
			double probs [] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6};
			String name = "";

			double d0, d1, d2, d3,d4;
			for(int i=0; i<probs.length; i++){
				for(int j=0; j<probs.length; j++){
					for(int k=0; k<probs.length; k++){
						for(int l=0; l<probs.length; l++){
							d0=probs[i];
							d1=probs[j];
							d2=probs[k];
							d3=probs[l];
							d4=1.0-d0-d1-d2-d3;
							d4=(double)(Math.round((d4*100)))/100;
							if(d4>=0.1){

								buffer = new StringBuffer();

								//	System.out.println("p0: " + d0 + " p1: " + d1 + " p2: " + d2 +
								//" p3: " + d3 + " p4: " + d4);
								//	nCombs++;

								buffer.append(bufferInit.toString()); // Copy the beginning of configuration
								
								buffer.append("\t\t<base-mutator type=\"net.sf.jclec.sbse.cl2cmp.CmpBaseMutator\" probability-invalids=\"true\">\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mutknow.AddComponentMutator\" random=\"false\" weight=\"" + d0 + "\"/>\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mutknow.RemoveComponentMutator\" random=\"false\" weight=\"" + d1 + "\"/>\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mutknow.MergeComponentsMutator\" random=\"false\" weight=\"" + d2 + "\"/>\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mutknow.SplitComponentMutator\" random=\"false\" weight=\"" + d3 + "\"/>\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mutknow.MoveClassMutator\" random=\"false\" weight=\"" + d4 + "\"/>\n");
								buffer.append("\t\t</base-mutator>\n");

								buffer.append(bufferEnd.toString()); // Copy the end of configuration

								name = "cfg/" + cfgBaseName + "-" + (int)(d0*100) + "-" + (int)(d1*100) + "-" 
										+ (int)(d2*100) + "-" + (int)(d3*100) + "-" + (int)(d4*100) + ext;

								// Write file
								writer = new FileWriter(name);
								writer.flush();
								writer.write(buffer.toString());
								writer.close();

								// Clear buffer
								buffer = null;
							}
						}
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
}
