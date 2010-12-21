/* $RCSfile$
 * $Author:  $
 * $Date: $
 * $Revision:  $
 * 
 * Copyright (C) 2008 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
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
import java.util.List;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.openscience.cdk.applications.art2aclassification.FingerprintItem;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Class which provides general methods for using the WEKA library
 * 
 * @author Thomas Kuhn
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
	public static Instances createInstancesFromFingerprintArray(FingerprintItem[] fingerprintItems, List<String> descriptorNames) {
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
	public static String compressIntoBase64String(String aString) {

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
	public static String decompressBase64String(String aBase64String) {
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
	public static Remove getIDRemover(Instances instances) throws Exception {
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
	public static Remove getIDGetter(Instances instances) throws Exception {
		Remove removeFilter = new Remove();
		String[] removerOptionArray = new String[3];
		removerOptionArray[0] = "-R";
		removerOptionArray[1] = "1";
		removerOptionArray[2] = "-V";
		removeFilter.setOptions(removerOptionArray);
		removeFilter.setInputFormat(instances);
		return removeFilter;
	}
}
