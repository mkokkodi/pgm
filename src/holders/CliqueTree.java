package holders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import utils.Counter;
import utils.CounterMap;

public class CliqueTree {

	LinkedList<String> edges;
	LinkedList<String> nodes;
	HashMap<String,Counter<String>> nodePotentials;
	HashMap<String,CounterMap<String,String>> pairPotentials;
	
	
	
	
	
	public CliqueTree() {
		this.edges = new LinkedList<String>();
		this.nodes = new LinkedList<String>();
	
		
	}




	public boolean containsCycle(){
		if(edges.size()!=nodes.size()-1)
			return true;
		return false;
	}




	public HashMap<String,Counter<String>> getNodePotentials() {
		return nodePotentials;
	}




	public void setNodePotentials(HashMap<String,Counter<String>> nodePotentials) {
		this.nodePotentials = nodePotentials;
	}




	public HashMap<String,CounterMap<String, String>> getPairPotentials() {
		return pairPotentials;
	}




	public void setPairPotentials(HashMap<String,CounterMap<String, String>> pairPotentials) {
		this.pairPotentials = pairPotentials;
	}




	public LinkedList<String> getEdges() {
		return edges;
	}




	public LinkedList<String> getNodes() {
		return nodes;
	}
	
	
	
}
