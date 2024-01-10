package domain;

import com.bayesserver.Variable;

/**
 * Class to represent a probe.
 * @author Peter
 *
 */
public class Probe {
	private final String NAMESUFFIX = "-probe";
	private final String DEFAULT_NAME = "new" + NAMESUFFIX;
	
	private String name;
	private Diagnoser diagnoser;
	private boolean enabled = true;
	private Variable target;
	private double cost = 0;
	
	public Probe(Diagnoser dg) {
		this.diagnoser = dg;
		name = DEFAULT_NAME;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Variable getTarget() {
		return target;
	}

	public void setTarget(Variable target) throws Exception {
		if (!diagnoser.getDConnectedVars().contains(target)) {
			throw new Exception("This variable is not d-connected with the problem defining variables.");
		}
		this.target = target;
		name = createProbeName();
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	private String createProbeName() {
		String temp = target.getName();
		temp = temp.concat(NAMESUFFIX);
		return temp;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return name + "; target: " + target + "; cost: " + cost + "; enabled: " + enabled;
	}
}
