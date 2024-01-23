package console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bayesserver.State;
import com.bayesserver.Variable;

import domain.Diagnoser;
import domain.InformationResult;
import domain.MEUResult;
import domain.Networkfactory;
import domain.Probe;
import domain.ProbeCombinations;
import domain.ProbeScenario;
import domain.StrategyFixed;
import domain.UtilityResult;
import domain.UtilityWeightedCost;
import domain.UtilityNames;

public class ConsoleCompute extends Console {

	public ConsoleCompute(Diagnoser diagnoser) {
		super(diagnoser);
		this.diagnoser = diagnoser;
	}

	public boolean handleCommand(List<String> args, int nextarg) {
		String subject = args.get(nextarg++);
		boolean go = true;
		try {			
			Map<String, String> params = getParameters(args);
			if (params.get("confirm") == null || params.get("confirm").toLowerCase().equals("true")) {
				ConsoleInOut.printMessage(diagnoser.showSettings());
				go = askConfirmation(CRLF + "Continue computation with these settings?");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (go) {
			switch (subject) {
				case "information":
					if (args.size() < 3) {
						return false;
					}
					ConsoleInOut.printMessage(getInformation(args, nextarg));
					return true;
				case "ei":
					ConsoleInOut.printMessage(getExpectedInformation(args, nextarg));
					return true;
				case "utility":
					if (args.size() < 3) {
						return false;
					}
					ConsoleInOut.printMessage(getUtility(args, nextarg));
					return true;
				case "eu":
					ConsoleInOut.printMessage(getExpectedUtility(args, nextarg));
					return true;
				case "meu":
					ConsoleInOut.printMessage(getMeu(args, nextarg));
					return true;				
				case "suggested-probe":
					ConsoleInOut.printMessage(getSuggestedProbe(args, nextarg));
					return true;				
				case "probe-scenario":
					ConsoleInOut.printMessage(getProbeScenario(args, nextarg));
					return true;
				case "optimal":
					ConsoleInOut.printMessage(getOptimalConstants(args, nextarg));
					return true;
				case "minimalcost":
					ConsoleInOut.printMessage(getMinimalCost(args, nextarg));
					return true;
				default:
					ConsoleInOut.printErrormessage("Command not found");
			}
			return false;
		} else {
			return true;
		}
	}

	private String getMinimalCost(List<String> args, int nextarg) {
		try {
			List<Variable> vars = new ArrayList<Variable>();
			for (Probe p: diagnoser.getProbes()) {
				vars.add(p.getTarget());
			}
			List<State> coll = new ArrayList<State>();
			Collection<Collection<State>> combis = ProbeCombinations.getStateProducts(vars, diagnoser.getProbes().size() , coll);
			//combis.forEach(c -> System.out.println(c));
			StrategyFixed sf = (StrategyFixed) diagnoser.getStrategy();
			sf.setProbelist(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private String getOptimalConstants(List<String> args, int nextarg) {
		try {
			Map<String, String> params = getParameters(args);
 			int itlimit = 0;
			int tlimit = -1;
			if (params.get("iteration-limit") != null) {
				String limit = params.get("iteration-limit");
				itlimit = Integer.parseInt(limit);
			}
			if (params.get("time-limit") != null) {
				String limit = params.get("time-limit");
				tlimit = Integer.parseInt(limit);
			}
			Collection<ProbeScenario> pss = diagnoser.getConstantfinder().getOptimalScenarios(itlimit, tlimit);
			String msg = "Optimal probescenarios (#="+ pss.size() + "):" + CRLF;
			var i = 0;
			for (ProbeScenario ps: pss)  {
				msg += "[" + i++ + "]: " + ps.getOptimalScenarioReport(diagnoser.getUtilityfunction().getType()) + CRLF;
			}
			return msg.substring(0, msg.length() - CRLF.length());
		} catch (Exception e ) {
			return "The optimal scenario could not be computed.\n" + e.getMessage();
		}
	}

	private String getInformation(List<String> args, int nextarg) {
		try {
			Map<String, String> params = getParameters(args);
			List<Integer> diagnoseIndices = new ArrayList<Integer>();
			if (params.get("dg") != null) {
				String[] list = params.get("dg").split(",");
				for (String str: list) {
					diagnoseIndices.add(Integer.parseInt(str));
				}
			}
			Probe probe = getProbe(args.get(nextarg++));
			List<InformationResult> information = new ArrayList<InformationResult>();
			if (nextarg < args.size() && args.get(nextarg).indexOf(":") == -1) {
				State probestate = getState(probe.getTarget().getName(), args.get(nextarg));
				information = diagnoser.getInformation(probe, probestate, diagnoseIndices);
			} else {
				for (State probestate: probe.getTarget().getStates()) {
					information.addAll(diagnoser.getInformation(probe, probestate, diagnoseIndices));
				}
			}
			String msg = "Information:" + CRLF;
			for (InformationResult ir: information)  {
				msg += ir.toString() + CRLF;
			}
			return msg.substring(0, msg.length() - CRLF.length());
		} catch (Exception e ) {
			return "The information could not be computed.\n" + e.getMessage();
		}
	}
	
	private String getExpectedInformation(List<String> args, int nextarg) {
		try {
			Map<String, String> params = getParameters(args);
			List<Integer> diagnoseIndices = new ArrayList<Integer>();
			if (params.get("dg") != null) {
				String[] list = params.get("dg").split(",");
				for (String str: list) {
					diagnoseIndices.add(Integer.parseInt(str));
				}
			}
			boolean weighted = false;
			if (params.get("weighted") != null) {
				weighted = params.get("weighted").toLowerCase().equals("true");
			}
			Probe probe = getProbe(args.get(nextarg++));
			List<InformationResult> information = new ArrayList<InformationResult>();
			if (weighted) {
				information.add(diagnoser.getWeightedExpectedInformationPerProbe(probe, diagnoseIndices));
			} else {
				information = diagnoser.getExpectedInformation(probe, diagnoseIndices);
			}
			String msg = "Expected Information:" + CRLF;
			for (InformationResult ir: information)  {
				msg += ir.toString() + CRLF;
			}
			return msg.substring(0, msg.length() - CRLF.length());
		} catch (Exception e ) {
			return "The information could not be computed.\n" + e.getMessage();
		}
	}
	
	private String getUtility(List<String> args, int nextarg) {
		try {
			Map<String, String> params = getParameters(args);
			List<Integer> diagnoseIndices = new ArrayList<Integer>();
			if (params.get("dg") != null) {
				String[] list = params.get("dg").split(",");
				for (String str: list) {
					diagnoseIndices.add(Integer.parseInt(str));
				}
			}
			Probe probe = getProbe(args.get(nextarg++));
			List<UtilityResult> utility = new ArrayList<UtilityResult>();
			if (nextarg < args.size() && args.get(nextarg).indexOf(":") == -1) {
				State probestate = getState(probe.getTarget().getName(), args.get(nextarg));
				utility = diagnoser.getUtility(probe, probestate, diagnoseIndices);
			} else {
				for (State probestate: probe.getTarget().getStates()) {
					utility.addAll(diagnoser.getUtility(probe, probestate, diagnoseIndices));
				}
			}
			String msg = "Utility:" + CRLF;
			for (UtilityResult ir: utility)  {
				msg += ir.toString() + CRLF;
			}
			return msg.substring(0, msg.length() - CRLF.length());
		} catch (Exception e ) {
			return "The utility could not be computed.\n" + e.getMessage();
		}
	}
	
	private String getExpectedUtility(List<String> args, int nextarg) {
		try {
			Map<String, String> params = getParameters(args);
			List<Integer> diagnoseIndices = new ArrayList<Integer>();
			if (params.get("dg") != null) {
				String[] list = params.get("dg").split(",");
				for (String str: list) {
					diagnoseIndices.add(Integer.parseInt(str));
				}
			}
			Probe probe = getProbe(args.get(nextarg++));
			List<UtilityResult> utility = new ArrayList<UtilityResult>();
			utility = diagnoser.getExpectedUtility(probe, diagnoseIndices);
			String msg = "Expected Utility" + CRLF;
			for (UtilityResult ir: utility)  {
				msg += ir.toString() + CRLF;
			}
			return msg.substring(0, msg.length() - CRLF.length());
		} catch (Exception e ) {
			return "The utility could not be computed.\n" + e.getMessage();
		}
	}
	
	private String getMeu(List<String> args, int nextarg) {
		try {
			Map<String, String> params = getParameters(args);
			List<Integer> diagnoseIndices = new ArrayList<Integer>();
			if (params.get("dg") != null) {
				String[] list = params.get("dg").split(",");
				for (String str: list) {
					diagnoseIndices.add(Integer.parseInt(str));
				}
			}
			MEUResult utility = diagnoser.getMEU(diagnoseIndices);
			String msg = "Expected Utilities:" + CRLF;
			for (UtilityResult res: utility.getMeuresults()) {
				msg += res.toString() + CRLF; 
			}
			msg += "Maximum expected Utility";
			msg += utility.toString() + CRLF;
			return msg.substring(0, msg.length() - CRLF.length());
		} catch (Exception e ) {
			return "The utility could not be computed.\n" + e.getMessage();
		}
	}
	
	private String getSuggestedProbe(List<String> args, int nextarg) {
		try {
			Probe probe = diagnoser.getStrategy().getSuggestedProbe();
			return "The suggested probe is " + probe.toString();
		} catch (Exception e) {
			return "The suggested probe could not be computed.\n" + e.getMessage();
		}
	}
	
	private String getProbeScenario(List<String> args, int nextarg) {
		try {
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
			List<ProbeScenario> result = diagnoser.runProbeSequencer();
			if (diagnoser.getReportManager().isDisplay()) {
				for (ProbeScenario sc: result) {
					String report = sc.makeReport(diagnoser.getReportManager().getDisplaydetail())  + CRLF;
					ConsoleInOut.printMessage(report);
				}
			}
			return "Probesequences finished." + CRLF + CRLF;
		} catch (Exception e) {
			return "The probescenario could not be computed.\n" + e.getMessage();
		}
	}
}
