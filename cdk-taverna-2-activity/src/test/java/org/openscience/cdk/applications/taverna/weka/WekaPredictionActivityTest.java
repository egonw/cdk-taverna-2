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
 * Test class for the weka prediciton activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class WekaPredictionActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity predicitonActivity = new WekaPredictionActivity();

	public WekaPredictionActivityTest() {
		super(WekaPredictionActivity.WEKA_PREDICTION_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(WekaPredictionActivity.WEKA_PREDICTION_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		predicitonActivity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		File dataset = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "regression.xrff");
		File model = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "predRegression.model");
		XRFFLoader loader = new XRFFLoader();
		loader.setSource(dataset);
		Instances regression = loader.getDataSet();
		inputs.put(predicitonActivity.INPUT_PORTS[0], CDKObjectHandler.getBytes(regression));
		inputs.put(predicitonActivity.INPUT_PORTS[1], model.getPath());
		HashMap<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(predicitonActivity.OUTPUT_PORTS[0], String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(predicitonActivity, inputs,
				expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 1, outputs.size());
		List<String> objectData = (List<String>) outputs.get(predicitonActivity.OUTPUT_PORTS[0]);
		Assert.assertEquals(184, objectData.size());
		dataset = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "SimpleKMeans-ID1_test.xrff");
		model = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "SimpleKMeans-ID1_test.model");
		loader = new XRFFLoader();
		loader.setSource(dataset);
		Instances clustering = loader.getDataSet();
		inputs.put(predicitonActivity.INPUT_PORTS[0], CDKObjectHandler.getBytes(clustering));
		inputs.put(predicitonActivity.INPUT_PORTS[1], model.getPath());
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(predicitonActivity.OUTPUT_PORTS[0], String.class);
		outputs = ActivityInvoker.invokeAsyncActivity(predicitonActivity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 1, outputs.size());
		objectData = (List<String>) outputs.get(predicitonActivity.OUTPUT_PORTS[0]);
		Assert.assertEquals(701, objectData.size());
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
		return new TestSuite(WekaPredictionActivityTest.class);
	}

}
