package org.openscience.cdk.applications.taverna.weka.utilities;

import java.util.ArrayList;

import org.openscience.cdk.applications.taverna.CDKTavernaException;

public class Vector {

	public ArrayList<Double> vector = new ArrayList<Double>();
	
	public void addValue(double value) {
		vector.add(value);
	}
	
	public Vector subtraction(Vector b) throws CDKTavernaException {
		Vector result = new Vector();
		if(this.vector.size() != b.vector.size()) {
			throw new CDKTavernaException(this.getClass().getSimpleName(), "Size of vectors differ!");
		}
		for(int i = 0; i < vector.size(); i++) {
			result.vector.add(vector.get(i) - b.vector.get(i));
		}
		return result;
	}
	
	public double length() {
		double sqrSum = 0;
		for(double value : vector) {
			sqrSum += Math.pow(value, 2);
		}
		return Math.sqrt(sqrSum);
	}
	
	
}
