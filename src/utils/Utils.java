package utils;

import holders.FactorHolder;
import holders.CliqueTree;
import holders.MutualInformation;
import holders.Network;
import holders.PairwiseCRF;
import hw4.HW42;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;

import comparator.DoubleValueComparatorDecreasing;
import comparator.ValueComparator;

public class Utils {

	public static Network loadNetwork() {

		HashMap<String, FactorHolder> probs = new HashMap<String, FactorHolder>();
		HashMap<String, LinkedList<String>> vars = new HashMap<String, LinkedList<String>>();
		HashSet<String> directedEdges = new HashSet<String>();
		Network net = new Network(probs, vars, directedEdges);

		try {
			BufferedReader input = new BufferedReader(new FileReader(
					PropertiesFactory.getInstance().getProps()
							.getProperty("path")
							+ "alarm.bif"));
			String line;
			while ((line = input.readLine()) != null) {
				/*
				 * variable HYPOVOLEMIA { type discrete [ 2 ] { TRUE, FALSE }; }
				 */
				if (line.contains("variable")) {
					String var = line.split(" ")[1].trim();
					// System.out.println(var);
					LinkedList<String> list = new LinkedList<String>();

					while (!(line = input.readLine()).equals("}")) {
						String[] tmpAr = line.split("\\{");
						String[] values = tmpAr[1].replace("};", "").split(",");
						for (String val : values) {
							list.add(val.trim());

						}

					}
					// System.out.println("Adding var:" + var + " " + list +
					// " ");
					vars.put(var, list);

				}
				if (line.contains("probability")) {
					String key = line.split("\\(")[1].replace(" ) {", "")
							.trim();
					String var = null;
					String[] givens = null;
					if (key.contains(" | ")) {
						String[] tmpAr = key.split(" \\| ");
						var = tmpAr[0].trim();
						givens = tmpAr[1].split(",");
					} else
						var = key;

					String mapKey = var;
					if (givens != null) {
						for (String given : givens) {
							mapKey += "_" + given.trim();
							directedEdges.add(given.trim() + ">" + var);
						}

					}

					/*
					 * (LOW, LOW) 0.98, 0.01, 0.01;
					 */
					HashMap<String, Float> valuesMap = new HashMap<String, Float>();

					while (!(line = input.readLine()).equals("}")) {
						String[] tmpAr = line.split("\\)");
						String[] givenValues = tmpAr[0].replace("(", "").split(
								",");
						// System.out.println(var.trim());
						LinkedList<String> mainVarValues = vars.get(var);
						/*
						 * ValuesKey main var value > given values
						 */
						String values = "";
						/*
						 * checks for unconditional probs
						 */
						String[] actualProbs;
						if (tmpAr.length > 1) {
							actualProbs = tmpAr[1].split(",");
							for (int i = 0; i < givenValues.length; i++) {
								values += givens[i].trim() + ":"
										+ givenValues[i].trim() + "_";
							}
							values = values.substring(0, values.length() - 1);
						} else {
							actualProbs = tmpAr[0].replace("table", "").trim()
									.split(",");
						}

						// System.out.println(values);

						int i = 0;
						for (String val : mainVarValues) {
							String valKey = var
									+ ":"
									+ val.trim()
									+ ((values.length() > 0) ? "_" + values
											: "");
							Float prob = Float.parseFloat(actualProbs[i]
									.replace(";", "").trim());
							// System.out.println(valKey+" :" +prob);
							valuesMap.put(valKey, prob);
							i++;

						}

					}
					FactorHolder fh = new FactorHolder(valuesMap, mapKey);
					probs.put(mapKey, fh);

				}
			}
			input.close();
		} catch (IOException e) {
		}

		return net;
	}

	public static void printUndirectedGraph(Network network) {
		PrintToFile pf = new PrintToFile();
		pf.openFile(PropertiesFactory.getInstance().getProps()
				.getProperty("path")
				+ "undirected.dot");
		pf.writeToFile("digraph simple { ");
		for (String node : network.getVars().keySet()) {
			// n0[label="Writing & Translation",style="filled",color="azure"];
			pf.writeToFile(node + "[label=\"" + node
					+ "\",style=\"filled\",color=\"darkolivegreen1\"];");
		}
		for (String edge : network.getUndirectedEdges()) {
			String[] nodes = edge.split("_");
			// n0->n0[dir=none]
			pf.writeToFile(nodes[0] + "->" + nodes[1] + "[dir=none]");

		}
		pf.writeToFile("}");

	}

	public static void printDirectedGraph(Network network) {
		PrintToFile pf = new PrintToFile();
		pf.openFile(PropertiesFactory.getInstance().getProps()
				.getProperty("path")
				+ "directed.dot");
		pf.writeToFile("digraph simple { ");
		for (String node : network.getVars().keySet()) {
			// n0[label="Writing & Translation",style="filled",color="azure"];
			pf.writeToFile(node + "[label=\"" + node
					+ "\",style=\"filled\",color=\"darkolivegreen1\"];");
		}
		for (String edge : network.getDirectedEdges()) {
			String[] nodes = edge.split(">");
			// n0->n0[dir=none]
			pf.writeToFile(nodes[0] + "->" + nodes[1]);

		}
		pf.writeToFile("}");

	}

	public static LinkedList<String> minFill(Network network) {

		LinkedList<String> ordering = new LinkedList<String>();
		HashMap<String, Integer> varNewEdges = new HashMap<String, Integer>();
		ValueComparator vc = new ValueComparator(varNewEdges);
		TreeMap<String, Integer> tm = new TreeMap<String, Integer>(vc);
		HashSet<String> markedNodes = new HashSet<String>();
		HashMap<String, LinkedList<String>> hm = new HashMap<String, LinkedList<String>>(
				network.getVars());
		while (network.getVars().size() > 0) {
			for (Entry<String, LinkedList<String>> e : network
					.getUndirectedDependencies().entrySet()) {
				String mainNode = e.getKey();
				if (markedNodes.contains(mainNode))
					continue;
				int newEdges = 0;
				if (e.getValue().size() > 1) {
					String[] dependentNodes = new String[e.getValue().size()];
					int l = 0;
					for (String node : e.getValue()) {
						dependentNodes[l] = node;
						l++;
					}
					for (int i = 0; i < dependentNodes.length; i++) {
						for (int j = i + 1; j < dependentNodes.length; j++) {
							if (!network.getUndirectedEdges()
									.contains(
											dependentNodes[i] + "_"
													+ dependentNodes[j])
									&& !network.getUndirectedEdges().contains(
											dependentNodes[j] + "_"
													+ dependentNodes[i])) {
								newEdges++;

							}
						}
					}

				}

				varNewEdges.put(mainNode, newEdges);
			}
			tm.putAll(varNewEdges);
			// System.out.println("Ceiling entry:"+tm.firstKey());
			String curNode = null;
			for (Entry<String, Integer> e : tm.entrySet()) {
				if (!markedNodes.contains(e.getKey())) {
					curNode = e.getKey();
					break;
				}
			}
			if (curNode == null)
				return ordering;
			LinkedList<String> l = network.getUndirectedDependencies().get(
					curNode);
			String[] dependentNodes = l.toArray(new String[l.size()]);

			for (int i = 0; i < dependentNodes.length; i++) {
				for (int j = i + 1; j < dependentNodes.length; j++) {
					if (!network.getUndirectedEdges().contains(
							dependentNodes[i] + "_" + dependentNodes[j])
							&& !network.getUndirectedEdges()
									.contains(
											dependentNodes[j] + "_"
													+ dependentNodes[i])) {
						// System.out.println("adding new Node;");
						network.getUndirectedEdges().add(
								dependentNodes[j] + "_" + dependentNodes[i]);

					}
				}

			}
			// network.getVars().remove(curNode);
			// System.out.println("Removing node.");
			markedNodes.add(curNode);
			// System.out.println("Marked nodes size:" + markedNodes.size());
			ordering.add(curNode);
			// System.out.println(curNode);
		}

		return null;

	}

	public static void printNetwork(Network network) {
		System.out
				.println("------------------- Current Network --------------");
		for (Entry<String, FactorHolder> e1 : network.getProbabilities()
				.entrySet()) {
			System.out.println("Factor:" + e1.getKey());
			for (Entry<String, Float> e2 : e1.getValue().getFactorTable()
					.entrySet())
				System.out.println(e2.getKey() + ":" + e2.getValue());
		}
	}

	/**
	 * 
	 * @param str
	 *            is in the form: Var = Value, Var2 = Value2...
	 * @param varValuesMap
	 */
	public static void insertToVarValuesMap(String str,
			HashMap<String, String> varValuesMap) {
		String[] tmpAr2 = str.split(",");
		for (String varValue : tmpAr2) {
			String[] tmpAr3 = varValue.split("=");

			varValuesMap.put(tmpAr3[0].toUpperCase().trim(), tmpAr3[1]
					.toUpperCase().trim());
		}

	}

	public static void printResult(FactorHolder finalFactorHolder,
			HashMap<String, String> conditionalVarValues,
			HashMap<String, String> uncoditionalVarValues) {

		// printFactor(finalFactorHolder);

		float normalizingFactor = estmateNormalizationFactor(finalFactorHolder,
				conditionalVarValues, uncoditionalVarValues);

		String[] tmpAr = finalFactorHolder.getId().split("_");
		String mapKey = "";
		for (String f : tmpAr) {
			if (conditionalVarValues.containsKey(f))
				mapKey += f + ":" + conditionalVarValues.get(f) + "_";
			else
				mapKey += f + ":" + uncoditionalVarValues.get(f) + "_";
		}
		mapKey = mapKey.substring(0, mapKey.length() - 1);

		float prob = finalFactorHolder.getFactorTable().get(mapKey);

		System.out
				.println("\n************** Result *************************\n");
		System.out.println("f("
				+ mapKey.replaceAll("_", ",").replaceAll(":", "=") + ")="
				+ prob);
		System.out.println("Pr(query)=" + prob / normalizingFactor);
		System.out
				.println("\n************************************************");

	}

	/**
	 * Estimates the normalizatoin factor by making sure that P(X|Y=y) sums to
	 * one. (i.e., the sum of X given Y=y sum to one)
	 * 
	 * @param finalFactorHolder
	 * @param conditionalVarValues
	 * @param uncoditionalVarValues
	 * @return
	 */
	private static float estmateNormalizationFactor(
			FactorHolder finalFactorHolder,
			HashMap<String, String> conditionalVarValues,
			HashMap<String, String> uncoditionalVarValues) {

		float normalizingFactor = 0;
		for (Entry<String, Float> e : finalFactorHolder.getFactorTable()
				.entrySet()) {
			String[] tmpAr = e.getKey().split("_");
			boolean f = true;
			for (String s : tmpAr) {
				String[] tmpAr2 = s.split(":");

				if (conditionalVarValues.containsKey(tmpAr2[0])
						&& !tmpAr2[1].equals(conditionalVarValues
								.get(tmpAr2[0]))) {
					f = false;
				}
			}
			if (f) {
				normalizingFactor += e.getValue();
			}

		}
		return normalizingFactor;
	}

	public static void printFactor(FactorHolder factor) {
		System.out.println("-------------- Factor " + factor.getId()
				+ " -----------------");
		for (Entry<String, Float> e : factor.getFactorTable().entrySet()) {
			System.out.println(e.getKey() + " | " + e.getValue());

		}
		System.out
				.println("-----------------------------------------------------------");
	}

	public static void checkProb(float f, String identifier) {
		if (f > 1.05) {
			System.err.println();
			System.err.println(identifier + " :" + f);
			System.exit(0);
		}
	}

	/**
	 * Recursively finds all possible combinations: unknown length of variables
	 * as well as unknown number of descrete values for each variable.
	 * 
	 * @param newFactorVariablesValuesList
	 * @param newFactorVariables
	 * @return
	 */
	public static String[] findCombinations(
			LinkedList<String[]> newFactorVariablesValuesList,
			LinkedList<String> newFactorVariables) {

		String[] result = null;
		if (newFactorVariablesValuesList.size() == 1) {
			result = new String[newFactorVariablesValuesList.get(0).length];
			String curVar = newFactorVariables.getFirst();
			for (int i = 0; i < result.length; i++) {
				result[i] = curVar + ":"
						+ newFactorVariablesValuesList.get(0)[i];
			}

			return result;
		}

		String[] curValues = newFactorVariablesValuesList.removeFirst();
		String curVar = newFactorVariables.removeFirst();
		String[] tmpResult = findCombinations(newFactorVariablesValuesList,
				newFactorVariables);
		result = new String[curValues.length * tmpResult.length];
		int i = 0;
		for (String curValue : curValues) {
			for (String tmpValue : tmpResult) {
				result[i] = curVar + ":" + curValue + "_" + tmpValue;
				i++;
			}
		}

		return result;

	}

	public static MutualInformation loadCounts() {

		HashMap<String, CounterMap<String, String>> mapOfCounterMaps = new HashMap<String, CounterMap<String, String>>();

		HashMap<String, Counter<String>> objectCounts = new HashMap<String, Counter<String>>();
		MutualInformation mi = new MutualInformation(mapOfCounterMaps,
				objectCounts);
		HashMap<Integer, String> objectNames = readNames();
		try {
			BufferedReader input = new BufferedReader(new FileReader(
					PropertiesFactory.getInstance().getProps()
							.getProperty("path")
							+ "chowliu-input.txt"));
			String line;
			// int instanceNumber = 0; //line in the matrix
			int size = 0;
			while ((line = input.readLine()) != null) {
				String[] tmpAr = line.split("\\s+");
				int objectNumber = 0;
				for (int i = 0; i < tmpAr.length - 1; i++) {
					int iValue = Integer.parseInt(tmpAr[i].trim());
					String iName = objectNames.get(i);
					Counter<String> c = objectCounts.get("" + iValue);
					if (c == null) {
						c = new Counter<String>();
						objectCounts.put("" + iValue, c);
					}
					c.incrementCount(iName, 1);
					for (int j = i + 1; j < tmpAr.length; j++) {
						int jValue = Integer.parseInt(tmpAr[j].trim());
						String jName = objectNames.get(j);
						CounterMap<String, String> curCounterMap = mapOfCounterMaps
								.get(iValue + "_" + jValue);
						if (curCounterMap == null) {
							curCounterMap = new CounterMap<String, String>();
							mapOfCounterMaps.put(iValue + "_" + jValue,
									curCounterMap);
						}
						curCounterMap.incrementCount(iName, jName, 1.0);
						// System.out.println("CounterMap:"+iValue+"_"+jValue);
						// System.out.println("Value of :"+iName+","+jName+":"+curCounterMap.getCount(iName,
						// jName));

					}
				}
				/**
				 * For the last object
				 */
				int iValue = Integer.parseInt(tmpAr[tmpAr.length - 1].trim());
				String iName = objectNames.get(tmpAr.length - 1);
				Counter<String> c = objectCounts.get("" + iValue);
				if (c == null) {
					c = new Counter<String>();
					objectCounts.put("" + iValue, c);
				}
				c.incrementCount(iName, 1);

				size++;
				// instanceNumber++;
			}
			input.close();

			for (Entry<String, CounterMap<String, String>> eOut : mapOfCounterMaps
					.entrySet()) {

				for (String key : eOut.getValue().keySet()) {
					Counter<String> c = eOut.getValue().getCounter(key);

					for (Entry<String, Double> e : c.getEntrySet()) {
						e.setValue(e.getValue() / size);
					}
				}

			}
			for (Entry<String, Counter<String>> eOut : objectCounts.entrySet()) {
				System.out.println(eOut.getKey() + " size:"
						+ eOut.getValue().size());
				for (Entry<String, Double> e : eOut.getValue().getEntrySet()) {
					e.setValue(e.getValue() / size);
				}
			}
		} catch (IOException e) {
		}

		return mi;

	}

	public static HashMap<Integer, String> readNames() {
		HashMap<Integer, String> names = new HashMap<Integer, String>();
		try {
			BufferedReader input = new BufferedReader(new FileReader(
					PropertiesFactory.getInstance().getProps()
							.getProperty("path")
							+ "names.txt"));
			String line;
			int i = 0;
			while ((line = input.readLine()) != null) {
				names.put(i, line.trim());
		//		HW42.nameToIndexMap.put(line.trim(), "" + i);
				i++;

			}
			input.close();
		} catch (IOException e) {

		}
		return names;
	}

	public static CliqueTree findMaximumTree(MutualInformation mi) {

		CliqueTree tree = new CliqueTree();

		TreeMap<String, Double> tm = new TreeMap<String, Double>();
		for (String key : mi.getInformation().keySet()) {
			Counter<String> c = mi.getInformation().getCounter(key);
			for (Entry<String, Double> e : c.getEntrySet()) {

				if (tm.containsKey(key + "_" + e.getKey()))
					System.err.println("Problem:" + e.getValue());
				tm.put(key + "_" + e.getKey(), e.getValue());

			}
		}

		DoubleValueComparatorDecreasing vc = new DoubleValueComparatorDecreasing(
				tm);

		TreeMap<String, Double> sortedGraph = new TreeMap<String, Double>(vc);
		sortedGraph.putAll(tm);

		while (sortedGraph.size() > 0) {
			Entry<String, Double> e = sortedGraph.pollFirstEntry();
			String edge = e.getKey();

			if (!tree.getEdges().contains(edge))
				tree.getEdges().add(edge);
			// System.out.println("Edges size now:"+tree.getEdges().size());
			String nodes[] = edge.split("_");
			boolean nodesIn[] = new boolean[2];
			for (int i = 0; i < 2; i++) {
				if (tree.getNodes().contains(nodes[i]))
					nodesIn[i] = true;
				else {
					tree.getNodes().add(nodes[i]);
					nodesIn[i] = false;
				}
			}

			if (tree.containsCycle()) {
				for (int i = 0; i < 2; i++) {
					if (!nodesIn[i]) {
						tree.getNodes().remove(nodes[i]);
					}
				}

				tree.getEdges().remove(edge);

			}

		}

		tree.setNodePotentials(mi.getHad_p_xi());
		tree.setPairPotentials(mi.getHat_p_xi_xj());
		return tree;
	}

	public static void printGraphvizTree(CliqueTree tree) {
		PrintToFile pf = new PrintToFile();
		pf.openFile(PropertiesFactory.getInstance().getProps()
				.getProperty("path")
				+ "tree.dot");
		pf.writeToFile("digraph unix { ");
		pf.writeToFile("node [color=lightblue2, style=filled];");
		for (String edge : tree.getEdges()) {
			String[] nodes = edge.split("_");
			// n0->n0[dir=none]
			pf.writeToFile(nodes[0] + "->" + nodes[1] + "[dir=none];");

		}
		pf.writeToFile("}");

	}

	public static void printUAIFormat(CliqueTree tree) {

		int nodesSize = tree.getNodes().size();
		PrintToFile pf = new PrintToFile();
		pf.openFile(PropertiesFactory.getInstance().getProps()
				.getProperty("path")
				+ "hw42a.uai");

		pf.writeToFile("MARKOV");

		pf.writeToFile("" + nodesSize);
		String cardinalityStr = "";
		for (int i = 0; i < nodesSize; i++)
			cardinalityStr += "2 ";
		pf.writeToFile(cardinalityStr);
		// Number of cliques equals the number of edges in our case??

		pf.writeToFile("" + (tree.getEdges().size() + tree.getNodes().size()));
		for (String node : tree.getNodes()) {
			pf.writeToFile("1 " + HW42.nameToIndexMap.get(node));

		}
		for (String edge : tree.getEdges()) {
			pf.writeToFile("2 " + HW42.nameToIndexMap.get(edge.split("_")[0])
					+ " " + HW42.nameToIndexMap.get(edge.split("_")[1]));
		}
		for (int i = 0; i < nodesSize; i++) {
			String curNode = HW42.indexToNameMap.get(i);
			pf.writeToFile("2");

			double pot1 = tree.getNodePotentials().get("1").getCount(curNode);
			pf.writeToFile(" " + (1 - pot1) + " " + pot1);
		}

		for (String edge : tree.getEdges()) {
			pf.writeToFile("4 ");
			String[] tmpAr = edge.split("_");

			double iZero = tree.getNodePotentials().get("0").getCount(tmpAr[0]);
			double iOne = tree.getNodePotentials().get("1").getCount(tmpAr[0]);
			double jZero = tree.getNodePotentials().get("0").getCount(tmpAr[1]);
			double jOne = tree.getNodePotentials().get("1").getCount(tmpAr[1]);

			pf.writeToFile(" "
					+ tree.getPairPotentials().get("0_0")
							.getCount(tmpAr[0], tmpAr[1])
					/ (iZero * jZero)
					+ " "
					+ tree.getPairPotentials().get("0_1")
							.getCount(tmpAr[0], tmpAr[1]) / (iZero * jOne));

			pf.writeToFile(" "
					+ tree.getPairPotentials().get("1_0")
							.getCount(tmpAr[0], tmpAr[1])
					/ (iOne * jZero)
					+ " "
					+ tree.getPairPotentials().get("1_1")
							.getCount(tmpAr[0], tmpAr[1]) / (iOne * jOne));

		}

	}

	/**
	 * SOS: Log potentials!!!!!
	 * 
	 * @param file
	 * @param logtransform
	 * @return
	 */
	public static PairwiseCRF readUAI(String file, boolean logtransform) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(
					PropertiesFactory.getInstance().getProps()
							.getProperty("path")
							+ file + ".uai"));
			String line;
			line = input.readLine();
			HashMap<String, Integer> cliqueToAbsoluteNumber = new HashMap<String, Integer>();
			HashMap<Integer, String> absoluteNumberToCliqueStr = new HashMap<Integer, String>();
			int variables = Integer.parseInt(input.readLine().trim());
			HashMap<Integer, Integer> nodeCardinalities = new HashMap<Integer, Integer>();
			line = input.readLine();
			String[] tmpa = line.split("\\s+");
			for (int i = 0; i < tmpa.length; i++) {
				nodeCardinalities.put(i, Integer.parseInt(tmpa[i].trim()));
			}
			int noOfCliques = Integer.parseInt(input.readLine().trim());
			LinkedList<Integer>[] cliques = new LinkedList[noOfCliques];
			for (int j = 0; j < noOfCliques; j++) {
				line = input.readLine();
				String[] tmpb = line.split("\\s+");
				LinkedList<Integer> l = new LinkedList<Integer>();
				String key = "";
				for (int k = 1; k < tmpb.length; k++) {
					int varNo = Integer.parseInt(tmpb[k].trim());
					l.add(varNo);
					key += varNo + "_";

				}
				key = key.substring(0, key.length() - 1);
				cliqueToAbsoluteNumber.put(key, j);
				absoluteNumberToCliqueStr.put(j, key);
				cliques[j] = l;
				// cliques.put(Integer.parseInt(tmpb[0].trim()),l);

			}
			
			System.out.println("Cardinalities 1, last:"+nodeCardinalities.get(0)+" "+nodeCardinalities.get(270));
			int i = 0;
			HashMap<Integer, Counter<Integer>> singleFactors = new HashMap<Integer, Counter<Integer>>();
			HashMap<Integer, CounterMap<Integer, Integer>> pairFactors = new HashMap<Integer, CounterMap<Integer, Integer>>();
			PairwiseCRF crf = new PairwiseCRF(variables, noOfCliques, cliques,
					singleFactors, pairFactors, cliqueToAbsoluteNumber,
					absoluteNumberToCliqueStr);
			while ((line = input.readLine()) != null) {
				if (line.length() > 0) {
					if (cliques[i].size() == 1) { // single factor (node)

						Counter<Integer> c = new Counter<Integer>();
						line = input.readLine();
						System.out.println("\nline1:"+line);
						String[] tmpc = line.trim().split("\\s+");
						for (int k = 0; k < nodeCardinalities.get(cliques[i]
								.getFirst()); k++) {
						
							double potential = Double.parseDouble(tmpc[k]);
							if (logtransform) {
								if (potential == 0)
									potential = 0.000000000000001;
								potential = Math.log(potential);
							}
							c.incrementCount(k, potential);
							System.out.print(k+ ":"+potential+ "|");
						}
						int nodeNumber = Integer
								.parseInt(absoluteNumberToCliqueStr.get(i));
						System.out.println("\nNode:"+nodeNumber);
						
						singleFactors.put(nodeNumber, c);

					} else {// line contains 4
						line = input.readLine();
						//System.out.println("\nline1:"+line);
						String[] tmpc = line.trim().split("\\s+");
						line = input.readLine();
						//System.out.println("line2:"+line);
						tmpc = conctenateArrays(tmpc, line.trim().split("\\s+"));
						CounterMap<Integer, Integer> cm = new CounterMap<Integer, Integer>();
						int m=0;
						for (int k = 0; k < nodeCardinalities.get(cliques[i]
								.getFirst()); k++) {
							//System.out.println();
							for (int l = 0; l < nodeCardinalities
									.get(cliques[i].getLast()); l++) {
								double potential = Double.parseDouble(tmpc[m]);
								m++;
								if (logtransform) {
									if (potential == 0)
										potential = 0.000000000000001;
									potential = Math.log(potential);
								}
								cm.incrementCount(k, l, potential); // 00, 01,
																	// 10, 11
								//System.out.print(k+","+ l+ ":"+potential+ "|");

							}
						}

						pairFactors.put(i, cm);

					}
					i++;
				}
			}
			input.close();
			return crf;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static String[] conctenateArrays(String[]... arrays) {
		ArrayList<String> l = new ArrayList<String>();
		for (String[] ar : arrays) {
			for (String str : ar) {
				l.add(str);
			}
		}
		return l.toArray(new String[l.size()]);
	}

	public static String getStringFromDouble(double score) {
		DecimalFormat myFormatter = new DecimalFormat("#.####");
		return myFormatter.format(score);
	}

}
