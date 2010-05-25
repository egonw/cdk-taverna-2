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
package org.openscience.cdk.applications.taverna;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.io.MDLMolFileReaderActivity;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Test class for the MDL Mol file reader activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class MDLMolFileReaderActivityTest {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity activity = new MDLMolFileReaderActivity();

	@Before
	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		// TODO read resource
		File molTestFile = new File("src\\test\\resources\\data\\mol\\molfile.mol");
		configBean.addAdditionalProperty(Constants.PROPERTY_FILE, molTestFile);
		configBean.setActivityName(MDLMolFileReaderActivity.MOL_FILE_READER_ACTIVITY);
	}

	@Test
	public void executeAsynch() throws Exception {
		activity.configure(configBean);
		// leave empty. No ports used
		Map<String, Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(MDLMolFileReaderActivity.RESULT_PORT, byte[].class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 1, outputs.size());
		byte[] objectData = (byte[]) outputs.get(MDLMolFileReaderActivity.RESULT_PORT);
		CMLChemFile chemFile = (CMLChemFile) CDKObjectHandler.getObject(objectData);
		IAtomContainer container = chemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
		Assert.assertEquals(16, container.getAtomCount());
		Assert.assertEquals(17, container.getBondCount());
	}

}
