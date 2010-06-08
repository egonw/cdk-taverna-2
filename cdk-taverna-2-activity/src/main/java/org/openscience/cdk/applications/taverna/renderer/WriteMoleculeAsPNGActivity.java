package org.openscience.cdk.applications.taverna.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.Constants;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class WriteMoleculeAsPNGActivity extends AbstractCDKActivity implements IFileWriter {

	private static final String WRITE_MOLECULE_AS_PNG_ACTIVITY = "Write Molecule As PNG";
	public WriteMoleculeAsPNGActivity() {
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
		return WriteMoleculeAsPNGActivity.WRITE_MOLECULE_AS_PNG_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(Constants.PROPERTY_FILE_EXTENSION, ".png");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + WriteMoleculeAsPNGActivity.WRITE_MOLECULE_AS_PNG_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return Constants.RENDERER_FOLDER_NAME;
	}


	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		try {
			List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
					context);
			for (byte[] data : dataArray) {
				Object obj = CDKObjectHandler.getObject(data);
				if (obj instanceof CMLChemFile) {
					chemFileList.add((CMLChemFile) obj);
				} else {
					throw new CDKTavernaException(this.getConfiguration().getActivityName(),
							CDKTavernaException.WRONG_INPUT_PORT_TYPE);
				}
			}
			File directory = (File) this.getConfiguration().getAdditionalProperty(Constants.PROPERTY_FILE);
			String extension = (String) this.getConfiguration().getAdditionalProperty(Constants.PROPERTY_FILE_EXTENSION);
			for (CMLChemFile cmlChemFile : chemFileList) {
				String filename = FileNameGenerator.getNewFile(directory.getPath(), extension);
				IAtomContainer molecule = ChemFileManipulator.getAllAtomContainers(cmlChemFile).get(0);
				BufferedImage image = Draw2DStructure.drawMolecule(molecule, 800, 600);
				ImageIO.write(image, "png", new File(filename));
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Exception handling
		}
		return null;

	}

}
