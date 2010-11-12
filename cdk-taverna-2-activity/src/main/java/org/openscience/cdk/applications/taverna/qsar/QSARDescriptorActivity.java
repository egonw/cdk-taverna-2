package org.openscience.cdk.applications.taverna.qsar;

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
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

public class QSARDescriptorActivity extends AbstractCDKActivity {

	public static final String QSAR_DESCRIPTOR_ACTIVITY = "QSAR Descriptor";

	private QSARDescriptorWorker[] workers = null;
	private AsynchronousActivityCallback callback = null;
	private int numberOfCompletedWorkers = 0;
	private ArrayList<byte[]> resultData = null;
	
	public QSARDescriptorActivity() {
		super();
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "Calculated Structures" };
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
		return QSARDescriptorActivity.QSAR_DESCRIPTOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback) throws Exception {
		this.callback = callback;
		InvocationContext context = this.callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		this.numberOfCompletedWorkers = 0;
		this.resultData = new ArrayList<byte[]>();
		ArrayList<Class<? extends AbstractCDKActivity>> classes = (ArrayList<Class<? extends AbstractCDKActivity>>) this
				.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_CHOSEN_QSARDESCRIPTORS);
		if (classes == null || classes.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), "No QSAR descriptors chosen!");
		}
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.getINPUT_PORTS()[0]),
				byte[].class, context);
		// Prepare worker
		int numberOfCores = Runtime.getRuntime().availableProcessors();
		if(dataArray.size() < numberOfCores) {
			numberOfCores = dataArray.size();
		}
		this.workers = new QSARDescriptorWorker[numberOfCores];
		int dataSizePerThread = dataArray.size() / numberOfCores;
		for(int i = 0; i < numberOfCores; i++) {
			List<byte[]> tempData = null;
			if(i < numberOfCores - 1) {
				tempData = dataArray.subList(i * dataSizePerThread, (i + 1) * dataSizePerThread);
			} else {
				tempData = dataArray.subList(i * dataSizePerThread, dataArray.size());
			}
			this.workers[i] = new QSARDescriptorWorker(this, classes, tempData);
			this.workers[i].start();
		}

		// Wait for workers
		synchronized (this) {
			this.wait();
		}
		// Congfigure output
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		try {
			T2Reference containerRef = referenceService.register(this.resultData, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while configurating output port!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}
		return outputs;
	}
	
	public void workerDone(List<byte[]> dataArray) {
		this.numberOfCompletedWorkers++;
		this.resultData.addAll(dataArray);
		if(this.numberOfCompletedWorkers == this.workers.length) {
			synchronized (this) {
				this.notify();
			}
		}
	}

	public AsynchronousActivityCallback getCallback() {
		return callback;
	}

}
