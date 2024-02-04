package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bayesserver.Distribution;
import com.bayesserver.Node;
import com.bayesserver.NodeGroupCollection;
import com.bayesserver.State;
import com.bayesserver.StateCollection;
import com.bayesserver.Table;
import com.bayesserver.TableIterator;
import com.bayesserver.Variable;
import com.bayesserver.VariableContext;
import com.bayesserver.VariableContextCollection;
import com.bayesserver.inference.InconsistentEvidenceException;

public class Networkfunctions {
	private static String SEPARATOR = ", ";

	public NodeGroupCollection getGroups(Variable var) {
		return var.getNode().getGroups();
	}
	
	public static String getGroupsAsString(Variable var) {
		String str = "";
		for (String gr: var.getNode().getGroups()) {
			str += gr + SEPARATOR;
		}
		if (str.length() >= SEPARATOR.length()) {
			str = str.substring(0, str.length() - SEPARATOR.length());
		}
		return str;
	}
	
	public static Collection<Node> getParents(Node node) {
		return GraphFunctions.getParentNodes(node);
	}
	
	public static String getParentsAsString(Node node) {
		String str = "";
		for (Node n: getParents(node)) {
			str += n.getName() + SEPARATOR;
		}
		if (str.length() >= SEPARATOR.length()) {
			str = str.substring(0, str.length() - SEPARATOR.length());
		}
		return str;
	}
	
	public static Collection<Node> getChildren(Node node) {
		return GraphFunctions.getChildNodes(node);
	}
	
	public static String getChildrenAsString(Node node) {
		return getChildrenAsString(node, SEPARATOR);
	}
	
	private static String getChildrenAsString(Node node, String separator) {
		String str = "";
		for (Node n: getChildren(node)) {
			str += n.getName() + separator;
		}
		if (str.length() >= separator.length()) {
			str = str.substring(0, str.length() - separator.length());
		}
		return str;
	}
	
	private static StateCollection getStates(Variable var) {
		return var.getStates();
	}
	
	public static String getStatesAsString(Variable var) {
		return getStatesAsString(getStates(var), SEPARATOR);
	}
	
	private static String getStatesAsString(Collection<State> states, String separator) {
		String str = "";
		for (State s: states) {
			str += "[" + s.getIndex() + "] " +  s.getName() + separator;
		}
		if (str.length() >= separator.length()) {
			str = str.substring(0, str.length() - separator.length());
		}
		return str;
	}
	
	public static String getCPTableToString(Node node) {
		String str = "";
		try {
			for (Map<State, Double> el: getCPTable(node)) {
				String substr = el.entrySet().iterator().next().getKey().getVariable() + "[";
				for (Entry<State, Double> entry: el.entrySet()) {
					substr += entry.getKey().getName() + ":" + entry.getValue() + ",";
				}
				str += substr.replaceFirst(",$", "]");
			}
		} catch (InconsistentEvidenceException e) {
			return "Could not fetch table.";
		}
		return str;
	}
		
	private static List<Map<State, Double>> getCPTable(Node node) throws InconsistentEvidenceException {
		Distribution distr = node.getDistribution();
		return iterateTable(node, distr.getTable());
	}
	
	private static List<Map<State, Double>> iterateTable(Node node, Table table) throws InconsistentEvidenceException {
		//get all variables and sort them beginning with the head and ending with the tail
		VariableContextCollection sortedvars = table.getSortedVariables();
		ArrayList<VariableContext> varHeads = new ArrayList<VariableContext>();
		ArrayList<VariableContext> varTails = new ArrayList<VariableContext>();
		ArrayList<VariableContext> varOrdered = new ArrayList<VariableContext>();
		
		for (VariableContext var : sortedvars) {
			if (var.isHead()) {
				varHeads.add(var);
			} else {
				varTails.add(var);
			}
		}
		varOrdered.addAll(varTails);
		varOrdered.addAll(varHeads);
		
		//build the condition state list
		ArrayList<ArrayList<State>> statesordered = new ArrayList<ArrayList<State>>();
		if (varTails.size() > 0) {
			statesordered = buildStateList(varTails);
		}
		TableIterator ti = new TableIterator(table, varOrdered);
 		int stateCount = 0;
 		List<Map<State, Double>> cpt = new ArrayList<Map<State, Double>>();
    	for (int i = ti.getRow(); i < ti.size(); i = ti.getRow()) {
    		Map<State, Double> stateValues = new HashMap<State, Double>();
    		for (VariableContext var: varHeads) {
    			for (State state: var.getVariable().getStates()) {
    				stateValues.put(state, ti.getValue());
    				ti.increment();
    			}
    		}    		
    		if (varTails.size() > 0) {
    			for (State s: statesordered.get(stateCount)) {
    				stateValues.put(s, (Double) s.getValue());
    			}
    		}
    		stateCount++;
    		cpt.add(stateValues);
		}
    	return cpt;
	}
	
	/*
	 * Builds a list of lists of combinations of the variables' states on which the values are conditioned.
	 */
	private static ArrayList<ArrayList<State>> buildStateList(ArrayList<VariableContext> vars) {
		//initiliaze
		ArrayList<ArrayList<State>> initList = new ArrayList<ArrayList<State>>();
		initList.add(new ArrayList<State>());
		ArrayList<ArrayList<State>> list = buildStateList(vars.get(vars.size() - 1), initList);
		for (int i = vars.size() - 2; i >= 0; i--) {
			list = buildStateList(vars.get(i), list);
		}
		return list;
	}
	
	private static ArrayList<ArrayList<State>> buildStateList(VariableContext var, ArrayList<ArrayList<State>> arrayList) {
		ArrayList<ArrayList<State>> templist = new ArrayList<ArrayList<State>>();
		for (State state: var.getVariable().getStates()) {			
			for(ArrayList<State> l: arrayList) {
				ArrayList<State> statelist = new ArrayList<State>();
				statelist.add(state);
				statelist.addAll(l);
				templist.add(statelist);
			}
		}
		return templist;
	}
	
	private static Collection<Node> getRootAncestors(Node node) {
		Collection<Node> rootAncestors = new HashSet<Node>();
		Collection<Node> ancestors = new HashSet<Node>();
		GraphFunctions.getAncestors(node, ancestors);
		Collection<Node> rootnodes = GraphFunctions.getRootNodes(node.getNetwork());
		for (Node anc: ancestors) {
			if (rootnodes.contains(anc)) {
				rootAncestors.add(anc);
			}
		}
		return rootAncestors;
	}
	
	private static Collection<Variable> getRootAncestors(Variable var) {
		Collection<Variable> rootAncestors = new HashSet<Variable>();
		Collection<Node> ancNodes = getRootAncestors(var.getNode());
		for (Node anc: ancNodes) {
			rootAncestors.addAll(anc.getVariables());
		}
		return rootAncestors;
	}
	
	public static Collection<Variable> getRootAncestors(Collection<Variable> vars) {
		Collection<Variable> rootAncestors = new HashSet<Variable>();		
		for (Variable var: vars) {
			rootAncestors.addAll(getRootAncestors(var));
		}
		return rootAncestors;
	}
}
