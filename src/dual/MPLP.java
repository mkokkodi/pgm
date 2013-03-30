package dual;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;

import comparator.DecreasingDoubleComparator;
import comparator.DoubleValueComparatorDecreasing;

import utils.CounterMap;
import utils.Counter;
import utils.Utils;
import holders.PairwiseCRF;

public class MPLP {

	// node i, node j, state of node x_i, value of state.
	private static HashMap<Integer, HashMap<Integer, Counter<Integer>>> cur_delta_j_i_xi;
	private static HashMap<Integer, HashMap<Integer, Counter<Integer>>> delta_i_to_minus_j_xi;
	private static PairwiseCRF curCrf;
	private static final double e = 0.0002;
	private static double diff = Double.POSITIVE_INFINITY;
	private static double prevL = Double.POSITIVE_INFINITY;
	private static double curL = 0;
	private static TreeMap<Integer, CounterMap<Integer, Integer>> tm;
	private static double theta_x = 0;

	// node, state, value
	private static TreeMap<Integer,  Integer> assignments;

	private static int iterations = 0;

	public static void runMLP(PairwiseCRF crf) {

		
		assignments = new TreeMap<Integer,  Integer>();
		curCrf = crf;
		// no need to initialize deltas -> by default zero.
		cur_delta_j_i_xi = new HashMap<Integer, HashMap<Integer, Counter<Integer>>>();
		delta_i_to_minus_j_xi = new HashMap<Integer, HashMap<Integer, Counter<Integer>>>();

		tm = new TreeMap<Integer, CounterMap<Integer, Integer>>(
				curCrf.getPairFactors()); // sequentially

		System.out.println(" Iter.\t & Objective \t & Theta \t & Gap \\\\ \\hline ");
		System.out.println("---------------------------------------------------");
		while (diff > e) {
			iterations++;
			theta_x = 0;
			

			for (Entry<Integer, CounterMap<Integer, Integer>> e : tm.entrySet()) {
				String[] tmpAr = curCrf.getAbsoluteNumberToCliqueStr()
						.get(e.getKey()).split("_");
				int i = Integer.parseInt(tmpAr[0].trim());
				int j = Integer.parseInt(tmpAr[1].trim());

				HashMap<Integer, Counter<Integer>> current_iMap = getCurrentMap(
						i, cur_delta_j_i_xi);
				HashMap<Integer, Counter<Integer>> current_jMap = getCurrentMap(
						j, cur_delta_j_i_xi);

				computeDelta_i_to_minus_j(i, j, current_iMap);
				computeDelta_i_to_minus_j(j, i, current_jMap);

				computeDelta_j_to_i_xi(i, j);
				computeDelta_j_to_i_xi(j, i);

			}
			/*
			 * for (Entry<Integer, HashMap<Integer, HashMap<Integer,
			 * Counter<Integer>>>> e : cur_delta_j_i_xi .entrySet()) { for
			 * (Entry<Integer, HashMap<Integer, Counter<Integer>>> e1 : e
			 * .getValue().entrySet()) { for (Entry<Integer, Counter<Integer>>
			 * e2 : e1.getValue() .entrySet()) { System.out.println(e.getKey() +
			 * " " + e1.getKey() + " " + e2.getKey()); } } }
			 */
			calculateDiff();
			prevL = curL;

		}
		System.out.println("---------------------------------------------------");
		System.out.println("Total iterations:" + iterations);
		System.out.println("---------------------------------------------------");
		HashMap<Integer, String> names = Utils.readNames();
		for(int i=0; i<111; i++){
			int ass = assignments.get(i);
			if(ass!=0){
				//System.out.print(names.get(i)+" ");
				System.out.print(i+" "+ass+",");
			}
		}
	}

	private static HashMap<Integer, Counter<Integer>> getCurrentMap(
			int i,
			HashMap<Integer, HashMap<Integer, Counter<Integer>>> cur_delta_j_i_xi2) {
		HashMap<Integer, Counter<Integer>> res = cur_delta_j_i_xi2.get(i);
		return (res != null) ? res : (new HashMap<Integer, Counter<Integer>>());
	}

	private static void computeDelta_j_to_i_xi(int i, int j) {

		Counter<Integer> delta_j_i = getCurrentCounter(i, j, cur_delta_j_i_xi);
		Counter<Integer> deltaToTheMinusCounter = getCurrentCounter(i, j,
				delta_i_to_minus_j_xi);
		Counter<Integer> maxCounter = esimateMaxOfPairs(i, j,
				getCurrentCounter(j, i, delta_i_to_minus_j_xi));
		for (Integer state : curCrf.getSingleFactors().get(i).keySet()) {
			double res = -(0.5 * deltaToTheMinusCounter.getCount(state))
					+ (0.5 * maxCounter.getCount(state));
			delta_j_i.setCount(state, res);
		}
	}

	private static Counter<Integer> esimateMaxOfPairs(int i, int j,
			Counter<Integer> counter) {
		String key = i + "_" + j;
		CounterMap<Integer, Integer> cm = null;

		Counter<Integer> jCounter = curCrf.getSingleFactors().get(j);
		Counter<Integer> iCounter = curCrf.getSingleFactors().get(i);

		Integer absoluteKey = curCrf.getCliqueToAbsoluteNumberOfVariable().get(
				key);

		HashMap<Integer, PriorityQueue<Double>> tmpMap = new HashMap<Integer, PriorityQueue<Double>>();
		if (absoluteKey == null) {
			key = j + "_" + i;
			absoluteKey = curCrf.getCliqueToAbsoluteNumberOfVariable().get(key);
			cm = curCrf.getPairFactors().get(absoluteKey);
			for (Integer ikey : iCounter.keySet()) {
				PriorityQueue<Double> iKeyQueue = tmpMap.get(ikey);
				if (iKeyQueue == null) {
					iKeyQueue = new PriorityQueue<Double>(11,
							new DecreasingDoubleComparator());
					tmpMap.put(ikey, iKeyQueue);
				}
				for (Integer jkey : jCounter.keySet()) {

					iKeyQueue.add(cm.getCount(jkey, ikey)
							+ counter.getCount(jkey));
				}
			}

		} else {
			cm = curCrf.getPairFactors().get(absoluteKey);
			for (Integer ikey : iCounter.keySet()) {
				PriorityQueue<Double> iKeyQueue = tmpMap.get(ikey);
				if (iKeyQueue == null) {
					iKeyQueue = new PriorityQueue<Double>(11,
							new DecreasingDoubleComparator());
					tmpMap.put(ikey, iKeyQueue);
				}

				for (Integer jkey : jCounter.keySet()) {

					iKeyQueue.add(cm.getCount(ikey, jkey)
							+ counter.getCount(jkey));
				}
			}
		}
		Counter<Integer> maxCounter = new Counter<Integer>();
		for (Entry<Integer, PriorityQueue<Double>> e : tmpMap.entrySet()) {
			maxCounter.incrementCount(e.getKey(), e.getValue().poll());
		}
		return maxCounter;
	}

	private static Counter<Integer> getCurrentCounter(int i, int j,
			HashMap<Integer, HashMap<Integer, Counter<Integer>>> delta_j_i_xi) {
		HashMap<Integer, Counter<Integer>> curMap = delta_j_i_xi.get(i);
		if (curMap == null) {
			curMap = new HashMap<Integer, Counter<Integer>>();
			delta_j_i_xi.put(i, curMap);
		}
		Counter<Integer> c = curMap.get(j);
		if (c == null) {
			c = new Counter<Integer>();
			curMap.put(j, c);
		}

		return c;
	}

	/**
	 * Bug free. (;)
	 * 
	 * @param i
	 * @param j
	 * @param current_iMap
	 */
	private static void computeDelta_i_to_minus_j(int i, int j,
			HashMap<Integer, Counter<Integer>> current_iMap) {

		Counter<Integer> sum = new Counter<Integer>();
		for (Entry<Integer, Counter<Integer>> e1 : current_iMap.entrySet()) {
			if (e1.getKey() != j) {

				for (Entry<Integer, Double> e2 : e1.getValue().getEntrySet()) {
					sum.incrementCount(e2.getKey(), e2.getValue());
				}

			}

		}

		Counter<Integer> c = getCurrentCounter(i, j, delta_i_to_minus_j_xi);
		//System.out.println("i:"+i);
		for (Integer key : curCrf.getSingleFactors().get(i).keySet()) {
			double res = sum.getCount(key)
					+ curCrf.getSingleFactors().get(i).getCount(key);
			c.setCount(key, res);
		}

	}

	private static void calculateDiff() {
		curL = estimateObjective();
		double gap = curL - theta_x;
		if(gap < e)
			gap=0;
		System.out.println(iterations +" \t& "+ Utils.getStringFromDouble(curL) 
				+ "\t& "+ Utils.getStringFromDouble(theta_x)+"\t& " + Utils.getStringFromDouble(gap)+ "\\\\");
		 diff =  prevL - curL ;
		

	}

	private static double estimateObjective() {

		/**
		 * \sum_{i \in V} \max_{x_i} (\theta_i (x_i) +\sum_{ij \in E}
		 * \delta_{j->i}(x_i))
		 */
		double firstTerm = 0;
		double secondTerm = 0;
		
		for (Entry<Integer, Counter<Integer>> e : curCrf.getSingleFactors()
				.entrySet()) {
			int i = e.getKey();
			Entry<Integer, Double> curEntry = estimateMax_xi(i,
					getCurrentMap(i, cur_delta_j_i_xi));
			firstTerm += curEntry.getValue();

			assignments.put(i, curEntry.getKey());
			theta_x +=e.getValue().getCount(curEntry.getKey());
		}

		for (Entry<Integer, CounterMap<Integer, Integer>> e : tm.entrySet()) {
			String[] tmpAr = curCrf.getAbsoluteNumberToCliqueStr()
					.get(e.getKey()).split("_");
			int i = Integer.parseInt(tmpAr[0].trim());
			int j = Integer.parseInt(tmpAr[1].trim());
			Entry<String,Double> curEntry = estimateMax_ij(i, j, e.getValue());
			secondTerm += curEntry.getValue();
			tmpAr = curEntry.getKey().split("_");
		//	int assignment_i = Integer.parseInt(tmpAr[0].trim());
		//	int assignment_j = Integer.parseInt(tmpAr[1].trim());
			theta_x += e.getValue().getCount(assignments.get(i), assignments.get(j));
			
		}

		// maximize -log l.
		return firstTerm + secondTerm;
	}

	private static Entry<String, Double> estimateMax_ij(int i, int j,
			CounterMap<Integer, Integer> cm) {
		HashMap<String, Double> resMap = new HashMap<String, Double>();
	
		for (Integer iKey : curCrf.getSingleFactors().get(i).keySet()) {
			for (Integer jKey : curCrf.getSingleFactors().get(j).keySet()) {
				String key = iKey+"_"+jKey;
				resMap.put(key,(cm.getCount(iKey, jKey)
						- cur_delta_j_i_xi.get(i).get(j).getCount(iKey)
						- cur_delta_j_i_xi.get(j).get(i).getCount(jKey)));
			}
		}
		
		DoubleValueComparatorDecreasing<String> vc = new DoubleValueComparatorDecreasing<String>(
				resMap);
		TreeMap<String, Double> tmp = new TreeMap<String, Double>(vc);
		tmp.putAll(resMap);
		return tmp.firstEntry();
		
		
	}

	private static Entry<Integer, Double> estimateMax_xi(Integer xi,
			HashMap<Integer, Counter<Integer>> curMap) {
		HashMap<Integer, Double> resMap = new HashMap<Integer, Double>();

		Counter<Integer> sum = new Counter<Integer>();

		for (Entry<Integer, Counter<Integer>> e2 : curMap.entrySet()) {
			for (Entry<Integer, Double> e3 : e2.getValue().getEntrySet()) {
				sum.incrementCount(e3.getKey(), e3.getValue());

			}
		}

		
		for (Entry<Integer, Double> e : curCrf.getSingleFactors().get(xi)
				.getEntrySet()) {
			resMap.put(e.getKey(), (e.getValue() + sum.getCount(e.getKey())));
		}
		DoubleValueComparatorDecreasing<Integer> vc = new DoubleValueComparatorDecreasing<Integer>(
				resMap);
		TreeMap<Integer, Double> tmp = new TreeMap<Integer, Double>(vc);
		tmp.putAll(resMap);

		return tmp.firstEntry();

	}
}
