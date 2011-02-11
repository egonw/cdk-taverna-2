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
 * Test class for the MDL SD file reader activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class CurateQSARVectorActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity loadActivity = new CSVToQSARVectorActivity();
	private AbstractCDKActivity curateActivity = new CurateQSARVectorActivity();

	public CurateQSARVectorActivityTest() {
		super(CurateQSARVectorActivity.CURATE_QSAR_VECTOR_COLUMNS_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_MIN_MAX_CURATION, true);
		configBean.setActivityName(CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		curateActivity.configure(configBean);
		loadActivity.configure(configBean);
		// leave empty. No ports used
		Map<String, Object> inputs = new HashMap<String, Object>();
		File csvFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "qsar" + File.separator + "qsar.csv");
		inputs.put(loadActivity.INPUT_PORTS[0], csvFile);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(loadActivity.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(loadActivity.OUTPUT_PORTS[1], byte[].class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(loadActivity, inputs, expectedOutputTypes);
		// leave empty. No ports used
		inputs = outputs;
		int[] vectorMapAsserts = { 9, 0, 6 };
		int[] descriptorNamesAssert = { 67, 245, 103 };
		for (int i = 0; i < 3; i++) {
			configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_CURATION_TYPE, i);
			expectedOutputTypes = new HashMap<String, Class<?>>();
			expectedOutputTypes.put(curateActivity.OUTPUT_PORTS[0], byte[].class);
			expectedOutputTypes.put(curateActivity.OUTPUT_PORTS[1], byte[].class);
			outputs = ActivityInvoker.invokeAsyncActivity(curateActivity, inputs, expectedOutputTypes);
			Assert.assertEquals("Unexpected outputs", 2, outputs.size());
			byte[] objectData = (byte[]) outputs.get(curateActivity.OUTPUT_PORTS[0]);
			Map<UUID, Map<String, Object>> vectorMap = (Map<UUID, Map<String, Object>>) CDKObjectHandler
					.getObject(objectData);
			Assert.assertEquals(vectorMapAsserts[i], vectorMap.size());
			objectData = (byte[]) outputs.get(curateActivity.OUTPUT_PORTS[1]);
			ArrayList<String> descriptorNames = (ArrayList<String>) CDKObjectHandler.getObject(objectData);
			Assert.assertEquals(descriptorNamesAssert[i], descriptorNames.size());
		}
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
		return new TestSuite(CurateQSARVectorActivityTest.class);
	}

}
