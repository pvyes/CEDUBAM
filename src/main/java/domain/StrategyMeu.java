package domain;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class StrategyMeu implements Strategy {
	Diagnoser diagnoser;
	StrategyName strategyname;
	ProbeSequence ps;
	
	public StrategyMeu(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.strategyname = StrategyName.MEU;
	}	

	public StrategyName getName() {
		return strategyname;
	}

	public void setStrategyName(StrategyName name) {
		this.strategyname = name;
	}
	
	public ProbeSequence getPs() {
		return ps;
	}

	public void setProbeSequence(ProbeSequence probesequence) {
		this.ps = probesequence;
	}

	@Override
	public Probe getSuggestedProbe() throws Exception {
		return chooseProbe();
	}

	private Probe chooseProbe() throws Exception {
		Collection<Probe> probes = null;
		Collection<Diagnosis> diagnoses = null;
		if (ps != null) {
			probes = ps.getRemainingProbes();
			diagnoses = ps.getRemainingDiagnoses();
			//set evidence of the ps 
			ps.activateStateEvidence(null, true);
		} else {
			probes = diagnoser.getEnabledProbes();
			diagnoses = diagnoser.getPossibleDiagnoses();
		}
		MEUResult result = diagnoser.getMEU(probes, diagnoses);
		diagnoser.setMeuresult(result);
		return getSuggestedProbe(result.getMeuProbes());
	}
	
	private Probe getSuggestedProbe(Collection<Probe> meus) {
		if (meus.size() == 1) {
			return meus.iterator().next();
		} else {
			Object[] array = meus.toArray();
			if (array.length > 0) {
				int randomNum = ThreadLocalRandom.current().nextInt(0, meus.size());
				return (Probe) Array.get(array, randomNum);
			}
		}
		return null;
	}
}
