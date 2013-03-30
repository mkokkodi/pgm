package exactinference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import utils.Counter;
import utils.CounterMap;

import holders.CliqueTree;
import holders.Network;
import holders.PairwiseCRF;
import hw4.HW42;

public class MaxProductBP {
	private static PairwiseCRF crf;
	private static HashMap<String, double[]> deltas;
	private static HashMap<Integer, double[]> betas;
	private static HashMap<String, HashSet<String>> dependencies;
	private static HashMap<String, HashSet<String>> neighbors;

	// Rule to pick a node for transmitting a message:
	// "C_i is ready to transmit to aneighbor C_j when C_i has messages from all of its neighbors exceptfrom C_j".

	public static void runAlgorithm(PairwiseCRF crf2) {
		crf = crf2;
		deltas = new HashMap<String, double[]>();
		betas = new HashMap<Integer, double[]>(); // normalized probabilities
		estimateDependencies();
		LinkedList<String> readyNodes = new LinkedList<String>();
		HashSet<String> alreadyAddedNodes = new HashSet<String>();

		for (String key : neighbors.keySet()) {
			if (neighbors.get(key).size() == 1) { // if only one neighbor then
													// we have a leaf;
				readyNodes.add(key);
				alreadyAddedNodes.add(key);

			}

		}
		System.out.println("Ready nodes:" + readyNodes.size());

		int n = 0;
		LinkedList<String> inverseOrder = new LinkedList<String>(); // will be
																	// used for
																	// the
																	// second
																	// pass,
																	// from root
																	// to the
																	// leafs!.
		while (!(readyNodes.size() == 1)) { // when readyNodes = 1, only root
											// has left in the list.

			String node_i = readyNodes.getFirst();
			inverseOrder.addFirst(node_i);
			HashSet<String> hsi = dependencies.get(node_i);
			if (hsi == null) {
				System.err.println("Null hsi.");
				System.exit(0);
			}
			for (String node_j : hsi) {
				computeDelta_i_to_j(node_i, node_j);
				HashSet<String> hsj = dependencies.get(node_j);
				hsj.remove(node_i);

				if (!alreadyAddedNodes.contains(node_j)) {
					if (hsj.size() == 1) {
						n++;
						alreadyAddedNodes.add(node_j);
						System.out.println("Addnig ready node:" + node_j);

						readyNodes.add(node_j);
					}

				}
			}

			readyNodes.remove(node_i);
			// break;

		}
		System.out.println("Added nodes:" + n);
		System.out.println("My root is:" + readyNodes.getFirst());
		System.out.println("From root to the leaf now:");
		inverseOrder.addFirst(readyNodes.getFirst());
		estimateDependencies();
		while (!inverseOrder.isEmpty()) {

			String node_i = inverseOrder.getFirst();
			HashSet<String> hsi = dependencies.get(node_i);

			for (String node_j : hsi) {
				computeDelta_i_to_j(node_i, node_j);
				HashSet<String> hsj = dependencies.get(node_j);
				hsj.remove(node_i);

			}
			inverseOrder.remove(node_i);
		}

		calculaterBetas();
		/*
		 * for (Entry<String, HashSet<String>> e : dependencies.entrySet()) {
		 * System.out.println("\n" + e.getKey() + ":"); for (String s :
		 * e.getValue()) { System.out.print(s + " "); } }
		 */

	}

	private static void calculaterBetas() {
		for (int i = 0; i < 111; i++) {
			String node_i = crf.getAbsoluteNumberToCliqueStr().get(i);
			HashSet<String> hs = neighbors.get(node_i);
			double[] bi = new double[2];
			bi[0] = crf.getSingleFactors().get(i).getCount(0);
			bi[1] = crf.getSingleFactors().get(i).getCount(1);
			// System.out.println("before:"+bi[0]+" "+bi[1]);

			for (String nbi : hs) {
				double[] d_k_i = deltas.get(nbi + "_" + node_i);

				// System.out.println(d_k_i[0]+" "+d_k_i[1]);
				for (int j = 0; j < bi.length; j++) {
					bi[j] *= d_k_i[j];
				}

			}
			// System.out.println("after:"+bi[0]+" "+bi[1]);

			betas.put(i, bi);
		}

		normalizeObjects();
		System.out.println("--- Betas ----");
		for (int i = 0; i < 111; i++) {
			if (betas.get(i)[1] >= 0.0)
				System.out.println(i + ":" + betas.get(i)[0] + " "
						+ betas.get(i)[1] + " " + HW42.indexToNameMap.get(i));

		}

	}

	private static void normalizeObjects() {
		double[][] denoms = new double[111][2];
		for (int i = 0; i < 111; i++) {
			double denom = betas.get(i)[0] + betas.get(i)[1];
			betas.get(i)[0] = betas.get(i)[0] / denom;
			betas.get(i)[1] = betas.get(i)[1] / denom;
		}

	}

	private static void computeDelta_i_to_j(String node_i, String node_j) {
		/*
		 * String [] tmpAr1 = node_i.split("_"); String key =""; for(String s:
		 * tmpAr1){ if(node_j.contains(s)){ key+= s; } }
		 */
		System.out.println("Computing i-j:" + node_i + " " + node_j);
		deltas.put(node_i + "_" + node_j, mpMessage(node_i, node_j));

	}

	// returns potential of 1.
	private static double[] mpMessage(String node_i, String node_j) {
		double[] phi_x_i = new double[2];
		double[] delta_x_i_to_x_j = new double[2];

		// Implementing david's notes:
		int key = crf.getCliqueToAbsoluteNumberOfVariable().get(node_i);

		Counter<Integer> c = crf.getSingleFactors().get(key);
		phi_x_i[0] = c.getCount(0);
		phi_x_i[1] = c.getCount(1);

		HashSet<String> hs = neighbors.get(node_i);
		double[] product_of_deltas = { 1.0, 1.0 };
		if (!hs.contains(node_j)) {
			System.err.println("wtf, does not contain node.");
			System.exit(0);
		}
		for (String k : hs) {
			if (!k.equals(node_j)) {

				double[] delta_k_i = deltas.get(k + "_" + node_i);
				if (delta_k_i == null) {
					System.err.println("Message:" + k + " -> " + node_i);

					System.exit(0);
				}

				product_of_deltas[0] *= delta_k_i[0];
				product_of_deltas[1] *= delta_k_i[1];

			}
		}

		String factor = node_i + "_" + node_j;
		double[] phi_x_i_x_j = new double[4];
		Integer factorKey = crf.getCliqueToAbsoluteNumberOfVariable().get(
				factor);
		if (factorKey != null) {
			CounterMap<Integer, Integer> cm = crf.getPairFactors().get(
					factorKey);

			phi_x_i_x_j[0] = cm.getCount(0, 0);
			phi_x_i_x_j[1] = cm.getCount(0, 1);
			phi_x_i_x_j[2] = cm.getCount(1, 0);
			phi_x_i_x_j[3] = cm.getCount(1, 1);
		} else {
			factor = node_j + "_" + node_i;
			factorKey = crf.getCliqueToAbsoluteNumberOfVariable().get(factor);
			CounterMap<Integer, Integer> cm = crf.getPairFactors().get(
					factorKey);

			phi_x_i_x_j[0] = cm.getCount(0, 0);
			phi_x_i_x_j[2] = cm.getCount(0, 1);
			phi_x_i_x_j[1] = cm.getCount(1, 0);
			phi_x_i_x_j[3] = cm.getCount(1, 1);

		}
		delta_x_i_to_x_j[0] = Math.max(product_of_deltas[0]
				* phi_x_i_x_j[0] * phi_x_i[0], product_of_deltas[0]
						* phi_x_i[0] *phi_x_i_x_j[1]);
		delta_x_i_to_x_j[1] = Math.max(product_of_deltas[1]
				* phi_x_i_x_j[2] * phi_x_i[1], product_of_deltas[1] * phi_x_i_x_j[3]* phi_x_i[1]) ;
		return delta_x_i_to_x_j;
	}

	public static void estimateDependencies() {
		dependencies = new HashMap<String, HashSet<String>>();
		neighbors = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < crf.getNoOfCliques(); i++) {
			LinkedList<Integer> l = crf.getCliques()[i];
			if (l.size() > 1) {
				addDependency("" + l.get(1), "" + l.get(0));
				addDependency("" + l.get(0), "" + l.get(1));
				addNeighbor("" + l.get(1), "" + l.get(0));
				addNeighbor("" + l.get(0), "" + l.get(1));

			}
		}
	}

	private static void addNeighbor(String node1, String node2) {
		HashSet<String> hs = neighbors.get(node1);
		if (hs == null) {
			hs = new HashSet<String>();
			neighbors.put(node1, hs);

		}
		if (!node2.equals(node1))
			hs.add(node2);

	}

	private static HashMap<String, HashSet<String>> createDependentsMap() {
		HashMap<String, HashSet<String>> hm = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < crf.getNoOfCliques(); i++) {
			LinkedList<Integer> l = crf.getCliques()[i];
			if (l.size() > 1) {
				String edge = l.get(0) + "_" + l.get(1);
				addNewNodeDependancy(hm, "" + l.get(0), edge);
				addNewNodeDependancy(hm, "" + l.get(1), edge);

			}

		}
		return hm;
	}

	private static void addNewNodeDependancy(
			HashMap<String, HashSet<String>> hm, String node, String edge) {
		HashSet<String> hs = hm.get(node);

		if (hs == null) {
			hs = new HashSet<String>();
			hm.put(node, hs);
		}
		hs.add(edge);

	}

	private static void addDependency(String node1, String node2) {
		HashSet<String> hs = dependencies.get(node1);
		if (hs == null) {
			hs = new HashSet<String>();
			dependencies.put(node1, hs);

		}
		if (!node2.equals(node1))
			hs.add(node2);

	}

}
