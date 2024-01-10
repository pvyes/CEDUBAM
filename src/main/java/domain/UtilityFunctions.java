package domain;

import java.util.ArrayList;
import java.util.List;

public class UtilityFunctions {
	
	private static List<UtilityFunction> utilityfunctions = new ArrayList<UtilityFunction>();
	
	private static void createUtilityfunctions() {
		new UtilityLinear();
		new UtilityInformationPerCost();
		new UtilityWeightedCost();
	}

	public static List<UtilityFunction> getUtilityfunctions() {
		utilityfunctions.clear();
		createUtilityfunctions();
		return utilityfunctions;
	}

	public static void addUtilityfunctions(UtilityFunction utilityfunction) {
		utilityfunctions.add(utilityfunction);
	}
}
