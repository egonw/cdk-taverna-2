/*
 erm we* Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.weka;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;

import weka.core.Instances;

/**
 * Test class for the create weka dataset from csv activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class CreateWekaDatasetFromCSVActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity wekaDatasetActivity = new CreateWekaDatasetFromCSVActivity();

	public CreateWekaDatasetFromCSVActivityTest() {
		super(CreateWekaDatasetFromCSVActivity.CREATE_WEKA_DATASET_FROM_CSV_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(CreateWekaDatasetFromCSVActivity.CREATE_WEKA_DATASET_FROM_CSV_ACTIVITY);
	}

	public void executeAsynch() throws Exception {
		wekaDatasetActivity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		File fullFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "full.csv");
		ArrayList<String> csv = new ArrayList<String>();
		LineNumberReader reader = new LineNumberReader(new FileReader(fullFile));
		String line;
		while ((line = reader.readLine()) != null) {
			csv.add(line);
		}
		reader.close();
		inputs.put(wekaDatasetActivity.INPUT_PORTS[0], csv);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(wekaDatasetActivity.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(wekaDatasetActivity.OUTPUT_PORTS[1], String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(wekaDatasetActivity, inputs,
				expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());
		byte[] objectData = (byte[]) outputs.get(wekaDatasetActivity.OUTPUT_PORTS[0]);
		Instances instances = CDKObjectHandler.getInstancesObject(objectData);
		Assert.assertEquals(5, instances.numAttributes());
		Assert.assertEquals(9, instances.numInstances());
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
		return new TestSuite(CreateWekaDatasetFromCSVActivityTest.class);
	}

}
