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
package org.openscience.cdk.applications.taverna.weka;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
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
import org.openscience.cdk.applications.taverna.qsar.CSVToQSARVectorActivity;

/**
 * Test class for the create extract clustering result as PDF activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ClusteringResultConsideringDifferentOriginsAsPDFActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;
	private CDKActivityConfigurationBean clusteringConfigBean;

	private AbstractCDKActivity wekaActivity = new ClusteringResultConsideringDifferentOriginsAsPDF();
	private File dir = null;
	private ArrayList<String> relationTable = new ArrayList<String>();
	private AbstractCDKActivity wekaClusteringActivity = new WekaClusteringActivity();
	private AbstractCDKActivity loadActivity = new CSVToQSARVectorActivity();
	private AbstractCDKActivity wekaDatasetActivity = new CreateWekaDatasetFromQSARVectorActivity();

	public ClusteringResultConsideringDifferentOriginsAsPDFActivityTest() {
		super(ClusteringResultConsideringDifferentOriginsAsPDF.CLUSTERING_RESULT_CONSIDERING_DIFFERENT_ORIGINS_AS_PDF_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		this.configBean.setActivityName(this.wekaActivity.getActivityName());
		this.dir = new File("." + File.separator + "Test" + File.separator);
		this.dir.mkdir();
		File relationFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "relationtable.txt");
		LineNumberReader reader = new LineNumberReader(new FileReader(relationFile));
		String line;
		while ((line = reader.readLine()) != null) {
			relationTable.add(line);
		}
		reader.close();
		File[] csvFile = new File[] { new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "weka" + File.separator + "qsardata.csv") };
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE, csvFile);
		clusteringConfigBean = new CDKActivityConfigurationBean();
		clusteringConfigBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE, this.dir);
		String jobData = "weka.clusterers.EM;-N;5;-I;100;-M;0.00001;-ID;1;";
		clusteringConfigBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_CLUSTERING_JOB_DATA, jobData);
		clusteringConfigBean.setActivityName(WekaClusteringActivity.WEKA_CLUSTERING_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		wekaActivity.configure(configBean);
		wekaDatasetActivity.configure(configBean);
		loadActivity.configure(configBean);
		wekaClusteringActivity.configure(clusteringConfigBean);
		// load QSAR vectors
		Map<String, Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(loadActivity.getRESULT_PORTS()[0], byte[].class);
		expectedOutputTypes.put(loadActivity.getRESULT_PORTS()[1], byte[].class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(loadActivity, inputs, expectedOutputTypes);
		inputs = outputs;
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(wekaDatasetActivity.getRESULT_PORTS()[0], byte[].class);
		outputs = ActivityInvoker.invokeAsyncActivity(wekaDatasetActivity, inputs, expectedOutputTypes);
		inputs = outputs;
		// Cluster
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(wekaClusteringActivity.getRESULT_PORTS()[0], String.class);
		outputs = ActivityInvoker.invokeAsyncActivity(wekaClusteringActivity, inputs, expectedOutputTypes);
		List<String> files = (List<String>) outputs.get(wekaClusteringActivity.getRESULT_PORTS()[0]);
		inputs = new HashMap<String, Object>();
		inputs.put(wekaActivity.getINPUT_PORTS()[0], files);
		inputs.put(wekaActivity.getINPUT_PORTS()[1], relationTable);
		expectedOutputTypes = new HashMap<String, Class<?>>();
		outputs = ActivityInvoker.invokeAsyncActivity(wekaActivity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 0, outputs.size());
		// Only check for exceptions
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
		for (File file : this.dir.listFiles()) {
			file.delete();
		}
		this.dir.delete();
	}

	/**
	 * Method which returns a test suit with the name of this class
	 * 
	 * @return TestSuite
	 */
	public static Test suite() {
		return new TestSuite(ClusteringResultConsideringDifferentOriginsAsPDFActivityTest.class);
	}

}