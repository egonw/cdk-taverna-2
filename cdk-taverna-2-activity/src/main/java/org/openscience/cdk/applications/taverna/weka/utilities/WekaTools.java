/* $RCSfile$
 * $Author:  $
 * $Date: $
 * $Revision:  $
 * 
 * Copyright (C) 2008 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
 * Copyright (C) 2010-11 by Andreas Truszkowski<atruszkowski@gmx.de>
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.openscience.cdk.applications.art2aclassification.FingerprintItem;
import org.openscience.cdk.applications.taverna.CDKTavernaException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Class which provides general methods for using the WEKA library
 * 
 * @author Thomas Kuhn, Andreas Truzskowski
 */
public class WekaTools {

	/**
	 * Creates a Weka Instances dataset from given fingerprint item list and descriptor names. The first entry is the molecule ID.
	 * The following entries are the descriptor values.
	 * 
	 * @param fingerprintItems
	 * @param descriptorNames
	 * @return The Weka Instances dataset
	 */
	public Instances createInstancesFromFingerprintArray(FingerprintItem[] fingerprintItems, List<String> descriptorNames) {
		FastVector attributes = new FastVector(descriptorNames.size() + 1);
		Attribute idAttr = new Attribute("ID", (FastVector) null);
		attributes.addElement(idAttr);
		Attribute descriptorAttr;
		for (String name : descriptorNames) {
			descriptorAttr = new Attribute(name);
			attributes.addElement(descriptorAttr);
		}
		Instances instances = new Instances("Weka Dataset", attributes, fingerprintItems.length);
		for (int i = 0; i < fingerprintItems.length; i++) {
			double[] values = new double[instances.numAttributes()];
			FingerprintItem item = fingerprintItems[i];
			values[0] = instances.attribute(0).addStringValue(((UUID) item.correspondingObject).toString());
			for (int j = 0; j < item.fingerprintVector.length; j++) {
				values[j + 1] = item.fingerprintVector[j];
			}
			Instance inst = new Instance(1.0, values);
			instances.add(inst);
		}
		return instances;
	}

	/**
	 * Comresses string into Base64 string that encodes the underlying compressed UTF-8 byte array. Can be decompressed with
	 * method Utility.decompressBase64String().
	 * 
	 * @param aString
	 *            String to be compressed
	 * @return Compressed Base64 string or null if string could not be compressed
	 */
	public String compressIntoBase64String(String aString) {

		// Region: Checks

		if (aString == null || aString.isEmpty()) {
			return null;
		}

		// End of region

		try {
			byte[] tmpOriginalByteArray = aString.getBytes("UTF-8");
			Deflater tmpCompresser = new Deflater();
			tmpCompresser.setInput(tmpOriginalByteArray);
			tmpCompresser.finish();
			ByteArrayOutputStream tmpByteArrayOutputStream = new ByteArrayOutputStream(tmpOriginalByteArray.length);
			byte[] tmpBuffer = new byte[1024];
			while (!tmpCompresser.finished()) {
				int tmpCount = tmpCompresser.deflate(tmpBuffer);
				tmpByteArrayOutputStream.write(tmpBuffer, 0, tmpCount);
			}
			tmpByteArrayOutputStream.close();
			byte[] tmpCompressedByteArray = tmpByteArrayOutputStream.toByteArray();
			return new BASE64Encoder().encode(tmpCompressedByteArray);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Decompresses Base64 string that was compressed with method Utility.compressIntoBase64String()
	 * 
	 * @param aBase64String
	 *            Base64 string (result string of method Utility.compressIntoBase64String())
	 * @return Decompressed string or null if string could not be decompressed
	 */
	public String decompressBase64String(String aBase64String) {
		if (aBase64String == null || aBase64String.isEmpty()) {
			return null;
		}
		try {
			byte[] tmpCompressedByteArray = new BASE64Decoder().decodeBuffer(aBase64String);
			Inflater tmpDecompresser = new Inflater();
			tmpDecompresser.setInput(tmpCompressedByteArray);
			ByteArrayOutputStream tmpByteArrayOutputStream = new ByteArrayOutputStream(tmpCompressedByteArray.length);
			byte[] tmpBuffer = new byte[1024];
			while (!tmpDecompresser.finished()) {
				int tmpCount = tmpDecompresser.inflate(tmpBuffer);
				tmpByteArrayOutputStream.write(tmpBuffer, 0, tmpCount);
			}
			tmpByteArrayOutputStream.close();
			byte[] tmpDecodedByteArray = tmpByteArrayOutputStream.toByteArray();
			return new String(tmpDecodedByteArray, 0, tmpDecodedByteArray.length, "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Method which returns a filter to remove the identifier from a given instance IMPORTANT: The identifier is the first
	 * attribute within the instances
	 * 
	 * @param instances
	 *            Instances which contains the format of the input instances
	 * @return Weka remover filter which is configured to remove the identifier from the instance
	 * @throws Exception
	 */
	public Remove getIDRemover(Instances instances) throws Exception {
		Remove removeFilter = new Remove();
		String[] removerOptionArray = new String[2];
		removerOptionArray[0] = "-R";
		removerOptionArray[1] = "1";
		removeFilter.setOptions(removerOptionArray);
		removeFilter.setInputFormat(instances);
		return removeFilter;
	}

	/**
	 * Method which returns a filter to get the identifier from a given instance IMPORTANT: The identifier is the first attribute
	 * within the instances
	 * 
	 * @param instances
	 *            Instances which contains the format of the input instances
	 * @return Weka remover filter which is configured to get the identifier from the instance
	 * @throws Exception
	 */
	public Remove getIDGetter(Instances instances) throws Exception {
		Remove removeFilter = new Remove();
		String[] removerOptionArray = new String[3];
		removerOptionArray[0] = "-R";
		removerOptionArray[1] = "1";
		removerOptionArray[2] = "-V";
		removeFilter.setOptions(removerOptionArray);
		removeFilter.setInputFormat(instances);
		return removeFilter;
	}

	/**
	 * Generates a silhouette plot.
	 * 
	 * @param dataset
	 *            The clustering data set
	 * @param clusterer
	 *            Weka clusterer model
	 * @return
	 * @throws Exception
	 */
	public double[][] generateSilhouettePlot(Instances dataset, Clusterer clusterer) throws Exception {
		HashMap<Integer, List<Vector>> vectorMap = new HashMap<Integer, List<Vector>>();
		HashMap<Vector, Instance> vectorToInstanceMap = new HashMap<Vector, Instance>();
		int[] numberOfVectorsInClass = new int[clusterer.numberOfClusters()];
		// build vector map
		for (int j = 0; j < dataset.numInstances(); j++) {
			Instance instance = dataset.instance(j);
			int cluster = clusterer.clusterInstance(instance);
			numberOfVectorsInClass[cluster]++;
			List<Vector> vectorList;
			if (vectorMap.get(cluster) == null) {
				vectorList = new ArrayList<Vector>();
				vectorMap.put(cluster, vectorList);
			} else {
				vectorList = vectorMap.get(cluster);
			}
			Vector vector = new Vector();
			for (int k = 0; k < instance.numAttributes(); k++) {
				vector.addValue(instance.value(k));
			}
			vectorList.add(vector);
			vectorToInstanceMap.put(vector, instance);
		}
		// Calculate distances within a cluster
		double[][] a = new double[clusterer.numberOfClusters()][];
		for (int j = 0; j < clusterer.numberOfClusters(); j++) {
			List<Vector> vectorOne = vectorMap.get(j);
			List<Vector> vectorTwo = vectorMap.get(j);
			a[j] = new double[vectorOne.size()];
			for (int k = 0; k < vectorOne.size(); k++) {
				for (int l = 0; l < vectorTwo.size(); l++) {
					if (k != l) {
						Vector result = vectorOne.get(k).subtraction(vectorTwo.get(l));
						a[j][k] += result.length();
					}
				}
				a[j][k] /= numberOfVectorsInClass[j];
			}
		}
		// Calculate distance to nearest cluster
		double[][] b = new double[clusterer.numberOfClusters()][];
		double[][] s = new double[clusterer.numberOfClusters()][];
		for (int j = 0; j < clusterer.numberOfClusters(); j++) {
			List<Vector> vectorOne = vectorMap.get(j);
			b[j] = new double[vectorOne.size()];
			s[j] = new double[vectorOne.size()];
			for (int k = 0; k < vectorOne.size(); k++) {
				double[][] tempResult = new double[clusterer.numberOfClusters()][];
				for (int l = 0; l < clusterer.numberOfClusters(); l++) {
					if (l == j) {
						continue;
					}
					List<Vector> vectorTwo = vectorMap.get(l);
					tempResult[l] = new double[vectorOne.size()];
					for (int m = 0; m < vectorTwo.size(); m++) {
						Vector result = vectorOne.get(k).subtraction(vectorTwo.get(m));
						tempResult[l][k] += result.length();
					}
					tempResult[l][k] /= numberOfVectorsInClass[l];
				}
				int nearestCluster = this.getNearestCluster(j, tempResult);
				b[j] = tempResult[nearestCluster];
				// Calculate silhouette width
				if (a[j][k] < b[j][k]) {
					s[j][k] = 1 - a[j][k] / b[j][k];
				}
				if (a[j][k] > b[j][k]) {
					s[j][k] = b[j][k] / a[j][k] - 1;
				}
			}
		}
		return s;
	}

	/**
	 * Calculates the mean of given silhouette.
	 * 
	 * @param silhouette
	 * @return The mean
	 */
	public double calculateSilhouetteMean(double[][] silhouette) {
		int n = 0;
		double value = 0;
		for (int i = 0; i < silhouette.length; i++) {
			for (int j = 0; j < silhouette[i].length; j++) {
				value += silhouette[i][j];
				n++;
			}
		}
		return value / n;
	}

	/**
	 * Determines the nearest cluster.
	 * 
	 * @return Nearest cluster.
	 */
	private int getNearestCluster(int currentCluster, double[][] values) {
		double min = 0;
		int nearestCluster = 0;
		boolean first = true;
		for (int i = 0; i < values.length; i++) {
			if (i == currentCluster) {
				continue;
			}
			double temp = 0;
			for (int j = 0; j < values[i].length; j++) {
				temp += values[i][j];
			}
			if (first) {
				min = temp;
				nearestCluster = i;
				first = false;
			} else {
				if (temp < min) {
					min = temp;
					nearestCluster = i;
				}
			}
		}
		return nearestCluster;
	}

	/**
	 * Extracts the options from given weka result file.
	 * 
	 * @param file
	 * @param clustererName
	 * @return Options string
	 */
	public String getOptionsFromFile(File file, String clustererName) {
		String name = file.getName();
		String[] parts = name.split("_");
		for (String part : parts) {
			if (part.startsWith(clustererName)) {
				return part.replaceAll(clustererName, "");
			}
		}
		return "";
	}

	/**
	 * Extracts the job ID from options String.
	 * 
	 * @param options
	 * @return The ID
	 * @throws CDKTavernaException
	 */
	public int getIDFromOptions(String options) throws CDKTavernaException {
		String[] splitted = options.split("-");
		for (String frag : splitted) {
			if (frag.startsWith("ID")) {
				return Integer.parseInt(frag.replace("ID", ""));
			}
		}
		throw new CDKTavernaException("WekaTools", CDKTavernaException.CLUSTER_MODEL_HAS_NO_ID);
	}
}
