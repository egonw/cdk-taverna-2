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
package org.openscience.cdk.applications.taverna.filter;

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
import org.openscience.cdk.applications.taverna.CDKTavernaTestData;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;

/**
 * Test class for the doublets filter activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class DoubletsFilterActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity activity = new DoubletsFilterActivity();

	public DoubletsFilterActivityTest() {
		super(DoubletsFilterActivity.DOUBLETS_FILTER_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(DoubletsFilterActivity.DOUBLETS_FILTER_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		activity.configure(configBean);
		// leave empty. No ports used
		Map<String, Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.getRESULT_PORTS()[0], byte[].class);
		expectedOutputTypes.put(activity.getCOMMENT_PORT(), String.class);
		CMLChemFile[] doublets = new CMLChemFile[3]; 
		for(int i = 0; i < doublets.length; i++) {
			doublets[i] = (CMLChemFile)CDKTavernaTestData.getCMLChemFile()[0].clone();
		}
		inputs.put(activity.getINPUT_PORTS()[0], CDKObjectHandler.getBytesList(doublets));
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());
		List<byte[]> objectData = (List<byte[]>) outputs.get(activity.getRESULT_PORTS()[0]);
		Assert.assertEquals(1, objectData.size());
		List<String> comment = (List<String>) outputs.get(activity.getCOMMENT_PORT());
		for (String c : comment) {
			Assert.assertTrue(!c.toLowerCase().contains("error"));
		}
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
		return new TestSuite(DoubletsFilterActivityTest.class);
	}
}
