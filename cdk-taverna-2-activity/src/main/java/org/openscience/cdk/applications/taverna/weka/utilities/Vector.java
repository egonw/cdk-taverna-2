/*
 * Copyright (C) 2010 - 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.openscience.cdk.applications.taverna.weka.utilities;

import java.util.ArrayList;

import org.openscience.cdk.applications.taverna.CDKTavernaException;

/**
 * Vector for double values.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class Vector {

	public ArrayList<Double> vector = new ArrayList<Double>();

	/**
	 * @param value
	 *            Value to add to the vector.
	 */
	public void addValue(double value) {
		vector.add(value);
	}

	/**
	 * Subtracts another vector from this vector.
	 * 
	 * @param The
	 *            vector subtract from this vector.
	 * @return The resulting vector.
	 * @throws CDKTavernaException
	 */
	public Vector subtraction(Vector b) throws CDKTavernaException {
		Vector result = new Vector();
		if (this.vector.size() != b.vector.size()) {
			throw new CDKTavernaException(this.getClass().getSimpleName(), "Size of vectors differ!");
		}
		for (int i = 0; i < vector.size(); i++) {
			result.vector.add(vector.get(i) - b.vector.get(i));
		}
		return result;
	}

	/**
	 * @return The length of the vector.
	 */
	public double length() {
		double sqrSum = 0;
		for (double value : vector) {
			sqrSum += Math.pow(value, 2);
		}
		return Math.sqrt(sqrSum);
	}

}
