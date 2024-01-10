package console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bayesserver.Network;
import com.bayesserver.NodeGroup;
import com.bayesserver.State;
import com.bayesserver.Variable;

import domain.CostVariance.CostVarianceType;
import domain.Diagnoser;
import domain.EvidenceMap;
import domain.InformationFunction;
import domain.InformationFunctions;
import domain.NamedEvidence;
import domain.Networkfactory;
import domain.ReportManager.Suffixtype;
import domain.Probe;
import domain.Strategies;
import domain.Strategy;
import domain.UtilityFunction;
import domain.UtilityFunctions;
import domain.UtilityWeightedCost;
import domain.UtilityNames;

public class ConsoleSet extends Console{

	public ConsoleSet(Diagnoser diagnoser) {
		super(diagnoser);
	}

	public boolean handleCommand(List<String> args, int nextarg) {
		String subject = args.get(nextarg++);
		switch (subject) {
			case "network": 
				return setNetwork(args, nextarg);
			case "healthgroups": 
				return setHealthgroupsk(args, nextarg);
			case "pdvs": 
				return setPdvs(args, nextarg);
			case "evidence": 
				return setEvidence(args, nextarg);
			case "defaultcost": 
				return setDefaultcost(args, nextarg);
			case "probes": 
				return setProbes(args, nextarg);
			case "probe": 
				return setProbe(args, nextarg);
			case "informationfunction": 
				return setInformationfunction(args, nextarg);
			case "utilityfunction": 
				return setUtilityfunction(args, nextarg);
			case "strategy": 
				return setStrategy(args, nextarg);
			case "report": 
				return setReport(args, nextarg);
			case "costvariance": 
				return setCostvariance(args, nextarg);
			case "experiment": 
				return setExperiment(args, nextarg);
			default:
				ConsoleInOut.printErrormessage("Command not found");
		}
		return false;
	}

	private boolean setExperiment(List<String> args, int nextarg) {
		Map<String, String> params = getParameters(args);
		if (params.get("infoprevalence") != null && diagnoser.getUtilityfunction() != null) {
			if (diagnoser.getUtilityfunction().getType() == UtilityNames.WEIGHTED_COST) {
				if (params.get("infoprevalence").toLowerCase().equals("clear")) {
					((UtilityWeightedCost) diagnoser.getUtilityfunction()).setInfoprevalences(new ArrayList<Boolean>());
				} else {
					List<Boolean> infoprevals = getInfoPrevals(params.get("infoprevalence"));
					((UtilityWeightedCost) diagnoser.getUtilityfunction()).setInfoprevalences(infoprevals);
				}
			} else {
				ConsoleInOut.printErrormessage("Infoprevalences cannot be set on utilitytype " + diagnoser.getUtilityfunction().getType());
			}
		}
		if (params.get("count") != null) {
			if (params.get("count").toLowerCase().equals("all")) {
				diagnoser.getExperiment().setAllCombinations(true);
			} else {
				diagnoser.getExperiment().setAllCombinations(false);
				diagnoser.getExperiment().setCounter(Integer.parseInt(params.get("count")));
			}			
		}
		ConsoleInOut.printMessage("The experiment is set to:\n" + diagnoser.getExperiment().getSettings());
		return true;
	}

	private boolean setCostvariance(List<String> args, int nextarg) {
		String type = args.get(nextarg++);
		switch (type.toLowerCase()) {
			case "equal": 
				return setEqualCostvariance(args, nextarg);
			case "polar": 
				return setPolarCostvariance(args, nextarg);
			case "scattered": 
				return setScatteredCostvariance(args, nextarg);
			default:
				ConsoleInOut.printErrormessage("Command not found");
		}
		return false;
	}
	
	private boolean setEqualCostvariance(List<String> args, int nextarg) {
		Map<String, String> params = getParameters(args);
		double cost = diagnoser.getDefaultcost();
		List<Probe> probes = diagnoser.getProbes();
		if (params.get("cost") != null) {
			cost = Double.parseDouble(params.get("cost"));
		}
		if (args.size() > 4) {
			probes = new ArrayList<Probe>();
			for (int i = nextarg; i < args.size(); i++)  {
				if (!args.get(i).contains(":")) {
	
					probes.add(getProbe(args.get(i)));
				}
			}
		}
		try {
			diagnoser.getCostvariance().setType(CostVarianceType.EQUAL);
			diagnoser.getCostvariance().setEqualCosts(cost, probes);
			ConsoleInOut.printMessage("The cost variance is set to " + diagnoser.getCostvariance().getType() + " with cost " + cost + " for probes " + getProbeNames(probes) + ".");
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage("The cost variance could not be set.");
		}
		return false;
	}
	


	private boolean setPolarCostvariance(List<String> args, int nextarg) {
		
		Map<String, String> params = getParameters(args);
		double min = diagnoser.getDefaultcost();
		double max = diagnoser.getDefaultcost();
		List<Probe> minprobes = new ArrayList<Probe>();
		List<Probe> maxprobes = new ArrayList<Probe>();
		if (params.get("min") != null) {
			min = Double.parseDouble(params.get("min"));
		} else {
			return false;
		}
		if (params.get("max") != null) {
			max = Double.parseDouble(params.get("max"));
		} else {
			return false;
		}
		try {
			diagnoser.getCostvariance().setType(CostVarianceType.POLAR);
			if (params.get("distribution") != null && (params.get("distribution").toLowerCase().equals("random"))) {
				diagnoser.getCostvariance().setRandomPolarCosts(min, max);
				ConsoleInOut.printMessage("The cost variance is set to " + diagnoser.getCostvariance().getType() + " randomly.");
				return true;
			}
			int indexMin = -1;
			int indexMax = -1;
			for (String arg: args) {
				if (arg.contains("min:")) {
					indexMin = args.indexOf(arg);
				}
				if (arg.contains("max:")) {
					indexMax = args.indexOf(arg);
				}
			}
			for (int i = indexMin + 1; i < indexMax; i++)  {
				minprobes.add(getProbe(args.get(i)));
			}
			for (int i = indexMax + 1; i < args.size(); i++)  {
				maxprobes.add(getProbe(args.get(i)));
			}
			if (minprobes.size() > 0 && maxprobes.size() > 0) {
				diagnoser.getCostvariance().setPolarCosts(min, max, minprobes, maxprobes);
				ConsoleInOut.printMessage("The cost variance is set to " + diagnoser.getCostvariance().getType() + " with min " + min  + " for probes " + getProbeNames(minprobes)+ " and max " + max + " for probes " + getProbeNames(maxprobes) + ".");
			} else {
				diagnoser.getCostvariance().setMin(min);
				diagnoser.getCostvariance().setMax(max);
				ConsoleInOut.printMessage("The cost variance is set to " + diagnoser.getCostvariance().getType() + " with min " + min  + " and max " + max + ".");		
			}
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage("The cost variance could not be set.");
		}
		return false;
	}
	
	private boolean setScatteredCostvariance(List<String> args, int nextarg) {
		Map<String, String> params = getParameters(args);
		double min = diagnoser.getDefaultcost();
		double max = diagnoser.getDefaultcost();
		List<Probe> probes = new ArrayList<Probe>();
		if (params.get("min") != null) {
			min = Double.parseDouble(params.get("min"));
		} else {
			return false;
		}
		if (params.get("max") != null) {
			max = Double.parseDouble(params.get("max"));
		} else {
			return false;
		}
		try {
			diagnoser.getCostvariance().setType(CostVarianceType.SCATTERED);
			if (params.get("distribution") != null && (params.get("distribution").toLowerCase().equals("random"))) {
				diagnoser.getCostvariance().setRandomScatteredCosts(min, max);
				ConsoleInOut.printMessage("The cost variance is set to " + diagnoser.getCostvariance().getType() + " randomly.");
				return true;
			}
			int indexMax = -1;
			for (String arg: args) {
				if (arg.contains("max:")) {
					indexMax = args.indexOf(arg);
				}
			}
			for (int i = indexMax + 1; i < args.size(); i++)  {
				probes.add(getProbe(args.get(i)));
			}
			if (probes.size() > 0) {
				diagnoser.getCostvariance().setScatteredCosts(min, max, probes);
				ConsoleInOut.printMessage("The cost variance is set to " + diagnoser.getCostvariance().getType() + " with min " + min  + " and max " + max + " for probes " + getProbeNames(probes) + ".");
			} else {
				diagnoser.getCostvariance().setMin(min);
				diagnoser.getCostvariance().setMax(max);
				ConsoleInOut.printMessage("The cost variance is set to " + diagnoser.getCostvariance().getType() + " with min " + min  + " and max " + max + ".");		
			}
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage("The cost variance could not be set.");
		}
		return false;
	}

	private boolean setReport(List<String> args, int nextarg) {
		Map<String, String> params = getParameters(args);
		if (params.get("displaydetail") != null) {
			diagnoser.getReportManager().setDisplaydetail(Integer.parseInt(params.get("displaydetail")));
			ConsoleInOut.printMessage("Report detaillevel set to " + diagnoser.getReportManager().getDisplaydetail() + ".");
		}
		if (params.get("exportdetail") != null) {
			diagnoser.getReportManager().setExportdetail(Integer.parseInt(params.get("exportdetail")));
			ConsoleInOut.printMessage("Report detaillevel set to " + diagnoser.getReportManager().getExportdetail() + ".");
		}
		if (params.get("display") != null) {
			diagnoser.getReportManager().setExport(params.get("display").toLowerCase().equals("true"));
			ConsoleInOut.printMessage("Report detail set to " + diagnoser.getReportManager().isDisplay() + ".");
		}
		if (params.get("export") != null) {
			diagnoser.getReportManager().setExport(params.get("export").toLowerCase().equals("true"));
			ConsoleInOut.printMessage("Report export set to " + diagnoser.getReportManager().isDisplay() + ".");
		}
		if (params.get("exportfile") != null) {
			diagnoser.getReportManager().setExport(params.get("exportfile").toLowerCase().equals("true"));
			ConsoleInOut.printMessage("Report export set to " + diagnoser.getReportManager().isExport() + ".");
		}
		if (params.get("path") != null) {
			diagnoser.getReportManager().setFolder(params.get("path"));
			ConsoleInOut.printMessage("Report path set to " + diagnoser.getReportManager().getFolder() + ".");
		}
		if (params.get("filename") != null) {
			diagnoser.getReportManager().setFilename(params.get("filename"));
			ConsoleInOut.printMessage("Report name set to " + diagnoser.getReportManager().getFilename() + ".");
		}
		if (params.get("csvname") != null) {
			diagnoser.getReportManager().setCsvname(params.get("csvname"));
			ConsoleInOut.printMessage("Report name set to " + diagnoser.getReportManager().getCsvname() + ".");
		}
		if (params.get("suffix") != null) {
			diagnoser.getReportManager().setSuffixtype(Suffixtype.valueOf(params.get("suffix").toUpperCase()));
			ConsoleInOut.printMessage("Report suffix set to " + diagnoser.getReportManager().getSuffixtype().toString() + ".");
		}
		if (params.get("counter") != null) {
			diagnoser.getReportManager().setCounter(Integer.parseInt(params.get("counter")));
			ConsoleInOut.printMessage("Report counter set to " + diagnoser.getReportManager().getCounter() + ".");
		}
		return true;
	}

	private boolean setStrategy(List<String> args, int nextarg) {
		try {
			int index = Integer.parseInt(args.get(nextarg));
			diagnoser.setStrategy(Strategies.getStrategies(diagnoser).get(index));
			ConsoleInOut.printMessage("Strategy " + diagnoser.getStrategy().getName() + " has been set.");
			return true;
		} catch (Exception e) {
			diagnoser.setStrategy(getStrategyByName(args.get(nextarg)));
			ConsoleInOut.printMessage("Strategy " + diagnoser.getStrategy().getName() + " has been set.");
			return true;
		}
	}

	private Strategy getStrategyByName(String string) {
		for (Strategy s: Strategies.getStrategies(diagnoser)) {
			if (s.getName().toString().toLowerCase().equals(string.toLowerCase())) {
				return s;
			}
		}
		return null;
	}

	private boolean setUtilityfunction(List<String> args, int nextarg) {
		try {
			int index = -1;
			if (hasIntegertype(args.get(nextarg))) {
				index = Integer.parseInt(args.get(nextarg));
				diagnoser.setUtilityfunction(UtilityFunctions.getUtilityfunctions().get(index));
			} else {
				diagnoser.setUtilityfunction(getUtilityfunctionByName(args.get(nextarg)));
			}
			Map<String, String> params = getParameters(args);
			//diagnoser.setUtilityfunction(SUtilityFunctions.getUtilityfunctions().get(index));
			if (diagnoser.getUtilityfunction().getType() == UtilityNames.INFORMATION_PER_COST) {
				double c = Double.NaN;
				if (params.containsKey("c")) {
					c = Double.parseDouble(params.get("c"));
				}
				diagnoser.setInfoPerCostUtilityParameter(c);
			}
			if (diagnoser.getUtilityfunction().getType() == UtilityNames.LINEAR_UTILITY) {
				double a = Double.NaN;
				double s = Double.NaN;
				if (params.containsKey("a")) {
					a = Double.parseDouble(params.get("a"));
				}
				if (params.containsKey("s")) {
					s = Double.parseDouble(params.get("s"));
				}
				diagnoser.setLinearUtilityParameters(a, s);
			}
			if (diagnoser.getUtilityfunction().getType() == UtilityNames.WEIGHTED_COST) {
				double alpha = Double.NaN;
				if (params.containsKey("alpha")) {
					alpha = Double.parseDouble(params.get("alpha"));
				}
				diagnoser.setWeightedCostUtilityParameter(alpha);
			}
			ConsoleInOut.printMessage("Utilityfunction " + diagnoser.getUtilityfunction().settingsToString() + "\n" + diagnoser.getUtilityfunction().functionToString() + "has been set.");
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage("The utilityfunction could not be set.");
			return false;
		}
	}

	private UtilityFunction getUtilityfunctionByName(String string) {
		for (UtilityFunction s: UtilityFunctions.getUtilityfunctions()) {
			if (s.getType().toString().toLowerCase().equals(string.toLowerCase())) {
				return s;
			}
		}
		return null;
	}
	
	private boolean setInformationfunction(List<String> args, int nextarg) {
		try {
			int index = Integer.parseInt(args.get(nextarg));
			diagnoser.setInformationfunction(InformationFunctions.getInformationfunctions().get(index));
			ConsoleInOut.printMessage("Information " + diagnoser.getInformationfunction().getInformationtype().toString() + " has been set.");
			return true;
		} catch (Exception e) {
			diagnoser.setInformationfunction(getInformationfunctionByName(args.get(nextarg)));
			ConsoleInOut.printMessage("Information " + diagnoser.getInformationfunction().getInformationtype().toString() + " has been set.");
			return true;
		}
	}

	private InformationFunction getInformationfunctionByName(String string) {
		for (InformationFunction s: InformationFunctions.getInformationfunctions()) {
			if (s.getInformationtype().toString().toLowerCase().equals(string.toLowerCase())) {
				return s;
			}
		}
		return null;
	}

/*		
	private List<List<String>> getArgs(String[] cmdlist) {
		String[] result = new String[cmdlist.length - 2];
		for (int i = 2; i < cmdlist.length; i++) {
			result[i - 2] = cmdlist[i];
		}
		return splitArgs(result);
	}
*/
	private  boolean setNetwork(List<String> args, int nextarg) {
		try {
			int index = -1;
			String network = args.get(nextarg++);
			Network system = null;
			if (!hasIntegertype(network)) {
				system = Networkfactory.loadBayesNetwork(network);
			} else {
				index = Integer.parseInt(network);
				system = Networkfactory.loadBayesNetwork(Networkfactory.getAvailableNetworks().get(index));
			}
			diagnoser.setNetwork(system);
			ConsoleInOut.printMessage("You choose network: " + system.getName());
			return true;
		} catch (Exception e) {
			String msg = "The network could not be loaded.";
			msg += "\n" + e;
			ConsoleInOut.printErrormessage(msg);
			return false;
		}
	}
	
	private boolean setHealthgroupsk(List<String> args, int nextarg) {
		String str = "";
		diagnoser.getHealthgroups().clear();
		for (int i = nextarg; i < args.size(); i++)  {
			NodeGroup ng = getNodeGroupByName(args.get(i));
			if (ng == null) {
				ConsoleInOut.printErrormessage("Nodegroup " + args.get(i) + " not found. Setting is aborted.");
				diagnoser.getHealthgroups().clear();
				return false;
			}
			diagnoser.getHealthgroups().add(getNodeGroupByName(args.get(i)));
			str += args.get(i) + SEPARATOR;
		}
		if (str.length() > SEPARATOR.length()) {
			str = str.substring(0, str.length() - SEPARATOR.length());
		}
		ConsoleInOut.printMessage("Healthgroups set to: " + str);
		return true;
	}
	
	
	private NodeGroup getNodeGroupByName (String name) {
		for (NodeGroup g: diagnoser.getSystem().getNodeGroups()) {
			if (g.getName().toLowerCase().equals(name.toLowerCase())) {
				return g;
			}
		}
		return null;
	}
	
	private boolean setPdvs(List<String> args, int nextarg) {
		diagnoser.clearPdvs();
		for (int i = nextarg; i < args.size() && args.get(i).indexOf(":") == -1; i++) {
			Variable var = getVariable(args.get(i));
			diagnoser.addPdvs(var);
			if (args.get(i + 1).indexOf(":") > -1) {
				i = i + var.getStates().size();
			} else {
				i++;
			}
		}
		ConsoleInOut.printMessage("Problem defining variables: " + ConsoleInOut.getNameList(diagnoser.getPdvs()));
		//for pdvs default evidence activity is true 
		Map<String, String> params = getParameters(args);
		if (params.get("active") == null) {
			args.add("active:true");
		}
		return setEvidence(args, nextarg);
	}
	
	private boolean setDefaultcost(List<String> args, int nextarg) {
		diagnoser.setDefaultcost(Double.parseDouble(args.get(nextarg)));
		ConsoleInOut.printMessage("Default cost is set to " + diagnoser.getDefaultcost());
		return true;
	}

	private boolean setProbes(List<String> args, int nextarg) {
		try {
			Map<String, String> params = getParameters(args);
			double cost = diagnoser.getDefaultcost();
			if (params.get("cost") != null) {
				cost = Double.parseDouble(params.get("cost"));
			}
			Collection<Probe> probes = diagnoser.createProbes(cost);
			String msg = "Probes created: "+ CRLF;
			for (Probe p: probes) {
				msg += p.toString() + CRLF;
			}
			ConsoleInOut.printMessage(msg.substring(0, msg.length() * CRLF.length()));
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage(e.getMessage());
			return false;
		}			
	}	
	
	private boolean setProbe(List<String> args, int nextarg) {
		try {
			String str = "";
			for (int i = nextarg; i < args.size() && args.get(i).indexOf(":") == -1; i++) {
				Probe p = getProbe(args.get(i));
				str = "Probe modified:" + CRLF;
				if (p == null) {
					p = new Probe(diagnoser);
					p.setTarget(getVariable(args.get(i)));
					diagnoser.getProbes().add(p);
					str = "Probe created:" + CRLF;
				}
				while (i < args.size() - 1 && args.get(i + 1).indexOf(":") > -1) {
					i++;
					String property = args.get(i).split(":")[0];
					String value = args.get(i).split(":")[1];
					setProperty(p, property, value);
				}
				str += p.toString() + CRLF;
			}
			ConsoleInOut.printMessage(str.substring(0, str.length() - CRLF.length()));
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage(e.getMessage());
			return false;
		}
	}
		
	private void setProperty(Probe p, String property, String value) {
		switch (property) {
			case "cost": p.setCost(Double.parseDouble(value));
			break;
			case "name": p.setName(value);
			break;
			case "enabled": p.setEnabled(value.toLowerCase().equals("true"));
			break;
		}		
	}

/*		for (; nextarg < args.size(); nextarg++) {
			String[] params = args.get(nextarg).split(":");
			Variable var = null;
			var = getVariable(params[0]);
			State state = null;
			if (params.length > 1) {
				state = getState(params[0], params[1]);
			}
			diagnoser.getPdvs().add(var);
			if (state != null) {
				
			}
		}
*/
/*
	private boolean setPdvsOnDiagnoser(Collection<Integer> indices) {
		try {
			diagnoser.setPdvs(indices);
			ConsoleInOut.printMessage("You choose problem defining variables: " + ConsoleInOut.getNameList(diagnoser.getPdvs()));
			return true;
		} catch (Exception e) {
			String msg = "The network could not be loaded.";
			msg += "\n" + e;
			ConsoleInOut.printErrormessage(msg);
			return false;
		}
	}

	private Collection<Integer> chooseVariables(Collection<Integer> indices) throws NumberFormatException, IOException {
		indices = ConsoleInOut.chooseVariables(diagnoser.getSystem().getVariables());
		return indices;
	}
/*
	private List<Integer> getVariablesIndices(List<String> varstrings) {
		List<Integer> result = new ArrayList<Integer>();
		if (!hasIntegertype(varstrings)) {
			for (String s: varstrings) {
				//result.add(SNetwork.getVariableIndexByName(s));
			}
		} else {
			getIntegerList(result, varstrings);
		}
		return result;
	}
*/
/*	private void getIntegerList(Collection<Integer> indices, List<String> vars) {
		for (var i = 0; i < vars.size(); i++) {
			if (vars.get(i).length() > 0) {
				indices.add(Integer.parseInt(vars.get(i)));
			}
		}
	}
	
	private List<List<String>> splitArgs(String[] args) {
		List<List<String>> splitArgs = new ArrayList<List<String>>();
		for (var i = 0; i < args.length; i++) {
			if (args[i].length() > 0) {
				String[] newarray = args[i].split(",");
				List<String> newlist = new ArrayList<String>();
				for (String s: newarray) {
					newlist.add(s);
				}
				splitArgs.add(newlist);
			}
		}
		return splitArgs;
	}

	private boolean setEvidence(List<List<String>> argslist, String name) {
		List<String> vars = argslist.get(0);
		List<List<String>> evlist = getEvidencelist(argslist, vars);
		return setEvidenceOnDiagnoser(vars, evlist, name);
	}
*/
	private boolean setEvidence(List<String> args, int nextarg) {
		Map<String, String> params = getParameters(args);
		List<EvidenceMap> evidences = new ArrayList<EvidenceMap>();
		String evstr = "";
		try {
			if (args.get(nextarg).indexOf(":") != -1) {
				evstr = handleParameterEvidence(params);
			} else {
				evstr = setEvidence(args, nextarg, params, evidences, evstr);			
				evstr = setNamedEvidence(params, evidences, evstr);
			}
			String msg = "Evidence has been set.\n";
			msg += evstr;
			ConsoleInOut.printMessage(msg);
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage(e.getMessage());
			//e.printStackTrace();
			return false;
		}
	}

	private String handleParameterEvidence(Map<String, String> params) throws Exception {
		String evstr = "";
		if (params.get("variable") != null) {
			Collection<Variable> vars = getVariablesFromString(params.get("variable"));
			evstr = handleVariableEvidence(vars, params);
		}
		if (params.get("name") != null && params.get("active") != null) {
			Collection<Variable> vars = diagnoser.getEvidencemanager().getNamedEvidence(params.get("name")).getVariables();
			evstr = handleVariableEvidence(vars, params);
		}
		return evstr;
	}

	private String setNamedEvidence(Map<String, String> params, List<EvidenceMap> evidences, String evstr) {
		NamedEvidence ne = null;
		if (params.get("name") != null) {
			diagnoser.getEvidencemanager().removeNamedEvidence(params.get("name"));
			ne = diagnoser.getEvidencemanager().addNamedEvidence(params.get("name"));
			ne.addEvidence(evidences);
		}
		if (ne != null) {
			evstr = ne.toString();
		}
		return evstr;
	}
	
	private String setEvidence(List<String> args, int nextarg, Map<String, String> params, List<EvidenceMap> evidences, String evstr) throws Exception {
		for (; nextarg < args.size() && args.get(nextarg).indexOf(":") == -1; nextarg++) {
			Variable var = getVariable(args.get(nextarg++));
			String statestr = args.get(nextarg);
			if (statestr.indexOf(":") == -1) {
				if (!statestr.toLowerCase().equals("null")) {
					evstr += setHardEvidence(args, nextarg, params, evidences, evstr, var);
				}
			} else {
				evstr += setSoftEvidence(args, nextarg, params, evidences, evstr, var);
				nextarg = nextarg + var.getStates().size() - 1;
			}			
		}
		return evstr;
	}

	private String handleVariableEvidence(Collection<Variable> vars, Map<String, String> params) throws Exception {
		String str = "";
		NamedEvidence ne = null;
		if (params.get("name") != null) {
			str += "Named evidence added.\n";
			diagnoser.getEvidencemanager().removeNamedEvidence(params.get("name"));
			ne = diagnoser.getEvidencemanager().addNamedEvidence(params.get("name"));
			for (Variable var: vars) {
				ne.addEvidence(diagnoser.getEvidencemanager().getEvidence(var));
			}
			str += ne.toString();
		}
		if (params.get("active") != null) {
			boolean active = false;
			if (params.get("active").toLowerCase().equals("true")) {
				active = true;
			}
			str += "Variable evidence active = " + active + ":\n";
			for (Variable var: vars) {
				diagnoser.getEvidencemanager().activateEvidence(var, active);
			}
			str += diagnoser.getEvidencemanager().getEvidences(active);
		}
		return str;
	}

	private String setSoftEvidence(List<String> args, int nextarg, Map<String, String> params, List<EvidenceMap> evidences, String evstr, Variable var) throws Exception {
		Map<State, Double> statevalues = new HashMap<State, Double>();
		int nrOfStates = var.getStates().size();
		for (int i = 0; i < nrOfStates; i++) {
			String[] statestr = args.get(nextarg++).split(":");
			statevalues.put(getState(var.getName(), statestr[0]), Double.parseDouble(statestr[1]));
		}
		boolean activate = false;
		if (params.get("active") != null && params.get("active").toLowerCase().equals("true")) {
			activate = true;
		}
		EvidenceMap ev = diagnoser.getEvidencemanager().setEvidence(var, statevalues, activate);
		evidences.add(ev);
		evstr += ev.toString() + "\n";
		return evstr;
	}
	
	private String setHardEvidence(List<String> args, int nextarg, Map<String, String> params, List<EvidenceMap> evidences, String evstr, Variable var) throws Exception {
		State state = getState(var.getName(), args.get(nextarg));
		boolean activate = false;
		if (params.get("active") != null && params.get("active").toLowerCase().equals("true")) {
			activate = true;
		}
		EvidenceMap ev = diagnoser.getEvidencemanager().setEvidence(state, activate);
		evidences.add(ev);
		evstr += ev.toString() + "\n";
		return evstr;
	}
}
