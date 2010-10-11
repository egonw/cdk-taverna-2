package org.openscience.cdk.applications.taverna.renderer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class WriteMoleculeAsPDFActivity extends AbstractCDKActivity implements IFileWriter {

	private static final String WRITE_MOLECULE_AS_PDF_ACTIVITY = "Write Molecule As PDF";

	public WriteMoleculeAsPDFActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
	}

	@Override
	protected void addInputPorts() {
		this.addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		// Nothing to add
	}

	@Override
	public String getActivityName() {
		return WriteMoleculeAsPDFActivity.WRITE_MOLECULE_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".pdf");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + WriteMoleculeAsPDFActivity.WRITE_MOLECULE_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.RENDERER_FOLDER_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		try {
			List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]),
					byte[].class, context);
			try {
				chemFileList = CDKObjectHandler.getChemFileList(dataArray);
			} catch (Exception e) {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
			}
			File directory = (File) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
			String extension = (String) this.getConfiguration()
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
			LinkedList<IAtomContainer> containerList = new LinkedList<IAtomContainer>();
			for (CMLChemFile cmlChemFile : chemFileList) {
				containerList.addAll(ChemFileManipulator.getAllAtomContainers(cmlChemFile));
			}
			try {
				File file = FileNameGenerator.getNewFile(directory.getPath(), extension, this.iteration);
				DrawPDF.drawMoleculesAsPDF(containerList, file);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO Exception handling
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Exception handling
		}
		return null;

	}

}
