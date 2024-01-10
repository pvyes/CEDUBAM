package domain;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class StrategyRandom implements Strategy {
	Diagnoser diagnoser;
	StrategyName name;
	ProbeSequence ps;
	
	public StrategyRandom(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.name = StrategyName.RANDOM;
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
		return chooseRandomProbe();
	}

	private Probe chooseRandomProbe() {
		Collection<Probe> probes = null;
		if (ps != null) {
			probes = ps.getRemainingProbes();
		} else {
			probes = diagnoser.getEnabledProbes();
		}
		Object[] array = probes.toArray();
		if (array.length > 0) {
			int randomNum = ThreadLocalRandom.current().nextInt(0, probes.size());
			return (Probe) Array.get(array, randomNum);
		}
		return null;
	}
}
