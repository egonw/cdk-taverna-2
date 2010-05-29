package org.openscience.cdk.applications.taverna.qsar;

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
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.Constants;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public abstract class AbstractMolecularDescriptor extends AbstractCDKActivity {

	private static final String INPUT_PORT = "Structures";
	private static final String[] RESULT_PORTS = { "Calculated Structures", "NOT Calculated Structures" };
	private IMolecularDescriptor descriptor;

	@Override
	protected void addInputPorts() {
		addInput(AbstractMolecularDescriptor.INPUT_PORT, 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : AbstractMolecularDescriptor.RESULT_PORTS) {
			addOutput(name, 0);
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
		return Constants.QSAR_MOLECULAR_DESCRIPTOR_FOLDER_NAME;
	}
	
	@Override
	protected Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		List<CMLChemFile> inputList = null;
		List<CMLChemFile> calculatedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> notCalculatedList = new ArrayList<CMLChemFile>();
		List<String> comment = new ArrayList<String>();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(INPUT_PORT), byte[].class, context);
		for (byte[] data : dataArray) {
			Object obj;
			try {
				obj = CDKObjectHandler.getObject(data);
			} catch (Exception e) {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(), "Error while deserializing object");
			}
			if (obj instanceof CMLChemFile) {
				inputList.add((CMLChemFile) obj);
			} else {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(),
						CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
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
				IAtomContainer molecules;
				for (Iterator<IAtomContainer> iterator = moleculeList.iterator(); iterator.hasNext();){
					molecules= iterator.next();
					try {
						DescriptorValue value = descriptor
						.calculate(molecules);
						molecules.setProperty(value.getSpecification(),
								value);
						calculatedList.add(file);
						//comment.add("Calculation done;");
					} catch (Exception e) {
						notCalculatedList.add(file);
						// TODO exception handling					
					}
				}
			}
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
			outputs.put(AbstractMolecularDescriptor.RESULT_PORTS[0], containerRef);
			dataArray = new ArrayList<byte[]>();
			if (!notCalculatedList.isEmpty()) {
				for (CMLChemFile c : notCalculatedList) {
					dataArray.add(CDKObjectHandler.getBytes(c));
				}
			}
			containerRef = referenceService.register(dataArray, 1, true, context);
			outputs.put(AbstractMolecularDescriptor.RESULT_PORTS[1], containerRef);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO exception handling
		}
		return outputs;
	}

	protected abstract IMolecularDescriptor getDescriptor();
}
