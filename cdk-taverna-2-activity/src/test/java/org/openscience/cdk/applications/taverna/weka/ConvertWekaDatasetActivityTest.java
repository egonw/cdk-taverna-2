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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import weka.core.converters.XRFFLoader;

/**
 * Test class for the convert weka dataset activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ConvertWekaDatasetActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity wekaDatasetActivity = new ConvertWekaDatasetActivity();

	public ConvertWekaDatasetActivityTest() {
		super(ConvertWekaDatasetActivity.CONVERT_WEKA_DATASET_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(ConvertWekaDatasetActivity.CONVERT_WEKA_DATASET_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		wekaDatasetActivity.configure(configBean);
		// leave empty. No ports used
		Map<String, Object> inputs = new HashMap<String, Object>();
		File fullFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "full.xrff");
		File partFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "part.xrff");
		XRFFLoader loader = new XRFFLoader();
		loader.setSource(fullFile);
		Instances full = loader.getDataSet();
		loader.reset();
		loader.setSource(partFile);
		Instances part = loader.getDataSet();
		inputs.put(wekaDatasetActivity.INPUT_PORTS[0], CDKObjectHandler.getBytesList(Collections.singletonList(full)));
		inputs.put(wekaDatasetActivity.INPUT_PORTS[1], CDKObjectHandler.getBytes(part));
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(wekaDatasetActivity.OUTPUT_PORTS[0], byte[].class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(wekaDatasetActivity, inputs,
				expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 1, outputs.size());
		List<byte[]> objectData = (List<byte[]>) outputs.get(wekaDatasetActivity.OUTPUT_PORTS[0]);
		Instances instances = CDKObjectHandler.getInstancesObject(objectData.get(0));
		Assert.assertEquals(3, instances.numAttributes());
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
		return new TestSuite(ConvertWekaDatasetActivityTest.class);
	}

}
