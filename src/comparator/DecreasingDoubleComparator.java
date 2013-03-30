package comparator;

import java.util.Comparator;

public class DecreasingDoubleComparator implements Comparator<Double> {

	@Override
	public int compare(Double arg0, Double arg1) {
		return -arg0.compareTo(arg1);
	}

}
