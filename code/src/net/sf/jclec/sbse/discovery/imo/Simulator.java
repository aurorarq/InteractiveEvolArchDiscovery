package net.sf.jclec.sbse.discovery.imo;

import java.util.ArrayList;

//import net.sf.jclec.sbse.cl2cmp.Cl2CmpNonTerminalNode;
//import net.sf.jclec.sbse.cl2cmp.Cl2CmpTerminalNode;

import net.sf.jclec.sbse.discovery.imo.preferences.ArchitecturalPreference;
import net.sf.jclec.sbse.discovery.imo.preferences.SimilarityBestComponent;
import net.sf.jclec.sbse.discovery.imo.preferences.SimilarityMeasureInRange;

public class Simulator {

	public Simulator() {
		super();
	}
	
	public ArchitecturalPreference createComponentPreference(){
		//SyntaxTree component = new SyntaxTree();
		ArrayList<String> classes = new ArrayList<String>();
		classes.add("A");
		classes.add("B");
		classes.add("C");
		ArchitecturalPreference pref = new SimilarityBestComponent(classes);
		return pref;
	}
	
	public ArchitecturalPreference createICDMeasurePreference(){
		ArchitecturalPreference pref = new SimilarityMeasureInRange(0, 0.4, 0.8);
		return pref;
	}
	
	public ArchitecturalPreference createERPMeasurePreference(){
		ArchitecturalPreference pref = new SimilarityMeasureInRange(1, 0.4, 0.6);
		return pref;
	}
	
	public ArchitecturalPreference createGCRMeasurePreference(){
		ArchitecturalPreference pref = new SimilarityMeasureInRange(2, 0.7, 1.0);
		return pref;
	}
	
	/*private void fillSyntaxTree(SyntaxTree tree){
		tree.addNode(new Cl2CmpNonTerminalNode("component", new String[]{"classes", "required-interfaces", "provided-interfaces"}));
		tree.addNode(new Cl2CmpNonTerminalNode("classes", new String[]{}));
		tree.addNode(new Cl2CmpTerminalNode("A"));
		tree.addNode(new Cl2CmpTerminalNode("B"));
		tree.addNode(new Cl2CmpTerminalNode("C"));
		tree.addNode(new Cl2CmpNonTerminalNode("required-interfaces", new String[]{}));
		tree.addNode(new Cl2CmpTerminalNode("A_req_E"));
		tree.addNode(new Cl2CmpNonTerminalNode("provided-interfaces", new String[]{}));
	}*/
}
