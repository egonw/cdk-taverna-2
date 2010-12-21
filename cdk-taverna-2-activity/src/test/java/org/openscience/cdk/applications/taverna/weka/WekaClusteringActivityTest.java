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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
import org.openscience.cdk.applications.taverna.qsar.CSVToQSARVectorActivity;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Test class for the MDL SD file reader activity.
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
		this.dir = new File("." + File.separator + "Test" + File.separator);
		this.dir.mkdir();
		File[] csvFile = new File[] { new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "qsar" + File.separator + "curatedQSARbig.csv") };
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE, csvFile);
		configBean.setActivityName(CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY);
		clusteringConfigBean = new CDKActivityConfigurationBean();
		clusteringConfigBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE, this.dir);
//		clusteringConfigBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_CLUSTERS, 5);
//		clusteringConfigBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_ITERATIONS, 10);
		clusteringConfigBean.setActivityName(WekaClusteringActivity.WEKA_CLUSTERING_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
//		wekaDatasetActivity.configure(configBean);
//		loadActivity.configure(configBean);
//		wekaClusteringActivity.configure(clusteringConfigBean);
//		// load QSAR vectors
//		Map<String, Object> inputs = new HashMap<String, Object>();
//		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
//		expectedOutputTypes.put(loadActivity.getRESULT_PORTS()[0], byte[].class);
//		expectedOutputTypes.put(loadActivity.getRESULT_PORTS()[1], byte[].class);
//		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(loadActivity, inputs, expectedOutputTypes);
//		inputs = outputs;
//		expectedOutputTypes = new HashMap<String, Class<?>>();
//		expectedOutputTypes.put(wekaDatasetActivity.getRESULT_PORTS()[0], byte[].class);
//		outputs = ActivityInvoker.invokeAsyncActivity(wekaDatasetActivity, inputs, expectedOutputTypes);
//		byte[] datasetData = (byte[]) outputs.get(wekaDatasetActivity.getRESULT_PORTS()[0]);
//		Instances dataset = CDKObjectHandler.getInstancesObject(datasetData);
//		inputs = outputs;
//		// Cluster
//		int[][] expectedValues = { { 278, 206, 46, 146, 24 }, { 65, 177, 194, 82, 182 }, { 670, 1, 2, 2, 25 } };
//		for (int j = 0; j < WekaClusteringActivity.clustererNames.length; j++) {
////			clusteringConfigBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_SELECTED_CLUSTERER, j);
//			expectedOutputTypes = new HashMap<String, Class<?>>();
//			expectedOutputTypes.put(wekaClusteringActivity.getRESULT_PORTS()[0], String.class);
//			outputs = ActivityInvoker.invokeAsyncActivity(wekaClusteringActivity, inputs, expectedOutputTypes);
//			List<String> files = (List<String>) outputs.get(wekaClusteringActivity.getRESULT_PORTS()[0]);
//			Assert.assertEquals(3, files.size());
//			// Load clusterer
//			ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(files.get(2))));
//			Clusterer clusterer = (Clusterer) reader.readObject();
//			reader.close();
//			// load data
//			BufferedReader buffReader = new BufferedReader(new FileReader(files.get(0)));
//			dataset = new Instances(buffReader);
//			buffReader.close();
//			// Instances ids = Filter.useFilter(dataset, WekaTools.getIDGetter(dataset));
//			dataset = Filter.useFilter(dataset, WekaTools.getIDRemover(dataset));
//			// Test clusterer
//			int[] numberOfVectorsInClass = new int[clusterer.numberOfClusters()];
//			for (int i = 0; i < dataset.numInstances(); i++) {
//				numberOfVectorsInClass[clusterer.clusterInstance(dataset.instance(i))]++;
//			}
//			Assert.assertEquals(5, clusterer.numberOfClusters());
//			for (int i = 0; i < clusterer.numberOfClusters(); i++) {
//				Assert.assertEquals(expectedValues[j][i], numberOfVectorsInClass[i]);
//			}
//		}
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
		return new TestSuite(WekaClusteringActivityTest.class);
	}

}
