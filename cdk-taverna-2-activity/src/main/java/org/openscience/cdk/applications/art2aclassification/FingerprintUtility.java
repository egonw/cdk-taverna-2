/* 
 * $Author:  $
 * $Date: $
 * $Revision:  $
 * 
 * Copyright (C) 2009 by Thomas Kuhn <thomas.kuhn@uni-koeln.de>
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
package org.openscience.cdk.applications.art2aclassification;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class contains utility methods for the handling of the fingerprint item, lists of fingerprint items and list of double
 * arrays
 * 
 * @author Thomas Kuhn
 * 
 */
public class FingerprintUtility {
	/**
	 * The max percentage of the not usable values.
	 */
	private double maxPercentageOfNotUsableValues = 1.0;
	/**
	 * Removes all double[] which contains nullable values or Double.NaN values. This check is done AFTER the removing of the
	 * values and components over the maxPerentageOfNotUsableValues TRUE == remove all vectors which contains null or Double.NaN
	 * values
	 */
	private boolean removeAllVectorsUnderTheCutoff = false;
	/**
	 * Removes all components of the list of Double[] which min and max value do not differ.
	 */
	private boolean removeAllComponentsWhichMinAndMaxValueDoNotDiffer = false;

	/**
	 * @return the removeAllComponentsWhichMinAndMaxValueDoNotDiffer
	 */
	public boolean isRemoveAllComponentsWhichMinAndMaxValueDoNotDiffer() {
		return removeAllComponentsWhichMinAndMaxValueDoNotDiffer;
	}

	/**
	 * @param removeAllComponentsWhichMinAndMaxValueDoNotDiffer
	 *            the removeAllComponentsWhichMinAndMaxValueDoNotDiffer to set
	 */
	public void setRemoveAllComponentsWhichMinAndMaxValueDoNotDiffer(boolean removeAllComponentsWhichMinAndMaxValueDoNotDiffer) {
		this.removeAllComponentsWhichMinAndMaxValueDoNotDiffer = removeAllComponentsWhichMinAndMaxValueDoNotDiffer;
	}

	/**
	 * This list of strings contains the names of the usable columns
	 */
	private List<String> usableColumnNames = new ArrayList<String>();
	/**
	 * This list of strings contains the names of the unusable columns
	 */
	private List<String> unusableColumnNames = new ArrayList<String>();

	/**
	 * @return the unusableColumnNames
	 */
	public List<String> getUnusableColumnNames() {
		return unusableColumnNames;
	}

	/**
	 * Hashset which contains the identifier of the removed vectors
	 */
	private HashSet<Integer> identifierOfRemovedVectors = new HashSet<Integer>();

	/**
	 * @return the maxPercentageOfNotUsableValues
	 */
	public double getMaxPercentageOfNotUsableValues() {
		return maxPercentageOfNotUsableValues;
	}

	/**
	 * @return the removeAllVectorsUnderTheCutoff
	 */
	public boolean isRemoveAllVectorsUnderTheCutoff() {
		return removeAllVectorsUnderTheCutoff;
	}

	/**
	 * @param removeAllVectorsUnderTheCutoff
	 *            the removeAllVectorsUnderTheCutoff to set
	 */
	public void setRemoveAllVectorsUnderTheCutoff(boolean removeAllVectorsUnderTheCutoff) {
		this.removeAllVectorsUnderTheCutoff = removeAllVectorsUnderTheCutoff;
	}

	/**
	 * @param maxPercentageOfNotUsableValues
	 *            the maxPercentageOfNotUsableValues to set
	 */
	public void setMaxPercentageOfNotUsableValues(double maxPercentage) {
		maxPercentageOfNotUsableValues = maxPercentage;
	}

	/**
	 * This method creates a list of fingerprint vectors From a given prepared statement It removes all columns and vectors which
	 * contains more non desired Double values which are null and Double.NaN than the maxPercentageOfNotUsableValues The order of
	 * checks within this method: 1. Check for columns with more than the maxPercentageOfNotUsableValues of undesired double
	 * values 2. Check for vectors with more than the maxPercentageOfNotUsableValues of undesired double values 3. Remove all
	 * vectors under the cutoff which still contains undesired double values 4. Replace undesired double values with 0.0 5.
	 * Replace all columns which min and max values are identical (This columns contains the same value on in every vector)
	 * 
	 * @param statement
	 * @param identifierColumnName
	 * @param useOnlyTheseVectors
	 * @return
	 * @throws Exception
	 */
	public List<FingerprintItem> createCleanFingerprintItemList(PreparedStatement statement, String identifierColumnName,
			HashSet<Integer> useOnlyTheseVectors) throws Exception {
		boolean getAllMolecules = true;
		int valueCounter = 0;
		ResultSet resultSet;
		String[] columnNames;
		int[] errorsInColumns;
		List<Integer> errorsInVectors;
		int errorsInVectorCounter = 0;
		List<Integer> identifierOfEachVector;
		int rowCounter = 0;
		boolean[] removeColumn;
		int maxNumberOfNotUsableValues = 0;
		if (useOnlyTheseVectors != null && useOnlyTheseVectors.size() > 0) {
			getAllMolecules = false;
		}

		resultSet = statement.executeQuery();
		columnNames = new String[resultSet.getMetaData().getColumnCount()];
		for (int i = 1; i < resultSet.getMetaData().getColumnCount() + 1; i++) {
			// Add all column names to the column array
			columnNames[valueCounter] = resultSet.getMetaData().getColumnName(i);
			valueCounter++;
		}
		errorsInColumns = new int[columnNames.length];
		removeColumn = new boolean[columnNames.length];
		errorsInVectors = new ArrayList<Integer>();

		identifierOfEachVector = new ArrayList<Integer>();
		rowCounter = 0;
		// Search for columns which are not usable (Each column will be removed which contains a Doulbe.NaN value. Columns with
		// null values will be removed if the number of null values is larger than the threshold)
		while (resultSet.next()) {
			// Search for the usable columns
			if (getAllMolecules || useOnlyTheseVectors.contains(resultSet.getInt(identifierColumnName))) {
				rowCounter++;
				for (int i = 0; i < columnNames.length; i++) {
					if (resultSet.getObject(columnNames[i]) == null) {
						errorsInColumns[i]++;
					} else {
						if (Double.isNaN(resultSet.getDouble(columnNames[i]))) {
							removeColumn[i] = true;
						}
					}
				}
			}
		}
		maxNumberOfNotUsableValues = (int) Math.round((maxPercentageOfNotUsableValues * rowCounter) / 100.0);
		for (int i = 0; i < errorsInColumns.length; i++) {
			if (removeColumn[i] || errorsInColumns[i] > maxNumberOfNotUsableValues) {
				this.unusableColumnNames.add(columnNames[i]);
			} else {
				this.usableColumnNames.add(columnNames[i]);
			}
		}
		resultSet.beforeFirst();
		// Search for vectors which contains null values or double.NaN (This should not be the case for Double.NaN) All vectors
		// will be removed which contains more errors than the threshold
		while (resultSet.next()) {
			errorsInVectorCounter = 0;
			// Search for the unusable vectors
			if (getAllMolecules || useOnlyTheseVectors.contains(resultSet.getInt(identifierColumnName))) {
				for (int i = 0; i < this.usableColumnNames.size(); i++) {
					if (resultSet.getObject(this.usableColumnNames.get(i)) == null) {
						errorsInVectorCounter++;
					} else {
						if (Double.isNaN(resultSet.getDouble(this.usableColumnNames.get(i)))) {
							errorsInVectorCounter++;
						}
					}
				}
			}
			identifierOfEachVector.add(resultSet.getInt(identifierColumnName));
			errorsInVectors.add(errorsInVectorCounter);
		}
		// Calculating the Threshold
		maxNumberOfNotUsableValues = (int) Math.round((maxPercentageOfNotUsableValues * this.usableColumnNames.size()) / 100.0);
		// Extract the identifier of each vectors and add them to a list
		for (int i = 0; i < errorsInVectors.size(); i++) {
			if (errorsInVectors.get(i) > maxNumberOfNotUsableValues) {
				this.identifierOfRemovedVectors.add(identifierOfEachVector.get(i));
			}
		}
		errorsInVectors = null;
		identifierOfEachVector = null;
		if (removeAllVectorsUnderTheCutoff) {
			resultSet.beforeFirst();
			while (resultSet.next()) {
				// Search for the unusable vectors
				if (getAllMolecules || useOnlyTheseVectors.contains(resultSet.getInt(identifierColumnName))) {
					if (!this.identifierOfRemovedVectors.contains(resultSet.getInt(identifierColumnName))) {
						for (int i = 0; i < this.usableColumnNames.size(); i++) {
							if (resultSet.getObject(this.usableColumnNames.get(i)) == null) {
								this.identifierOfRemovedVectors.add(resultSet.getInt(identifierColumnName));
								break;
							} else {
								if (Double.isNaN(resultSet.getDouble(this.usableColumnNames.get(i)))) {
									this.identifierOfRemovedVectors.add(resultSet.getInt(identifierColumnName));
									break;
								}
							}
						}
					}
				}
			}
		}
		if (removeAllComponentsWhichMinAndMaxValueDoNotDiffer) {
			Double[] min = new Double[this.usableColumnNames.size()];
			Double[] max = new Double[this.usableColumnNames.size()];
			boolean initialize = true;
			resultSet.beforeFirst();
			while (resultSet.next()) {
				if (getAllMolecules || useOnlyTheseVectors.contains(resultSet.getInt(identifierColumnName))) {
					if (!this.identifierOfRemovedVectors.contains(resultSet.getInt(identifierColumnName))) {
						if (initialize) {
							for (int i = 0; i < this.usableColumnNames.size(); i++) {
								min[i] = resultSet.getDouble(this.usableColumnNames.get(i));
								max[i] = resultSet.getDouble(this.usableColumnNames.get(i));
							}
							initialize = false;
						} else {
							for (int i = 0; i < this.usableColumnNames.size(); i++) {
								if (resultSet.getDouble(this.usableColumnNames.get(i)) > max[i]) {
									max[i] = resultSet.getDouble(this.usableColumnNames.get(i));
								}
								if (resultSet.getDouble(this.usableColumnNames.get(i)) < min[i]) {
									min[i] = resultSet.getDouble(this.usableColumnNames.get(i));
								}
							}
						}

					}
				}

			}
			List<String> columnNamesToRemove = new ArrayList<String>(this.usableColumnNames.size());
			for (int i = 0; i < max.length; i++) {
				if (Double.isNaN(max[i]) || Double.isNaN(min[i]) || Double.isInfinite(max[i]) || Double.isInfinite(min[i])) {
					columnNamesToRemove.add(this.usableColumnNames.get(i));
				}
				if (max[i].doubleValue() == min[i].doubleValue()) {
					columnNamesToRemove.add(this.usableColumnNames.get(i));
				}
			}
			this.unusableColumnNames.addAll(columnNamesToRemove);
			this.usableColumnNames.removeAll(columnNamesToRemove);
		}

		resultSet.beforeFirst();
		this.usableColumnNames.remove(identifierColumnName);
		List<FingerprintItem> itemList = new ArrayList<FingerprintItem>(rowCounter);
		FingerprintItem item = null;
		double[] vector = null;
		while (resultSet.next()) {
			if (getAllMolecules || useOnlyTheseVectors.contains(resultSet.getInt(identifierColumnName))) {
				if (!this.identifierOfRemovedVectors.contains(resultSet.getInt(identifierColumnName))) {
					item = new FingerprintItem();
					vector = new double[this.usableColumnNames.size()];
					for (int j = 0; j < this.usableColumnNames.size(); j++) {
						vector[j] = resultSet.getDouble(this.usableColumnNames.get(j));
					}
					item.fingerprintVector = vector;
					item.correspondingObject = resultSet.getInt(identifierColumnName);
					itemList.add(item);
				}
			}
		}
		resultSet.close();
		statement.close();
		resultSet = null;
		System.out.println("End Cleaning ");
		for (FingerprintItem fingerprintItem : itemList) {
			for (int i = 0; i < fingerprintItem.fingerprintVector.length; i++) {
				if (Double.isNaN(fingerprintItem.fingerprintVector[i])) {
					throw new Exception("The fingerprint item list contains a Double.NaN value");
				}
			}
		}
		return itemList;
	}

	/**
	 * @return the usableColumnNames
	 */
	public List<String> getUsableColumnNames() {
		return usableColumnNames;
	}

	/**
	 * Remove Components form the vector list. This will reduce the size of each vector in the list according to the list of
	 * removable columns
	 * 
	 * @param listOfValues
	 *            The list of double[] which will be processed
	 * @param removableColumns
	 *            The column number which can be removed
	 * @return
	 */
	/**
	 * Get the identifier of the removed vectors
	 * 
	 * @return
	 */
	public HashSet<Integer> getIdentifierOfUnusableVectors() {
		return this.identifierOfRemovedVectors;
	}
}
