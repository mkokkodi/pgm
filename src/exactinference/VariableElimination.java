package exactinference;

import holders.FactorHolder;
import holders.Network;
import hw4.HW41;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import utils.Utils;

public class VariableElimination {
	private static boolean sumToOneVariable = false;
	private static Network network;
	private static int newEdges = 0;

	public static void eliminateVars(HashSet<String> givens, Network network,
			LinkedList<String> order) {
		VariableElimination.network = network;
		newEdges = 0;
		for (String eliminatingVar : order) {
			if (!givens.contains(eliminatingVar)) {
				HashSet<String> scopeFactors = network.getVarScope().get(
						eliminatingVar);

				if (HW41.verbose) {
					System.out
							.println("\n------------------------------------------------");

					System.out
							.println("Eliminating Variable:" + eliminatingVar);
					System.out.println("Factors in scope:");
					System.out.println(scopeFactors + " ");
					System.out.println("Fill edges so far:" + newEdges);

				}
				/**
				 * Summation over the values of the eliminating var
				 */

				LinkedList<FactorHolder> fhList = new LinkedList<FactorHolder>();
				for (String factor : scopeFactors) {
					fhList.add(network.getProbabilities().get(factor));
				}
				FactorHolder newFactor;

				newFactor = createNewFactor(fhList, eliminatingVar);

				removeFactorsFromNetwork(scopeFactors, eliminatingVar);
				if (newFactor != null)
					addNewFactorToNetwork(newFactor);

			}
		}
		System.out.println("New fill-edges:" + newEdges);

	}

	private static void addNewFactorToNetwork(FactorHolder fh) {
		if (HW41.verbose)
			System.out.println("Adding Factor to the Network:" + fh.getId());
		network.getProbabilities().put(fh.getId(), fh);

		String[] dependendVars = fh.getId().split("_");
		/*
		 * Dependencies are also new edges.
		 */
		for (String dependentVar : dependendVars) {

			if (network.getVars().containsKey(dependentVar)) {
				LinkedList<String> s = network.getUndirectedDependencies().get(
						dependentVar);

				if (s != null && !dependentVar.equals(fh.getId())
						&& !s.contains(fh.getId())) {
					// System.out.println("new edge:"+dependentVar+"_"+fh.getId());
					s.add(fh.getId());
					newEdges++;
				}

			}
			network.getVarScope().get(dependentVar).add(fh.getId());

		}

		if (HW41.printNewFactorScope) {

			for (String dependentVar : dependendVars) {
				HashSet<String> hs = network.getVarScope().get(dependentVar);
				if (!HW41.inducedWidths.containsKey(hs.size())) {
					LinkedList<String> ls = new LinkedList<String>();
					ls.add(dependentVar);
					for (String id : hs) {
						ls.add(id);
					}
					HW41.inducedWidths.put(hs.size(),ls);
				}

			}
		}

	}

	private static void removeFactorsFromNetwork(HashSet<String> scopeFactors,
			String eliminatingVar) {

		if (HW41.verbose) {
			/*
			 * Logging
			 */
			System.out.print("Removing Vars:" + eliminatingVar);
			for (String var : scopeFactors)
				System.out.print(" " + var);
			System.out.println();
		}
		/*
		 * 
		 */

		network.getVars().remove(eliminatingVar);
		network.getUndirectedDependencies().remove(eliminatingVar);

		for (String factor : scopeFactors) {
			network.getProbabilities().remove(factor);
			network.getUndirectedDependencies().remove(factor);

		}
		network.getVarScope().remove(eliminatingVar);

		for (Entry<String, HashSet<String>> e : network.getVarScope()
				.entrySet()) {

			e.getValue().removeAll(scopeFactors);
			network.getUndirectedDependencies().get(e.getKey())
					.removeAll(scopeFactors);

		}

	}

	private static FactorHolder createNewFactor(
			LinkedList<FactorHolder> fhList, String eliminatingVar) {

		String newKey = createNewKey(fhList, eliminatingVar);
		return multiplyFactors(fhList, eliminatingVar, newKey);
	}

	private static String createNewKey(LinkedList<FactorHolder> fhList,
			String eliminatingVar) {
		String newKey = "";
		sumToOneVariable = false;
		for (FactorHolder fh : fhList) {

			// String tmp = fh.getId().replace(eliminatingVar, "");
			String[] tmpAr = fh.getId().split("_");
			for (String var : tmpAr) {
				if (!newKey.contains("_" + var + "_")
						&& !var.equals(eliminatingVar)) {
					newKey += "_" + var + "_"; // the two __ needed just for the
												// case where var is a subset of
												// some other variable.
				}
			}
		}
		newKey = newKey.replaceAll("_(_)+", "_");
		if (newKey.startsWith("_"))
			newKey = newKey.replaceFirst("_", "");

		if (newKey.endsWith("_"))
			newKey = newKey.substring(0, newKey.length() - 1);
		if (!eliminatingVar.equals("")
				&& network.getProbabilities().keySet().contains(newKey)) {
			if (HW41.verbose)
				System.err.println("Key " + newKey
						+ " already in set. Will sum to 1 so I can ignore it.");
			sumToOneVariable = true;

		}

		return newKey;
	}

	private static FactorHolder multiplyFactors(
			LinkedList<FactorHolder> fhList, String eliminatingVar,
			String newKey) {

		if (HW41.verbose)
			System.out.println("Multiplying factors:" + fhList.size());
		FactorHolder newFactor = null;
		/*
		 * If it sums to one I can ignore the summing and continue to
		 * adding/removing factors.
		 */
		if (!sumToOneVariable && newKey.length() > 1) {
			HashMap<String, Float> newFactorMap = new HashMap<String, Float>();
			newFactor = new FactorHolder(newFactorMap, newKey);
			String[] newFactorVariables = newKey.split("_");

			/*
			 * Adding values to new factor variables.
			 */
			LinkedList<String[]> newFactorVariablesValuesList = new LinkedList<String[]>();

			for (String newFactorVariable : newFactorVariables) {
				LinkedList<String> tmp = network.getVars().get(
						newFactorVariable);

				newFactorVariablesValuesList.add(tmp.toArray(new String[tmp
						.size()]));
			}

			/*
			 * Estimate combinations of all these values.
			 */

			String[] combinations = Utils.findCombinations(
					newFactorVariablesValuesList,
					new LinkedList<String>(Arrays.asList(newFactorVariables)));

			// System.out.println("Combinations found:"+combinations.length);
			for (String newVarValue : combinations) {

				if (eliminatingVar != null) {
					/*
					 * Sums the products in terms of the eliminating Variable.
					 */
					marginalize(newFactor, fhList, eliminatingVar, newVarValue,
							newFactorMap);
				} else {
					/*
					 * Multiplies Only the factors in fhList. (This is used only
					 * in the estimation fo the final factor map.
					 */
					multiply(newFactor, fhList, newVarValue, newFactorMap);
				}
			}

		}
		return newFactor;

	}

	private static void multiply(FactorHolder newFactor,
			LinkedList<FactorHolder> fhList, String newVarValue,
			HashMap<String, Float> newFactorMap) {
		String[] newFactorValues = newVarValue.split("_");
		float f = 1f;

		for (FactorHolder fh : fhList) {

			String key = getMapKey(fh.getId(), null, null, newFactorValues);
			f *= fh.getFactorTable().get(key);
		}

		Float curV = newFactorMap.get(newVarValue);
		if (curV != null)
			f += curV;

		newFactorMap.put(newVarValue, f);
		Utils.checkProb(f, newVarValue);

	}

	private static void marginalize(FactorHolder newFactor,
			LinkedList<FactorHolder> fhList, String eliminatingVar,
			String newVarValue, HashMap<String, Float> newFactorMap) {

		// System.out.println("Marginalize..");
		String[] newFactorValues = newVarValue.split("_");
		for (String val : network.getVars().get(eliminatingVar)) {
			float f = 1f;

			for (FactorHolder fh : fhList) {

				String key = getMapKey(fh.getId(), eliminatingVar, val,
						newFactorValues);
				f *= fh.getFactorTable().get(key);

			}

			// System.out.println("loop by loop.");
			Float curV = newFactorMap.get(newVarValue);
			if (curV != null)
				f += curV;

			newFactorMap.put(newVarValue, f);

			Utils.checkProb(f, newVarValue);

		}

	}

	private static String getMapKey(String factorId, String eliminatingVar,
			String eliminatingVarValue, String[] newFactorValues) {

		String mapKey = "_";
		String[] factorVars = factorId.split("_");
		for (String factor : factorVars) {
			if (factor.equals(eliminatingVar)
					&& !mapKey.contains("_" + eliminatingVar + ":"
							+ eliminatingVarValue)) {

				mapKey += factor + ":" + eliminatingVarValue + "_";
			} else {
				for (String tmpFactorValue : newFactorValues) {
					String[] tmpAr = tmpFactorValue.split(":");
					if (factor.equals(tmpAr[0])
							&& !mapKey
									.contains("_" + tmpAr[0] + ":" + tmpAr[1])) {
						mapKey += factor + ":" + tmpAr[1] + "_";
						break;
					}
				}
			}
		}

		mapKey = mapKey.replaceFirst("_", "");
		return mapKey.substring(0, mapKey.length() - 1);
	}

	public static FactorHolder estimateFinalFactor(Network network) {
		LinkedList<FactorHolder> fhList = new LinkedList<FactorHolder>(network
				.getProbabilities().values());
		String newKey = createNewKey(fhList, "");
		FactorHolder finalFactorHolder = multiplyFactors(fhList, null, newKey);
		return finalFactorHolder;
	}
}
