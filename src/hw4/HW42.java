package hw4;

import holders.CliqueTree;
import holders.MutualInformation;
import holders.PairwiseCRF;

import java.util.HashMap;
import java.util.Map.Entry;

import exactinference.MaxProductBP;
import exactinference.SumProductBP;

import utils.Counter;
import utils.CounterMap;
import utils.Utils;

public class HW42 {

	/**
	 * @param args
	 */
	private static MutualInformation mutualInformation;
	public static HashMap<String, String> nameToIndexMap;
	public static HashMap<Integer, String> indexToNameMap;

	public static void main(String[] args) {
		nameToIndexMap = new HashMap<String, String>();
		indexToNameMap = Utils.readNames();
		a();
	//	b();

	}

	private static void a() {
		
		mutualInformation = Utils.loadCounts();
	
		CliqueTree tree = Utils.findMaximumTree(mutualInformation);
		System.out.println("Nodes size:" + tree.getNodes().size());
		System.out.println("edges size:" + tree.getEdges().size());

		Utils.printGraphvizTree(tree);
		Utils.printUAIFormat(tree);

	}

	private static void b() {
		PairwiseCRF kitchenCRF = Utils.readUAI("kitchen");
		kitchenCRF.printCRFStats();
		SumProductBP.runAlgorithm(kitchenCRF);
		MaxProductBP.runAlgorithm(kitchenCRF);
		// / PairwiseCRF officeCRF = Utils.readUAI("office");
		// officeCRF.printCRFStats();

	}

}