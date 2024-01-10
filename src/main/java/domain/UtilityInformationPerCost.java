package domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Computes the utility of the information as the information per cost of the action
 * A constant c can be added to create the costfunction f(C) = c * Information/Cost.
 * If cost = 0, returns NaN as utility.
 * 
 * @author Peter
 *
 */
public final class UtilityInformationPerCost implements UtilityFunction {
	private final Double DEFAULT_C = 1.0;
	private UtilityNames type = UtilityNames.INFORMATION_PER_COST;
	
	private Map<String, Double> constants;
	private String name;
	
	public UtilityInformationPerCost() {
		this.name = type.toString();
		UtilityFunctions.addUtilityfunctions(this);
		this.constants = new HashMap<String, Double>();
		constants.put("c", DEFAULT_C);
	}
	
	@Override
	public UtilityResult getUtility(InformationResult inforesult) throws Exception {
		UtilityResult utilityresult = new UtilityResult(inforesult);
		utilityresult.setUtility(computeUtility(inforesult));
		return utilityresult;
	}
	
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public UtilityNames getType() {
		return type;
	}
	
	public double getC() {
		return constants.get("c");
	}

	public void setC(double c) {
		boolean newname = false;
		if (name == type.toString() || name.equals(createName())) {
			newname = true;
		}
		constants.put("c", c);
		if (newname) {
			name = createName();
		}	
	}

	private String createName() {
		String temp = type.toString();
		temp += "_c:" + constants.get("c");
		return temp;
	}

	private double computeUtility(InformationResult ir) throws Exception {
		double cost = ir.getProbe().getCost();
		double info = ir.getInformation();
		if (cost != 0) {
			return constants.get("c") *  info / cost;
		} else {
			throw new Exception("The cost cannot be 0, using utilityfunction " + this.name);
		}
	}
	
	public String settingsToString() {
		String str = "";
		str += "Name: " + name + ", ";
		str += "c = " + constants.get("c") + "\n";
		return str;
	}
	
	public String functionToString() {
		return "Function: f(C) = c * Information/Cost" + "\n";
	}
}