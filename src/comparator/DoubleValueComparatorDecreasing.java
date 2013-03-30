package comparator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class DoubleValueComparatorDecreasing<K> implements Comparator<K> {



	Map<K, Double> base;

	public DoubleValueComparatorDecreasing(Map<K, Double> base) {
		this.base = base;
	}



	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(K a, K b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
					//(Math.random()>0.5)?1:-1;
		} else {
			return 1;//(Math.random()>0.5)?1:-1;
		} // returning 0 would merge keys
	}

	
}


