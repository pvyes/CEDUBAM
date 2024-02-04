package domain;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.bayesserver.Network;
import com.bayesserver.Node;
import com.bayesserver.NodeGroup;
import com.bayesserver.State;
import com.bayesserver.Variable;
import com.bayesserver.analysis.DSeparation;
import com.bayesserver.analysis.DSeparationCategory;
import com.bayesserver.analysis.DSeparationOptions;
import com.bayesserver.analysis.DSeparationTestResult;
import com.bayesserver.analysis.DSeparationTestResultCollection;
import com.bayesserver.inference.DefaultEvidence;
import com.bayesserver.inference.Evidence;
import com.bayesserver.inference.InconsistentEvidenceException;

public class Diagnoser {
	
	public static String PDVS_EVIDENCENAME = "pdvs";
	public static final String ROOTINPUT = "rootinput";
	public static final String OUTPUT = "output";	
	public static final String HEALTH = "health";	
	public static final String LEAF = "leaf";
	
	private Network system;
	private Collection<Variable> pdvs;
	private Collection<NodeGroup> healthgroups; //the groups defining a health variable

	private List<Diagnosis> possiblediagnoses;
	private List<Probe> probes;
	private double defaultcost = 0;
	private EvidenceManager evidencemanager;
	private QueryManager querymanager;
	private InformationFunction informationfunction;
	private UtilityFunction utilityfunction;
	private Strategy strategy;
	private ReportManager reportmanager;
	private CostVariance costvariance;
	private Experiment experiment;
	private ConstantFinder constantfinder;
	
	private MEUResult meuresult;
	
	public Diagnoser() {
		this.pdvs = new ArrayList<Variable>();
		this.evidencemanager = new EvidenceManager(this);
		this.querymanager = new QueryManager(this);
		this.reportmanager = new ReportManager(this);
		this.costvariance = new CostVariance(this);
		this.experiment = new Experiment(this);
		this.healthgroups = new HashSet<NodeGroup>();
		this.probes = new ArrayList<Probe>();
		this.possiblediagnoses = new ArrayList<Diagnosis>();
	}
	
	/** Network and variables **/
	
	public Network setNetwork(Network system) {
		pdvs.clear();
		healthgroups.clear();
		clearPossibleDiagnoses();
		evidencemanager.clearEvidence();
		probes.clear();
		return this.system = system;
	}
	
	public Network getSystem() {
		return system;
	}

	public void addPdvs(Variable var) {
		clearPossibleDiagnoses();
		evidencemanager.clearPdvsEvidence();
		probes.clear();
		pdvs.add(var);
	}
	
	public void clearPdvs() {
		evidencemanager.clearPdvsEvidence();		
		clearPossibleDiagnoses();
		probes.clear();
		pdvs.clear();
	}
	
	private void clearPossibleDiagnoses() {
		possiblediagnoses.clear();
	}

	public Collection<Variable> getPdvs() {
		return pdvs;
	}
	
	public Collection<NodeGroup> getHealthgroups() {
		return healthgroups;
	}
	
	public MEUResult getMeuresult() {
		return meuresult;
	}

	public void setMeuresult(MEUResult meuresult) {
		this.meuresult = meuresult;
	}

	public void setHealthgroups(Collection<NodeGroup> healthgroups) {
		this.healthgroups = healthgroups;
	}
	
	public Collection<Variable> getVariables(String property) {
		Collection<Variable> result = new ArrayList<Variable>();
		switch (property) {
		case ROOTINPUT:
			result = getRootinputVariables();
			break;
		case OUTPUT:
			result = getOutputVariables();
			break;
		case HEALTH:
			result = getHealthVariables();
			break;
		case LEAF:
			result = getLeafVariables();
			break;			
		}
		return result;		
	}

	public Collection<Variable> getLeafVariables() {
		Collection<Variable> result = new ArrayList<Variable>();
		for (Variable var: this.system.getVariables()) {
			if (Networkfunctions.getChildren(var.getNode()).size() == 0) {
				result.add(var);
			}
		}
		return result;
	}
	
	public CostVariance getCostvariance() {
		return costvariance;
	}

	public void setCostvariance(CostVariance costvariance) {
		this.costvariance = costvariance;
	}
	
	public Experiment getExperiment() {
		return experiment;
	}
	
	public ConstantFinder getConstantfinder() {
		return constantfinder;
	}

	public void setConstantfinder(ConstantFinder constantfinder) {
		this.constantfinder = constantfinder;
	}
	
	public void setConstantfinder(UtilityFunction uf) {
		if (uf.getType().equals(UtilityNames.LINEAR_UTILITY)) {
			this.constantfinder = new ConstantFinderLinear(this);
		}
		if (uf.getType().equals(UtilityNames.WEIGHTED_COST)) {
			this.constantfinder = new ConstantFinderWeightedCost(this);
		}
	}

	/*
	 * A variable is a health variable when it is a member of all healthgroups (AND-condition).
	 */
	public Collection<Variable> getHealthVariables() {
		Collection<Variable> result = new ArrayList<Variable>();
		for (Variable var: this.system.getVariables()) {
			boolean ismember = true;
			for (NodeGroup ng: healthgroups) {
				boolean local = Networkfunctions.getGroupsAsString(var).contains(ng.getName());
				ismember = ismember && local;
			}
			if (ismember) {
				result.add(var);
			}
		}
		return result;
	}

	public Collection<Variable> getOutputVariables() {
		Collection<Variable> result = new ArrayList<Variable>();
		for (Variable var: this.system.getVariables()) {
			if (Networkfunctions.getParents(var.getNode()).size() != 0 && !getHealthVariables().contains(var)) {
				result.add(var);
			}
		}
		return result;
	}

	public Collection<Variable> getRootinputVariables() {
		Collection<Variable> result = new ArrayList<Variable>();
		for (Variable var: this.system.getVariables()) {
			if (Networkfunctions.getParents(var.getNode()).size() == 0 && !getHealthVariables().contains(var)) {
				result.add(var);
			}
		}
		return result;
	}
	
	public InformationFunction getInformationfunction() {
		return informationfunction;
	}

	public void setInformationfunction(InformationFunction informationfunction) {
		this.informationfunction = informationfunction;
	}

	public UtilityFunction getUtilityfunction() {
		return utilityfunction;
	}

	public void setUtilityfunction(UtilityFunction utilityfunction) {
		this.utilityfunction = utilityfunction;
		setConstantfinder(utilityfunction);
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy sStrategy) {
		this.strategy = sStrategy;
	}
			
	public ReportManager getReportManager() {
		return reportmanager;
	}

	/** Evidences **/
	//is delegated to the EvidenceManager
	
	public EvidenceManager getEvidencemanager() {
		return evidencemanager;
	}

	/** Probabilities 
	 * @throws InconsistentEvidenceException */
	
	//is delegated to the QueryManager
	
	public QueryManager getQuerymanager() {
		return querymanager;
	}

	/** diagnoses **/
	
	public List<Diagnosis> getPossibleDiagnoses() {
		return possiblediagnoses;
	}
	
	public List<Diagnosis> createPossibleDiagnoses() {
		Map<Variable, Map<State, Double>> pdvsEvidenceMap = getPdvsEvidence();
		possiblediagnoses = setPossibleDiagnoses(system, pdvsEvidenceMap);
		return possiblediagnoses;
	}
	
	public Map<Variable, Map<State, Double>> getPdvsEvidence() {
		Map<Variable, Map<State, Double>> result = new HashMap<Variable, Map<State, Double>>();
		for (Variable var: pdvs) {
			result.put(var, evidencemanager.getEvidence(var).getEvidenceMap());
		}
		return result;
	}

	public List<Diagnosis> setPossibleDiagnoses(Network system, Map<Variable, Map<State, Double>> pdvsEvidenceMap) {
		List<Variable> evVars = new ArrayList<Variable>(pdvsEvidenceMap.keySet());
		Collection<Variable> dVars = Networkfunctions.getRootAncestors(evVars);
		
		int nr = 1;
		List<Diagnosis> pds = new ArrayList<Diagnosis>();
		List<List<State>> stateproducts = getStateProducts(dVars, nr, pds);
		while (stateproducts.size() > 0) {		
			for (Collection<State> sp: stateproducts) {
				List<State> statelist = new ArrayList<State>(sp);
				Evidence evidence = new DefaultEvidence(system);
				for (State s: sp) {
					evidence.setState(s);
				}
				boolean equals;
				try {
					equals = compareStates(system, pdvsEvidenceMap, evidence);
					if (equals) {
						Diagnosis dg = new Diagnosis(statelist);
						dg.setIndex(pds.size());
						pds.add(dg);
					}
				} catch (InconsistentEvidenceException e) {
					System.out.println("Catched exception inconsistent Evidence while computing possible diagnoses on statelist " + statelist +".");
				}
			}
			nr++;
			stateproducts = getStateProducts(dVars, nr, pds);
		}
		return pds;
	}
	
	/*
	 * Makes a set collections of the states of the given variables, containing nrOfElements states per collection. 
	 * Of each variable at most one state is included in the collection.
	 */
	private List<List<State>> getStateProducts(Collection<Variable> vars, int nrOfElements, List<Diagnosis> pds) {
		List<List<State>> result = new ArrayList<List<State>>();
		List<List<State>> products = getStateProducts(vars, nrOfElements);
		for (List<State> resultlist: products) {
			result.add(resultlist);
			for (Diagnosis d: pds) {
				if (resultlist.containsAll(d.getStates())) {
					result.remove(resultlist);
				}
			}
		}
		return result;
	}
	
	/*
	 * Makes a set collections of the states of the given variables, containing nrOfElements states per collection. 
	 * Of each variable at most one state is included in the collection.
	 */
	private List<List<State>> getStateProducts(Collection<Variable> vars, int nrOfElements) {
		List<List<State>> result = new ArrayList<List<State>>();
		List<List<State>> product = new ArrayList<List<State>>(); 
		List<List<State>> lists = new ArrayList<List<State>>();
		int i = 0;
		for (Variable var: vars) {
			List<State> coll = new ArrayList<State>();
			for (State s: var.getStates()) {
				coll.add(s);
			}
			lists.add(i, coll);
			i++;
		}
		//make combinations				
		product = SetCombinations.choose(lists, nrOfElements);
		product.forEach(p -> result.add(p));
		return result;
	}

	/*
	 * Compares the states of the problem defining variables with the outcome (P(pdv | pd)) of the possible diagnoses (pd).
	 * @return true if all the states equal.
	 */
	private boolean compareStates(Network system, Map<Variable, Map<State, Double>> pdvsEvidenceMap, Evidence evidence) throws InconsistentEvidenceException {
		boolean equals = true;
		for (Variable evVar: pdvsEvidenceMap.keySet()) {
			for (State evState: evVar.getStates()) {
				double prob = querymanager.getProbability(evidence, evState);
				equals = equals && (pdvsEvidenceMap.get(evVar).get(evState) == prob);
			}
		}
		return equals;
	}
	
	/* Probes */
	
	public double getDefaultcost() {
		return defaultcost;
	}

	public void setDefaultcost(double defaultcost) {
		this.defaultcost = defaultcost;
	}
	
	public Collection<Probe> createProbes(double cost) throws Exception {
		probes.clear();
		List<Variable> dConnected = getDConnectedVars();
		for (Variable v: dConnected) {
			Probe p = new Probe(this);
			p.setTarget(v);
			p.setCost(cost);
			p.setEnabled(true);
			probes.add(p);
		}			
		return probes;
	}

	public List<Variable> getDConnectedVars() throws Exception {
		List<Variable> dConnected = new ArrayList<Variable>();
		List<Node> sourcenodes = new ArrayList<Node>(getNodes(pdvs));
		List<Node> otherNodes = new ArrayList<Node>(getNodes(system.getVariables()));
		otherNodes.removeAll(sourcenodes);
		List<Node> testnodes = new ArrayList<Node>(otherNodes);
		//get dseparation
		//deactivate evidence on pdvs
		for (Variable pdv: pdvs) {
			this.evidencemanager.activateEvidence(pdv, false);
		}
		DSeparationTestResultCollection dsepresult = DSeparation.calculate(system, sourcenodes, testnodes, new DefaultEvidence(system), new DSeparationOptions()).getTestResults();
		for (DSeparationTestResult d: dsepresult) {
			if (d.getCategory() == DSeparationCategory.D_CONNECTED) {
				dConnected.addAll(d.getNode().getVariables());
			}
		}
		for (Variable pdv: pdvs) {
			this.evidencemanager.activateEvidence(pdv, true);
		}
		//remove variables which are healthvariables
		dConnected.removeAll(getHealthVariables());
		return dConnected;
	}
	
	public List<Probe> getProbes() {
		return probes;
	}
	
	public Probe getProbe(String name) {
		for (Probe p: probes) {
			if (p.getName().toLowerCase().equals(name.toLowerCase())) {
				return p;
			}
		}
		return null;
	}
	
	public List<Probe> getEnabledProbes() {
		List<Probe> result = new ArrayList<Probe>();
		for (Probe p: probes) {
			if (p.isEnabled()) {
				result.add(p);
			}
		}
		return result;
	}
	
	public Probe getProbe(Variable var) {
		for (Probe p: probes) {
			if (p.getTarget() == var) {
				return p;
			}
		}
		return null;
	}

	private static Collection<Node> getNodes(Collection<Variable> variables) {
		Collection<Node> result = new ArrayList<Node>();
		for (Variable v: variables) {
			result.add(v.getNode());
		}	
		return result;
	}
	
	/* Information and utility */

	public List<InformationResult> getInformation(Probe probe, State probestate, List<Integer> diagnoseIndices) throws Exception {
		List<InformationResult> results = new ArrayList<InformationResult>();
		List<Diagnosis> dgs = getDiagnoses(diagnoseIndices);
		if (informationfunction != null) {
			for (Diagnosis dg: dgs) {
				results.add(informationfunction.getInformation(this, probe, probestate, dg));
			}
		}
		return results;
	}

	private List<Diagnosis> getDiagnoses(List<Integer> diagnoseIndices) {
		List<Diagnosis> dgs = new ArrayList<Diagnosis>();
		if (diagnoseIndices != null && diagnoseIndices.size() > 0) {
			for (int i: diagnoseIndices) {
				dgs.add(possiblediagnoses.get(i));
			}
		} else {
			dgs.addAll(possiblediagnoses);
		}
		return dgs;
	}

	public List<InformationResult> getExpectedInformation(Probe probe, List<Integer> diagnoseIndices) throws Exception {
		List<Diagnosis> dgs = getDiagnoses(diagnoseIndices);
		return getExpectedInformation(probe, dgs);
	}
	
	public List<InformationResult> getExpectedInformation(Probe probe, Collection<Diagnosis> diagnoses) throws Exception {
		List<InformationResult> results = new ArrayList<InformationResult>();
		if (informationfunction != null) {
			for (Diagnosis dg: diagnoses) {
				results.add(informationfunction.getExpectedInformation(this, probe, dg));
			}
		}
		return results;
	}
		
	public List<UtilityResult> getUtility(Probe probe, State probestate, List<Integer> diagnoseIndices) throws Exception {
		List<Diagnosis> dgs = getDiagnoses(diagnoseIndices);
		return getUtility(probe, probestate, dgs);
	}
	
	public List<UtilityResult> getUtility(Probe probe, State probestate, Collection<Diagnosis> diagnoses) throws Exception {
		List<UtilityResult> results = new ArrayList<UtilityResult>();
		if (utilityfunction != null) {
			for (Diagnosis dg: diagnoses) {
				InformationResult inforesult = informationfunction.getInformation(this, probe, probestate, dg);
				results.add(utilityfunction.getUtility(inforesult));
			}
		}
		return results;
	}
	
	public List<UtilityResult> getExpectedUtility(Probe probe, List<Integer> diagnoseIndices) throws Exception {
		Collection<Diagnosis> dgs = getDiagnoses(diagnoseIndices);
		return getExpectedUtility(probe, dgs);
	}
	
	public List<UtilityResult> getExpectedUtility(Probe probe, Collection<Diagnosis> diagnoses) throws Exception {
		List<UtilityResult> results = new ArrayList<UtilityResult>();
		if (utilityfunction != null) {
			for (Diagnosis dg: diagnoses) {
				InformationResult inforesult = informationfunction.getExpectedInformation(this, probe, dg);
				results.add(utilityfunction.getUtility(inforesult));
			}
		}
		return results;
	}

	public Map<Probe, InformationResult> getWeightedExpectedInformationPerProbe(Collection<Probe> probes, Collection<Diagnosis> diagnoses) throws Exception {
		Map<Probe, InformationResult> result = new HashMap<Probe, InformationResult>();
		for (Probe p: probes) {
			InformationResult inforesult = getWeightedExpectedInformationPerProbe(p, diagnoses);
			result.put(p, inforesult);
		}
	return result;
	}
	
	public InformationResult getWeightedExpectedInformationPerProbe(Probe probe, Collection<Diagnosis> diagnoses) throws Exception {
		InformationResult result = new InformationResult(this.informationfunction.getInformationtype());
		double sum = 0;
		result.setProbe(probe);
		for (Diagnosis dg: diagnoses) {
			InformationResult inforesult = informationfunction.getExpectedInformation(this, probe, dg);
			result.getUsedInformationResults().add(inforesult);
			sum += inforesult.getInformation() * inforesult.getDiagnosisProbability();
		}
		result.setInformation(sum);
		return result;
	}
	
	public InformationResult getWeightedExpectedInformationPerProbe(Probe probe, List<Integer> diagnoseIndices) throws Exception {
		InformationResult result = new InformationResult(this.informationfunction.getInformationtype());
		double sum = 0;
		result.setProbe(probe);
		List<Diagnosis> dgs = getDiagnoses(diagnoseIndices);
		for (Diagnosis dg: dgs) {
			InformationResult inforesult = informationfunction.getExpectedInformation(this, probe, dg);
			result.getUsedInformationResults().add(inforesult);
			sum += inforesult.getInformation() * inforesult.getDiagnosisProbability();
		}
		result.setInformation(sum);
		return result;
	}
	
	public MEUResult getMEU(Collection<Probe> probes, Collection<Diagnosis> diagnoses) throws Exception {
		return getMEUInfoPerProbe(probes, diagnoses);
	}
	
	public MEUResult getMEU(List<Integer> diagnoseIndices) throws Exception {
		Collection<Diagnosis> diagnoses = getDiagnoses(diagnoseIndices);
		return getMEUUtilityPerProbe(getEnabledProbes(), diagnoses);
	}
	
	/* Get the MEU where the weighting for the probability of the diagnoses is done on the expected information.
	 * The utility is computed on the weighted expected information per probe.
	 **/
	public MEUResult getMEUInfoPerProbe(Collection<Probe> probes2, Collection<Diagnosis> diagnoses) throws Exception {
		Map<Probe, InformationResult> infos = getWeightedExpectedInformationPerProbe(probes2, diagnoses);
		List<UtilityResult> results = new ArrayList<UtilityResult>();
		if (utilityfunction.getType() == UtilityNames.WEIGHTED_COST) {
			((UtilityWeightedCost) utilityfunction).setAlpha(infos);
		}
		for (Entry<Probe, InformationResult> entry: infos.entrySet()) {
			results.add(utilityfunction.getUtility(entry.getValue()));
		}
		Map<Probe, UtilityResult> utilsums = new HashMap<Probe, UtilityResult>();
		Map<Probe, Double> sumsPerProbe = new HashMap<Probe, Double>();
		for (UtilityResult res: results) {
			utilsums.put(res.getProbe(), res);
			sumsPerProbe.put(res.getProbe(), res.getUtility());
		}
		MEUResult meu = new MEUResult(results);
		meu.setSumPerProbe(sumsPerProbe);
		getHighest(meu, results);
		return meu;
	}
	
	/* Get the MEU where the weighting for the probability of the diagnoses is done on the expected utilities.
	 * The utility is computed per diagnosis per probe.
	 */
	public MEUResult getMEUUtilityPerProbe(Collection<Probe> probes2, Collection<Diagnosis> diagnoses) throws Exception {
		List<UtilityResult> results = new ArrayList<UtilityResult>();
		for (Probe probe: probes2) {
			results.addAll(getExpectedUtility(probe, diagnoses));
		}
		Map<Probe, Collection<UtilityResult>> weighted = getWeightedUtilities(results, probes2);
		MEUResult meu = new MEUResult(results);
		meu.setWeightedresults(weighted);
		Map<Probe, Double> sums = getSum(weighted);
		meu.setSumPerProbe(sums);
		getHighest(meu, weighted);
		return meu;
	}

	private void getHighest(MEUResult meu, Map<Probe, Collection<UtilityResult>> weighted) {
		double highest = Double.NaN;		
		for (Entry<Probe, Collection<UtilityResult>> entry: weighted.entrySet()) {
			double total = getSum(entry.getValue());
			if (Double.isNaN(highest) || total > highest) {
				meu.setUtility(total);
				meu.getMeuresults().clear();
				meu.getMeuresults().addAll(entry.getValue());
				highest = total;
			}
			if (total == highest) {
				meu.getMeuresults().addAll(entry.getValue());
			}
		}
	}
	
	private void getHighest(MEUResult meu, Collection<UtilityResult> results) {
		double highest = Double.NaN;		
		for (UtilityResult ur: results) {
			if (Double.isNaN(highest) || ur.getUtility() > highest) {
				meu.setUtility(ur.getUtility());
				meu.getMeuresults().clear();
				meu.getMeuresults().add(ur);
				highest = ur.getUtility();
			}
			if (ur.getUtility() == highest) {
				meu.getMeuresults().add(ur);
			}
		}		
	}
	
	private Map<Probe, Double> getSum(Map<Probe, Collection<UtilityResult>> weighted) {
		Map<Probe, Double> sums = new HashMap<Probe, Double>();	
		for (Entry<Probe, Collection<UtilityResult>> entry: weighted.entrySet()) {
			sums.put(entry.getKey(), getSum(entry.getValue()));
		}
		return sums;	
	}

	private double getSum(Collection<UtilityResult> results) {
		double sum = 0.0;
		for (UtilityResult ur: results) {
			sum += ur.getUtility();
		}
		return sum;
	}

	private Map<Probe, Collection<UtilityResult>> getWeightedUtilities(List<UtilityResult> results, Collection<Probe> probes2) throws InconsistentEvidenceException {
		Map<Probe, Collection<UtilityResult>> result = new HashMap<Probe, Collection<UtilityResult>>();
		//prepare resultmap
		for (Probe dg: probes2) {
			result.put(dg, new ArrayList<UtilityResult>());
		}

		for (UtilityResult ur: results) {
			Probe p = ur.getProbe();
			UtilityResult weighted = getWeightedUtility(ur);
			result.get(p).add(weighted);
		}
		return result;
	}

	private UtilityResult getWeightedUtility(UtilityResult ur) {
		UtilityResult utilityresult = new UtilityResult(ur.getInformationresult());
		utilityresult.setUtility(ur.getUtility() * ur.getInformationresult().getDiagnosisProbability());
		return utilityresult;
	}
	
	public void setLinearUtilityParameters(double a, double s) {
		if (utilityfunction.getType() == UtilityNames.LINEAR_UTILITY) {
			if (!Double.isNaN(a)) {
				((UtilityLinear) utilityfunction).setA(a);
			}
			if (!Double.isNaN(s)) {
				((UtilityLinear) utilityfunction).setS(s);
			}
		}	
	}

	public void setWeightedCostUtilityParameter(double ratio) {
		if (utilityfunction.getType() == UtilityNames.WEIGHTED_COST) {
			if (!Double.isNaN(ratio)) ((UtilityWeightedCost) utilityfunction).setRatio(ratio);
		}
	}

	public void setInfoPerCostUtilityParameter(double c) {
		if (utilityfunction.getType() == UtilityNames.INFORMATION_PER_COST) {
			if (!Double.isNaN(c)) ((UtilityInformationPerCost) utilityfunction).setC(c);
		}
		
	}
	
	public List<ProbeScenario> runProbeSequencer() throws Exception {
		ProbeSequencer seq = new ProbeSequencer(this);
		return seq.run();
	}

	public void printScenario(ProbeScenario ps) throws Exception {
		reportmanager.export(ps);		
	}
	
	public List<File> getReports() {
		return reportmanager.getReportList();
	}
	
	public String showSettings() {
		String str = "";
		str += "Settings:" + "\n";
		str += "Network: " + this.getSystem().getName() + "\n";
		str += "Problem defining variables: " + this.getPdvsEvidence()  + "\n";
		str += "Default cost: " + this.defaultcost + "\n";
		str += "Costvariance: " + this.costvariance.getType() + "\n";
		if (this.getInformationfunction() != null) {
			str += "Information function: " + this.getInformationfunction().getInformationtype() + "\n";
		}
		if (this.getUtilityfunction() != null) {
			str += "Utility function: " + this.getUtilityfunction().settingsToString() + "\n";
		}
		if (this.getStrategy() != null) {
			str += "Strategy: " + this.getStrategy().getName() + "\n";
		}
		str += "\n";
		str += "Probes: "  + "\n";
		str += this.probesToString()  + "\n";;
		str += "Diagnoses: "  + "\n";
		str += possiblediagnosesToString(this.getPossibleDiagnoses());
		return str;
	}
	
	public String possiblediagnosesToString(List<Diagnosis> pds) {
		String str = "";
		for (Diagnosis s: pds) {
			str += s.toString();
			try {
				double prob = this.getQuerymanager().getProbability(this.getEvidencemanager().getActiveEvidence(), s.getStates());
				str += " (probability=" + prob + ")" + "\n";
			} catch (InconsistentEvidenceException e) {
				str += "Probability could not be computed because of an error (" + e.getMessage() + ")." + "\n";
			}
		}
		if (str.length() > 0) {
			str = str.substring(0, str.length() - 1);
		} else {
			str = "No diagnoses available";
		}
		return str;
	}
	
	public String probesToString() {
		String str = "";
		for (Probe p: this.getProbes()) {
			str += p.toString() + "\n";
		}
		if (str.length() > 0) {
			str = str.substring(0, str.length() - 1);
		} else {
			str = "No probes available";
		}
		return str;
	}

	public <T> Collection<Collection<T>> getRandomPolarDivision(Collection<T> els) {
		List<Collection<T>> result = new ArrayList<Collection<T>>();
		result.add(new ArrayList<T>());
		result.add(new ArrayList<T>());
		for (T el: els) {
			Random r = new Random();
			int i = r.nextInt(2);
			result.get(i).add(el);
		}
		return result;
	}

	public <T> List<T> getRandomDivision(List<T> els) {
		List<T> tmp = new ArrayList<T>(els);
		List<T> result = new ArrayList<T>();
		while (tmp.size() > 0) {
			Random r = new Random();
			int i = r.nextInt(tmp.size());
			result.add(tmp.get(i));
			tmp.remove(i);
		}
		return result;
	}

	public int getNrOfProbeCombinations() {
		return factorial(probes.size()).intValue();
	}
	
	private BigInteger factorial(int n) {
	    BigInteger result = BigInteger.ONE;
	    for (int i = 2; i <= n; i++)
	        result = result.multiply(BigInteger.valueOf(i));
	    return result;
	}

	public double getHighestProbeCost() {
		double highest = Double.NaN;
		for (Probe p: probes) {
			if (Double.isNaN(highest)) {
				highest = p.getCost();
			}
			if (p.getCost() > highest) {
				highest = p.getCost();
			}
		}
		return highest;
	}
}
