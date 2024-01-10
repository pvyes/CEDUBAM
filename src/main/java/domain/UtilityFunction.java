package domain;

public interface UtilityFunction {
	/*
	 * Returns the name and the detailed settings (if available);
	 */
	public String settingsToString();
	public String functionToString();
	public UtilityNames getType();
	public String getName();
	public UtilityResult getUtility(InformationResult inforesult) throws Exception;
}
