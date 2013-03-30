package hw5;

import java.util.Map.Entry;

import dual.MPLP;
import holders.PairwiseCRF;
import utils.Counter;
import utils.CounterMap;
import utils.Utils;

public class Hw5 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PairwiseCRF pairCRF = Utils.readUAI("2dri",false); // reads log potentials.
		System.out.println("Single factors:"+pairCRF.getSingleFactors().size());
		
		System.out.println("Cliqueues:"+pairCRF.getCliques().length);
		//System.exit(0);
		MPLP.runMLP(pairCRF);
		
	}

}
