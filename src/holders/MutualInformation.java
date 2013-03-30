package holders;

import java.util.HashMap;
import java.util.Map.Entry;

import utils.Counter;
import utils.CounterMap;

public class MutualInformation {

	/**
	 * Key: iValue+"_"+jValue
	 * Total of four counterMaps in binary variables.
	 */
	private HashMap<String,CounterMap<String, String>> hat_p_xi_xj;
	private CounterMap<String, String> information;

	private HashMap<String,Counter<String>> had_p_xi;

	public MutualInformation(HashMap<String,CounterMap<String, String>> hat_p_xi_xj,
			HashMap<String,Counter<String>> had_p_xi) {

		this.hat_p_xi_xj = hat_p_xi_xj;
		this.had_p_xi = had_p_xi;
	}

	public CounterMap<String, String> getInformation() {
		if (information == null) {
			information = new CounterMap<String, String>();
			for(Entry<String,CounterMap<String, String>> eOut:hat_p_xi_xj.entrySet()){
				String iValue = eOut.getKey().split("_")[0];
				String jValue = eOut.getKey().split("_")[1];
				System.out.println("currentMap:"+eOut.getKey()+" iValue size:"+had_p_xi.get(iValue).size());
			for (String key : eOut.getValue().keySet()) {
				Counter<String> c = eOut.getValue().getCounter(key);
				for (Entry<String, Double> e : c.getEntrySet()) {
					double fraction = e.getValue()
							/ (had_p_xi.get(iValue).getCount(key) * had_p_xi.get(jValue).getCount(e
									.getKey()));
					
			//		System.out.println("p(xi,xj)="+e.getValue()+" p(x_i) = "+had_p_xi.get(iValue).getCount(key)+ 
				//	" p(x_j)="+ had_p_xi.get(jValue).getCount(e
					//		.getKey()));
					
					Double curProduct = e.getValue() * Math.log(fraction);
					if(curProduct.isInfinite())
						curProduct = 0.0;
					information.incrementCount(key, e.getKey(), curProduct);
					
				}
			}
			}
		}
		return information;

	}

	public HashMap<String,CounterMap<String, String>> getHat_p_xi_xj() {
		return hat_p_xi_xj;
	}

	public HashMap<String,Counter<String>> getHad_p_xi() {
		return had_p_xi;
	}

	
	
}
