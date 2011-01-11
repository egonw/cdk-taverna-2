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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;

/**
 * Test class for the create extract clustering result as CSV activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ExtractClusteringStatisticsActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity wekaActivity = new ExtractClusteringResultAsCSVActivity();
	private ArrayList<String> files = new ArrayList<String>();
	private File dir = null;

	public ExtractClusteringStatisticsActivityTest() {
		super(ExtractClusteringResultAsCSVActivity.EXTRACT_CLUSTERING_RESULT_AS_CSV_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		this.configBean.setActivityName(this.wekaActivity.getActivityName());
		this.dir = new File("." + File.separator + "Test" + File.separator);
		this.dir.mkdir();
		File input = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "data.arff");
		File output = new File(this.dir.getPath() + File.separator + "data.arff");
		FileNameGenerator.copyFile(input, output);
		files.add(output.getPath());
		files.add("");
		input = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "weka" + File.separator + "SimpleKMeans-ID1_test.model");
		output = new File(this.dir.getPath() + File.separator + "SimpleKMeans-ID1_test.model");
		FileNameGenerator.copyFile(input, output);
		files.add(output.getPath());
	}

	public void executeAsynch() throws Exception {
		wekaActivity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put(wekaActivity.getINPUT_PORTS()[0], files);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(wekaActivity, inputs, expectedOutputTypes);
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
		return new TestSuite(ExtractClusteringStatisticsActivityTest.class);
	}

}
