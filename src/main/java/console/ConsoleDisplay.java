package console;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.bayesserver.NodeGroup;
import com.bayesserver.State;
import com.bayesserver.Variable;
import com.bayesserver.inference.InconsistentEvidenceException;

import domain.Diagnoser;
import domain.Diagnosis;
import domain.EvidenceMap;
import domain.InformationFunction;
import domain.InformationFunctions;
import domain.NamedEvidence;
import domain.Networkfactory;
import domain.Networkfunctions;
import domain.Probe;
import domain.Strategies;
import domain.Strategy;
import domain.UtilityFunction;
import domain.UtilityFunctions;

public class ConsoleDisplay extends Console {

	public ConsoleDisplay(Diagnoser diagnoser) {
		super(diagnoser);
		this.diagnoser = diagnoser;
	}

	public boolean handleCommand(List<String> args, int nextarg) {
		String subject = args.get(nextarg++);
		switch (subject) {
			case "print":
				System.out.println(args.get(nextarg));
				return true;		
			case "networks":
				if (args.size() > 2) {
					return false;
				}
				ConsoleInOut.printMessage(getNetworks(args));
				return true;
			case "network":
				if (args.size() > 2) {
					return false;
				}
				ConsoleInOut.printMessage(getNetwork(args));
				return true;
			case "variables":
				ConsoleInOut.printMessage(getVariables(args, nextarg));
				return true;
			case "variable":
				if (args.size() < 3) {
					return false;
				}
				ConsoleInOut.printMessage(getVariable(args, nextarg));
				return true;
			case "groups":
				if (args.size() < 2) {
					return false;
				}
				ConsoleInOut.printMessage(getGroups(args, nextarg));
				return true;
			case "healthgroups":
				if (args.size() < 2) {
					return false;
				}
				ConsoleInOut.printMessage(getHealthgroups(args, nextarg));
				return true;
			case "pdvs": 
				ConsoleInOut.printMessage(getPdvs(args));
				return true;
			case "evidence":
				ConsoleInOut.printMessage(getEvidence(args, nextarg));
				return true;
			case "diagnoses":
				ConsoleInOut.printMessage(getDiagnoses(args, nextarg));
				return true;
			case "defaultcost":
				ConsoleInOut.printMessage(getDefaultcost(args, nextarg));
				return true;
			case "probes":
				ConsoleInOut.printMessage(diagnoser.probesToString());
				return true;
			case "probe":
				ConsoleInOut.printMessage(getProbe(args, nextarg));
				return true;
			case "informationfunctions":
				ConsoleInOut.printMessage(getInformationfunctions(args, nextarg));
				return true;
			case "utilityfunctions":
				ConsoleInOut.printMessage(getUtilityfunctions(args, nextarg));
				return true;
			case "strategies":
				ConsoleInOut.printMessage(getStrategies(args, nextarg));
				return true;
			case "informationfunction":
				ConsoleInOut.printMessage(getInformationfunction(args, nextarg));
				return true;
			case "utilityfunction":
				ConsoleInOut.printMessage(getUtilityfunction(args, nextarg));
				return true;
			case "strategie":
				ConsoleInOut.printMessage(getStrategie(args, nextarg));
				return true;
			case "settings":
				ConsoleInOut.printMessage(diagnoser.showSettings());
				return true;
			case "reports":
				ConsoleInOut.printMessage(showReports());
				return true;
			case "report":
				ConsoleInOut.printMessage(showReport(args, nextarg));
				return true;
			default:
			ConsoleInOut.printErrormessage("Command not found");
		}
		return false;
	}
	
	private String showReport(List<String> args, int nextarg) {
		String str = "";
		String arg = args.get(nextarg++);
		if (arg.toLowerCase().equals("settings")) {
			str = diagnoser.getReportManager().getSettings();
		} else {
			try {
				str = diagnoser.getReportManager().getReportFile(arg);
			} catch (IOException e) {
				str = "File could not be read.";
			}
		}
		return str;
	}

	private String showReports() {
		List<File> reports = diagnoser.getReports();
		String str = "";
		for (var i = 0; i < reports.size(); i++) {
			str += "[" + i + "] " + reports.get(i).getName() + CRLF; 
		}
		return str;
	}

	private String getStrategie(List<String> args, int nextarg) {
		return diagnoser.getStrategy().getName().toString();
	}

	private String getUtilityfunction(List<String> args, int nextarg) {
		return diagnoser.getUtilityfunction().settingsToString();
	}

	private String getInformationfunction(List<String> args, int nextarg) {
		return diagnoser.getInformationfunction().getInformationtype().toString();
	}

	private String getStrategies(List<String> args, int nextarg) {
		String str = "";
		List<Strategy> list = Strategies.getStrategies(diagnoser);
		for (int i = 0; i < list.size(); i++) {
			str += "[" + i + "] " + list.get(i).getName() + CRLF;
		}		
		str = str.substring(0, str.length() * CRLF.length());
		return str;
	}

	private String getUtilityfunctions(List<String> args, int nextarg) {
		String str = "";
		List<UtilityFunction> list = UtilityFunctions.getUtilityfunctions();
		for (int i = 0; i < list.size(); i++) {
			str += "[" + i + "] " + list.get(i).getType() + CRLF;;
		}		
		str = str.substring(0, str.length() * CRLF.length());
		return str;
	}

	private String getInformationfunctions(List<String> args, int nextarg) {
		String str = "";
		List<InformationFunction> list = InformationFunctions.getInformationfunctions();
		for (int i = 0; i < list.size(); i++) {
			str += "[" + i + "] " + list.get(i).getInformationtype() + CRLF;;
		}		
		str = str.substring(0, str.length() * CRLF.length());
		return str;
	}

	private String getProbe(List<String> args, int nextarg) {
		String msg = "";
		Probe p = getProbe(args.get(nextarg));
		msg += p.toString() + CRLF;		
		msg = msg.substring(0, msg.length() * CRLF.length());
		return msg;
	}

	private String getNetworks(List<String> args) {
		String str = "";
		int i = 0;
		for (String s: Networkfactory.getAvailableNetworks()) {
			str += "[" + i++ + "] " + s + CRLF;
		}
		return str;
	}
	
	private String getNetwork(List<String> args) {
		if (diagnoser.getSystem() != null) {
			return diagnoser.getSystem().getName();
		} else {
			return "No network is currently set.";
		}
	}
	
	private String getVariables(List<String> args, int nextarg) {
		String str = "";
		int i = 0;
		if (nextarg >= args.size()) {
			for (Variable v: diagnoser.getSystem().getVariables()) {
				str += "[" + i++ + "] " + v.getName() + CRLF;
			}
		} else {
			String parameter = getParameter(args.get(nextarg));
			String value = getParameterValue(parameter, args.get(nextarg));
			if (value == null) {
				return "Parameter " + value + " not found or bad command.";
			}
			str += "Variables of " + parameter + " " + value + ":" + CRLF + makeString(diagnoser.getVariables(value), CRLF);  
		}
		return str;
	}
	
	private String getVariable(List<String> args, int nextarg) {
		Variable var = getVariable(args.get(nextarg++));
		Map<String, String> params = getParameters(args);
		if (params.get("property") != null) {
			return makeString(var, params.get("property"));
		}
		return makeString(var);
	}
	
	private String getGroups(List<String> args, int nextarg) {
		Collection<String> names = new ArrayList<String>();
		for (NodeGroup g: diagnoser.getSystem().getNodeGroups()) {
			names.add(g.getName());
		}
		return makeString(names, CRLF);
	}

	private String getHealthgroups(List<String> args, int nextarg) {
		return "Healthgroups: "  + makeString(diagnoser.getHealthgroups(), SEPARATOR);
	}

	private String getEvidence(List<String> args, int nextarg) {
		String str = "";
		if (nextarg >= args.size()) {
			str = "All evidences:\n";
			for (EvidenceMap em: diagnoser.getEvidencemanager().getEvidences()) {
				str += em.toString() + "[active:" + em.isActivated() + "]\n";
			}
		}
		Map<String, String> params = getParameters(args);
		if (params.get("active") != null) {
			boolean active = false;
			if (params.get("active").toLowerCase().equals("true")) {
				active = true;
			}
			str = "All evidences where active = " + active + ":\n";
			for (EvidenceMap em: diagnoser.getEvidencemanager().getEvidences(active)) {
				str += em.toString() + "\n";
			}
		}
		if (params.get("name") != null) {
			String name = params.get("name");
			if (name.equals("all")) {
				for (NamedEvidence em: diagnoser.getEvidencemanager().getNamedEvidences()) {
					str += em.toString() + "\n";
				}
			} else {
				NamedEvidence ne = diagnoser.getEvidencemanager().getNamedEvidence(params.get("name"));
				str = "Named evidence " + params.get("name") + ":\n";
				for (EvidenceMap em: ne.getEvidences()) {
					str += em.toString() + "\n";
				}
			}
		}
		for (; nextarg < args.size(); nextarg++) {
			if (!args.get(nextarg).contains(":")) {
				Variable var = getVariable(args.get(nextarg));
				str += diagnoser.getEvidencemanager().getEvidence(var) + "\n";
			}
		}
		if (str.length() > 1) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	private String getPdvs(List<String> args) {
		if (diagnoser.getPdvs().size() > 0)  {
			String str = "";
			for (Variable var: diagnoser.getPdvs()) {
				str += var.getName() + " " + diagnoser.getEvidencemanager().getEvidence(var) + CRLF; 
			}
			return str.substring(0, str.length() - CRLF.length());
		} else {
			return "No problem defining variables are currently set.";
		}
	}
	
	private String getDiagnoses(List<String> args, int nextarg) {
		List<Diagnosis> ds = diagnoser.getPossibleDiagnoses();
		return diagnoser.possiblediagnosesToString(ds);
	}
	
	private String getDefaultcost(List<String> args, int nextarg) {
		return "Defaultcost = " + diagnoser.getDefaultcost();
	}
	
	private String makeString(Variable var) {
		String str = "";
		str += "id: " + var.getIndex() + CRLF;
		str += "name: " + var.getName() + CRLF;
		str += "description: " + var.getDescription() + CRLF;
		str += "groups: " + Networkfunctions.getGroupsAsString(var) + CRLF;
		str += "parents: " + Networkfunctions.getParentsAsString(var.getNode()) + CRLF;
		str += "children: " + Networkfunctions.getChildrenAsString(var.getNode()) + CRLF;
		str += "states: " + Networkfunctions.getStatesAsString(var) + CRLF;
		str += "children: " + Networkfunctions.getChildrenAsString(var.getNode()) + CRLF;
		str += "distribution: " + Networkfunctions.getCPTableToString(var.getNode()) + CRLF;
		str += "belief state: " + getProbabilityString(var) + CRLF;
		return str; 
	}
	private String makeString(Variable var, String property) {
		String str = "";
		str += var.getName() + ": ";
		switch (property) {
			case "id": return str += "id: " + var.getIndex();
			case "name": return str += "name: " + var.getName();
			case "description": return str += "description: " + var.getDescription();
			case "groups": return str += "groups: " + Networkfunctions.getGroupsAsString(var);
			case "parents": return str += "parents: " + Networkfunctions.getParentsAsString(var.getNode());
			case "children": return str += "children: " + Networkfunctions.getChildrenAsString(var.getNode());
			case "states": return str += "states: " + Networkfunctions.getStatesAsString(var);
			case "distribution": return str += "distribution: " + Networkfunctions.getCPTableToString(var.getNode());
			case "belief-state": return str += "belief state: " + getProbabilityString(var);
			default: return "Property " + property + " not found.";
		}
	}
	
	private String getProbabilityString(Variable var) {
		try {
			String str = "";
			for (State s: var.getStates()) {
				str += s.getName() + ":" + diagnoser.getQuerymanager().getProbability(s) + SEPARATOR;
			}
			return str.substring(0, str.length() - SEPARATOR.length());
		} catch (InconsistentEvidenceException e) {
			return "Belief cound not be fetched.";
		}
	}

	private <T> String makeString(Collection<T> coll, String separator) {
		String str = "";
		for (T g: coll ) {
			str += g.toString() + separator;
		}
		if (str.length() > separator.length()) {
			str = str.substring(0, str.length() - separator.length());
		}
		return str;
	}
}
