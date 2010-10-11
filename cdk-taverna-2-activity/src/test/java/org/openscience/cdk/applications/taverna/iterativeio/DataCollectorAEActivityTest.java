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
package org.openscience.cdk.applications.taverna.iterativeio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.openscience.cdk.applications.taverna.isomorphism.SubgraphIsomorphismFilterActivity;

/**
 * Test class for the data collector acceptor/emitter activities.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class DataCollectorAEActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBeanAcceptor;
	private CDKActivityConfigurationBean configBeanEmitter;

	private AbstractCDKActivity acceptor = new DataCollectorAcceptorActivity();
	private AbstractCDKActivity emitter = new DataCollectorEmitterActivity();

	public DataCollectorAEActivityTest() {
		super("Data collector acceptor/emitter activities");
	}

	public void makeConfigBean() throws Exception {
		this.configBeanAcceptor = new CDKActivityConfigurationBean();
		this.configBeanAcceptor.setActivityName(DataCollectorAcceptorActivity.DATA_COLLECTOR_ACCEPTOR_ACTIVITY);
		this.configBeanEmitter = new CDKActivityConfigurationBean();
		this.configBeanEmitter.setActivityName(DataCollectorEmitterActivity.DATA_COLLECTOR_EMITTER_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		this.acceptor.configure(this.configBeanAcceptor);
		this.emitter.configure(configBeanEmitter);
		// Execute acceptor
		CMLChemFile[] chemFiles = CDKTavernaTestData.getCMLChemFile();
		List<byte[]> structures = new ArrayList<byte[]>();
		for (CMLChemFile chemFile : chemFiles) {
			structures.add(CDKObjectHandler.getBytes(chemFile));
		}
		UUID uuid = UUID.randomUUID();
		for (byte[] data : structures) {
			Map<String, Object> inputs = new HashMap<String, Object>();
			List<byte[]> dataList = new ArrayList<byte[]>();
			dataList.add(data);
			inputs.put(this.acceptor.getINPUT_PORTS()[0], dataList);
			inputs.put(this.acceptor.getINPUT_PORTS()[1], uuid.toString());
			Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
			expectedOutputTypes.put(this.acceptor.getCOMMENT_PORT(), String.class);
			Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(this.acceptor, inputs, expectedOutputTypes);
			Assert.assertEquals("Unexpected outputs", 1, outputs.size());
			List<String> comment = (List<String>) outputs.get(this.acceptor.getCOMMENT_PORT());
			for (String c : comment) {
				Assert.assertTrue(!c.toLowerCase().contains("error"));
			}
		}
		// Execute emitter
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put(this.emitter.getINPUT_PORTS()[0], uuid.toString());
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(this.emitter.getRESULT_PORTS()[0], byte[].class);
		expectedOutputTypes.put(this.emitter.getCOMMENT_PORT(), String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(this.emitter, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());
		List<byte[]> objectData = (List<byte[]>) outputs.get(this.emitter.getRESULT_PORTS()[0]);
		Assert.assertEquals(10, objectData.size());
		List<String> comment = (List<String>) outputs.get(this.emitter.getCOMMENT_PORT());
		for (String c : comment) {
			Assert.assertTrue(!c.toLowerCase().contains("error"));
		}
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
	
	/**
	 * Method which returns a test suit with the name of this class
	 * 
	 * @return TestSuite
	 */
	public static Test suite() {
		return new TestSuite(DataCollectorAEActivityTest.class);
	}

}
