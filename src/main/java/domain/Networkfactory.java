package domain;

import java.util.ArrayList;

import com.bayesserver.Network;

public class Networkfactory {
	
	public static final String GENERATED_CIRCUIT = "resources/systems/generated_circuit.bayes";
	public static final String TWO_LIGHTS_REDUNDANT = "resources/systems/twolights-redundant.bayes";
	public static final String WATERPIPES = "resources/systems/WaterPipes.bayes";
	public static final String OR_AND = "resources/systems/OR_AND_Undefined.bayes";
	
	private static Network network;
	
	public static Network loadBayesNetwork(String networkpath) throws Exception {
		network = new Network();
		network.load(networkpath);		
		return network;
	}
	
	public static ArrayList<String> getAvailableNetworks() {
		ArrayList<String> networks = new ArrayList<String>();
		networks.add(TWO_LIGHTS_REDUNDANT);
		networks.add(GENERATED_CIRCUIT);
		networks.add(WATERPIPES);
		networks.add(OR_AND);
		return networks;		
	}

	public static int getNetworkIndexByName(String name) {
		for (int i = 0; i < Networkfactory.getAvailableNetworks().size(); i++) {
			String nw = Networkfactory.getAvailableNetworks().get(i).toLowerCase();
			String ln = name.toLowerCase();
			String regex = ".*"+ln+".*";
			if (nw.matches(regex)) {
				return i;
			}
		}
		return -1;
	}
}
