/*
 * Copyright (C) 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.weka.regression;

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
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.setup.SetupController;

import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.XRFFLoader;

/**
 * Test class for the weka clustering activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class WekaRegressionActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity wekaRegressionActivity = new WekaRegressionActivity();
	private File dir = null;

	public WekaRegressionActivityTest() {
		super(WekaRegressionActivity.WEKA_LEARNING_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		this.dir = new File(SetupController.getInstance().getWorkingDir());
		configBean.setActivityName(WekaRegressionActivity.WEKA_LEARNING_ACTIVITY);
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		String[] classNames = new String[] { "weka.classifiers.functions.LinearRegression",
				"weka.classifiers.functions.MultilayerPerceptron", "weka.classifiers.trees.M5P",
				"weka.classifiers.functions.LibSVM" };
		String[] options = new String[] { "-S 0 -R 0.00000008 -C", "-H a -L 0.3 -M 0.2 -N 500", "-M 4 -R",
				"-S 3 -K 2 -D 3 -R 0.0 -J -V -C 1" };
		this.wekaRegressionActivity.configure(this.configBean);
		// load regression data
		File file = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "regression.xrff");
		XRFFLoader loader = new XRFFLoader();
		loader.setSource(file);
		Instances instances = loader.getDataSet();
		// Setup input
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, 1);
		for (int i = 0; i < classNames.length; i++) {
			Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
			Map<String, Object> inputs = new HashMap<String, Object>();
			this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_NAME, classNames[i]);
			this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_OPTIONS, options[i]);
			List<byte[]> data = CDKObjectHandler.getBytesList(Collections.singletonList(instances));
			inputs.put(this.wekaRegressionActivity.INPUT_PORTS[0], data);
			inputs.put(this.wekaRegressionActivity.INPUT_PORTS[1], this.dir);
			expectedOutputTypes.put(this.wekaRegressionActivity.OUTPUT_PORTS[0], String.class);
			Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(this.wekaRegressionActivity, inputs,
					expectedOutputTypes);
			List<String> resultFiles = (List<String>) outputs.get(this.wekaRegressionActivity.OUTPUT_PORTS[0]);
			Assert.assertEquals(1, resultFiles.size());
			SerializationHelper.read(resultFiles.get(0));
		}
	}

	@Override
	protected void executeTest() {
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

	public void cleanUp() {
		FileNameGenerator.deleteDir(this.dir);
	}

	/**
	 * Method which returns a test suit with the name of this class
	 * 
	 * @return TestSuite
	 */
	public static Test suite() {
		return new TestSuite(WekaRegressionActivityTest.class);
	}

}
