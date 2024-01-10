package domain;

import com.bayesserver.State;

public interface InformationFunction {
	public InformationResult getInformation(Diagnoser sDiagnoser, Probe probe, State probestate, Diagnosis diagnosis) throws Exception;
	public InformationResult getExpectedInformation(Diagnoser sDiagnoser, Probe probe, Diagnosis diagnosis) throws Exception;
	public InformationType getInformationtype();
}
