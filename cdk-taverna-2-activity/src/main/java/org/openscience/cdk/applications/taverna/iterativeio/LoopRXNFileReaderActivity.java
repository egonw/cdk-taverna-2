package org.openscience.cdk.applications.taverna.iterativeio;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileReader;
import org.openscience.cdk.io.MDLRXNReader;

public class LoopRXNFileReaderActivity extends AbstractCDKActivity implements IIterativeFileReader {

	public static final String LOOP_RXN_FILE_READER_ACTIVITY = "Loop RXN file Reader";
	public static final String RUNNING = "RUNNING";
	public static final String FINISHED = "FINISHED";

	private List<File> fileList = null;

	public LoopRXNFileReaderActivity() {
		this.RESULT_PORTS = new String[] { "Reactions", "State" };
	}

	@Override
	protected void addInputPorts() {
		// Nothing to add
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
		addOutput(this.RESULT_PORTS[1], 0);
	}

	@Override
	public String getActivityName() {
		return LoopRXNFileReaderActivity.LOOP_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".rxn");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "MDL RXN file");
		properties.put(CDKTavernaConstants.PROPERTY_ITERATIVE_READ_SIZE, 50);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + LoopRXNFileReaderActivity.LOOP_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		int readSize = (Integer) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_ITERATIVE_READ_SIZE);
		String state = RUNNING;
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		// Read RXNfile
		if (this.fileList == null) {
			File[] files = (File[]) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
			if (files == null || files.length == 0) {
				throw new CDKTavernaException(this.getActivityName(), "Error, no file(s) chosen!");
			}
			this.fileList = Arrays.asList(files);
		}
		List<byte[]> dataList = new ArrayList<byte[]>();
		for (int i = 0; i < readSize; i++) {
			try {
				MDLRXNReader reader = new MDLRXNReader(new FileReader(fileList.remove(0)));
				Reaction reaction = (Reaction) reader.read(new Reaction());
				dataList.add(CDKObjectHandler.getBytes(reaction));
				if (fileList.isEmpty()) {
					state = FINISHED;
					comment.add("All done!");
				} else {
					comment.add("Has next iteration!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new CDKTavernaException(this.getActivityName(), "Error reading RXN file!");
			}
		}
		T2Reference containerRef = referenceService.register(dataList, 1, true, context);
		outputs.put(this.RESULT_PORTS[0], containerRef);
		containerRef = referenceService.register(state, 0, true, context);
		outputs.put(this.RESULT_PORTS[1], containerRef);
		// Return results
		return outputs;
	}
}
