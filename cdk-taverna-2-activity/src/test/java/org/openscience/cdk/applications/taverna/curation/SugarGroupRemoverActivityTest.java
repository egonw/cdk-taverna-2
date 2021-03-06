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
package org.openscience.cdk.applications.taverna.curation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * 
 * @author kalai
 */
public class SugarGroupRemoverActivityTest extends CDKTavernaTestCases {
	private CDKActivityConfigurationBean configBean;
	private AbstractCDKActivity activity = new SugarGroupRemoverActivity();

	public SugarGroupRemoverActivityTest() {
		super(SugarGroupRemoverActivity.REMOVE_SUGAR_GROUPS_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(SugarGroupRemoverActivity.REMOVE_SUGAR_GROUPS_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {

		activity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();

		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		IMolecule mol = sp
				.parseSmiles("CC1CCC2(OC1)OC3C(O)C4C5CCC6CC(OC7OC(CO)C(OC8OC(CO)C(O)C(OC9OCC(O)C(O)C9O)C8OC%10OC(CO)C(O)C(OC%11OC(CO)C(O)C(O)C%11O)C%10O)C(O)C7O)C(O)CC6(C)C5CCC4(C)C3C2C");

		List<CMLChemFile> chemfile = new ArrayList<CMLChemFile>();
		chemfile.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(mol));
		List<byte[]> dataList = CDKObjectHandler.getBytesList(chemfile);

		inputs.put(activity.INPUT_PORTS[0], dataList);

		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(activity.OUTPUT_PORTS[1], byte[].class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);

		List<byte[]> objectDataList = (List<byte[]>) outputs.get(activity.OUTPUT_PORTS[0]);
		List<CMLChemFile> chemFile = CDKObjectHandler.getChemFileList(objectDataList);
		for (CMLChemFile cml : chemFile) {

			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			int count = moleculeList.size();
			Assert.assertEquals(1, count);

		}

		List<byte[]> objectDataList2 = (List<byte[]>) outputs.get(activity.OUTPUT_PORTS[1]);
		List<CMLChemFile> chemFile2 = CDKObjectHandler.getChemFileList(objectDataList2);

		for (CMLChemFile cml : chemFile2) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			int count2 = moleculeList.size();
			Assert.assertEquals(0, count2);

		}

	}

	public void executeTest() {
		try {
			this.makeConfigBean();
			this.executeAsynch();
			// this.cleanUp();
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
		return new TestSuite(SugarGroupRemoverActivityTest.class);

	}

}
