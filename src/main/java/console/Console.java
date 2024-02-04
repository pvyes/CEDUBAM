package console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bayesserver.State;
import com.bayesserver.Variable;

import domain.Diagnoser;
import domain.Probe;

public abstract class Console {
	public final String SEPARATOR = ", ";
	public final String CRLF = "\n";
	public final String STRING_SEPARATOR = ","; 
	
	Diagnoser diagnoser;
	
	public Console(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
	}

	public Variable getVariable(String varname) {
		if (hasIntegertype(varname)) {
			return diagnoser.getSystem().getVariables().get(Integer.parseInt(varname));
		}
		for (Variable var: diagnoser.getSystem().getVariables()) {
			if (var.getName().toLowerCase().equals(varname.toLowerCase())) {
				return var;
			}
		}
		return null;
	}
	
	public State getState(String varname, String statename) {
		Variable var = getVariable(varname);
		if (hasIntegertype(statename)) {
			return var.getStates().get(Integer.parseInt(statename));
		}
		for (State state: var.getStates()) {
			if (state.getName().toLowerCase().equals(statename.toLowerCase())) {
				return state;
			}
		}
		return null;
	}
	
	/**
	 * Returns a map of the parameters and their values. If more values are present for the same parameter, they are assembeld in a STRING_SEPARATOR-separated string (assuming the values do never contain STRING_SEPARATOR);
	 * @param args
	 * @return
	 */
	public Map<String, String> getParameters(List<String> args) {
		Map<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < args.size(); i++) {
			int n = args.get(i).indexOf(":");
			if (n != -1) {
				String key = args.get(i).split(":", 2)[0];
				String value = args.get(i).split(":", 2)[1];
				if (!result.containsKey(key)) {
					result.put(key, value);
				} else {
					String tmpvalue = result.get(key) + STRING_SEPARATOR + value;
					result.put(key, tmpvalue);
				}
			}
		}
		return result;
	}
	
	public String getParameter(String arg) {
		if (arg.split(":").length > 1) {
			return arg.split(":")[0];
		}
		return null;
	}
	
	public String getParameterValue(String paramname, String arg) {
		if (arg.split(paramname + ":").length > 1) {
			return arg.split(paramname + ":")[1];
		}
		return null;
	}
	
	public boolean hasIntegertype(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public Collection<Variable> getVariablesFromString(String string) {
		Collection<Variable> result = new ArrayList<Variable>();
		String[] varnames = string.split(STRING_SEPARATOR);
		for (int i = 0; i < varnames.length; i++) {
			result.add(getVariable(varnames[i]));
		}
		return result;
	}
	
	public Probe getProbe(String string) {
		Variable var = getVariable(string);
		if (var != null) {
			return diagnoser.getProbe(var);
		} else {
			return diagnoser.getProbe(string);
		}
	}
	
	public String getProbeNames(List<Probe> probes) {
		String str = "[";
		for (Probe p: probes ) {
			str += p.getName() + ", ";
		}
		return str.substring(0, str.length() - 2) + "]";
	}
	
	public static Object printDiagnose(Collection<State> pd) {
		String str = "";
		for (State s: pd) {
			str += s.getVariable().getName() + ":" + s.getName() + ",";
		}
		return str;
	}
	
	public boolean askConfirmation(String question) throws IOException {
		System.out.println(question + " (Y, N)");
		System.out.print("Choice: ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s = in.readLine();
		return s.trim().toLowerCase().equals("y");
	}
	
	public List<Boolean> getInfoPrevals(String string) {
		List<Boolean> result = new ArrayList<Boolean>();
		String[] list = string.split(",");
		for (String str: list) {
			if (str.toLowerCase().equals("true")) {
				result.add(true);
			} else {
				result.add(false);
			}
		}
		return result;
	}
}
