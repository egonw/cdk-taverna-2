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
package org.openscience.cdk.applications.taverna.weka;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;

import weka.core.Instances;
import weka.core.converters.XRFFLoader;

/**
 * Test class for the merge weka datasets activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class MergeWekaDatasetsActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBeanMerge;

	private AbstractCDKActivity mergeActivity = new MergeWekaDatasetsActivity();

	public MergeWekaDatasetsActivityTest() {
		super(MergeWekaDatasetsActivity.MERGE_WEKA_DATASETS_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBeanMerge = new CDKActivityConfigurationBean();
		configBeanMerge.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_PORTS, 2);
		configBeanMerge.setActivityName(MergeWekaDatasetsActivity.MERGE_WEKA_DATASETS_ACTIVITY);
	}

	public void executeAsynch() throws Exception {
		mergeActivity.configure(configBeanMerge);
		Map<String, Object> inputs = new HashMap<String, Object>();
		File datasetFile1 = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "full.xrff");
		File datasetFile2 = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "part.xrff");
		XRFFLoader loader = new XRFFLoader();
		loader.setSource(datasetFile1);
		Instances full = loader.getDataSet();
		loader.reset();
		loader.setSource(datasetFile2);
		Instances part = loader.getDataSet();
		String name1 = "CSV1";
		String name2 = "CSV2";
		inputs.put(mergeActivity.INPUT_PORTS[0] + "_1", CDKObjectHandler.getBytes(full));
		inputs.put(mergeActivity.INPUT_PORTS[1] + "_1", name1);
		inputs.put(mergeActivity.INPUT_PORTS[0] + "_2", CDKObjectHandler.getBytes(part));
		inputs.put(mergeActivity.INPUT_PORTS[1] + "_2", name2);
		HashMap<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(mergeActivity.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(mergeActivity.OUTPUT_PORTS[1], String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(mergeActivity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());
		byte[] objectData = (byte[]) outputs.get(mergeActivity.OUTPUT_PORTS[0]);
		Instances instances = CDKObjectHandler.getInstancesObject(objectData);
		Assert.assertEquals(3, instances.numAttributes());
		Assert.assertEquals(18, instances.numInstances());
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
		return new TestSuite(MergeWekaDatasetsActivityTest.class);
	}

}
