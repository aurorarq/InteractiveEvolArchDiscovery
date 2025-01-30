package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeModel;

import net.sf.jclec.sbse.discovery.control.InteractionController;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.FinishIndividualButtonListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.AddToArchiveButtonListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.ComponentButtonListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.ConfidenceComponentButtonListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.FrozenComponentButtonListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.ListPreferencesListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.ReferencePointTextFieldListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.RemoveSolutionButtonListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.StopSearchButtonListener;
import net.sf.jclec.sbse.discovery.imo.gui.listeners.TextFieldListener;

/**
 * Interaction window
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
public class InteractionView{

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Main frame */
	private JFrame frame;

	/** Dynamic preference panel */
	private JPanel prefPanel;

	/** Controller */
	private InteractionController controller;

	/** Preference labels */
	private String [] preferences;

	/** Number of components of the current individual */
	private int nComponents;

	/** Number of preferences in each components */
	private int [] nInterfaces;

	/** Time to be set in the counting down */
	private long maxTime;

	/** Count down frame*/
	private CountDown timeCounter;

	/** User dialog */
	private ReportUserDialog dialogReport;
	
	private ErrorUserDialog dialogError;
	
	private WaitUserDialog dialogWait;
	
	private ErrorMetricDialog dialogErrorMetric;
	
	private ErrorWeightDialog dialogErrorWeight;
	
	private Color labelColor;
	private Font labelFont;
	
	private String [] preferenceLabels;

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/**
	 * Parameterized constructor
	 * @param controller Interaction controller
	 * @param maxTimeInSeconds Maximum evaluation time
	 * */
	public InteractionView(InteractionController controller){
		
		labelColor = new Color(0,51,102);
		labelFont = new Font("Sans Serif",Font.BOLD,14);
		
		frame = new JFrame("USER INTERACTION");
		frame.setVisible(false);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.controller = controller;
		addButtons();
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	public void addStatisticsPanel(int generation, double meanFitness, double stdFitness, double [] distribution, double percentageInvalids, int min, int max){
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		JLabel label = new JLabel("POPULATION STATISTICS");
		label.setFont(labelFont);
		label.setForeground(labelColor);
		JLabel text1 = new JLabel("Current generation: " + generation);
		JLabel text2;
		if(Double.isNaN(meanFitness))
			text2 = new JLabel("Mean fitness in population: -");
		else
			text2 = new JLabel("Mean fitness in population: " + meanFitness);
		JLabel text3;
		if(Double.isNaN(stdFitness))
			text3 = new JLabel("Std. Dev. in population: -");
		else
			text3 = new JLabel("Std. Dev. in population: " + stdFitness);

		JLabel text4 = new JLabel("% Infeasible solutions: " + percentageInvalids);
		StringBuffer sb = new StringBuffer("");
		for(int i=0; i<distribution.length; i++)
			sb.append(distribution[i] + " ");
		JLabel text5 = new JLabel("Distribution of solutions (from " + min + " to " + max + " components): " + sb.toString());

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(label)
						.addComponent(text2)
						.addComponent(text4))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(text1)
						.addComponent(text3)
						.addComponent(text5)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(label)
						.addComponent(text1))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(text2)
						.addComponent(text3))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(text4)
						.addComponent(text5)));

		frame.getContentPane().add(panel, BorderLayout.NORTH);
	}

	public void addIndividualPanel(TreeModel treeModel, MeasureTableModel tableModel, double f1, double f2, double f){
		TreeElement tree = new TreeElement(treeModel);
		TableElement table = new TableElement(tableModel);

		JScrollPane scrollTree = new JScrollPane(tree.getElement());
		JScrollPane scrollTable = new JScrollPane(table.getComponent());
		//JPanel scrollTable = new JPanel();
		//scrollTable.setMaximumSize(table.getComponent().getMaximumSize());

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel label = new JLabel("ARCHITECTURAL SOLUTION");
		label.setFont(labelFont);
		label.setForeground(labelColor);
		JLabel label2 = new JLabel("OBJECTIVE VALUES");
		label2.setFont(labelFont);
		label2.setForeground(labelColor);
		JLabel label2b = new JLabel("\tRange [0,1], to be minimised for the overall architecture (last row)");
		JLabel label3 = new JLabel("FITNESS VALUE");
		label3.setFont(labelFont);
		label3.setForeground(labelColor);
		JLabel label4 = new JLabel("Achievement of user's preferences (range [0,1], to be maximised): " + f1);
		JLabel label5 =  new JLabel("Trade-off among software metrics (range [-1,1], to be minimised): " + f2);
		JLabel label6 = new JLabel("Overall fitness (range [0,1], to be minimised): " + f);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(label)
				.addComponent(scrollTree)
				.addComponent(label2)
				.addComponent(label2b)
				.addComponent(scrollTable)
				.addComponent(label3)
				.addComponent(label4)
				.addComponent(label5)
				.addComponent(label6));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(label)
				.addComponent(scrollTree)
				.addComponent(label2)
				.addComponent(label2b)
				.addComponent(scrollTable)
				.addComponent(label3)
				.addComponent(label4)
				.addComponent(label5)
				.addComponent(label6));

		frame.getContentPane().add(panel, BorderLayout.WEST);
	}

	public void addPreferencePanel(int n, int [] interfaces, int frozen, String [] preferenceList, String [] preferenceLabels){

		this.nComponents = n;
		this.preferences = preferenceList;
		this.preferenceLabels = preferenceLabels;
		this.nInterfaces = interfaces;

		// Additional actions
		JLabel actionLabel = new JLabel("OPTIONAL ACTIONS");
		actionLabel.setFont(labelFont);
		actionLabel.setForeground(labelColor);
		
		// Frozen component (always appears)
		JLabel labelFrozen = new JLabel("Frozen component");
		ListComponentsElement frozenGroup = new ListComponentsElement(this.nComponents,frozen);
		Enumeration<AbstractButton>groupElements = frozenGroup.getButtonGroup().getElements();
		ActionListener l;
		AbstractButton button;
		int i = 0;
		while(groupElements.hasMoreElements()){
			button = groupElements.nextElement();
			l = new FrozenComponentButtonListener(controller, i);
			button.addActionListener(l);
			i++;
		}

		JRadioButton buttonRemoveSolution = new JRadioButton("Remove solution from population");
		buttonRemoveSolution.setSelected(false);
		l = new RemoveSolutionButtonListener(controller);
		buttonRemoveSolution.addActionListener(l);

		JRadioButton buttonAddToArchive = new JRadioButton("Add solution to the archive");
		buttonRemoveSolution.setSelected(false);
		l = new AddToArchiveButtonListener(controller);
		buttonAddToArchive.addActionListener(l);

		// Choice button
		PreferenceListElement list = new PreferenceListElement(this.preferences);
		list.getComponent().addActionListener(new ListPreferencesListener(controller));
		JLabel labelPrefs = new JLabel("ARCHITECTURAL PREFERENCES");
		labelPrefs.setFont(labelFont);
		labelPrefs.setForeground(labelColor);
		
		// User's confidence button
		JLabel labelConfidence = new JLabel("User's confidence (1=low, 5=high)");
		ListLikeScale confidenceGroup = new ListLikeScale();
		groupElements = confidenceGroup.getButtonGroup().getElements();
		i=0;
		while(groupElements.hasMoreElements()){
			button = groupElements.nextElement();
			l = new ConfidenceComponentButtonListener(controller,i);
			button.addActionListener(l);
			i++;
		}

		// Create the panel
		prefPanel = new JPanel();
		GroupLayout layout = new GroupLayout(prefPanel);
		prefPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(actionLabel)
				.addComponent(buttonAddToArchive)
				.addComponent(buttonRemoveSolution)
				.addComponent(labelFrozen)
				.addComponent(frozenGroup.getComponent())
				.addComponent(labelPrefs)
				.addComponent(list.getComponent())
				.addComponent(labelConfidence)
				.addComponent(confidenceGroup.getComponent())
				);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(actionLabel)
				.addComponent(buttonAddToArchive)
				.addComponent(buttonRemoveSolution)
				.addComponent(labelFrozen)
				.addComponent(frozenGroup.getComponent())
				.addComponent(labelPrefs)
				.addComponent(list.getComponent())
				.addComponent(labelConfidence)
				.addComponent(confidenceGroup.getComponent())
				);

		frame.getContentPane().add(prefPanel, BorderLayout.EAST);
	}

	public void updatePreferencePanel(int index, int frozen, boolean storeInArchive, boolean removeSolution, int minComp, int maxComp, String [] metricNames){

		// remove older panel
		frame.getContentPane().remove(prefPanel);

		// Additional actions
		JLabel actionLabel = new JLabel("OPTIONAL ACTIONS");
		actionLabel.setFont(labelFont);
		actionLabel.setForeground(labelColor);
		
		// Frozen component (always appears)
		JLabel labelFrozen = new JLabel("Frozen component");
		ListComponentsElement frozenGroup = new ListComponentsElement(this.nComponents,frozen);
		Enumeration<AbstractButton>groupElements = frozenGroup.getButtonGroup().getElements();
		ActionListener l;
		AbstractButton button;
		int i = 0;
		while(groupElements.hasMoreElements()){
			button = groupElements.nextElement();
			l = new FrozenComponentButtonListener(controller, i);
			button.addActionListener(l);
			i++;
		}

		// Addition actions
		
		JRadioButton buttonRemoveSolution = new JRadioButton("Remove solution from population");
		buttonRemoveSolution.setSelected(removeSolution);
		l = new RemoveSolutionButtonListener(controller);
		buttonRemoveSolution.addActionListener(l);

		JRadioButton buttonAddToArchive = new JRadioButton("Add solution to the archive");
		buttonAddToArchive.setSelected(storeInArchive);
		l = new AddToArchiveButtonListener(controller);
		buttonAddToArchive.addActionListener(l);

		// User's confidence button
		JLabel labelConfidence = new JLabel("User's confidence (1=low, 5=high)");
		ListLikeScale confidenceGroup = new ListLikeScale();
		groupElements = confidenceGroup.getButtonGroup().getElements();
		i=0;
		while(groupElements.hasMoreElements()){
			button = groupElements.nextElement();
			l = new ConfidenceComponentButtonListener(controller,i);
			button.addActionListener(l);
			i++;
		}

		// Choice button with the selected index
		PreferenceListElement list = new PreferenceListElement(this.preferences, index+1); // the list panel has an extra position
		//list.disable();
		list.getComponent().addActionListener(new ListPreferencesListener(controller));
		JLabel label = new JLabel("ARCHITECTURAL PREFERENCES");
		label.setFont(labelFont);
		label.setForeground(labelColor);
		
		// Create the panel of the selected preference
		JLabel labelPrefs;
		if(index>=0){
			labelPrefs = new JLabel(this.preferenceLabels[index]);
		}
		else{
			labelPrefs = new JLabel();
		}
		JComponent component = null;
		switch(index){
		case -1:
			component = new JLabel();
			break;
		case 0: // no preference
			component = new JLabel();
			break; // a new panel is not needed
		case 1: // Similarity best component
			component = similarityComponentPreferencePanel();
			break;
		case 2: // Similarity best interface
			component = similarityInterfacePreferencePanel();
			break;
		case 3: // Dissimilarity worst component
			component = similarityComponentPreferencePanel();
			break;
		case 4: // Dissimilarity worst interface
			component = similarityInterfacePreferencePanel();
			break;
		case 5: // Measure in range
			component = measureRangePreferencePanel(metricNames);
			break;
		case 6: // Number of components
			component = numberComponentsPreferencePanel(minComp,maxComp);
			break;
		case 7: // Aspiration levels
			component = referencePointPreferencePanel(metricNames.length);
			break;
		}

		// Create the panel
		prefPanel = new JPanel();
		GroupLayout layout = new GroupLayout(prefPanel);
		prefPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(actionLabel)
				.addComponent(buttonAddToArchive)
				.addComponent(buttonRemoveSolution)
				.addComponent(labelFrozen)
				.addComponent(frozenGroup.getComponent())
				.addComponent(label)
				.addComponent(list.getComponent())
				.addComponent(labelPrefs)
				.addComponent(component)
				.addComponent(labelConfidence)
				.addComponent(confidenceGroup.getComponent())
				);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(actionLabel)
				.addComponent(buttonAddToArchive)
				.addComponent(buttonRemoveSolution)
				.addComponent(labelFrozen)
				.addComponent(frozenGroup.getComponent())
				.addComponent(label)
				.addComponent(list.getComponent())
				.addComponent(labelPrefs)
				.addComponent(component)
				.addComponent(labelConfidence)
				.addComponent(confidenceGroup.getComponent())
				);

		frame.getContentPane().add(prefPanel, BorderLayout.EAST);
		frame.pack();
		frame.repaint();
	}

	public void show(boolean showTimeCounter){
		frame.pack();
		frame.setLocationRelativeTo(null); // center the frame
		frame.setVisible(true);
		if(showTimeCounter){
			this.timeCounter = new CountDown(this.maxTime); // Start counting down...
			this.timeCounter.start();
		}
	}

	public void close(){
		if(this.timeCounter!=null)
			this.timeCounter.stopAndClose();
		frame.setVisible(false);
	}

	public void dispose(){
		if(this.timeCounter!=null)
			this.timeCounter.stopAndClose();
		frame.dispose();
		frame = null;
	}

	public void setMaximunTime(long maxTime){
		this.maxTime = maxTime;
	}

	public void showUserDialog(boolean isLastInteraction){
		if(dialogReport==null)
			dialogReport = new ReportUserDialog(controller,isLastInteraction);
		dialogReport.show();
		if(timeCounter!=null)
			timeCounter.stopAndClose();
	}

	public void closeUserDialog(){
		dialogReport.close();
	}
	
	public void showErrorDialog() {
		if(dialogError==null)
			dialogError = new ErrorUserDialog(controller);
		dialogError.show();
	}
	
	public void closeErrorDialog(){
		dialogError.close();
	}
	
	public void showWaitDialog(){
		if(dialogWait == null)
			dialogWait = new WaitUserDialog(controller);
		dialogWait.show();
	}
	
	public void closeWaitDialog(){
		dialogWait.close();
	}
	
	public void showErrorMetricDialog(){
		if(dialogErrorMetric == null)
			dialogErrorMetric = new ErrorMetricDialog(controller);
		dialogErrorMetric.show();
	}
	
	public void closeErrorMetricDialog(){
		dialogErrorMetric.close();
	}
	
	public void showErrorWeightDialog(){
		if(dialogErrorWeight == null)
			dialogErrorWeight = new ErrorWeightDialog(controller);
		dialogErrorWeight.show();
	}
	
	public void closeErrorWeightDialog(){
		dialogErrorWeight.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Private methods
	/////////////////////////////////////////////////////////////////

	private void addButtons(){
		JButton buttonFinish = new JButton("Finish this evaluation");
		ActionListener l = new FinishIndividualButtonListener(controller);
		buttonFinish.addActionListener(l);

		JButton buttonStopSearch = new JButton("Stop search");
		l = new StopSearchButtonListener(controller);
		buttonStopSearch.addActionListener(l);

		JPanel panel = new JPanel();
		panel.add(buttonFinish);
		panel.add(buttonStopSearch);
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
	}

	private JComponent similarityComponentPreferencePanel(){
		ListComponentsElement group = new ListComponentsElement(nComponents,0);
		// button listeners
		Enumeration<AbstractButton> groupElements = group.getButtonGroup().getElements();
		AbstractButton button;
		ActionListener l;
		int i = 0;
		while(groupElements.hasMoreElements()){
			button = groupElements.nextElement();
			l = new ComponentButtonListener(controller, i);
			button.addActionListener(l);
			i++;
		}
		return group.getComponent();
	}

	private JComponent similarityInterfacePreferencePanel(){

		ListInterfacesElement group = new ListInterfacesElement(nComponents, nInterfaces);
		int i = 0;
		// button listeners
		Enumeration<AbstractButton> groupElements = group.getButtonGroup().getElements();
		AbstractButton button;
		ActionListener l;
		while(groupElements.hasMoreElements()){
			button = groupElements.nextElement();
			l = new ComponentButtonListener(controller, i);
			button.addActionListener(l);
			i++;
		}
		return group.getComponent();
	}

	private JComponent measureRangePreferencePanel(String [] metricNames){
		MeasureTextPanel group = new MeasureTextPanel(metricNames,0);

		// button listeners
		Enumeration<AbstractButton> groupElements = group.getButtonGroup().getElements();
		AbstractButton button;
		ActionListener l;
		int i = 0;
		while(groupElements.hasMoreElements()){
			button = groupElements.nextElement();
			l = new ComponentButtonListener(controller, i);
			button.addActionListener(l);
			i++;
		}

		// text field listeners
		DocumentListener dl;
		JTextField field = group.getMinTextField();
		dl = new TextFieldListener(controller, true);
		field.getDocument().addDocumentListener(dl);
		field = group.getMaxTextField();
		dl = new TextFieldListener(controller, false);
		field.getDocument().addDocumentListener(dl);

		return group.getComponent();
	}

	private JComponent numberComponentsPreferencePanel(int minComp, int maxComp) {
		ListNumberComponentsElement group = new ListNumberComponentsElement(minComp,maxComp,((maxComp-minComp)/2+minComp));
		// button listeners
		Enumeration<AbstractButton> groupElements = group.getButtonGroup().getElements();
		AbstractButton button;
		ActionListener l;
		int i = 0;
		while(groupElements.hasMoreElements()){
			button = groupElements.nextElement();
			l = new ComponentButtonListener(controller, i);
			button.addActionListener(l);
			i++;
		}
		return group.getComponent();
	}

	private JComponent referencePointPreferencePanel(int nObjectives) {
		ReferencePointTextPanel group = new ReferencePointTextPanel(nObjectives);
		// text field listeners
		DocumentListener dl;
		JTextField field = group.getWeightsTextField();
		dl = new ReferencePointTextFieldListener(controller, false);
		field.getDocument().addDocumentListener(dl);
		field = group.getPointTextField();
		dl = new ReferencePointTextFieldListener(controller, true);
		field.getDocument().addDocumentListener(dl);
		return group.getComponent();
	}

	
}
