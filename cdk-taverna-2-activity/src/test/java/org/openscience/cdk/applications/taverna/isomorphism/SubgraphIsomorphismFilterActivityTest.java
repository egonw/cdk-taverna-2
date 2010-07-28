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
package org.openscience.cdk.applications.taverna.isomorphism;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.CDKTavernaTestData;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.io.MDLReader;

/**
 * Test class for the isomorphism tester activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class SubgraphIsomorphismFilterActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity activity = new SubgraphIsomorphismFilterActivity();

	private File dir = null;

	public SubgraphIsomorphismFilterActivityTest() {
		super(SubgraphIsomorphismFilterActivity.SUBGRAPHISOMORPHISM_FILTER_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(SubgraphIsomorphismFilterActivity.SUBGRAPHISOMORPHISM_FILTER_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		activity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		CMLChemFile[] chemFiles = CDKTavernaTestData.getCMLChemFile();
		CMLChemFile substructure = new CMLChemFile();
		List<byte[]> structures = new ArrayList<byte[]>();
		for (CMLChemFile chemFile : chemFiles) {
			structures.add(CDKObjectHandler.getBytes(chemFile));
		}
		inputs.put(activity.getINPUT_PORTS()[0], structures);
		File testFile = new File("src\\test\\resources\\data\\mol\\substructure.mol");
		MDLReader reader = new MDLReader(new FileReader(testFile));
		reader.read(substructure);
		inputs.put(activity.getINPUT_PORTS()[1], CDKObjectHandler.getBytes(substructure));
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.getRESULT_PORTS()[0], byte[].class);
		expectedOutputTypes.put(activity.getRESULT_PORTS()[1], byte[].class);
		expectedOutputTypes.put(activity.getCOMMENT_PORT(), String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 3, outputs.size());
		List<String> comment = (List<String>) outputs.get(activity.getCOMMENT_PORT());
		for (String c : comment) {
			Assert.assertTrue(!c.toLowerCase().contains("error"));
		}
		List<CMLChemFile> calcStructures = CDKObjectHandler.getChemFileList((List<byte[]>) outputs.get(activity.getRESULT_PORTS()[0]));
		List<CMLChemFile> notCalcStructures = CDKObjectHandler.getChemFileList((List<byte[]>) outputs.get(activity.getRESULT_PORTS()[1]));
		Assert.assertEquals(1, calcStructures.size());
		Assert.assertEquals(9, notCalcStructures.size());
	}

	public void cleanUp() {
	}

	public void executeTest() {
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

}
