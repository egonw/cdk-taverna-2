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
import java.io.ObjectInputStream;
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
 * Test class for the GAAttributeEvaluation activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class GAAttributeSelectionActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity gaSelectionActivity = new GAAttributeSelectionActivity();

	public GAAttributeSelectionActivityTest() {
		super(GAAttributeSelectionActivity.GA_ATTRIBUTE_SELECTION_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(GAAttributeSelectionActivity.GA_ATTRIBUTE_SELECTION_ACTIVITY);
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		String options = "0.05;0.05;25;10;-1  ;weka.classifiers.functions.LinearRegression;-S 0 -R 0.00000008 -C ;false;10;";
		this.gaSelectionActivity.configure(this.configBean);
		// load regression data
		File file = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "regression.xrff");
		XRFFLoader loader = new XRFFLoader();
		loader.setSource(file);
		Instances instances = loader.getDataSet();
		// Setup input
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, 1);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		Map<String, Object> inputs = new HashMap<String, Object>();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_GA_ATTRIBUTE_SELECTION_OPTIONS, options);
		byte[] data = CDKObjectHandler.getBytes(instances);
		inputs.put(this.gaSelectionActivity.INPUT_PORTS[0], data);
		expectedOutputTypes.put(this.gaSelectionActivity.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(this.gaSelectionActivity.OUTPUT_PORTS[1], String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(this.gaSelectionActivity, inputs,
				expectedOutputTypes);
		List<byte[]> resultData = (List<byte[]>) outputs.get(this.gaSelectionActivity.OUTPUT_PORTS[0]);
		Assert.assertEquals(1, resultData.size());
		List<String> resultString = (List<String>) outputs.get(this.gaSelectionActivity.OUTPUT_PORTS[1]);
		Assert.assertEquals(1, resultString.size());
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
		return new TestSuite(GAAttributeSelectionActivityTest.class);
	}

}
