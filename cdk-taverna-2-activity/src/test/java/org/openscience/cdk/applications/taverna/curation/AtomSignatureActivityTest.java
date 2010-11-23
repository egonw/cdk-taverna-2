/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.cdk.applications.taverna.curation;

import java.lang.String;
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
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * 
 * @author kalai
 */
public class AtomSignatureActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;
	private AbstractCDKActivity activity = new AtomSignatureActivity();

	public AtomSignatureActivityTest() {
		super(AtomSignatureActivity.ATOM_SIGNATURE_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(AtomSignatureActivity.ATOM_SIGNATURE_ACTIVITY);
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT, new Integer(2));
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		activity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		CMLChemFile[] cmlFiles = CDKTavernaTestData.getatomSignatureSample();
		HashMap<Object, Object> properties = new HashMap<Object, Object>();
		List<CMLChemFile> cmlInputList = new ArrayList<CMLChemFile>();
		UUID uuid = UUID.randomUUID();
		properties.put(CDKTavernaConstants.MOLECULEID, uuid);
		for (CMLChemFile cml : cmlFiles) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {
				atomContainer.setProperties(properties);
				cmlInputList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(atomContainer));
			}
		}
		List<byte[]> dataList = CDKObjectHandler.getBytesList(cmlInputList);
		inputs.put(activity.getINPUT_PORTS()[0], dataList);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.getRESULT_PORTS()[0], String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 1, outputs.size());
		ArrayList<String> signatures = (ArrayList<String>) outputs.get(activity.getRESULT_PORTS()[0]);
		assertEquals(23, signatures.size());
	}

	public void executeTest() {
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
		return new TestSuite(AtomSignatureActivityTest.class);
	}
}
