package net.sf.jclec.sbse.discovery.control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;

import net.sf.jclec.IConfigure;
import net.sf.jclec.IIndividual;
import net.sf.jclec.mo.strategy.MOStrategyContext;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOFitness;
import net.sf.jclec.sbse.discovery.imo.InteractiveMOIndividual;
import net.sf.jclec.sbse.discovery.imo.gui.DiagramTreeModel;
import net.sf.jclec.sbse.discovery.imo.gui.InteractionView;
import net.sf.jclec.sbse.discovery.imo.gui.MeasureTableModel;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.*;
import net.sf.jclec.sbse.discovery.imo.preferences.*;
import net.sf.jclec.sbse.discovery.imo.selectors.InteractiveSelector;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Controller for the interaction in the discovery problem.
 * 
 * <p>HISTORY:
 * <ul>
 * 	<li>1.0: Creation (March 2015)
 * </ul>
 *  
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class InteractionController implements Observer, IConfigure{

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -2690462577828391132L;

	/** Execution context (linkage with the algorithm) */
	private MOStrategyContext context;

	/** View */
	private InteractionView view;

	/** Selector mechanism */
	private InteractiveSelector selector;

	/** Number of individuals to be selected */
	private int nofsel;

	/** List of preferences (names to be shown in the menu) */
	private String [] preferences = new String[]{"No preference", "Best component", "Best provided interface", "Worst component", "Worst provided interface", "Measure in range", "Number of components", "Aspiration levels (all measures)"}; // ALL
	//private String [] preferences = new String[]{"No preference", "Best component", "Best provided interface"}; // MAEB

	/** List of preferences (extended information when the preference is chosen) */
	private String [] preferenceLabels = new String[]{
			"No information will be added",
			"Choose the best component (set of classes):",
			"Choose the best provided interface (set of operations):",
			"Choose the worst component (set of classes):",
			"Choose the worst provided interface (set of operations):",
			"Choose the metric and set the desired range of values:",
			"Choose the most suitable number of components:",
			"Specify a trade-off among metrics:"
	};
	/*private String [] preferenceLabels = new String[]{
			"No information will be added",
			"Choose the best component (set of classes):",
			"Choose the best provided interface (set of operations):"
	};*/

	/** Flag indicating whether the evaluation of the individual has finished */
	private boolean finishIndividual = false;

	/** Flag indicating that the user wants to stop the search */
	private boolean finishSearch = false;

	/** Flag indicating that the user wants to store the solution in the archive */
	private boolean storeSolutionInArchive = false;

	/** Flag indicating that the users wants to remove the solution from the population */
	private boolean removeSolution = false;

	/** Index of the selected element (component, interface...) */
	private int selectedElement = 0;

	/** Index of the frozen component */
	private int frozenComponent = -1;

	/** Type of preference */
	private int selectedPreference = -1;

	/** Index of the selected interface */
	//private int selectedInterface = 0;

	/** Selected confidence level */
	private int selectedConfidenceLevel = 3;

	/** Minimum number of components */
	private int minNumComponents;

	/** Maximum number of components */
	private int maxNumComponents;

	/** Metric names */
	private String [] metricNames;

	/** Minimum value for 'measure in range' preference */
	private double metricMinValue = -1.0;

	/** Maximum value for 'measure in range' preference */
	private double metricMaxValue = -1.0;

	/** Weights for aspiration levels */
	private double [] weights;

	/** Aspiration levels */
	private double [] refPoint;

	/** Evaluation time (start) */
	private long startingTime;

	/** Evaluation time */
	private long evaluationTime;

	// LOG

	/** Maximum evaluation time */
	private long maxEvalTime;

	/** User identification */
	private int id;

	/** Writer */
	private FileWriter fwriter;

	/** Log Directory */
	private String directory = "log";

	/** Log file */
	private String filename;

	/** String buffer to record events */
	private StringBuffer sb = new StringBuffer();

	/** Time in date format*/
	//private Calendar date = new GregorianCalendar();

	/** Time to evaluate is over */
	private boolean timeIsOver;

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private boolean useTimeCounter;

	private boolean userFinishReport;

	/** Use log file? */
	private boolean log;

	private int previousPreference;

	private boolean errorDialog;

	private boolean startInteraction;

	boolean numericError;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Parameterized constructor
	 * @param context The execution context
	 * */
	public InteractionController(MOStrategyContext context) {
		super();
		this.context = context;
		view = new InteractionView(this);
	}

	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Get/set methods
	/////////////////////////////////////////////////////////////////

	public void setNumberOfSolutions(int n){
		this.nofsel = n;
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Configuration method
	 * */
	@Override
	public void configure(Configuration settings) {
		// Interactive selector
		try{
			String classname = 
					settings.getString("interactive-selector[@type]");
			@SuppressWarnings("unchecked")
			Class<? extends InteractiveSelector> selectorClass = 
			(Class<? extends InteractiveSelector>) Class.forName(classname);
			this.selector = selectorClass.newInstance();
			this.selector.contextualize(context);
		}catch (ClassNotFoundException e) {
			throw new ConfigurationRuntimeException("Illegal interactive selector classname");
		} 
		catch (InstantiationException|IllegalAccessException e) {
			throw new ConfigurationRuntimeException("Problems creating an instance of interactive selector", e);
		}

		// Number of individuals to be selected, 1 by default
		//this.nofsel = settings.getInt("number-to-select",1);

		/** Maximum evaluation time (in seconds) */
		this.maxEvalTime = settings.getLong("max-eval-time", Long.MAX_VALUE);
		if(this.maxEvalTime != Long.MAX_VALUE)
			this.maxEvalTime *= 1000; // seconds to milliseconds

		/** Use time counter? */
		this.useTimeCounter = settings.getBoolean("use-time-counter",false);

		/** User id (optional) */
		this.id = settings.getInt("user-id",-1);
		if(this.id != -1){
			log = true;
			try {
				File dir = new File(directory);
				this.filename = directory+"/user-"+id+".txt";
				File file = new File(filename);
				if(!dir.exists()){
					dir.mkdirs();
					file.createNewFile();
					this.fwriter = new FileWriter(file);
				}
				else{
					this.fwriter = new FileWriter(file, true);
				}

				this.sb = new StringBuffer();
				this.fwriter.append("\nUSER: " + this.id + " TIMESTAMP: " + getTimeStamp() + "\n");
				this.fwriter.write(this.sb.toString());
				this.fwriter.flush();
				this.fwriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			log = false;
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Interactive iteration
	 * */
	public List<ArchitecturalPreference> interactiveInteraction(List<IIndividual> population, List<IIndividual> archive, int maxArchiveSize,
			List<ArchitecturalPreference> preferences, int generation, double meanFitness, double stdFitness, double [] distribution, double percentageInvalids, boolean noPreferencesYet,
			int minComponents, int maxComponents){

		this.minNumComponents = minComponents;
		this.maxNumComponents = maxComponents;

		// First, select the individuals to be shown
		List<IIndividual> selectedInds = selectIndividuals(population, archive, maxArchiveSize,noPreferencesYet,generation);

		List<ArchitecturalPreference> newPreferences = new ArrayList<ArchitecturalPreference>();
		if(preferences != null)
			newPreferences.addAll(preferences);
		for(int i=0; i<newPreferences.size(); i++){
			newPreferences.get(i).setAddedInLastInteraction(false); // update
		}
		ArchitecturalPreference preference;

		// Wait user response
		startInteraction = false;
		waitUserSelection();

		// LOG
		// Open device
		if(log){

			// Statistics
			this.sb.append("\n\nTIMESTAMP: " + getTimeStamp() + " EVENT: Interaction in generation " + generation);
			this.sb.append("\n\tStatistics: \n\t\tAverage fitness in population: " + meanFitness + "\n\t\tFitness deviation in population: " + stdFitness 
					+ "\n\t\tPercentage infeasible solutions: " + percentageInvalids + "\n\t\tSolutions in archive: " + archive.size() + "\n\t\tNumber of preferences: " + preferences.size());
			this.sb.append("\n\t\tDistribution of solutions (from " + this.minNumComponents + " to " + this.maxNumComponents + " components): ");
			for(int i=0; i<distribution.length; i++){
				sb.append(distribution[i] + " ");
			}
			sb.append("\n");
			writeLog();
		}

		// For each individual, create the view and wait for user response
		int nObjectives = ((InteractiveMOFitness)population.get(0).getFitness()).getNumberOfObjectives();
		this.finishSearch = false;
		int size = selectedInds.size();
		for(int i=0; !finishSearch && i<size; i++){

			if(selectedInds.get(i) != null){
				// clean flags/default values
				timeIsOver = false;
				userFinishReport = false;
				finishIndividual = false;
				removeSolution = false;
				storeSolutionInArchive = false;
				selectedElement = 0;
				previousPreference = -1;
				frozenComponent = -1;
				selectedPreference = 0;
				selectedConfidenceLevel = 3;
				this.metricMinValue = 0;
				this.metricMaxValue = 1;
				this.weights = null;
				this.refPoint = null;
				this.weights = new double[nObjectives];
				this.refPoint = new double[nObjectives];
				for(int j=0; j<weights.length; j++){
					this.weights[j] = 1.0;
					this.refPoint[j] = 0.0;
				}

				// Log
				if(log){
					this.sb.append("\n\nTIMESTAMP: " + getTimeStamp() + " EVENT: Evaluating solution #" + (i+1));
					this.sb.append("\n\nSolution:" + selectedInds.get(i).toString()+"\n");
					writeLog();
					this.startingTime = System.currentTimeMillis();
					this.evaluationTime = -1;
				}

				createView((InteractiveMOIndividual)selectedInds.get(i), generation, meanFitness, stdFitness, distribution, percentageInvalids);

				if(!finishSearch && !timeIsOver){

					this.evaluationTime = System.currentTimeMillis() - this.startingTime;

					waitUserResponse();

					// process user response
					preference = processUserResponse((InteractiveMOIndividual)selectedInds.get(i));

					if(preference!=null){
						preference.setGeneration(context.getGeneration());
						preference.setAddedInLastInteraction(true);
						newPreferences.add(preference);
					}
				}

				if(log){
					if(i==size-1)
						waitUserReport(true);
					else
						waitUserReport(false);
				}
				view.close();
			}
		}

		// wait to close output device
		/*if(finishSearch){
			try {
				if(log)
					this.fwriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		// LOG close device
		/*if(log){
			try {
				this.fwriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/

		//System.out.println("Number prefs (interaction controller)="+newPreferences.size());
		return newPreferences;
	}

	public boolean userStopSearch(){
		return this.finishSearch;
	}

	public void stopInteraction(){
		view.dispose();
	}

	/////////////////////////////////////////////////////////////////
	// ---------------------------------- Observable/Observer pattern
	/////////////////////////////////////////////////////////////////

	/**
	 * This method captures user's events
	 * */
	public void update(final Observable obs, final Object arg){
		if(obs instanceof StopSearchButtonListener){
			if(!this.errorDialog){
				this.finishSearch = (Boolean)arg;
				if(this.finishSearch && log){
					sb.append("\nTIMESTAMP: " + getTimeStamp() + " EVENT: User stops search.");
					writeLog();
				}
			}
		}
		else if(obs instanceof FinishIndividualButtonListener){
			if(!this.errorDialog)
				this.finishIndividual = (Boolean)arg;
		}
		else if(obs instanceof ComponentButtonListener){
			switch(selectedPreference){
			case 0: break;	// No selection yet
			case 1: break; // No preference
			case 2: this.selectedElement = (Integer)arg; break; // Best component
			case 3: this.selectedElement = (Integer)arg; break; // Best interface
			case 4: this.selectedElement = (Integer)arg; break; // Worst component
			case 5: this.selectedElement = (Integer)arg; break; // Worst interface
			case 6: this.selectedElement = (Integer)arg; break; // Metric index
			case 7: this.selectedElement = this.minNumComponents+(Integer)arg; break; // Ideal number of components
			case 8: break; // Aspiration levels are handled by other listener
			}
		}
		else if(obs instanceof FrozenComponentButtonListener){
			this.frozenComponent = (Integer)arg;
		}
		else if(obs instanceof ListPreferencesListener){
			this.selectedPreference = (Integer)arg;
		}
		else if(obs instanceof ConfidenceComponentButtonListener){
			this.selectedConfidenceLevel = (Integer)arg;
		}
		else if(obs instanceof AddToArchiveButtonListener){
			this.storeSolutionInArchive = (Boolean)arg;
		}
		else if(obs instanceof RemoveSolutionButtonListener){
			this.removeSolution = (Boolean)arg;
		}
		else if(obs instanceof TextFieldListener){
			if(arg==null)
				numericError=true;
			else{
				if(((TextFieldListener)obs).isMinValue()){
					try{
						this.metricMinValue = Double.valueOf((String)arg);
						numericError=false;
					}catch(Exception e){
						numericError = true;
					}
				}
				else{
					try{
						this.metricMaxValue = Double.valueOf((String)arg);
						numericError=false;
					}catch(Exception e){
						numericError = true;
					}
				}
			}
		}
		else if(obs instanceof ReferencePointTextFieldListener){
			if(arg==null){
				numericError=true;
			}
			else{
				String [] values = ((String)arg).split(",");
				numericError = false;
				if(values.length<metricNames.length)
					numericError=true;
				else{
					if(((ReferencePointTextFieldListener)obs).isPoint()){

						for(int i=0; !numericError && i<values.length; i++){
							try{
								this.refPoint[i] = Double.valueOf(values[i]);
								if(this.refPoint[i]<0 || this.refPoint[i]>1){
									numericError = true;
								}
							}catch (Exception e) {
								numericError = true;
							}
						}
					}
					else{
						for(int i=0; !numericError && i<values.length; i++){
							try{
								this.weights[i] = Double.valueOf(values[i]);
							}catch(Exception e){
								numericError = true;
							}
						}
					}
				}
			}
			/*if(numericError){
				this.view.showErrorWeightDialog();
				numericError=false;
				//this.selectedPreference = -1;
			}*/
		}
		else if(obs instanceof UserReportListener){
			this.userFinishReport = true;
		}
		else if(obs instanceof UserErrorListener){
			this.view.closeErrorDialog();
			this.errorDialog = false;
		}
		else if(obs instanceof UserWaitListener){
			this.view.closeWaitDialog();
			this.startInteraction = true;
		}

		else if(obs instanceof UserErrorMetricListener){
			this.view.closeErrorMetricDialog();
		}
		else if(obs instanceof UserErrorWeightListener){
			this.view.closeErrorWeightDialog();
		}
	}

	/////////////////////////////////////////////////////////////////
	// -------------------------------------------- Protected methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Wait until one of these events happen:
	 * 1) User has selected a preference
	 * 2) User stops the search
	 * 3) Evaluation time is over
	 * */
	protected void waitUserSelection(){

		this.view.showWaitDialog();
		while(!startInteraction){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.view.closeWaitDialog();
		/*//while(selectedPreference==-1 && !finishSearch && !timeIsOver){
		while(selectedPreference<1 && !finishSearch && !timeIsOver){
			if(useTimeCounter && (System.currentTimeMillis()-this.startingTime)>this.maxEvalTime){
				timeIsOver = true;
			}

			if(finishIndividual){
				// Mostrar mensaje de aviso!
				System.out.println("No ha seleccionado la preferencia");
				finishIndividual = false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
	}

	/**
	 * Wait until one of these events happen:
	 * 1) User has selected a preference
	 * 2) User stops the search
	 * 3) Evaluation time is over
	 * */
	protected void waitUserResponse(){
		boolean exit=false;

		while(!exit){
			if(this.finishSearch && !this.errorDialog){
				exit = true;
			}

			if(this.useTimeCounter && (System.currentTimeMillis()-this.startingTime)>this.maxEvalTime){
				this.timeIsOver = true;
				exit = true;
				this.errorDialog = false;
				if(log){
					sb.append("\nTIMESTAMP: " + getTimeStamp() + " EVENT: Evaluation time is over.");
					writeLog();
				}
			}

			if(this.finishIndividual && this.selectedPreference==0){
				this.view.showErrorDialog();
				this.errorDialog = true;
				this.finishIndividual = false;
			}

			if(this.finishIndividual && this.selectedPreference == 6){ //metric range
				//System.out.println("error="+numericError + " min=" + this.metricMinValue + " max=" + this.metricMaxValue);
				if(numericError){
					this.view.showErrorMetricDialog();
					this.finishIndividual = false;
				}
				else if(this.metricMinValue <0 || this.metricMinValue>1 
						|| this.metricMaxValue<0 || this.metricMaxValue > 1 
						|| this.metricMinValue>this.metricMaxValue){
					this.view.showErrorMetricDialog();
					this.finishIndividual = false;
				}
			}

			if(this.finishIndividual && this.selectedPreference == 8){ // aspiration levels
				if(numericError){
					this.view.showErrorWeightDialog();
					this.finishIndividual = false;
				}
				else{
					boolean error = false;
					for(int i=0; !error && i<this.weights.length; i++){
						if(this.weights[i]>1 || this.weights[i]<0 || this.refPoint[i]<0 || this.refPoint[i]>1){
							error = true;
						}
					}
					if(error){
						this.view.showErrorWeightDialog();
						this.finishIndividual = false;
						//this.weights=null;
						//this.refPoint=null;
					}
				}
			}

			if(!finishIndividual && selectedPreference!=previousPreference && !this.errorDialog){
				updateView();
				this.previousPreference=selectedPreference;
			}

			if(this.finishIndividual && !this.errorDialog){
				exit = true;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Wait until the user completes the report
	 * */
	protected void waitUserReport(boolean isLastSolution){

		sb.append("\nTIMESTAMP: " + getTimeStamp() + " EVENT: User starts to write the report for this evaluation.");

		if(isLastSolution){
			this.view.showUserDialog(true);
		}
		else{
			this.view.showUserDialog(false);
		}

		while(!userFinishReport){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.view.closeUserDialog();

		if(!isLastSolution){
			sb.append("\nTIMESTAMP: " + getTimeStamp() + " EVENT: User finishes the report for this evaluation.");
		}
		if(log)
			writeLog();
	}

	/**
	 * Process the information given by the user for an individual
	 * */
	protected ArchitecturalPreference processUserResponse(InteractiveMOIndividual individual){

		ArchitecturalPreference preference = null;
		SyntaxTree tree;
		int inter;
		int i=-1,j=-1;
		boolean exit = false;

		// LOG
		if(log)
			sb.append("\nTIMESTAMP: " + getTimeStamp() + " EVENT: Processing user's response");

		//switch(selectedPreference){
		switch(previousPreference){
		case 0: 
			break;
		case 1: // No preference
			preference = null;
			// Log
			if(log)
				this.sb.append("\n\tPreference: No preference has been selected");
			break;
		case 2: // Similarity best component
			preference = new SimilarityBestComponent();
			tree = individual.getComponentTree(selectedElement);
			((SimilarityBestComponent) preference).setComponentSubTree(tree);
			// Set the confidence level
			preference.setConfidence((double)selectedConfidenceLevel);
			// Log
			if(log)
				this.sb.append("\n\tPreference: Best component - Selected component: " + this.selectedElement + " - User's confidence [1-5]: " + this.selectedConfidenceLevel + " - Evaluation time (ms): " + this.evaluationTime);
			break;
		case 3: // Similarity best interface
			inter = -1;
			for(i=0; !exit && i<individual.getNumberOfComponents(); i++){
				for(j=0; !exit && j<individual.getNumberOfProvided(i); j++){
					inter++;
					if(inter==selectedElement){
						exit = true;
					}
				}
			}
			preference = new SimilarityBestInterface();
			tree = individual.getComponentTree(i-1);
			((SimilarityBestInterface)preference).setInterfaceSubTree(tree,j-1);
			// Set the confidence level
			preference.setConfidence((double)selectedConfidenceLevel);
			// Log
			if(log)
				this.sb.append("\n\tPreference: Best interface - Selected interface: " + this.selectedElement + " - User's confidence [1-5]: " + this.selectedConfidenceLevel + " - Evaluation time (ms): " + this.evaluationTime);
			break;
		case 4: // Dissimilarity worst component
			preference = new DissimilarityWorstComponent();
			tree = individual.getComponentTree(selectedElement);
			((DissimilarityWorstComponent) preference).setComponentSubTree(tree);
			// Set the confidence level
			preference.setConfidence((double)selectedConfidenceLevel);
			// Log
			if(log)
				this.sb.append("\n\tPreference: Worst component - Selected component: " + this.selectedElement + " - User's confidence [1-5]: " + this.selectedConfidenceLevel + " - Evaluation time (ms): " + this.evaluationTime);
			break;
		case 5: // Dissimilarity worst interface
			inter = -1;
			for(i=0; !exit && i<individual.getNumberOfComponents(); i++){
				for(j=0; j<individual.getNumberOfProvided(i); j++){
					inter++;
					if(inter==selectedElement){
						exit = true;
					}
				}
			}
			preference = new DissimilarityWorstInterface();
			//tree = individual.getInterfaceTree(i-1, j-1);
			tree = individual.getComponentTree(i-1);
			//System.out.println(tree);
			((DissimilarityWorstInterface)preference).setInterfaceSubTree(tree,j-1);
			// Set the confidence level
			preference.setConfidence((double)selectedConfidenceLevel);
			// Log
			if(log)
				this.sb.append("\n\tPreference: Worst interface - Selected interface: " + this.selectedElement + " - User's confidence [1-5]: " + this.selectedConfidenceLevel + " - Evaluation time (ms): " + this.evaluationTime);
			break;

		case 6: // Measure in range
			// Check default values
			if(this.metricMinValue < 0) 
				this.metricMinValue = 0.0;
			if(this.metricMaxValue < 0)
				this.metricMaxValue = 1.0;
			// Create the preference
			preference = new SimilarityMeasureInRange(this.selectedElement, this.metricMinValue, this.metricMaxValue);
			// Set the confidence level
			preference.setConfidence((double)selectedConfidenceLevel);
			// Log
			if(log){
				this.sb.append("\n\tPreference: Measure in range - Selected metric (index): " + this.selectedElement + " - Minimum value: " 
						+ this.metricMinValue +  " - Maximum value: " + this.metricMaxValue + " - User's confidence [1-5]: " + this.selectedConfidenceLevel + " - Evaluation time (ms): " + this.evaluationTime);
			}
			break;	

		case 7: // Ideal number of components
			// Check default values
			if(this.selectedElement==0) // default value (the same that is shown in the GUI)
				this.selectedElement = (maxNumComponents-minNumComponents)/2+minNumComponents;
			// Create the preference
			preference = new SimilarityNumberComponents(this.minNumComponents, this.maxNumComponents, this.selectedElement);
			// Set the confidence level
			preference.setConfidence((double)selectedConfidenceLevel);
			// Log
			if(log)
				this.sb.append("\n\tPreference: Number of components - Selected size: " + this.selectedElement + " - User's confidence [1-5]: " + this.selectedConfidenceLevel + " - Evaluation time (ms): " + this.evaluationTime);
			break;

		case 8: // Aspiration levels
			// Create the preference
			preference = new SimilarityReferencePoint(this.refPoint, this.weights);
			// Set the confidence level
			preference.setConfidence((double)selectedConfidenceLevel);
			// Log
			if(log){
				this.sb.append("\n\tPreference: Aspiration level - Reference point: ");//;+ this.selectedElement + " - User's confidence:" + this.selectedConfidenceLevel);
				int k;
				for(k=0; k<this.refPoint.length-1; k++){
					this.sb.append(this.refPoint[k] +",");
				}
				this.sb.append(this.refPoint[k] + " Weight: ");
				for(k=0; k<this.refPoint.length-1; k++){
					this.sb.append(this.weights[k] +",");
				}
				this.sb.append(this.weights[k] + " User's confidence [1-5]: " + this.selectedConfidenceLevel + " - Evaluation time (ms): " + this.evaluationTime);
			}
			break;
		}

		// Save the frozen component, if it was specified by the user
		if(frozenComponent!=-1){
			for(i=0; i<individual.getNumberOfComponents();i++)
				individual.setFrozenComponent(i,false);
			individual.setFrozenComponent(frozenComponent, true);
			if(log)
				this.sb.append("\n\tFrozen component: " + this.frozenComponent);
		}		

		// Save whether the individual should be added in the archive
		if(storeSolutionInArchive){
			individual.setSolutionInArchive(true);
			if(log)
				this.sb.append("\n\tThe solution has been marked to be included in the archive.");
		}

		// Save whether the individual should be removed from population
		if(removeSolution){
			individual.setToBeRemoved(true);
			if(log)
				this.sb.append("\n\tThe solution has been marked to be removed from the population.");
		}

		// LOG
		if(log)
			writeLog();

		return preference;
	}

	/**
	 * Select two individuals, the best from the current population,
	 * and a random individual from the archive (different from the previous one).
	 * @param population Current population
	 * @param archive Archive of best solutions
	 * @return Individuals to be shown to the architect.
	 * */
	protected List<IIndividual> selectIndividuals(List<IIndividual> population, List<IIndividual> archive, int maxArchiveSize, boolean noPreferencesYet, int generation){

		List<IIndividual> all = new ArrayList<IIndividual>();
		all.addAll(population);
		for(IIndividual ind: archive){
			if(!all.contains(ind)){
				all.add(ind);
			}
		}

		List<IIndividual> selected;
		IIndividual ind;
		int rndIndex;
		if(noPreferencesYet){
			// Call the selector for n solutions
			selected = this.selector.select(all, nofsel);
			// check that all individuals are valid (null is possible when using clustering)
			for(int i=0; i<nofsel; i++){
				ind = selected.get(i);
				if(ind==null){
					rndIndex = this.context.createRandGen().choose(0, all.size());
					selected.set(i,all.get(rndIndex));
				}
			}
		}

		else{
			// Call the selector for n-1 solutions
			selected = this.selector.select(all, nofsel-1);
			// check that all individuals are valid (null is possible when using clustering)
			for(int i=0; i<nofsel-1; i++){
				ind = selected.get(i);
				if(ind==null){
					rndIndex = this.context.createRandGen().choose(0, all.size());
					selected.set(i,all.get(rndIndex));
				}
			}

			// Add the individual with the higher preference value
			double maxPrefValue = Double.NEGATIVE_INFINITY;
			double prefValue;
			int index = -1;
			for(int i=0; i<all.size(); i++){
				prefValue = ((InteractiveMOFitness)all.get(i).getFitness()).getPreferenceValue();
				if(prefValue > maxPrefValue){
					maxPrefValue = prefValue;
					index = i;
				}
			}
			selected.add(all.get(index));
		}

		// Save metric names
		if(this.metricNames == null){
			this.metricNames = ((InteractiveMOIndividual)population.get(0)).getMeasuresNames();
		}

		return selected;
	}

	private void createView(InteractiveMOIndividual individual, int generation, double meanFitness, double stdFitness, double [] distribution, double percentageInvalids){

		// Create the interaction view
		view = new InteractionView(this);
		view.addStatisticsPanel(generation, meanFitness, stdFitness, distribution, percentageInvalids, this.minNumComponents, this.maxNumComponents);
		view.setMaximunTime(this.maxEvalTime);

		// The individual visualization (tree)
		DiagramTreeModel treeModel = new DiagramTreeModel(individual);

		// The measure and fitness function
		MeasureTableModel tableModel = new MeasureTableModel(individual);
		double f1 = ((InteractiveMOFitness)individual.getFitness()).getPreferenceValue();
		double f2 = ((InteractiveMOFitness)individual.getFitness()).getDominanceValue();
		double f = ((InteractiveMOFitness)individual.getFitness()).getValue();
		view.addIndividualPanel(treeModel.getModel(),tableModel,f1,f2,f);

		int frozenIndex = -1;
		for(int i=0; frozenIndex==-1 && i<individual.getNumberOfComponents(); i++)
			if(individual.isFrozenComponent(i))
				frozenIndex = i;

		// number of interfaces
		int [] nInterfaces = new int[individual.getNumberOfComponents()];
		for(int i=0; i<individual.getNumberOfComponents(); i++){
			nInterfaces[i] = individual.getNumberOfProvided(i);
		}
		view.addPreferencePanel(individual.getNumberOfComponents(),nInterfaces,frozenIndex,preferences,preferenceLabels);

		view.show(useTimeCounter);
	}

	private void updateView(){
		// the user can change the index before the selection of the preference
		view.updatePreferencePanel(selectedPreference-1,frozenComponent,storeSolutionInArchive,removeSolution,this.minNumComponents,this.maxNumComponents,this.metricNames);
	}

	/**
	 * 0: Algorithm starts
	 * 1: Algorithm in generation 'x'
	 * 2: Algorithm ends
	 * */
	public void printAlgorithmState(int i, int generation){
		if(log){
			try {

				String timeStamp = getTimeStamp();
				String message = "\nTIMESTAMP: " + timeStamp + " EVENT: ";

				// write
				switch(i){
				case 0: message += "Algorithm starts."; break;
				case 1: message += "Algorithm does generation " + generation +"."; break;
				case 2: message += "Algorithm finishes.\n\n"; break;
				default: break;
				}

				// open
				this.fwriter = new FileWriter(filename, true);
				// write and close
				this.fwriter.write(message);
				this.fwriter.flush();
				this.fwriter.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void writeLog(){
		try {// write current content
			this.fwriter = new FileWriter(filename, true);
			this.fwriter.write(this.sb.toString());
			this.fwriter.flush();
			this.fwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		this.sb.delete(0, this.sb.length()); // remove from buffer
	}

	protected String getTimeStamp(){
		String timestamp = this.format.format(System.currentTimeMillis());
		return timestamp;
	}
}