package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bayesserver.State;
import com.bayesserver.Table;
import com.bayesserver.TableAccessor;
import com.bayesserver.Variable;
import com.bayesserver.VariableContext;
import com.bayesserver.inference.Evidence;
import com.bayesserver.inference.InconsistentEvidenceException;
import com.bayesserver.inference.InconsistentEvidenceMode;
import com.bayesserver.inference.Inference;
import com.bayesserver.inference.QueryOptions;
import com.bayesserver.inference.QueryOutput;
import com.bayesserver.inference.VariableEliminationInferenceFactory;
import com.bayesserver.statistics.Entropy;
import com.bayesserver.statistics.LogarithmBase;

public class QueryManager {

	private Diagnoser diagnoser;

	public QueryManager(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
	}
	
	public Double getProbability(State state) throws InconsistentEvidenceException {
		return getProbability(diagnoser.getEvidencemanager().getActiveEvidence(), state);
	}
	
	public Double getProbability(List<State> states) throws InconsistentEvidenceException {
		return getProbability(diagnoser.getEvidencemanager().getActiveEvidence(), states);
	}
	
	/*
	 * Returns the probability of the given state under the given evidence. 
	 */
	public double getProbability(Evidence evidence, State state) throws InconsistentEvidenceException {
		List<State> statelist = new ArrayList<State>();
		statelist.add(state);
		return getProbability(evidence, statelist);
	}
	
	/*
	 * Returns the joint probability of the given states (at most one state per variable!) under the given evidence. 
	 */
	public double getProbability(Evidence evidence, List<State> states) throws InconsistentEvidenceException {
		List<Variable> vars = getVariableList(states);
		Map<List<State>, Double> statesmap = getProbabilityMap(evidence, vars);
		return getValueOf(states, statesmap);
	}
	
	/*
	 * Returns a map of joint probabilities with keys the states over which the probabilities are computed and value the probability, under the given evidence.
	 */
	private Map<List<State>, Double> getProbabilityMap(Evidence evidence, List<Variable> vars) throws InconsistentEvidenceException {
		//System.out.println("getProbabilityMap vars:" + vars + "; evidence (healt.joint 3): " + evidence.get(system.getNodes().get(14)));
		VariableEliminationInferenceFactory factory = new VariableEliminationInferenceFactory();
		QueryOptions qopts = getQueryOptions(factory);
		QueryOutput qout = getQueryOutput(factory);
		Inference inf = getInference(factory);
		inf.setEvidence(evidence);
		List<List<State>> stateslist = makeStatesList(vars);
		Table t = new Table(toArray(vars));        
        inf.getQueryDistributions().add(t);
    	//System.out.println("Inference target system = " + inf.getNetwork().getName() + "; var = " + var.getName() + ", state = "  + state.getName());
        inf.query(qopts, qout);
		Map<List<State>, Double> statesmap = makeStatesmap(stateslist, t, vars);
		return statesmap;
	}
	
	private static List<Variable> getVariableList(Collection<State> states) {
		List<Variable> vars = new ArrayList<Variable>();
		for (State s: states) {
			vars.add(s.getVariable());
		}
		return vars;
	}
	
	private double getValueOf(List<State> states, Map<List<State>, Double> statesmap) {
		//System.out.println();
		//System.out.println("getValueOf states: " + states + "; statesmap: " + statesmap);
		for (List<State> list: statesmap.keySet()) {
			if (list.containsAll(states)) {
				return statesmap.get(list);
			}
		}
		return -1;
	}
	
	private QueryOutput getQueryOutput(VariableEliminationInferenceFactory factory) {
		return factory.createQueryOutput();
	}
	
	private QueryOptions getQueryOptions(VariableEliminationInferenceFactory factory) {
		QueryOptions queryOptions = factory.createQueryOptions();
		queryOptions.setInconsistentEvidenceMode(InconsistentEvidenceMode.ALL_EVIDENCE);
		queryOptions.setLogLikelihood(true);
		return queryOptions;
	}
	
	private Inference getInference(VariableEliminationInferenceFactory factory) {
		return factory.createInferenceEngine(diagnoser.getSystem());
	}
	
	private List<List<State>> makeStatesList(List<Variable> vars) {
		List<List<State>> map = new ArrayList<List<State>>();
		for (Variable v: vars) {
			map = makeStateList(v, map);
		}
		return map;
	}

	private List<List<State>> makeStateList(Variable v, List<List<State>> list) {
		List<List<State>> result = new ArrayList<List<State>>();
		if (list.size() == 0) {
			for (State s: v.getStates()) {
				List<State> newlist = new ArrayList<State>();
				newlist.add(s);
				result.add(newlist);
			}
		} else {
			for (List<State> l: list) {
				for (State s: v.getStates()) {				
					List<State> newlist = new ArrayList<State>();
					newlist.addAll(l);
					newlist.add(s);
					result.add(newlist);
				}
			}
		}
		return result;
	}
	
	private static Variable[] toArray(List<Variable> coll) {
		Variable[] result = new Variable[coll.size()];
		for (int i = 0; i < coll.size(); i++) { 
			result[i] = coll.get(i);
		}
		return result;
	}
	
	private static Map<List<State>, Double> makeStatesmap(List<List<State>> stateslist, Table t, List<Variable> vars) {
		Map<List<State>, Double> result = new HashMap<List<State>, Double>();
		TableAccessor ta = new TableAccessor(t, toArray(vars));
		for (int i = 0; i < stateslist.size(); i++) {
			result.put(stateslist.get(i), ta.get(i));
		}
		return result;
	}

	 public double getEntropy(Evidence evidence, Collection<Variable> head, Collection<Variable> tail, LogarithmBase logBase) throws InconsistentEvidenceException {    
		 VariableEliminationInferenceFactory factory = new VariableEliminationInferenceFactory();
		 QueryOptions qopts = getQueryOptions(factory);
		 QueryOutput qout = getQueryOutput(factory);
		 Inference inf = getInference(factory);
		 inf.setEvidence(evidence);
	     List<Variable> nodelist = new ArrayList<Variable>();
	     nodelist.addAll(head);
	     nodelist.addAll(tail);
	     Table t = new Table(toArray(new ArrayList<Variable>(nodelist)));       
	     inf.getQueryDistributions().add(t);
	     inf.query(qopts, qout);		 
	     ArrayList<VariableContext> conditionOn = new ArrayList<VariableContext>();
	     conditionOn.add(t.getSortedVariables().get(t.getSortedVariables().indexOf(tail.iterator().next())));
	     double entropy = Entropy.calculate(t, conditionOn, logBase);    
	     return entropy;
	 }
	  
	 public double getEntropy(Evidence evidence, Collection<Variable> head, LogarithmBase logBase) throws InconsistentEvidenceException {    
		 VariableEliminationInferenceFactory factory = new VariableEliminationInferenceFactory();
		 QueryOptions qopts = getQueryOptions(factory);
		 QueryOutput qout = getQueryOutput(factory);
		 Inference inf = getInference(factory);
		 inf.setEvidence(evidence);
	     List<Variable> nodelist = new ArrayList<Variable>();
	     nodelist.addAll(head);
	     Table t = new Table(toArray(new ArrayList<Variable>(nodelist)));       
	     inf.getQueryDistributions().add(t);
	     inf.query(qopts, qout);		 
	     ArrayList<VariableContext> conditionOn = new ArrayList<VariableContext>();
	     double entropy = Entropy.calculate(t, conditionOn, logBase);    
	     return entropy;
	 }

	public double getKLDifference(Collection<Variable> vars, Evidence referenceEvidence, Evidence secondEvidence, LogarithmBase logbase) throws InconsistentEvidenceException {
		double result = 0;
		List<State> states = new ArrayList<State>();
		for (Variable v: vars) {
			states.addAll(v.getStates());
		}
		for (State s: states) {
			double refProb = getProbability(referenceEvidence, s);
			double secondProb = getProbability(secondEvidence, s);
			double log = getLogFraction(secondProb, refProb, logbase);
			result += secondProb * log;
		}
		return result;
	}
	
	private double getLogFraction(double num, double denom, LogarithmBase base) {
		if (denom == 0 || num == 0) {
			return 0;
		}
		if (base == LogarithmBase.NATURAL) {
			return Math.log(num / denom);
		}
		if (base == LogarithmBase.TWO) {
			return (Math.log(num / denom)) / Math.log(2);
		}
		return 0;
	}
	
	public double getMutualInformation(Evidence baseevidence, Collection<Variable> probevars, Diagnosis diagnosis, LogarithmBase logbase) throws InconsistentEvidenceException {			
		double initialInfo = getEntropy(baseevidence, diagnosis.getVariables(), logbase);
		double secondInfo = getEntropy(baseevidence, diagnosis.getVariables(), probevars, logbase);
		double info = initialInfo - secondInfo;
		return info;
	}
	
	public double getMutualInformation(Evidence baseevidence, Collection<Variable> probevars, Diagnosis diagnosis, Collection<Variable> dgvars, LogarithmBase logbase) throws InconsistentEvidenceException {			
		double initialInfo = getEntropy(baseevidence, diagnosis.getVariables(), logbase);
		double secondInfo = getEntropy(baseevidence, dgvars, probevars, logbase);
		double info = initialInfo - secondInfo;
		return info;
	}
}
