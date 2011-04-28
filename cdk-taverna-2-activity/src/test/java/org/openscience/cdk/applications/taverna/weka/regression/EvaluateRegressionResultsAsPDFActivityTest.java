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
import weka.core.converters.XRFFLoader;

/**
 * Test class for the evaluate regression results as PDF activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class EvaluateRegressionResultsAsPDFActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;
	private File dir = null;
	private AbstractCDKActivity setActivity = new EvaluateRegressionResultsAsPDFActivity();

	public EvaluateRegressionResultsAsPDFActivityTest() {
		super(EvaluateRegressionResultsAsPDFActivity.EVALUATE_REGRESSION_RESULTS_AS_PDF_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		this.dir = new File(SetupController.getInstance().getWorkingDir());
		configBean = new CDKActivityConfigurationBean();
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_SCATTER_PLOT_OPTIONS, "0;false");
		configBean
				.setActivityName(EvaluateRegressionResultsAsPDFActivity.EVALUATE_REGRESSION_RESULTS_AS_PDF_ACTIVITY);
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		this.setActivity.configure(this.configBean);
		// load regression data
		File fileXRFF = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "regression.xrff");
		XRFFLoader loader = new XRFFLoader();
		loader.setSource(fileXRFF);
		Instances instances = loader.getDataSet();
		File input = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "regression.model");
		File fileModel = new File(this.dir.getPath() + File.separator + "test.model");
		FileNameGenerator.copyFile(input, fileModel);
		// Setup input
		List<byte[]> data = CDKObjectHandler.getBytesList(Collections.singletonList(instances));
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put(this.setActivity.INPUT_PORTS[0], Collections.singletonList(fileModel.getPath()));
		inputs.put(this.setActivity.INPUT_PORTS[1], data);
		inputs.put(this.setActivity.INPUT_PORTS[2], data);
		expectedOutputTypes.put(this.setActivity.OUTPUT_PORTS[0], String.class);
		Map<String, Object> outputs = ActivityInvoker
				.invokeAsyncActivity(this.setActivity, inputs, expectedOutputTypes);
		List<String> resultData = (List<String>) outputs.get(this.setActivity.OUTPUT_PORTS[0]);
		Assert.assertEquals(2, resultData.size());
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
		return new TestSuite(EvaluateRegressionResultsAsPDFActivityTest.class);
	}

}
