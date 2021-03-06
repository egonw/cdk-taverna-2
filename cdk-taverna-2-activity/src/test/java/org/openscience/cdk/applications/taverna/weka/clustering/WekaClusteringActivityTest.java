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
package org.openscience.cdk.applications.taverna.weka.clustering;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
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
import org.openscience.cdk.applications.taverna.qsar.CSVToQSARVectorActivity;
import org.openscience.cdk.applications.taverna.setup.SetupController;
import org.openscience.cdk.applications.taverna.weka.CreateWekaDatasetFromQSARVectorActivity;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Test class for the weka regression activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class WekaClusteringActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;
	private CDKActivityConfigurationBean clusteringConfigBean;

	private AbstractCDKActivity loadActivity = new CSVToQSARVectorActivity();
	private AbstractCDKActivity wekaDatasetActivity = new CreateWekaDatasetFromQSARVectorActivity();
	private AbstractCDKActivity wekaClusteringActivity = new WekaClusteringActivity();
	private File dir = null;

	public WekaClusteringActivityTest() {
		super(CreateWekaDatasetFromQSARVectorActivity.CREATE_WEKA_DATASET_FROM_QSAR_VECTOR_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		this.dir = new File(SetupController.getInstance().getWorkingDir());
		configBean.setActivityName(CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY);
		clusteringConfigBean = new CDKActivityConfigurationBean();
		String jobData = "weka.clusterers.EM;-N;5;-I;100;-M;0.00001;-ID;1;weka.clusterers.FarthestFirst;-N;5;-ID;5;weka.clusterers.HierarchicalClusterer;-N;5;-L;SINGLE;-ID;2;weka.clusterers.SimpleKMeans;-N;5;-I;100;-ID;3;weka.clusterers.XMeans;-I;1;-M;1000;-J;1000;-L;2;-H;4;-ID;4";
		clusteringConfigBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_CLUSTERING_JOB_DATA, jobData);
		clusteringConfigBean.setActivityName(WekaClusteringActivity.WEKA_CLUSTERING_ACTIVITY);
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		wekaDatasetActivity.configure(configBean);
		loadActivity.configure(configBean);
		wekaClusteringActivity.configure(clusteringConfigBean);
		// load QSAR vectors
		Map<String, Object> inputs = new HashMap<String, Object>();
		File csvFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "qsar" + File.separator + "curatedQSARbig.csv");
		inputs.put(loadActivity.INPUT_PORTS[0], csvFile);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(loadActivity.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(loadActivity.OUTPUT_PORTS[1], byte[].class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(loadActivity, inputs, expectedOutputTypes);
		inputs = outputs;
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(wekaDatasetActivity.OUTPUT_PORTS[0], byte[].class);
		outputs = ActivityInvoker.invokeAsyncActivity(wekaDatasetActivity, inputs, expectedOutputTypes);
		byte[] datasetData = (byte[]) outputs.get(wekaDatasetActivity.OUTPUT_PORTS[0]);
		Instances dataset = CDKObjectHandler.getInstancesObject(datasetData);
		inputs = outputs;
		// Cluster
		inputs.put(wekaClusteringActivity.INPUT_PORTS[1], this.dir);
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(wekaClusteringActivity.OUTPUT_PORTS[0], String.class);
		outputs = ActivityInvoker.invokeAsyncActivity(wekaClusteringActivity, inputs, expectedOutputTypes);
		List<String> files = (List<String>) outputs.get(wekaClusteringActivity.OUTPUT_PORTS[0]);
		// Test results
		HashMap<String, Integer[]> resultMap = new HashMap<String, Integer[]>();
		resultMap.put("XMeans", new Integer[] { 47, 178, 175, 300 });
		resultMap.put("FarthestFirst", new Integer[] { 670, 1, 2, 2, 25 });
		resultMap.put("SimpleKMeans", new Integer[] { 278, 206, 46, 146, 24 });
		resultMap.put("EM", new Integer[] { 68, 172, 200, 64, 196 });
		resultMap.put("HierarchicalClusterer", new Integer[] { 695, 2, 1, 1, 1 });
		Assert.assertEquals(5, files.size());
		WekaTools tool = new WekaTools();
		for (int i = 0; i < 5; i++) {
			// Load clusterer
			ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(files.get(i))));
			Clusterer clusterer = (Clusterer) reader.readObject();
			reader.close();
			// Instances ids = Filter.useFilter(dataset,
			// WekaTools.getIDGetter(dataset));
			Instances cleanDataset = Filter.useFilter(dataset, tool.getIDRemover(dataset));
			// Test clusterer
			int[] numberOfVectorsInClass = new int[clusterer.numberOfClusters()];
			for (int j = 0; j < cleanDataset.numInstances(); j++) {
				numberOfVectorsInClass[clusterer.clusterInstance(cleanDataset.instance(j))]++;
			}
			Integer[] expectedValues = resultMap.get(clusterer.getClass().getSimpleName());
			Assert.assertEquals(expectedValues.length, clusterer.numberOfClusters());
			for (int j = 0; j < clusterer.numberOfClusters(); j++) {
				Assert.assertEquals(expectedValues[j].intValue(), numberOfVectorsInClass[j]);
			}
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
		return new TestSuite(WekaClusteringActivityTest.class);
	}

}
