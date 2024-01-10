package domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Computes the utility of the information as the information per cost of the action
 * A ratio (0 or higher) is used to weight the importance of the cost. 
 * If the ratio is zero, costs do not play a role. The higher the ration, the more costs are taken into account.
 * If cost = 0, returns NaN as utility.
 * 
 * @author Peter
 *
 */
public final class UtilityWeightedCost implements UtilityFunction {
	private UtilityNames type = UtilityNames.WEIGHTED_COST;
	private int ALPHA_ROUNDING_PLACES = 6;
	private final Double DEFAULT_C = 1.0;
	
	private Map<String, Double> constants;
	//private double c = 0;
	private String name;
	private List<Boolean> infoprevalences;
	private int treelevel = 0;
	
	public UtilityWeightedCost() {
		this.constants = new HashMap<String, Double>();
		constants.put("c", DEFAULT_C);
		this.name = type.toString();
		UtilityFunctions.addUtilityfunctions(this);
		this.infoprevalences = new ArrayList<Boolean>();
	}

	public void setInfoprevalences(List<Boolean> infoprevals) {
		this.infoprevalences = new ArrayList<Boolean>(infoprevals);
	}
		
	public void setTreelevel(int level) {
		this.treelevel = level;	
	}

	@Override
	public UtilityResult getUtility(InformationResult inforesult) {
		UtilityResult ur = new UtilityResult(inforesult);
		ur.setUtility(computeUtility(inforesult));
		return ur;
	}

	public double setAlpha(Map<Probe, InformationResult> inforesults) {
		if (infoprevalences.size() > treelevel) {
			double alpha = Double.NaN;
			// make all combinations of probes
			List<Entry<Probe, InformationResult>> irs = new ArrayList<Entry<Probe, InformationResult>>(inforesults.entrySet());
			List<List<Entry<Probe, InformationResult>>> combis = SetCombinations.generateCombinations(irs, 2);
			//get the smallest ratio between infodifference and costdifference, for smaller costs
			for (List<Entry<Probe, InformationResult>> combi: combis) {
				double infodiff = Math.abs(combi.get(0).getValue().getInformation() - combi.get(1).getValue().getInformation());
				double costdiff = combi.get(0).getKey().getCost() - combi.get(1).getKey().getCost();
				if (costdiff > 0 && infodiff != 0) {
					double ratio = infodiff / costdiff;
					if (Double.isNaN(alpha)) {
						alpha = ratio;
					} else {
						if (ratio < alpha) {
							alpha = ratio;
						}
					}
				}			
			}
			//info infopreval: set c just below the alpha, else set c just above
			if (infoprevalences.get(treelevel) && !Double.isNaN(alpha)) {
				constants.put("c", round(alpha, RoundingMode.DOWN));
			} else {
				if (Double.isNaN(alpha)) {
					alpha = constants.get("c");
				} else {
					constants.put("c", round(alpha, RoundingMode.UP));
				}
			}
			//System.out.println("infoprevalence for alpha level:" + treelevel + " set to " + constants.get("c"));
		}
		return constants.get("c");
	}
	
	private double round(double value, RoundingMode roundingmode) {
	    if (ALPHA_ROUNDING_PLACES < 0) throw new IllegalArgumentException();
		    try { 
		    	BigDecimal bd = new BigDecimal(Double.toString(value));
		    	bd = bd.setScale(ALPHA_ROUNDING_PLACES, roundingmode);
			    return bd.doubleValue();
		    } catch (Exception e) {
		    	System.out.println("Error in the rounding function Utility_Weighted_Cost (time = " + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + System.currentTimeMillis() + ")." + e);
		    }
		    return constants.get("c");
		    
	}

	private double computeUtility(InformationResult ir) {
		double currentCost = ir.getProbe().getCost();
		return ir.getInformation() - constants.get("c") * currentCost;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public UtilityNames getType() {
		return type;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public double getRatio() {
		return constants.get("c");
	}

	public void setRatio(double c) {
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
		temp += "_" + constants.get("c");
		return temp;
	}
	
	@Override
	public String settingsToString() {
		String str = "";
		str += "Name: " + name + ", ";
		str += "c = " + constants.get("c") + "\n";
		return str;
	}

	@Override
	public String functionToString() {
		return "Function: f(C) = information - c * cost" + "\n";
	}
}