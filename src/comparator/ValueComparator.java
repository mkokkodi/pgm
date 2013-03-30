package comparator;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator  implements Comparator<String> {

	Map<String, Integer> base;

	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return 1;
					//(Math.random()>0.5)?1:-1;
		} else {
			return -1;//(Math.random()>0.5)?1:-1;
		} // returning 0 would merge keys
	}
}


