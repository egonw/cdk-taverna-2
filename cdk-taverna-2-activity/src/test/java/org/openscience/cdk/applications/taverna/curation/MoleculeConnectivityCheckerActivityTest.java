/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.cdk.applications.taverna.curation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * 
 * @author kalai
 */
public class MoleculeConnectivityCheckerActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;
	private AbstractCDKActivity activity = new MoleculeConnectivityCheckerActivity();

	public MoleculeConnectivityCheckerActivityTest() {
		super(MoleculeConnectivityCheckerActivity.CONNECTIVITY_CHECKER_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF, 6);
		configBean.setActivityName(MoleculeConnectivityCheckerActivity.CONNECTIVITY_CHECKER_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		activity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		CMLChemFile[] cmlFiles = CDKTavernaTestData.getconnectivityCheckSample();

		List<byte[]> dataList = CDKObjectHandler.getBytesList(cmlFiles);

		inputs.put(activity.getINPUT_PORTS()[0], dataList);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.getRESULT_PORTS()[0], byte[].class);
		expectedOutputTypes.put(activity.getRESULT_PORTS()[1], byte[].class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());

		List<byte[]> objectDataList = (List<byte[]>) outputs.get(activity.getRESULT_PORTS()[1]);
		List<CMLChemFile> chemFile = CDKObjectHandler.getChemFileList(objectDataList);
		Assert.assertEquals(2, chemFile.size());
		objectDataList = (List<byte[]>) outputs.get(activity.getRESULT_PORTS()[0]);
		chemFile = CDKObjectHandler.getChemFileList(objectDataList);
		Assert.assertEquals(1, chemFile.size());

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
		return new TestSuite(MoleculeConnectivityCheckerActivityTest.class);

	}
}
