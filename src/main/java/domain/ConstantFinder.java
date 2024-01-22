package domain;

import java.util.Collection;

public interface ConstantFinder {
	public Collection<ProbeScenario> getOptimalScenarios(int iterationlimit, int timelimit) throws Exception;

}
