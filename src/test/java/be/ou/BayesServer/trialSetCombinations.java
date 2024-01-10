package be.ou.BayesServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import domain.SetCombinations;

public class trialSetCombinations {

	public static void main(String[] args) {
		Map<String, Collection<String>> list1 = new HashMap<String, Collection<String>>();
		list1.put("A", new HashSet<String>());
		list1.get("A").add("Aa");
		list1.get("A").add("Ab");
		list1.get("A").add("Ac");
		Map<String, Collection<String>> list2 = new HashMap<String, Collection<String>>();
		list2.put("B", new HashSet<String>());
		list2.get("B").add("Ba");
		list2.get("B").add("Bb");
		Map<String, Collection<String>> list3 = new HashMap<String, Collection<String>>();
		list3.put("C", new HashSet<String>());
		list3.get("C").add("Ca");
		list3.get("C").add("Cb");
		list3.get("C").add("Cc");
		Map<String, Collection<String>> list4 = new HashMap<String, Collection<String>>();
		list4.put("D", new HashSet<String>());
		list4.get("D").add("Da");
		list4.get("D").add("Db");
		
		List<Collection<String>> combi = new ArrayList<Collection<String>>();
		combi.add(list1.get("A"));
		combi.add(list2.get("B"));
		combi.add(list3.get("C"));
		combi.add(list4.get("D"));
		
/*		Collection<Collection<String>> result = SetCombinations.choose(combi, 1);	
		System.out.println("\nn = 1");
//		result.forEach(r -> System.out.print(r));
		Collection<Collection<String>> result2 = SetCombinations.choose(combi, 2);
		System.out.println("\nn = 2");
		result2.forEach(r -> System.out.print(r));
		Collection<Collection<String>> result3 = SetCombinations.choose(combi, 3);	
		System.out.println("\nn = 3");
		result3.forEach(r -> System.out.print(r));
		Collection<Collection<String>> result4 = SetCombinations.choose(combi, 4);	
		System.out.println("\nn = 4");
		result4.forEach(r -> System.out.print(r));
*/	}

}
