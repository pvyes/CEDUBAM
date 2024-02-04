package console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.bayesserver.Variable;

import domain.Networkfactory;

public class ConsoleInOut {
	
	public static enum Messagetype {
		ERROR,
		CONFIRMATION
	}
	
	public static String listen() throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		return in.readLine();
	}
	
	public static void printMessage(String message, Messagetype messagetype) {
		System.out.println(messagetype + ": " + message);
	}
	
	public static void printMessage(String message) {
		System.out.println(message);
	}
	
	public static void printErrormessage(String message) {
		ConsoleInOut.printMessage(message, ConsoleInOut.Messagetype.ERROR);
	}
	
	public static int chooseSimplifiedNetwork() throws Exception {
		System.out.println("Choose system:");
		int i = 0;
		for (String str: Networkfactory.getAvailableNetworks()) {
			System.out.println("[" + i++ + "] " + str);
		}
		System.out.print("Choice: ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		int choice = Integer.parseInt(in.readLine());
		return choice;
	}
	
	/**
	 * Returns the choosen indices for the variables
	 * @param system
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static Collection<Integer> chooseVariables(List<Variable> vars) throws NumberFormatException, IOException {
		Collection<Integer> result = new HashSet<Integer>();
		System.out.println("Choose problem defining variables:");
		printVariables(vars);
		System.out.print("Choice: ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input = in.readLine();
		String[] choices = input.split("\\D");
		for (String s: choices) {
			if (s.length() > 0) {
				result.add(Integer.parseInt(s));
			}
		}
		return result;
	}
	
	private static void printVariables(List<Variable> vars) {
		for (int i = 0; i < vars.size(); i++) {
			System.out.println("[" +i + "] " + vars.get(i).getName());
		}
	}

	public static <T> String getNameList(Collection<T> objects) {
		String str = "";
		for (T o: objects) {
			str += o.toString() + ",";
		}
		return str;
	}
	
	public static List<String> askStatesEvidence(String var, List<String> list) {		
		System.out.println("Set evidence on states:");
		return setStatesEvidence(var, list);
	}
	
	private static List<String> setStatesEvidence(String var, List<String> list) {
		List<String> result = new ArrayList<String>();
		for (String s: list) {
			result.add(s + ":" + readEvidence(var, s));
		}
		return result;
	}

	private static Double readEvidence(String var, String state) {
		System.out.print("Evidence for " + var +":" + state + ": ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Double ev = Double.NaN;
		try {
			ev = Double.parseDouble(in.readLine());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ev;
	}
	
	public static void clear() {  
		try {  
			final String os = System.getProperty("os.name");  
			if (os.contains("Windows")) {  
				Runtime.getRuntime().exec("cls");  
			} else {  
				Runtime.getRuntime().exec("clear");  
			}  
		} catch (final Exception e) {  
			System.out.println("Display cannot be cleared (platform-dependent).");  
		}
	}
}

