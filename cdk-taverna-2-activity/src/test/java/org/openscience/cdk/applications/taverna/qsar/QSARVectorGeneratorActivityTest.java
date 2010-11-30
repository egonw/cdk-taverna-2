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
package org.openscience.cdk.applications.taverna.qsar;

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
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.CDKTavernaTestData;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.qsar.descriptors.atomic.AtomDegree;
import org.openscience.cdk.applications.taverna.qsar.descriptors.atompair.PiContactDetection;
import org.openscience.cdk.applications.taverna.qsar.descriptors.bond.AtomicNumberDifference;
import org.openscience.cdk.applications.taverna.qsar.descriptors.molecular.RuleOfFive;
import org.openscience.cdk.applications.taverna.qsar.descriptors.protein.TaeAminoAcid;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Test class for the QSAAR descriptor activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class QSARVectorGeneratorActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity descriptorActivity = new QSARDescriptorActivity();
	private AbstractCDKActivity generatorActivity = new QSARVectorGeneratorActivity();

	public QSARVectorGeneratorActivityTest() {
		super(QSARDescriptorActivity.QSAR_DESCRIPTOR_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		ArrayList<Class<? extends AbstractCDKActivity>> selectedClasses = new ArrayList<Class<? extends AbstractCDKActivity>>();
		selectedClasses.add(AtomDegree.class);
		selectedClasses.add(PiContactDetection.class);
		selectedClasses.add(AtomicNumberDifference.class);
		selectedClasses.add(RuleOfFive.class);
		selectedClasses.add(TaeAminoAcid.class);
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_CHOSEN_QSARDESCRIPTORS, selectedClasses);
		configBean.setActivityName(QSARDescriptorActivity.QSAR_DESCRIPTOR_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		descriptorActivity.configure(configBean);
		generatorActivity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		CMLChemFile[] chemFiles = CDKTavernaTestData.getCMLChemFile();
		for (CMLChemFile chemFile : chemFiles) {
			IAtomContainer container = chemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
			UUID uuid = UUID.randomUUID();
			container.setProperty(CDKTavernaConstants.MOLECULEID, uuid);
		}
		List<byte[]> data = CDKObjectHandler.getBytesList(chemFiles);
		inputs.put(descriptorActivity.getINPUT_PORTS()[0], data);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(descriptorActivity.getRESULT_PORTS()[0], byte[].class);
		expectedOutputTypes.put(descriptorActivity.getRESULT_PORTS()[1], String.class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(descriptorActivity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());
		inputs.put(generatorActivity.getINPUT_PORTS()[0], outputs.get(descriptorActivity.getRESULT_PORTS()[0]));
		expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(generatorActivity.getRESULT_PORTS()[0], byte[].class);
		expectedOutputTypes.put(generatorActivity.getRESULT_PORTS()[1], byte[].class);
		outputs = ActivityInvoker.invokeAsyncActivity(generatorActivity, inputs, expectedOutputTypes);
		byte[] vectorData = (byte[]) outputs.get(generatorActivity.getRESULT_PORTS()[0]);
		Map<UUID, Map<String, Object>> resultVector = (Map<UUID, Map<String, Object>>) CDKObjectHandler.getObject(vectorData);
		Assert.assertEquals(10, resultVector.size());
		byte[] nameData = (byte[]) outputs.get(generatorActivity.getRESULT_PORTS()[1]);
		ArrayList<String> resultNames = (ArrayList<String>) CDKObjectHandler.getObject(nameData);
		Assert.assertEquals(177, resultNames.size());
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
		return new TestSuite(QSARVectorGeneratorActivityTest.class);
	}

}
