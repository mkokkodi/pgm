package holders;

import java.util.HashMap;

public class FactorHolder {

	/**
	 * @param args
	 */
	private HashMap<String,Float> factorTable;
	private String id;//variables of factor connected with "_"
	
	
	public FactorHolder(HashMap<String,Float> factorTable, String id){
		this.factorTable = factorTable;
		this.id = id;
		
	}



	public HashMap<String, Float> getFactorTable() {
		return factorTable;
	}


	public String getId() {
		return id;
	}
	
	
}
