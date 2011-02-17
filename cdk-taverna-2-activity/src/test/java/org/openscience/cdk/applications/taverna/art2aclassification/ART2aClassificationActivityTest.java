/*
 * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.art2aclassification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.openscience.cdk.applications.art2aclassification.Art2aClassificator;
import org.openscience.cdk.applications.art2aclassification.FingerprintItem;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.classification.art2a.ART2aClassificationActivity;
import org.openscience.cdk.applications.taverna.io.XMLFileIO;
import org.openscience.cdk.applications.taverna.setup.SetupController;

/**
 * Test class for the ART2aClassification activity.
 * 
 * @author Andreas Truszkowski
 */
public class ART2aClassificationActivityTest extends CDKTavernaTestCases {

	/**
	 * Boolean which allows a scaling of the input fingerprint between 0 and 1
	 */
	private boolean scaleFingerprintItemToInternalZeroOne = true;
	/**
	 * The number of classificator to calculate
	 */
	private int numberOfClassifications = 2;
	/**
	 * The upper limit for the vigilance parameter
	 */
	private double upperVigilanceLimit = 0.9;
	/**
	 * The lower limit for the vigilance parameter
	 */
	private double lowerVigilanceLimit = 0.8;
	/**
	 * The maximum classification time for one epoch
	 */
	private int maximumClassificationTime = 15;

	private File dir = null;
	private CDKActivityConfigurationBean configBean;
	private AbstractCDKActivity activity = new ART2aClassificationActivity();

	public ART2aClassificationActivityTest() {
		super(ART2aClassificationActivity.ART2A_CLASSIFICATOR_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(ART2aClassificationActivity.ART2A_CLASSIFICATOR_ACTIVITY);
		this.dir = new File(SetupController.getInstance().getWorkingDir());
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".art2a");
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_CLASSIFICATIONS,
				this.numberOfClassifications);
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_UPPER_VIGILANCE_LIMIT, this.upperVigilanceLimit);
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_LOWER_VIGILANCE_LIMIT, this.lowerVigilanceLimit);
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_MAXIMUM_CLASSIFICATION_TIME,
				this.maximumClassificationTime);
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_SCALE_FINGERPRINT_ITEMS,
				this.scaleFingerprintItemToInternalZeroOne);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		activity.configure(configBean);
		// Get test data
		String path = "." + File.separator + "target" + File.separator + "test-classes" + File.separator + "data"
				+ File.separator + "IrisFlowerData" + File.separator;
		String sepalLength = path + "sepal length.txt";
		String sepalWidth = path + "sepal width.txt";
		String petalLength = path + "petal length.txt";
		String petalWidth = path + "petal width.txt";
		String species = path + "species.txt";

		int[] sl = getDataArrayFromFile(sepalLength);
		int[] sw = getDataArrayFromFile(sepalWidth);
		int[] pl = getDataArrayFromFile(petalLength);
		int[] pw = getDataArrayFromFile(petalWidth);
		int[] sp = getDataArrayFromFile(species);

		FingerprintItem objectToClassify;
		List<FingerprintItem> fingerprintList = new ArrayList<FingerprintItem>();

		for (int i = 0; i < sl.length; i++) {
			objectToClassify = new FingerprintItem();
			objectToClassify.fingerprintVector = new double[4];
			objectToClassify.fingerprintVector[0] = new Double(sl[i]);
			objectToClassify.fingerprintVector[1] = new Double(sw[i]);
			objectToClassify.fingerprintVector[2] = new Double(pl[i]);
			objectToClassify.fingerprintVector[3] = new Double(pw[i]);
			objectToClassify.correspondingObject = sp[i];
			fingerprintList.add(objectToClassify);
		}
		// Run test
		Map<String, Object> inputs = new HashMap<String, Object>();
		List<byte[]> dataList = CDKObjectHandler.getBytesList(fingerprintList);
		inputs.put(activity.INPUT_PORTS[0], dataList);
		inputs.put(activity.INPUT_PORTS[1], this.dir);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.OUTPUT_PORTS[0], String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		ArrayList<String> files = (ArrayList<String>) outputs.get(activity.OUTPUT_PORTS[0]);
		XMLFileIO xmlFileIO = new XMLFileIO();
		for (int i = 0; i < files.size(); i++) {
			String fileName = files.get(i);
			XMLStreamReader xmlReader = xmlFileIO.getXMLStreamReaderWithCompression(fileName);
			xmlReader.next();
			Art2aClassificator classificator = new Art2aClassificator(xmlReader, true);
			xmlReader.close();
			xmlFileIO.closeXMLStreamReader();
			if (i == 0) {
				int[] vectorsInClasses = { 4, 36, 50, 60 };
				for (int j = 0; j < vectorsInClasses.length; j++) {
					assertEquals(vectorsInClasses[j], classificator.getNumberOfVectorsInClass(j));
				}
			}
			if (i == 1) {
				int[] vectorsInClasses = { 1, 1, 4, 3, 1, 36, 45, 59 };
				for (int j = 0; j < vectorsInClasses.length; j++) {
					assertEquals(vectorsInClasses[j], classificator.getNumberOfVectorsInClass(j));
				}
			}
		}
	}

	/**
	 * I/O-method. Reads the data from the iris flower files to an integer
	 * array.
	 * 
	 * @param filename
	 *            The location of the file.
	 * @return An int[] containing the data of the file.
	 */
	private int[] getDataArrayFromFile(String filename) {
		FileReader myReader = null;
		BufferedReader myBufferedReader = null;
		LinkedList<Integer> myList = new LinkedList<Integer>();
		try {
			myReader = new FileReader(filename);
			myBufferedReader = new BufferedReader(myReader);
			String text;
			while (true) {
				text = myBufferedReader.readLine();
				if (text != null) {
					myList.add(Integer.parseInt(text));
				} else {
					break;
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.fillInStackTrace());
		} catch (IOException e) {
			throw new RuntimeException(e.fillInStackTrace());
		} finally {
			try {
				if (myBufferedReader != null)
					myBufferedReader.close();
				if (myReader != null)
					myReader.close();
			} catch (IOException e) {
				throw new RuntimeException(e.fillInStackTrace());
			}
		}
		int[] data = new int[myList.size()];
		for (int i = 0; i < myList.size(); i++) {
			data[i] = myList.get(i).intValue();
		}
		return data;
	}

	private void cleanUp() {
		FileNameGenerator.deleteDir(this.dir);
	}

	public void executeTest() {
		try {
			this.makeConfigBean();
			this.executeAsynch();
			this.cleanUp();
		} catch (Exception e) {
			e.printStackTrace();
			// This test causes an error
			assertEquals(false, true);
		}
	}

	/**
	 * Method which returns a test suit with the name of this class
	 * 
	 * @return TestSuite
	 */
	public static Test suite() {
		return new TestSuite(ART2aClassificationActivityTest.class);
	}
}
