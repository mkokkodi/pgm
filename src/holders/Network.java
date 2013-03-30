package holders;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.media.jai.operator.AddDescriptor;


public class Network {

	/**
	 * @param args
	 */
	/**
	 * variable (including givens) -> values -> prob
	 */
	private HashMap<String, FactorHolder> probabilities;

	/**
	 * variable -> values
	 */
	private HashMap<String, LinkedList<String>> vars;
	private HashSet<String> undirectedEdges; // firstNode_secondNode
	private HashSet<String> directedEdges; // firstNode>secondNode
	
	private HashMap<String,HashSet<String>> varScope;//variable -> keysForVariableScopes: i.e. var = A-> A_B, G_A etc.. (where ever A appears)
	/*
	 * node -> list of dependend nodes.
	 */
	private HashMap<String, LinkedList<String>> undirectedDependencies;
	private HashMap<String, LinkedList<String>> directedDependencies;

	public Network(HashMap<String, FactorHolder> probs,
			HashMap<String, LinkedList<String>> vars, HashSet<String> edges) {

		this.probabilities = probs;
		this.vars = vars;
		this.directedEdges = edges;
	}

	public HashMap<String, FactorHolder> getProbabilities() {
		return probabilities;
	}

	public HashMap<String, LinkedList<String>> getVars() {
		return vars;
	}

	public HashSet<String> getDirectedEdges() {
		return directedEdges;
	}

	public HashSet<String> getUndirectedEdges() {
		if (undirectedEdges == null) {
			moralizeGraph();
		}
		return undirectedEdges;
	}

	private void moralizeGraph() {

		undirectedEdges = new HashSet<String>();
		HashMap<String, HashSet<String>> coParents = new HashMap<String, HashSet<String>>();
		for (String edge : directedEdges) {
			String newEdge = edge.replace(">", "_");
			String newEdge2 = newEdge.split("_")[1]+"_"+newEdge.split("_")[0];
			if(!undirectedEdges.contains(newEdge) && !undirectedEdges.contains(newEdge2)){
			undirectedEdges.add(newEdge);
			String[] tmpAr = edge.split(">");
			HashSet<String> parents = coParents.get(tmpAr[1]);
			if (parents == null) {
				parents = new HashSet<String>();
				coParents.put(tmpAr[1], parents);
			}
			parents.add(tmpAr[0]);
			}
		}
		for (Entry<String, HashSet<String>> e : coParents.entrySet()) {
			if (e.getValue().size() > 1) {
				String[] nodesArr = new String[e.getValue().size()];
				int i = 0;
				for (String node : e.getValue()) {
					nodesArr[i] = node;
					i++;
				}
				for (int j = 0; j < nodesArr.length; j++) {
					for (int k = j + 1; k < nodesArr.length; k++) {
						if (!undirectedEdges.contains(nodesArr[j] + "_"
								+ nodesArr[k])
								&& !undirectedEdges.contains(nodesArr[k] + "_"
										+ nodesArr[j])) {
							undirectedEdges
									.add(nodesArr[j] + "_" + nodesArr[k]);

						}
					}
				}
			}
		}
	}

	public HashMap<String, LinkedList<String>> getUndirectedDependencies() {
		if (undirectedDependencies == null) {
			undirectedDependencies = new HashMap<String, LinkedList<String>>();
			for (String edge : getUndirectedEdges()) {
				String[] nodes = edge.split("_");
				addDependency(nodes[0], nodes[1], undirectedDependencies);
				addDependency(nodes[1], nodes[0], undirectedDependencies);

			}
		}
		return undirectedDependencies;
	}

	public HashMap<String, LinkedList<String>> getDirectedDependencies() {
		if (directedDependencies == null) {
			directedDependencies = new HashMap<String, LinkedList<String>>();
			for (String edge : getDirectedEdges()) {
				String[] nodes = edge.split(">");
				//parent > child , P(child | Pa)
				addDependency(nodes[1], nodes[0], directedDependencies);

			}
		}
		return directedDependencies;
	}

	private void addDependency(String node1, String node2,
			HashMap<String, LinkedList<String>> dependencies) {
		LinkedList<String> l = dependencies.get(node1);
		if (l == null) {
			l = new LinkedList<String>();
			dependencies.put(node1, l);
		}
		l.add(node2);

	}

	public HashMap<String, HashSet<String>> getVarScope() {
		if(varScope==null){
			varScope = new HashMap<String, HashSet<String>>();
			for(String mapKey:getProbabilities().keySet()){
				String [] tmpAr = mapKey.split("_");
				for(String node:tmpAr){
					HashSet<String> curScope = varScope.get(node);
					if(curScope==null){
						curScope = new HashSet<String>();
						if(getProbabilities().keySet().contains(node))
							curScope.add(node); //The node is also in its scope.
						varScope.put(node,curScope);
					}
					curScope.add(mapKey);
				}
			}
		}
		return varScope;
	}
	

}
