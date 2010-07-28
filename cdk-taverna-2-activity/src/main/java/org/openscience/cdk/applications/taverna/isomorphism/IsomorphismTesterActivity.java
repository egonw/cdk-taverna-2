package org.openscience.cdk.applications.taverna.isomorphism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class IsomorphismTesterActivity extends AbstractCDKActivity {

	public static final String ISOMORPHISM_TESTER_ACTIVITY = "Isomorphism Tester";

	public IsomorphismTesterActivity() {
		this.INPUT_PORTS = new String[] { "Structures", "Query Structure" };
		this.RESULT_PORTS = new String[] { "Calculated Structures", "NOT Calculated Structures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.RESULT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	public String getActivityName() {
		return IsomorphismTesterActivity.ISOMORPHISM_TESTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + IsomorphismTesterActivity.ISOMORPHISM_TESTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ISOMORPHISM_FOLDER_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		List<CMLChemFile> inputList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> calculatedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> notCalculatedList = new ArrayList<CMLChemFile>();
		CMLChemFile queryChemFile = null;
		IAtomContainer queryMolecule = null;
		List<byte[]> dataInputOne = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]),
				byte[].class, context);
		byte[] dataInputTwo = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]), byte[].class, context);
		try {
			inputList = CDKObjectHandler.getChemFileList(dataInputOne);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		Object obj;
		try {
			obj = CDKObjectHandler.getObject(dataInputTwo);
		} catch (Exception e1) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
		}
		if (obj instanceof CMLChemFile) {
			queryChemFile = (CMLChemFile) obj;
		} else {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
		}
		try {
			queryMolecule = CMLChemFileWrapper.wrapChemModelInAtomContainer(queryChemFile);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		try {
			for (Iterator<CMLChemFile> iter = inputList.iterator(); iter.hasNext();) {
				CMLChemFile file = iter.next();
				List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(file);
				for (IAtomContainer molecule : moleculeList) {
					try {
						if (UniversalIsomorphismTester.isIsomorph(molecule, queryMolecule)) {
							calculatedList.add(file);
						} else {
							notCalculatedList.add(file);
						}
					} catch (Exception e) {
						notCalculatedList.add(file);
						// TODO exception handling
					}
				}
			}
			comment.add("Calculation done;");
		} catch (Exception exception) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), exception.getMessage());
		}
		// Congfigure output
		try {
			dataInputOne = new ArrayList<byte[]>();
			if (!calculatedList.isEmpty()) {
				for (CMLChemFile c : calculatedList) {
					dataInputOne.add(CDKObjectHandler.getBytes(c));
				}
			}
			T2Reference containerRef = referenceService.register(dataInputOne, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			dataInputOne = new ArrayList<byte[]>();
			if (!notCalculatedList.isEmpty()) {
				for (CMLChemFile c : notCalculatedList) {
					dataInputOne.add(CDKObjectHandler.getBytes(c));
				}
			}
			containerRef = referenceService.register(dataInputOne, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO exception handling
		}
		return outputs;
	}

}
