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

public class IterativeMultiRXNFileReaderActivity extends AbstractCDKActivity implements IIterativeFileReader {

	public static final String ITERATIVE_MULTI_RXN_FILE_READER_ACTIVITY = "Iterative Multi RXN File Reader";

	public IterativeMultiRXNFileReaderActivity() {
		this.RESULT_PORTS = new String[] { "Reactions" };
	}

	@Override
	protected void addInputPorts() {
		// Nothing to add
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1, 0);
	}

	@Override
	public String getActivityName() {
		return IterativeMultiRXNFileReaderActivity.ITERATIVE_MULTI_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".rxn");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "MDL Multi RXN File");
		properties.put(CDKTavernaConstants.PROPERTY_ITERATIVE_READ_SIZE, 50);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + IterativeMultiRXNFileReaderActivity.ITERATIVE_MULTI_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		int readSize = (Integer) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_ITERATIVE_READ_SIZE);
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<CMLChemFile> cmlChemFileList = null;
		// Read SDfile
		File file = (File) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (file == null) {
			throw new CDKTavernaException(this.getActivityName(), "Error, no file chosen!");
		}
		List<T2Reference> outputList = new ArrayList<T2Reference>();
		try {
			LineNumberReader lineReader = new LineNumberReader(new FileReader(file));
			String line;
			String RXNFilePart = "";
			int counter = 0;
			int index = 0;
			do {
				line = lineReader.readLine();
				if (line != null) {
					RXNFilePart += line + "\n";
					if (line.contains("$$$$")) {
						counter++;
					}
				}
				if (line == null || counter >= readSize) {
					List<byte[]> dataList = new ArrayList<byte[]>();
					CMLChemFile cmlChemFile = new CMLChemFile();
					MDLV2000Reader tmpMDLReader = new MDLV2000Reader(new ByteArrayInputStream(RXNFilePart.getBytes()));
					tmpMDLReader.read(cmlChemFile);
					cmlChemFileList = CMLChemFileWrapper.wrapInChemModelList(cmlChemFile);
					// Congfigure output
					for (CMLChemFile c : cmlChemFileList) {
						dataList.add(CDKObjectHandler.getBytes(c));
					}
					T2Reference containerRef = referenceService.register(dataList, 1, true, context);
					outputList.add(index, containerRef);
					outputs.put(this.RESULT_PORTS[0], containerRef);
					callback.receiveResult(outputs, new int[] { index });
					index++;
					counter = 0;
					RXNFilePart = "";
				}
			} while (line != null);
			T2Reference containerRef = referenceService.register(outputList, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getActivityName(), "Error reading RXN file!");
		}
		comment.add("done");
		// Return results
		return outputs;
	}

}