package utils;

import java.util.HashMap;
import java.util.LinkedList;

public class Table {


	private Variable dependentVar;
	private LinkedList<Variable> givenVars;
	
	HashMap<String,Float> probabilities = new HashMap<String, Float>();
	
	


	class Variable{
		private String name;
		private LinkedList<String> values;
		
		public Variable(String name,LinkedList<String> values)
		{
			this.name = name;
			this.values = values;
		}
		public LinkedList<String> getValues() {
			return values;
		}
		public String getName() {
			return name;
		}
		
		
	}
}
