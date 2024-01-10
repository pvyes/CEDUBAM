package console;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.bayesserver.Variable;

import domain.Diagnoser;
import domain.NamedEvidence;
import domain.Probe;

public class ConsoleRemove extends Console {

	public ConsoleRemove(Diagnoser diagnoser) {
		super(diagnoser);
	}
	
	public boolean handleCommand(List<String> args, int nextarg) {
		String subject = args.get(nextarg++);
		switch (subject) {
			case "evidence":
				ConsoleInOut.printMessage(removeEvidence(args, nextarg));
				return true;
			case "probe":
				ConsoleInOut.printMessage(removeProbe(args, nextarg));
				return true;
			case "report":
				ConsoleInOut.printMessage(removeReport(args, nextarg));
				return true;
			default:
				ConsoleInOut.printErrormessage("Command not found");
		}
		return false;
	}

	private String removeEvidence(List<String> args, int nextarg) {
		String str = "";
		Map<String, String> params = getParameters(args);
		if (args.get(nextarg).indexOf(":") != -1) {
			diagnoser.getEvidencemanager().removeNamedEvidence(params.get("name"));
			str += "The named evidence " + params.get("name") + " and its evidence is removed."; 
		} else {
			Collection<Variable> vars = new ArrayList<Variable>();
			for (; nextarg < args.size() && args.get(nextarg).indexOf(":") == -1; nextarg++) {
				vars.add(getVariable(args.get(nextarg)));
			}
			 NamedEvidence ne = null;
			if (params.get("name") != null) {
				ne = diagnoser.getEvidencemanager().getNamedEvidence(params.get("name"));
				str += "Evidence of named evidence " + (params.get("name")) + " is removed:\n";
			} else {
				str += "Evidence is removed:\n";
			}
			for (Variable var: vars) {			
				if (ne != null) {
					ne.getEvidences().remove(diagnoser.getEvidencemanager().getEvidence(var));
				} else {
					diagnoser.getEvidencemanager().removeEvidence(diagnoser.getEvidencemanager().getEvidence(var));
				}
				str += var.getName() + SEPARATOR;
			}
			str = str.substring(0, str.length() - SEPARATOR.length());
		}
		return str;
	}
	
	private String removeProbe(List<String> args, int nextarg) {
		String str = "";
		for (; nextarg < args.size(); nextarg++) {
			if (args.get(nextarg).equals("all")) {
				diagnoser.getProbes().clear();
				str += "All probes are removed." + CRLF;
			} else {
				Probe p = getProbe(args.get(nextarg));
				String name = p.getName();
				diagnoser.getProbes().remove(p);
				str += "Probe " + name + " is removed." + CRLF;
			}
		}
		return str.substring(0, str.length() - CRLF.length());
	}
	
	private String removeReport(List<String> args, int nextarg) {
		String str = "";
		try {
			for (; nextarg < args.size(); nextarg++) {
				if (args.get(nextarg).equals("all")) {
					for (File f: diagnoser.getReportManager().getReportList()) {
						String name = f.getName(); 
						diagnoser.getReportManager().removeReport(name);
					}
					str += "All reports are removed." + CRLF;
				} else {
					String filename =args.get(nextarg);
					boolean ok = diagnoser.getReportManager().removeReport(filename);
					if (ok) {
						str += "Report " + filename + " is removed." + CRLF;
					} else {
						str += "Report " + filename + " could not be removed." + CRLF;
					}
				}
			}
			return str.substring(0, str.length() - CRLF.length());
		} catch (Exception e) {
			return "There was an error removing the files." + CRLF + e.getMessage();
		}
	}
}
