package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bayesserver.State;

public class ProbeSequencer {
	
	private Diagnoser diagnoser;
	private List<ProbeScenario> scenarios;
	private Timer timer = new Timer();
	private boolean success = false;
	private ProbeSequence currentProbeSequence;
	
	public ProbeSequencer(Diagnoser sDiagnoser) {
		this.diagnoser = sDiagnoser;
		this.scenarios = new ArrayList<ProbeScenario>();
	}
	
	public Diagnoser getDiagnoser() {
		return diagnoser;
	}
		
	public boolean isSuccess() {
		return success;
	}

	public List<ProbeScenario> getScenarios() {
		return scenarios;
	}

	public ProbeSequence getCurrentProbeSequence() {
		return currentProbeSequence;
	}

	public List<ProbeScenario> run() throws Exception {
		//start
		timer.start();
		ProbeScenario ps = makeScenario();
		diagnoser.printScenario(ps);
		timer.stop();
		return this.scenarios;
	}

	public ProbeScenario makeScenario() throws Exception {
		ProbeScenario scenario = new ProbeScenario(diagnoser);
		ProbeSequence ps = setNewProbeSequence();
		scenario.addBranch(ps);
		scenario.getTimer().start();
		startProbeSequencing(scenario);
		scenarios.add(scenario);
		scenario.getTimer().stop();
		return scenario;
	}
	
	public ProbeSequence setNewProbeSequence() {
		ProbeSequence ps = new ProbeSequence(diagnoser, diagnoser.getEvidencemanager().getEvidences(true));
		ps.setRemainingProbes(diagnoser.getEnabledProbes());
		ps.setRemainingDiagnoses(diagnoser.getPossibleDiagnoses());
		return ps;
	}
	
	private ProbeScenario startProbeSequencing(ProbeScenario scenario) throws Exception {
		List<ProbeSequence> unfinishedbranches = scenario.getUnfinishedBranches();
		while (unfinishedbranches.size() > 0) {		
			scenario.getBranches().removeAll(unfinishedbranches);
			for (ProbeSequence branch: unfinishedbranches) {
				if (branch.getRemainingProbes().size() > 0 && branch.getRemainingDiagnoses().size() > 0) {
					scenario.getBranches().addAll(getSequenceResult(branch)); 
				} else {
					branch.setStatus(ProbeSequence.Status.FINISHED);
					scenario.getBranches().add(branch);
				}	
			}
			unfinishedbranches = scenario.getUnfinishedBranches();
		}
		return scenario;			
	}

	private Collection<ProbeSequence> getSequenceResult(ProbeSequence ps) throws Exception {
		Collection<ProbeSequence> result = new ArrayList<ProbeSequence>();
		Probe nextprobe = getNextprobe(ps);
 		for (State state: nextprobe.getTarget().getStates()) {
 			result.add(getSequenceResult(ps, state));
		}
		return result;	
	}

	private ProbeSequence getSequenceResult(ProbeSequence ps, State state) throws Exception {
		ProbeSequence newps = ps.getClone();
		newps.run(state);
		return newps;
	}

	private Probe getNextprobe(ProbeSequence ps) throws Exception {
		diagnoser.getStrategy().setProbeSequence(ps);
		if (diagnoser.getUtilityfunction() != null && diagnoser.getUtilityfunction().getType() == UtilityNames.WEIGHTED_COST) {
			((UtilityWeightedCost) diagnoser.getUtilityfunction()).setTreelevel(ps.getUsedStates().size());
		}
		Probe nextprobe = diagnoser.getStrategy().getSuggestedProbe();
		if (diagnoser.getStrategy().getName().equals(StrategyName.MEU)) {
			ps.setMEUResult(diagnoser.getMeuresult());
		}
		return nextprobe;
	}
}