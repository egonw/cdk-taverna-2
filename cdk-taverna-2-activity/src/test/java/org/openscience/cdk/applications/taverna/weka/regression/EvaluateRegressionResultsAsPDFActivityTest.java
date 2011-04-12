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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jfree.chart.plot.RainbowPalette;
import org.junit.Assert;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.qsar.CSVToQSARVectorActivity;
import org.openscience.cdk.applications.taverna.setup.SetupController;
import org.openscience.cdk.applications.taverna.weka.CreateWekaDatasetFromQSARVectorActivity;
import org.openscience.cdk.applications.taverna.weka.clustering.WekaClusteringActivity;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.XRFFLoader;
import weka.filters.Filter;

/**
 * Test class for the AttributeEvaluation activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class EvaluateRegressionResultsAsPDFActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity setActivity = new CreateWekaRegressionSetActivity();

	public EvaluateRegressionResultsAsPDFActivityTest() {
		super(CreateWekaRegressionSetActivity.CREATE_WEKA_REGRESSION_DATASET_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(CreateWekaRegressionSetActivity.CREATE_WEKA_REGRESSION_DATASET_ACTIVITY);
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
		File fileCSV = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "regression.csv");
		List<String> csv = new ArrayList<String>();
		LineNumberReader reader = new LineNumberReader(new FileReader(fileCSV));
		String line = null;
		while ((line = reader.readLine()) != null) {
			csv.add(line);
		}
		// Setup input
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		Map<String, Object> inputs = new HashMap<String, Object>();
		byte[] data = CDKObjectHandler.getBytes(instances);
		inputs.put(this.setActivity.INPUT_PORTS[0], data);
		inputs.put(this.setActivity.INPUT_PORTS[1], csv);
		expectedOutputTypes.put(this.setActivity.OUTPUT_PORTS[0], byte[].class);
		Map<String, Object> outputs = ActivityInvoker
				.invokeAsyncActivity(this.setActivity, inputs, expectedOutputTypes);
		byte[] resultData = (byte[]) outputs.get(this.setActivity.OUTPUT_PORTS[0]);
		Instances set = (Instances) CDKObjectHandler.getObject(resultData);
		Assert.assertEquals(183, set.numInstances());
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
