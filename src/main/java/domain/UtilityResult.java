package domain;

public class UtilityResult {
	private InformationResult informationresult;
	private double utility;
	
	public UtilityResult() {
		
	}
	
	public UtilityResult(InformationResult informationresult) {
		this.informationresult = informationresult;
	}
	
	public UtilityResult(InformationResult informationresult, double utility) {
		this.informationresult = informationresult;
		this.utility = utility;
	}

	public InformationResult getInformationresult() {
		return informationresult;
	}

	public void setInformationresult(InformationResult informationresult) {
		this.informationresult = informationresult;
	}

	public double getUtility() {
		return utility;
	}

	public void setUtility(double utility) {
		this.utility = utility;
	}

	public Probe getProbe() {
		return informationresult.getProbe();
	}

	@Override
	public String toString() {
		return "[probe: " + informationresult.getProbe().getTarget() + " " +  informationresult.toString() + ", utility=" + utility + "]";
	}	
}
