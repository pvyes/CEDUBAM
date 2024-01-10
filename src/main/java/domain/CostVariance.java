package domain;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CostVariance {
	public enum CostVarianceType {
		EQUAL,
		SCATTERED, //costs from low to high are evenly distributed
		POLAR, //costs are divided in two sets one with a high, the other with a low cost. 
	}
	
	private Diagnoser diagnoser;
	private CostVarianceType type;
	private double min;
	private double max;
	
	public CostVariance(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.type = CostVarianceType.EQUAL; //default value
		this.min = diagnoser.getDefaultcost();
		this.max = diagnoser.getDefaultcost();
	}
	
	public CostVarianceType getType() {
		return type;
	}

	public double getMin() {
		return min;
	}

	public void setType(CostVarianceType type) {
		this.type = type;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	/**
	 * Sets the costs of all the actions with the default test cost as given in the class Cost.
	 * @param actions
	 * @throws Exception 
	 */
	public void setEqualCosts(double cost, Collection<Probe> probes) throws Exception {
		type = CostVarianceType.EQUAL;
		setCosts(cost, cost);
		for (Probe a: probes) {
			a.setCost(cost);
		}
	}
	
	/**
	 * Sets the costs of all the probes scattered over the list of probes, beginning with the minimum cost.
	 * @param actions
	 * @throws Exception 
	 */
	public void setScatteredCosts(double min, double max, List<Probe> probes) throws Exception {
		type = CostVarianceType.SCATTERED;
		setCosts(min, max);
		setScatteredCosts(probes);
	}
	
	/**
	 * Sets the costs of all the probes scattered over the list of probes, beginning with the minimum cost.
	 * @param actions
	 * @throws Exception 
	 */
	public void setScatteredCosts(List<Probe> probes) throws Exception {
		type = CostVarianceType.SCATTERED;
		double distance = max - min / 2;
		if (probes.size() == 1) {
			probes.iterator().next().setCost(distance);
		} else {
			distance = (max - min) / (probes.size() - 1);
			int i = 0;
			for (; i < probes.size(); i++) {
				probes.get(i).setCost(min + distance * i);
				if (i == probes.size() - 1) {
					probes.get(i).setCost(max);
				}
			}
		}
	}
	
	/**
	 * Sets the costs of the actions for two separated sets of actions: a set which gets the minimum cost, the other set gets the maximum cost. 
	 * The minimumcost and the maximumcost are as given.
	 * @param actions
	 * @param min
	 * @param max
	 * @throws Exception 
	 */
	public void setPolarCosts(double min, double max, Collection<Probe> probesMin, Collection<Probe> probesMax) throws Exception {
		type = CostVarianceType.POLAR;
		setCosts(min, max);
		setPolarCosts(probesMin, probesMax);
	}
	
	/**
	 * Sets the costs of the actions for two separated sets of actions: a set which gets the minimum cost, the other set gets the maximum cost. 
	 * The minimumcost and the maximumcost are as given.
	 * @param actions
	 * @param min
	 * @param max
	 * @throws Exception 
	 */
	public void setPolarCosts(Collection<Probe> probesMin, Collection<Probe> probesMax) throws Exception {
		type = CostVarianceType.POLAR;
		for (Probe a: probesMin) {
			a.setCost(min);
		}
		for (Probe a: probesMax) {
			a.setCost(max);
		}
	}
	
	public void setRandomPolarCosts(double min2, double max2) throws Exception {
		type = CostVarianceType.POLAR;
		setCosts(min2, max2);
		setRandomPolarCosts();		
	}
	
	public void setRandomPolarCosts() throws Exception {
		type = CostVarianceType.POLAR;
		Collection<Collection<Probe>> lists = diagnoser.getRandomPolarDivision(diagnoser.getProbes());
		Iterator<Collection<Probe>> it = lists.iterator();
		setPolarCosts(min, max, it.next(), it.next());		
	}

	public void setRandomScatteredCosts(double min2, double max2) throws Exception {
		setCosts(min2, max2);
		setRandomScatteredCosts();		
	}
	
	public void setRandomScatteredCosts() throws Exception {
		type = CostVarianceType.SCATTERED;
		List<Probe> list = diagnoser.getRandomDivision(diagnoser.getProbes());
		setScatteredCosts(min, max, list);	
	}
	
	private void setCosts(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public String settingsToString() {
		String str = "";
		str += "Type: " + type + "; ";
		if (min != Double.NaN) {
			str += "min = " + min + "; ";
		}
		if (max != Double.NaN) {
			str += "max = " + max + "; ";
		}
		str += "mean = " + (min + max) / 2; 
		return str;
	}
}
