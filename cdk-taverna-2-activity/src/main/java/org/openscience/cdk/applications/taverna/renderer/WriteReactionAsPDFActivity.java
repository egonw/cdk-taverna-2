package org.openscience.cdk.applications.taverna.renderer;

import java.io.File;
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
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.interfaces.IReaction;

public class WriteReactionAsPDFActivity extends AbstractCDKActivity implements IFileWriter {

	private static final String WRITE_REACTION_AS_PDF_ACTIVITY = "Write Reaction As PDF";

	public WriteReactionAsPDFActivity() {
		this.INPUT_PORTS = new String[] { "Reactions" };
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
		return WriteReactionAsPDFActivity.WRITE_REACTION_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".pdf");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + WriteReactionAsPDFActivity.WRITE_REACTION_AS_PDF_ACTIVITY;
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
		List<IReaction> reactionList = new ArrayList<IReaction>();
		try {
			List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]),
					byte[].class, context);
			try {
				reactionList = CDKObjectHandler.getReactionList(dataArray);
			} catch (Exception e) {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
			}
			File directory = (File) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
			String extension = (String) this.getConfiguration()
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
			try {
				File file = FileNameGenerator.getNewFile(directory.getPath(), extension);
				DrawPDF.drawReactionAsPDF(reactionList, file);
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
