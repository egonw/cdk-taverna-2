/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.cdk.applications.taverna.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.Atom;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.miscellaneous.RemoveHydrogensActivity;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 *
 * @author kalai
 */
public class RemoveHydrogensActivityTest extends CDKTavernaTestCases {

    private CDKActivityConfigurationBean configBean;
    private AbstractCDKActivity activity = new RemoveHydrogensActivity();

    public RemoveHydrogensActivityTest() {
        super(RemoveHydrogensActivity.REMOVE_HYDROGENS_ACTIVITY);
    }

    public void makeConfigBean() throws Exception {
        configBean = new CDKActivityConfigurationBean();
        configBean.setActivityName(RemoveHydrogensActivity.REMOVE_HYDROGENS_ACTIVITY);
    }

    @SuppressWarnings("unchecked")
    public void executeAsynch() throws Exception {
        activity.configure(configBean);
        Map<String, Object> inputs = new HashMap<String, Object>();
        String smiles = "[1H][12C]2([12C][16O][12C][12C]2([1H])([12C]([16O])[12C]=1[12C]=[12C][12C]([16O])=[12C]([12C]=1)[16O][12C]))([12C][12C]3=[12C][12C]=[12C]([16O])[12C](=[12C]3)[16O][12C])";
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer input_molecule = sp.parseSmiles(smiles);
        List<byte[]> data = new ArrayList<byte[]>();
        data.add(CDKObjectHandler.getBytes(CMLChemFileWrapper.wrapAtomContainerInChemModel(input_molecule)));
        inputs.put(activity.INPUT_PORTS[0], data);
        Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
        expectedOutputTypes.put(activity.OUTPUT_PORTS[0], byte[].class);
        Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
        Assert.assertEquals("Unexpected outputs", 1, outputs.size());
        List<byte[]> objectData = (List<byte[]>) outputs.get(activity.OUTPUT_PORTS[0]);
        List<CMLChemFile> resultChemFiles = CDKObjectHandler.getChemFileList(objectData);
        for (int i = 0; i < resultChemFiles.size(); i++) {
            IAtomContainer resultContainer = ChemFileManipulator.getAllAtomContainers(resultChemFiles.get(i)).get(0);
            boolean Has_hydrogen = resultContainer.contains(new Atom("H"));
            assertEquals(true, Has_hydrogen == false);
        }
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
        return new TestSuite(RemoveHydrogensActivityTest.class);
    }
}
