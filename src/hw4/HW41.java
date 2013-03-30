package hw4;

import holders.FactorHolder;
import holders.Network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import utils.Utils;

import exactinference.VariableElimination;

public class HW41 {

	/**
	 * @param args
	 */
	private static Network network;
	public static boolean verbose = false;
	public static boolean printNewFactorScope = true;
	public static TreeMap<Integer,LinkedList<String>> inducedWidths;
	private static LinkedList<String> order;

	public static void main(String[] args) {

		
		if(printNewFactorScope)
			inducedWidths = new TreeMap<Integer, LinkedList<String>>();
		network = Utils.loadNetwork();

		// Utils.printUndirectedGraph(network);
		// Utils.printDirectedGraph(network);

		
		order = Utils.minFill(network);
		
		//printOrderForLatex();
		runQuery("StrokeVolume = High | Hypovolemia = True"
				+ ", ErrCauter = True, PVSat =Normal, Disconnect = True, MinVolSet = Low");
		network = Utils.loadNetwork();
		runQuery("HRBP = Normal | LVEDVolume = Normal, Anaphylaxis = False, Press = Zero,"
				+ "VentTube = Zero, BP = High");

		network = Utils.loadNetwork();
		runQuery("LVFailure = False | Hypovolemia = True, MinVolSet = Low, VentLung = Normal, "
				+ "BP = Normal");

		network = Utils.loadNetwork();
		runQuery("PVSAT = Normal, CVP = Normal | LVEDVolume = High, Anaphylaxis = False,"
				+ "Press = Zero");

		if(printNewFactorScope)
			printTree();
			
	}

	private static void printOrderForLatex() {
		int i=0;
		for(String node:order){
			if(i % 6 == 0)
				System.out.println("\\\\");
			System.out.print(node+" \\rightarrow " );
		i++;
	}
	
		
	}

	private static void printTree() {
	for(Entry<Integer,LinkedList<String>> e:inducedWidths.entrySet()){
		System.out.println(e.getKey());
		for(String node:e.getValue()){
			System.out.print(node+" ");
		}
		System.out.println("\n------------");
	}
		
	}

	private static void runQuery(String query) {

		System.out.println("\n --------------------------------------------- ");
		System.out.println("\nRunning Query: P(" + query + ")");

		// Utils.printNetwork(network);

		HashSet<String> resultVars = new HashSet<String>(); // not to be
															// eliminated
		HashMap<String, String> uncoditionalVarValues = new HashMap<String, String>();
		HashMap<String, String> conditionalVarValues = new HashMap<String, String>();

		if (query.length() > 1) {
			String[] tmpAr = query.split("\\|");
			Utils.insertToVarValuesMap(tmpAr[0], uncoditionalVarValues);
			if (tmpAr.length > 1) {
				Utils.insertToVarValuesMap(tmpAr[1], conditionalVarValues);

			}
		}
		resultVars.addAll(uncoditionalVarValues.keySet());
		resultVars.addAll(conditionalVarValues.keySet());

		System.out.println("\nVariables that we will not marginalize:\n"
				+ resultVars + " ");
		System.out.println("\n --------------------------------------------- ");

		VariableElimination.eliminateVars(resultVars, network, order);

		// Utils.printNetwork(network);

		/*
		 * Estimate Final Products.
		 */
		// Utils.printNetwork(network);
		if (resultVars.size() > 0) {
			FactorHolder finalFactorHolder = null;
			if (network.getProbabilities().size() > 1)
				finalFactorHolder = VariableElimination
						.estimateFinalFactor(network);
			else {
				for (FactorHolder fh : network.getProbabilities().values())
					finalFactorHolder = fh;
			}
			/*
			 * --------------------
			 */

			Utils.printResult(finalFactorHolder, conditionalVarValues,
					uncoditionalVarValues);
		}

	}

}
