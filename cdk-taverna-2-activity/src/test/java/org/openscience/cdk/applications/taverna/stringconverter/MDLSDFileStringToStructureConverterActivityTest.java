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
package org.openscience.cdk.applications.taverna.stringconverter;

import java.io.StringWriter;
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
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.CDKTavernaTestData;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.io.SDFWriter;

/**
 * Test class for the SDFile String to structure converter activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class MDLSDFileStringToStructureConverterActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity activity = new MDLSDFileStringToStructureConverterActivity();

	public MDLSDFileStringToStructureConverterActivityTest() {
		super(MDLSDFileStringToStructureConverterActivity.MDL_SDFILE_STRING_CONVERTER_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(MDLSDFileStringToStructureConverterActivity.MDL_SDFILE_STRING_CONVERTER_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		activity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		CMLChemFile[] cmlFiles = CDKTavernaTestData.getCMLChemFile();
		List<String> strings = new ArrayList<String>();
		StringWriter stringWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(stringWriter);
		for (int i = 0; i < cmlFiles.length; i++) {
			writer.write(cmlFiles[i]);
		}
		writer.close();
		strings.add(stringWriter.toString());
		inputs.put(activity.getINPUT_PORTS()[0], strings);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.getRESULT_PORTS()[0], byte[].class);
		expectedOutputTypes.put(activity.getCOMMENT_PORT(), String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());
		List<byte[]> structuresData = (List<byte[]>) outputs.get(activity.getRESULT_PORTS()[0]);
		List<CMLChemFile> structures = CDKObjectHandler.getChemFileList(structuresData);
		Assert.assertEquals(10, structures.size());
		List<String> comment = (List<String>) outputs.get(activity.getCOMMENT_PORT());
		for (String c : comment) {
			Assert.assertTrue(!c.toLowerCase().contains("error"));
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
		return new TestSuite(MDLSDFileStringToStructureConverterActivityTest.class);
	}
}
