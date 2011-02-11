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
package org.openscience.cdk.applications.taverna.qsar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;

/**
 * Test class for the QSAR vector generator activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class MergeQSARVectorsActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBeanLoad1;
	private CDKActivityConfigurationBean configBeanLoad2;
	private CDKActivityConfigurationBean configBeanMerge;

	private AbstractCDKActivity loadActivity1 = new CSVToQSARVectorActivity();
	private AbstractCDKActivity loadActivity2 = new CSVToQSARVectorActivity();
	private AbstractCDKActivity mergeActivity = new MergeQSARVectorsActivity();

	public MergeQSARVectorsActivityTest() {
		super(CurateQSARVectorActivity.CURATE_QSAR_VECTOR_COLUMNS_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBeanLoad1 = new CDKActivityConfigurationBean();
		configBeanLoad1.setActivityName(CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY);
		configBeanLoad2 = new CDKActivityConfigurationBean();
		configBeanLoad2.setActivityName(CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY);
		configBeanMerge = new CDKActivityConfigurationBean();
		configBeanMerge.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_PORTS, 2);
		configBeanMerge.setActivityName(MergeQSARVectorsActivity.MERGE_QSAR_VECTORS_ACTIVITY);
	}

	@SuppressWarnings({ "unchecked" })
	public void executeAsynch() throws Exception {
		mergeActivity.configure(configBeanMerge);
		loadActivity1.configure(configBeanLoad1);
		loadActivity2.configure(configBeanLoad2);
		// leave empty. No ports used
		Map<String, Object> inputs = new HashMap<String, Object>();
		File csvFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "qsar" + File.separator + "qsar.csv");
		inputs.put(loadActivity1.INPUT_PORTS[0], csvFile);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(loadActivity1.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(loadActivity1.OUTPUT_PORTS[1], byte[].class);
		Map<String, Object> outputsLoad1 = ActivityInvoker.invokeAsyncActivity(loadActivity1, inputs,
				expectedOutputTypes);
		inputs = new HashMap<String, Object>();
		csvFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "qsar" + File.separator + "notcuratedqsar.csv");
		inputs.put(loadActivity2.INPUT_PORTS[0], csvFile);
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(loadActivity2.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(loadActivity2.OUTPUT_PORTS[1], byte[].class);
		Map<String, Object> outputsLoad2 = ActivityInvoker.invokeAsyncActivity(loadActivity2, inputs,
				expectedOutputTypes);
		// Create new input
		inputs = new HashMap<String, Object>();
		byte[] qsarVectorData1 = (byte[]) outputsLoad1.get(loadActivity1.OUTPUT_PORTS[0]);
		byte[] descriptorNamesData1 = (byte[]) outputsLoad1.get(loadActivity1.OUTPUT_PORTS[1]);
		byte[] qsarVectorData2 = (byte[]) outputsLoad2.get(loadActivity2.OUTPUT_PORTS[0]);
		byte[] descriptorNamesData2 = (byte[]) outputsLoad2.get(loadActivity2.OUTPUT_PORTS[1]);
		String name1 = "CSV1";
		String name2 = "CSV2";
		inputs.put(mergeActivity.INPUT_PORTS[0] + "_1", qsarVectorData1);
		inputs.put(mergeActivity.INPUT_PORTS[1] + "_1", descriptorNamesData1);
		inputs.put(mergeActivity.INPUT_PORTS[2] + "_1", name1);
		inputs.put(mergeActivity.INPUT_PORTS[0] + "_2", qsarVectorData2);
		inputs.put(mergeActivity.INPUT_PORTS[1] + "_2", descriptorNamesData2);
		inputs.put(mergeActivity.INPUT_PORTS[2] + "_2", name2);
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(mergeActivity.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(mergeActivity.OUTPUT_PORTS[1], byte[].class);
		expectedOutputTypes.put(mergeActivity.OUTPUT_PORTS[2], String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(mergeActivity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 3, outputs.size());
		byte[] vectorData = (byte[]) outputs.get(mergeActivity.OUTPUT_PORTS[0]);
		Map<UUID, Map<String, Object>> resultVector = (Map<UUID, Map<String, Object>>) CDKObjectHandler
				.getObject(vectorData);
		Assert.assertEquals(18, resultVector.size());
		byte[] nameData = (byte[]) outputs.get(mergeActivity.OUTPUT_PORTS[1]);
		ArrayList<String> resultNames = (ArrayList<String>) CDKObjectHandler.getObject(nameData);
		Assert.assertEquals(277, resultNames.size());
		ArrayList<String> relationString = (ArrayList<String>) outputs.get(mergeActivity.OUTPUT_PORTS[2]);
		Assert.assertEquals(20, relationString.size());
	}

	@Override
	protected void executeTest() {
		try {
			this.makeConfigBean();
			this.executeAsynch();
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
		return new TestSuite(MergeQSARVectorsActivityTest.class);
	}

}
