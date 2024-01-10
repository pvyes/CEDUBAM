package domain;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class StrategyCheapest implements Strategy {
	Diagnoser diagnoser;
	StrategyName name;
	ProbeSequence ps;
	
	public StrategyCheapest(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.name = StrategyName.CHEAPEST;
	}	

	public StrategyName getName() {
		return name;
	}

	public void setName(StrategyName name) {
		this.name = name;
	}
	
	public ProbeSequence getPsr() {
		return ps;
	}
	
	@Override
	public void setProbeSequence(ProbeSequence probesequence) {
		this.ps = probesequence;
	}
	
	@Override
	public Probe getSuggestedProbe() {
		return chooseProbe();
	}

	private Probe chooseProbe() {
		Collection<Probe> probes = null;
		if (ps != null) {
			probes = ps.getRemainingProbes();
		} else {
			probes = diagnoser.getEnabledProbes();
		}
		Collection<Probe> cheapestProbes = getCheapestProbes(probes);
		Object[] array = cheapestProbes.toArray();
		if (array.length > 0) {
			int randomNum = ThreadLocalRandom.current().nextInt(0, cheapestProbes.size());
			return (Probe) Array.get(array, randomNum);
		}
		return null;
	}

	private Collection<Probe> getCheapestProbes(Collection<Probe> probes) {
		Collection<Probe> result = new HashSet<Probe>();
		double min = Double.POSITIVE_INFINITY;
		for (Probe p: probes) {
			double cost = p.getCost();
			//initialize
			if (min == Double.POSITIVE_INFINITY) {
				min = cost;
			}
			if (cost < min) {
				result.clear();
				min = cost;
			}
			if (cost == min) {
				result.add(p);
			}
		}
		return result;
	}	
}
