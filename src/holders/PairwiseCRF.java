package holders;

import java.util.HashMap;
import java.util.LinkedList;

import utils.Counter;
import utils.CounterMap;

public class PairwiseCRF {

	private int variables;
	private int noOfCliques;
	private LinkedList<Integer>[] cliques;
	HashMap<Integer, Counter<Integer>> singleFactors;
	HashMap<Integer, CounterMap<Integer, Integer>> pairFactors;
	HashMap<String, Integer> cliqueToAbsoluteNumberOfVariable;
	HashMap<Integer, String> absoluteNumberToCliqueStr;

	public PairwiseCRF(int variables, int noOfCliques,
			LinkedList<Integer>[] cliques,
			HashMap<Integer, Counter<Integer>> singleFactors,
			HashMap<Integer, CounterMap<Integer, Integer>> pairFactors,
			HashMap<String, Integer> cliqueToAbsoluteNumberOfVariable,
			HashMap<Integer, String> absoluteNumberToCliqueStr) {
		this.variables = variables;
		this.noOfCliques = noOfCliques;
		this.cliques = cliques;
		this.singleFactors = singleFactors;
		this.pairFactors = pairFactors;
		this.cliqueToAbsoluteNumberOfVariable = cliqueToAbsoluteNumberOfVariable;
		this.absoluteNumberToCliqueStr = absoluteNumberToCliqueStr;
	}

	public int getVariables() {
		return variables;
	}

	public int getNoOfCliques() {
		return noOfCliques;
	}

	public LinkedList<Integer>[] getCliques() {
		return cliques;
	}

	public HashMap<Integer, Counter<Integer>> getSingleFactors() {
		return singleFactors;
	}

	public HashMap<Integer, CounterMap<Integer, Integer>> getPairFactors() {
		return pairFactors;
	}

	public void printCRFStats() {
		System.out.println("Vars:" + variables);
		System.out.println("Cliques:" + noOfCliques);
		System.out.println("Single factors:" + singleFactors.size());
		System.out.println("PaisFactors:" + pairFactors.size());
	}

	public HashMap<String, Integer> getCliqueToAbsoluteNumberOfVariable() {
		return cliqueToAbsoluteNumberOfVariable;
	}

	public HashMap<Integer, String> getAbsoluteNumberToCliqueStr() {
		return absoluteNumberToCliqueStr;
	}

}
