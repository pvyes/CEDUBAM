package domain;

import java.util.HashMap;
import java.util.Map;

public final class UtilityLinear implements UtilityFunction {
	public final static String  A = "a";
	public final static String  S = "s";
	
	private final Double DEFAULT_A = 0.1;
	private final Double DEFAULT_S = 1.0;
	
	private UtilityNames type = UtilityNames.LINEAR_UTILITY;	
	private Map<String, Double> constants;
	private String name;
	private double totalcost;
	
	/**
	 * Constructor of a linear function with default constants a = 1 and s = 0 to form the costfunction f(C) = a*C + s.
	 */
	public UtilityLinear() {
		this.constants = new HashMap<String, Double>();
		constants.put(A, DEFAULT_A);
		constants.put(S, DEFAULT_S);
		this.name = type.toString();
		UtilityFunctions.addUtilityfunctions(this);
	}
	
	/**
	 * Constructor of a linear function with constants a and s to form the costfunction f(C) = a*C + s. 
	 * @param a 
	 * @param s
	 */
	public UtilityLinear(double a, double s) {
		this.constants = new HashMap<String, Double>();
		constants.put(A, a);
		constants.put(S, s);
		name = createName();
		UtilityFunctions.addUtilityfunctions(this);		
	}
	
	@Override
	public UtilityResult getUtility(InformationResult inforesult) {
		UtilityResult ur = new UtilityResult(inforesult);
		ur.setUtility(computeUtility(inforesult));
		ur.setConstants(constants);
		return ur;
	}

	private double computeUtility(InformationResult ir) {
		double currentCost = ir.getProbe().getCost();
		setTotalcost(currentCost);
		return (-1 * constants.get(A) * currentCost + constants.get(S)) * ir.getInformation();
	}	

	@Override
	public UtilityNames getType() {
		return type;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getTotalcost() {
		return totalcost;
	}

	public void setTotalcost(double totalcost) {
		this.totalcost = totalcost;
	}
	
	public Map<String, Double> getConstants() {
		return constants;
	}
	
	public void setConstants(Map<String, Double> constants) {
		this.constants = constants;
	}

	public void setConstants(Double a, Double s) {
		this.constants.put(A, a);
		this.constants.put(S, s);
	}

	public double getA() {
		return constants.get(A);
	}

	public void setA(double a) {
		boolean newname = false;
		if (name == type.toString() || name.equals(createName())) {
			newname = true;
		}
		constants.put(A, a);
		if (newname) {
			name = createName();
		}	
	}

	public double getS() {
		return constants.get(S);
	}

	public void setS(double s) {
		boolean newname = false;
		if (name == type.toString() || name.equals(createName())) {
			newname = true;
		}
		constants.put(S, s);
		if (newname) {
			name = createName();
		}
	}
	
	private String createName() {
		String temp = type.toString();
		temp = temp.concat("-a:" + constants.get(A) + "-s:" + constants.get(S));
		return temp;
	}
	
	@Override
	public String settingsToString() {
		String str = "";
		str += "Name: " + name + ", ";
		str += "a = " + constants.get(A) + ", ";
		str += "s = " + constants.get(S);
		return str;
	}

	@Override
	public String functionToString() {
		return "Function: f(C) = (-a*C + s)*I" + "\n";
	}
}
