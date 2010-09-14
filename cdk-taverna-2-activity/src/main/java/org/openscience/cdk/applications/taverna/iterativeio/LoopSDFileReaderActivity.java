package org.openscience.cdk.applications.taverna.iterativeio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
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
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileReader;
import org.openscience.cdk.io.MDLV2000Reader;

public class LoopSDFileReaderActivity extends AbstractCDKActivity implements IIterativeFileReader {

	public static final String LOOP_SD_FILE_READER_ACTIVITY = "Loop SDfile Reader";
	public static final String RUNNING = "RUNNING";
	public static final String FINISHED = "FINISHED";

	private LineNumberReader lineReader = null;

	public LoopSDFileReaderActivity() {
		this.RESULT_PORTS = new String[] { "Structures", "State" };
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
		return LoopSDFileReaderActivity.LOOP_SD_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".sdf");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "MDL SDFile");
		properties.put(CDKTavernaConstants.PROPERTY_ITERATIVE_READ_SIZE, 50);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + LoopSDFileReaderActivity.LOOP_SD_FILE_READER_ACTIVITY;
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
		List<CMLChemFile> cmlChemFileList = null;
		// Read SDfile
		File file = ((File[]) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE))[0];
		if (file == null) {
			throw new CDKTavernaException(this.getActivityName(), "Error, no file chosen!");
		}
		// clear comments
		comment.clear();
		try {
			if (this.lineReader == null) {
				this.lineReader = new LineNumberReader(new FileReader(file));
			}
			String line;
			String SDFilePart = "";
			int counter = 0;
			List<byte[]> dataList = new ArrayList<byte[]>();
			do {
				line = lineReader.readLine();
				if (line != null) {
					SDFilePart += line + "\n";
					if (line.contains("$$$$")) {
						counter++;
					}
				}
				if (line == null || counter >= readSize) {
					try {
						CMLChemFile cmlChemFile = new CMLChemFile();
						MDLV2000Reader tmpMDLReader = new MDLV2000Reader(new ByteArrayInputStream(SDFilePart.getBytes()));
						tmpMDLReader.read(cmlChemFile);
						cmlChemFileList = CMLChemFileWrapper.wrapInChemModelList(cmlChemFile);
						// Congfigure output
						for (CMLChemFile c : cmlChemFileList) {
							dataList.add(CDKObjectHandler.getBytes(c));
						}
						if (line == null) {
							this.lineReader = null;
							state = FINISHED;
							comment.add("All done!");
						} else {
							comment.add("Has next iteration!");
						}
						SDFilePart = "";
					} catch (Exception e) {
						System.out.println(SDFilePart);
						e.printStackTrace();
					}
				}
			} while (line != null && counter < readSize);
			T2Reference containerRef = referenceService.register(dataList, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			containerRef = referenceService.register(state, 0, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CDKTavernaException(this.getActivityName(), "Error reading SDF file!");
		}
		// Return results
		return outputs;
	}
}
