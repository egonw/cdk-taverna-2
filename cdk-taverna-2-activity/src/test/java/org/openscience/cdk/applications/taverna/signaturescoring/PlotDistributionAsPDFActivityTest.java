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
package org.openscience.cdk.applications.taverna.signaturescoring;

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
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.setup.SetupController;

/**
 * Test class for the AttributeEvaluation activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class PlotDistributionAsPDFActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;
	private File dir = null;
	private AbstractCDKActivity setActivity = new PlotDistributionAsPDFActivity();

	public PlotDistributionAsPDFActivityTest() {
		super(PlotDistributionAsPDFActivity.PLOT_DISTRIBUTION_AS_PDF_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		this.dir = new File(SetupController.getInstance().getWorkingDir());
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(PlotDistributionAsPDFActivity.PLOT_DISTRIBUTION_AS_PDF_ACTIVITY);
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		this.setActivity.configure(this.configBean);
		// load regression data
		File fileCSV = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "scoringTestFiles" + File.separator + "scores.csv");
		// Setup input
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put(this.setActivity.INPUT_PORTS[0], Collections.singletonList(fileCSV.getPath()));
		inputs.put(this.setActivity.INPUT_PORTS[1], this.dir.getPath());
		expectedOutputTypes.put(this.setActivity.OUTPUT_PORTS[0], String.class);
		Map<String, Object> outputs = ActivityInvoker
				.invokeAsyncActivity(this.setActivity, inputs, expectedOutputTypes);
		List<String> resultFiles = (List<String>) outputs.get(this.setActivity.OUTPUT_PORTS[0]);
		Assert.assertEquals(1, resultFiles.size());
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
		return new TestSuite(PlotDistributionAsPDFActivityTest.class);
	}

}
