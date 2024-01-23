package domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StrategyFixed implements Strategy {
	Diagnoser diagnoser;
	StrategyName name;
	ProbeSequence ps;
	List<Probe> probelist;
	Iterator<Probe> iterator = null;
	
	public StrategyFixed(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.name = StrategyName.FIXED;
		this.probelist = new ArrayList<Probe>();
	}	

	public StrategyName getName() {
		return name;
	}

	public void setName(StrategyName name) {
		this.name = name;
	}
	
	public ProbeSequence getPs() {
		return ps;
	}

	public void setProbeSequence(ProbeSequence probesequence) {
		this.ps = probesequence;
	}
	
	public List<Probe> getProbelist() {
		return probelist;
	}

	public void setProbelist(List<Probe> probelist) {
		this.probelist = probelist;
	}

	@Override
	public Probe getSuggestedProbe() {
		return getNext();
	}
	
	private Probe getNext() {
		int index = ps.getUsedProbes().size();
		return probelist.get(index);
	}
	
	public String probelistToString() {
		String str = "[";
		for (Probe p: probelist) {
			str += p.getName() + ", ";
		}
		str = str.substring(0, str.length() - 2);
		str += "]";
		return str;
	}
}
