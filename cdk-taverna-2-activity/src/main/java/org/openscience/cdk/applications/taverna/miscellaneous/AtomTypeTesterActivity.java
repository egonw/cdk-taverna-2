package org.openscience.cdk.applications.taverna.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

public class AtomTypeTesterActivity extends AbstractCDKActivity {

	public static final String ATOMTYPE_TESTER_ACTIVITY = "Atom Type Tester";

	public AtomTypeTesterActivity() {
		this.INPUT_PORTS = new String[] { "Structures", };
		this.RESULT_PORTS = new String[] { "Typed Structures", "NOT Typed Structures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.RESULT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	public String getActivityName() {
		return AtomTypeTesterActivity.ATOMTYPE_TESTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + AtomTypeTesterActivity.ATOMTYPE_TESTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.MISCELLANEOUS_FOLDER_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		List<CMLChemFile> typedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> notTypedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		IAtomContainer[] containers;
		try {
			containers = CMLChemFileWrapper.convertCMLChemFileListToAtomContainerArray(chemFileList);
		} catch (Exception exception) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), exception.getMessage());
		}
		for (IAtomContainer cont : containers) {
			IAtom tmpAtom = null;
			try {
				CDKAtomTypeMatcher tmpMatcher = CDKAtomTypeMatcher.getInstance(cont.getBuilder());
				for (int i = 0; i < cont.getAtomCount(); i++) {
					tmpAtom = cont.getAtom(i);
					if (tmpAtom.getAtomTypeName() == null) {
						IAtomType tmpType = tmpMatcher.findMatchingAtomType(cont, tmpAtom);
						AtomTypeManipulator.configure(tmpAtom, tmpType);
					}
				}
			} catch (Exception e) {
				comment.add("Atom with element " + tmpAtom.getSymbol() + " could not be typed!");
				notTypedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(cont));
				e.printStackTrace();
			}
			typedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(cont));
		}
		comment.add("Calculation done;");
		// Congfigure output
		try {
			T2Reference containerRef = referenceService.register(CDKObjectHandler.getBytesList(typedList), 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			containerRef = referenceService.register(CDKObjectHandler.getBytesList(notTypedList), 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO exception handling
		}
		return outputs;
	}
}
