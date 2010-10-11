package org.openscience.cdk.applications.taverna.qsar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public abstract class AbstractAtomicDescriptor extends AbstractCDKActivity {

	private IAtomicDescriptor descriptor;

	public AbstractAtomicDescriptor() {
		super();
		this.INPUT_PORTS = new String[] { "Structures", "Query Structure" };
		this.RESULT_PORTS = new String[] { "Calculated Structures", "NOT Calculated Structures" };
	}

	@Override
	protected void addInputPorts() {
		for (String name : this.INPUT_PORTS) {
			addInput(name, 1, true, null, byte[].class);
		}
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.RESULT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	public String getActivityName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + this.getClass().getSimpleName();
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_ATOMIC_DESCRIPTOR_FOLDER_NAME;
	}

	protected abstract IAtomicDescriptor getDescriptor();

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
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			inputList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		if (descriptor == null) {
			descriptor = getDescriptor();
			if (descriptor == null) {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(),
						"The descriptor could not be initialized!");
			}
		}
		try {
			for (Iterator<CMLChemFile> iter = inputList.iterator(); iter.hasNext();) {
				CMLChemFile file = iter.next();
				List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(file);
				for (IAtomContainer molecule : moleculeList) {
					try {
						if (molecule.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
							UUID uuid = UUID.randomUUID();
							molecule.setProperty(CDKTavernaConstants.MOLECULEID, uuid);
						}
						for (int j = 0; j < molecule.getAtomCount(); j++) {
							DescriptorValue value = descriptor.calculate(molecule.getAtom(j), molecule);
							molecule.getAtom(j).setProperty(value.getSpecification(), value);
							// molecules.setProperty(value.getSpecification(),
							// value);
						}
						calculatedList.add(file);
					} catch (Exception e) {
						e.printStackTrace();
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
			dataArray = new ArrayList<byte[]>();
			if (!calculatedList.isEmpty()) {
				for (CMLChemFile c : calculatedList) {
					dataArray.add(CDKObjectHandler.getBytes(c));
				}
			}
			T2Reference containerRef = referenceService.register(dataArray, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			dataArray = new ArrayList<byte[]>();
			if (!notCalculatedList.isEmpty()) {
				for (CMLChemFile c : notCalculatedList) {
					dataArray.add(CDKObjectHandler.getBytes(c));
				}
			}
			containerRef = referenceService.register(dataArray, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO exception handling
		}
		return outputs;
	}

}
