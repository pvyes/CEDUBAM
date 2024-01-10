package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SetCombinations {

    public static <T> List<Collection<T>> getCartesianProduct(List<Collection<T>> collectionsList) {
        List<Collection<T>> result =  new ArrayList<Collection<T>>();
        if (collectionsList == null || collectionsList.isEmpty()) {
        	return result;
          } else {
            permutationsImpl(collectionsList, result, 0, new ArrayList<T>());
            return result;
          }
    }
    
   /** Recursive implementation for {@link #permutations(List, Collection)} 
    * @param <T>
    * */
   public static <T> void permutationsImpl(List<Collection<T>> collectionsList, List<Collection<T>> result, int d, List<T> arrayList) {
     // if depth equals number of original collections, final reached, add and return
     if (d == collectionsList.size()) {
       result.add(arrayList);
       return;
     }

     // iterate from current collection and copy 'current' element N times, one for each element
     Collection<T> currentCollection = collectionsList.get(d);
     for (T element : currentCollection) {
       List<T> copy = new ArrayList<T>(arrayList);
       copy.add(element);
       permutationsImpl(collectionsList, result, d + 1, copy);
     }
   }
   
   /**
    * Combines the elements of the lists in sets of the given cardinality n, where the combinations do not include more than one element per list.
    * If n = 1, returns all elements as lists with a single element. 
    * If the cardinality is greater than the amount of lists, combinations are not possible, and the return is an empty list.
    * @param n the cardinality of the lists to return
    * @param lists the sets of which the resulting lists are combined
    * @return a list of all combinations of cardinality n with at most one element of each starting collection per returned combination. 
    */
	public static <T> List<List<T>> choose(List<List<T>> lists, int n) {
		List<List<T>> result = new ArrayList<List<T>>();
		if (n > lists.size()) {
			return result;
		}
		//transform collection into lists
		List<List<T>> collslist = new ArrayList<List<T>>();
		for(List<T> coll: lists) {
			collslist.add(new ArrayList<T>(coll));
		}
		//make combinations
		result = combineLists(collslist, n);
		return result;  
	}
  
	/*
	 * Combines the elements of the lists in sets of the given cardinality n, where the combinations do not include more than one element per list.
     * If n = 1, returns all elements as lists with a single element. 
	 */
	private static <T> List<List<T>> combineLists(List<List<T>> collslist, int n) {
		List<List<T>> result = new ArrayList<List<T>>();
		//if n = 1 return a list of sets with a single element for all elements of elements of collslist
		if (n == 1) {
			for (List<T> list: collslist) {
				for (T el: list) {
					ArrayList<T> newlist = new ArrayList<T>();
					newlist.add(el);
					result.add(newlist);
				}
			}
			return result;
		}
		result = combineHelper(collslist, n);
		return result;
	}

	/*
	 * Combines all elements of firstlist with the elements of the elements of collslist
	 * If next = 0, all elements of elements of colllist are returned as lists.
	 */
	private static <T> List<List<T>> combineHelper(List<List<T>> collslist, int n) {
		List<List<T>> result = new ArrayList<List<T>>();		
		//Make all listcombinations for n - 1 lists
		List<List<List<T>>> listcombinations = makeListCombinations(collslist, n - 1);
		//Make all combinations of elements of cardinality n -1
		//key is the list of sets, value is the list of combinations of elements of this set
		Map<List<List<T>>, List<List<T>>> elementcombinations = new HashMap<List<List<T>>, List<List<T>>>();
		for (List<List<T>> listcombination: listcombinations) {
			elementcombinations.put(listcombination, combineListElements(listcombination));
		}
		//Combine the elementcombinations with all particular elements of the remaining lists
		for (Entry<List<List<T>>, List<List<T>>> entry: elementcombinations.entrySet()) {
			List<T> lastlist = entry.getKey().get(n - 2);
			int index = collslist.indexOf(lastlist);
			if (index < collslist.size() - 1) {
				List<List<T>> otherlists = new ArrayList<List<T>>();
				for (int i = index + 1; i < collslist.size(); i++) {
					otherlists.add(collslist.get(i));
				}
				result.addAll(combine(entry.getValue(), otherlists));
			}
		}
		return result; 
	}
	
	private static <T> List<List<T>> combineListElements(List<List<T>> listcombination) {
		List<List<T>> firstlist = makeListOfElements(listcombination.get(0));
		for (int i = 1; i < listcombination.size(); i++) {
			List<List<T>> secondlist = makeListOfElements(listcombination.get(i));
			firstlist = combine(firstlist, secondlist);
		}
		return firstlist;
	}

	/**
	 * Returns all combination of the lists (not of their elements) of n elements
	 * @param <T>
	 * @param collslist
	 * @param n
	 * @return
	 */
	public static <T> List<List<List<T>>> makeListCombinations(List<List<T>> collslist, int n) {		
		List<List<List<T>>> result = generateCombinations(collslist, n);				
		return result;		
	}

	/**
	 * Makes the combinations with cardinality k out of the elements given in the input list.
	 * @param <U> The type of the elements of the input
	 * @param input A list of elements to combine
	 * @param k The number of elements in the combination. If this number is 0 or is greater than the size of the input, an empty list is returned. If negative, an exception is thrown.
	 * @return The list of combinations.
	 */
	public static <U> List<List<U>> generateCombinations(List<U> input, int k) {
		List<List<U>> subsets = new ArrayList<List<U>>();
		int[] s = new int[k]; // here we'll keep indices pointing to elements in input array
		if (k == 0) {
			return subsets;
		}
		if (k <= input.size()) {
		    // first index sequence: 0, 1, 2, ...
		    for (int i = 0; (s[i] = i) < k - 1; i++);  
		    subsets.add(getSubset(input, s));
		    for(;;) {
		        int i;
		        // find position of item that can be incremented
		        for (i = k - 1; i >= 0 && s[i] == input.size() - k + i; i--); 
		        if (i < 0) {
		            break;
		        }
		        s[i]++;                    // increment this item
		        for (++i; i < k; i++) {    // fill up remaining items
		            s[i] = s[i - 1] + 1; 
		        }
		        subsets.add(getSubset(input, s));
		    }
		}
		return subsets;
	}

	// generate actual subset by index sequence
	private static <U> List<U> getSubset(List<U> input, int[] s) {
	    List<U> result = new ArrayList<U>(); 
	    for (int i = 0; i < s.length; i++) 
	        result.add(i, input.get(s[i]));
	    return result;
	}

	/*
    * Combines the elements of firstlist with the elements of the elements of otherlists.
    */
	private static <T> List<List<T>> combine(List<List<T>> firstlist, List<List<T>> otherlists) {
		List<List<T>> result = new ArrayList<List<T>>();
		for (List<T> first : firstlist) {
			List<List<T>> nextlists = new ArrayList<List<T>>(otherlists);
			result.addAll(combineList(first, nextlists));
		}
		return result;
	}

   /*
    * Combines the list with the particular elements of lists to a list of collections.
    */
	private static <T> List<List<T>> combineList(List<T> list, List<List<T>> secondlists) {
		//combine with elements of the second list
		List<List<T>> combi = new ArrayList<List<T>>();
		for (int j = 0; j < secondlists.size(); j++) {
			List<T> secondlist = secondlists.get(j);
			for (T el2: secondlist) {
				ArrayList<T> current = new ArrayList<T>();
				current.addAll(list);
				current.add(el2);
				combi.add(current);
			}
		}
		return combi;
	}
	
	private static <T> List<List<T>> makeListOfElements(List<T> list) {
		List<List<T>> newlist = new ArrayList<List<T>>();
		   for (T el: list) {
			   List<T> ellist = new ArrayList<T>();
			   ellist.add(el);
			   newlist.add(ellist);
		   }
		return newlist;
	}

   
   /*
    * As the functionname suggests: returns all permutations of the elements of the list, without repetition.
    * This is an implementation of Heap's algorithm.
    */
   public static <T> Collection<List<T>> getPermutationsWithoutRepeat(Collection<T> list) {
	   Collection<List<T>> result = new HashSet<List<T>>();
	   ArrayList<T> elementsList = new ArrayList<T>(list);
	   getAllRecursive(elementsList.size(), elementsList, result);
	   return result;
   }
   
   /**
    * 
    * @param <T>
    * @param n the number of elements in the permutation
    * @param elements the possible elements of the permutations
    * @param result a Collection to store the results
    */
   private static <T> void getAllRecursive(int n, List<T> elements, Collection<List<T>> result) {		   
	     if(n == 1) {
	    	 List<T> copy = new ArrayList<T>(elements);
	         result.add(copy);
	     } else {
	         for(int i = 0; i < n-1; i++) {
	             getAllRecursive(n - 1, elements, result);
	             if(n % 2 == 0) {
	                 swap(elements, i, n-1);
	             } else {
	                 swap(elements, 0, n-1);
	             }
	         }
	         getAllRecursive(n - 1, elements, result);
	     }
	 }

	 private static <T> void swap(List<T> elements, int a, int b) {
	     T tmp = elements.get(a);
	     elements.set(a, elements.get(b));
	     elements.set(b, tmp);
	 }
	 
	 public static <T> List<List<T>> addElementsOfsecondToFirst(List<T> first, List<T> second) {
		List<List<T>> result = new ArrayList<List<T>>();
		for (T s: second) {
			ArrayList<T> tmp = new ArrayList<T>(first);
			tmp.add(s);
			result.add(tmp);
		}
		return result; 
	 }

	public static <T> Collection<Pair<List<T>, List<T>>> divideInTwoSets(List<T> list) {
		Collection<Pair<List<T>, List<T>>> result = new HashSet<Pair<List<T>, List<T>>>();
		for (var i = 1; i < list.size(); i++) {
			List<List<T>> combis = generateCombinations(list, i);
			for (var j = 0; j < combis.size(); j ++) {
				List<T> first = combis.get(j);
				List<T> second = new ArrayList<T>(list);
				second.removeAll(first);
				Pair<List<T>, List<T>> pair = new Pair<List<T>, List<T>>();
				pair.setFirst(first);
				pair.setSecond(second);
				result.add(pair);
			}
		}
		return result;
	}
	
	 public static <T> Collection<List<T>> getPermutationsWithRepeat(Collection<T> list) {
		return getPermutationsWithRepeat(list, list.size());
	}

	public static <T> Collection<List<T>> getPermutationsWithRepeat(Collection<T> list, int k) {
		List<T> elements = new ArrayList<T>(list);
		List<List<T>> result = new ArrayList<List<T>>();
		int n = elements.size();
		if (k < 1 || k > n) {
			throw new IllegalArgumentException("Illegal number of positions.");
		}	
		int[] indexes = new int[n];
		int total = (int) Math.pow(n, k);
		
		while (total-- > 0) {
			List<T> perm = new ArrayList<T>();
			
			for (int i = 0; i < n - (n - k); i++) {	        		
				perm.add(elements.get(indexes[i]));
			}
			System.out.println(perm);
			result.add(perm);
			
			for (int i = 0; i < n; i++) {
				if (indexes[i] >= n - 1) {
					indexes[i] = 0;
				} else {
					indexes[i]++;
					break;
				}
			}
		}
		return result;
	}
}
