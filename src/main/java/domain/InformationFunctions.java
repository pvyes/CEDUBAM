package domain;

import java.util.ArrayList;
import java.util.List;

public class InformationFunctions {
	
	private static List<InformationFunction> informationfunctions = new ArrayList<InformationFunction>();

	private static void createInformationfunctions() {
		informationfunctions.add(new InformationStateProbability());
		informationfunctions.add(new InformationEntropyReversed());
		informationfunctions.add(new InformationEntropyDifference());
	}

	public static List<InformationFunction> getInformationfunctions() {
		informationfunctions.clear();
		createInformationfunctions();
		return informationfunctions;
	}

	public static void addInformationfunctions(InformationFunction informationfunction) {
		informationfunctions.add(informationfunction);
	}
}
