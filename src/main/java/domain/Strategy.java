package domain;

public interface  Strategy {	
	public Probe getSuggestedProbe() throws Exception;
	public StrategyName getName();
	public void setProbeSequence(ProbeSequence ps);
}
