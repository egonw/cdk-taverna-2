package org.openscience.cdk.applications.taverna.qsar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;

public class QSARDescriptorActivity extends AbstractCDKActivity {

	public static final String QSAR_DESCRIPTOR_ACTIVITY = "QSAR Descriptor";

	public QSARDescriptorActivity() {
		super();
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "Calculated Structures", "NOT Calculated Structures" };
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
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<String, T2Reference> outputs = null;
		ArrayList<Class<? extends AbstractCDKActivity>> classes = (ArrayList<Class<? extends AbstractCDKActivity>>) this
				.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_CHOSEN_QSARDESCRIPTORS);
		if (classes == null || classes.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), "No QSAR descriptors chosen!");
		}
		for (Class<? extends AbstractCDKActivity> clazz : classes) {
			long timeStart = System.nanoTime();
			AbstractCDKActivity descriptor = null;
			try {
				descriptor = clazz.newInstance();
			} catch (InstantiationException e) {
				comment.add(e.getMessage() + " Descriptor: " + clazz.getSimpleName());
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				comment.add(e.getMessage() + " Descriptor: " + clazz.getSimpleName());
				e.printStackTrace();
			}
			if (clazz != null) {
				if (outputs == null) {
					List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs
							.get(this.getINPUT_PORTS()[0]), byte[].class, context);
					T2Reference containerRef = referenceService.register(dataArray, 1, true, context);
					outputs = new HashMap<String, T2Reference>();
					outputs.put(descriptor.getINPUT_PORTS()[0], containerRef);
				} else {
					List<byte[]> dataArray = new ArrayList<byte[]>();
					try {
						dataArray.addAll((List<byte[]>) referenceService.renderIdentifier(outputs.get(descriptor
								.getRESULT_PORTS()[0]), byte[].class, context));
						comment.add("Error: " + clazz.getSimpleName() + " not calculated!");
					} catch (NullPointerException e) {
					}
					try {
						dataArray.addAll((List<byte[]>) referenceService.renderIdentifier(outputs.get(descriptor
								.getRESULT_PORTS()[1]), byte[].class, context));
						comment.add("Error: " + "In " + clazz.getSimpleName() + " not all structures calculated!");
					} catch (NullPointerException e) {
					}
					outputs = new HashMap<String, T2Reference>();
					T2Reference containerRef = referenceService.register(dataArray, 1, true, context);
					outputs.put(descriptor.getINPUT_PORTS()[0], containerRef);
				}
				outputs = descriptor.work(outputs, callback);
			}
			long timeEnd = System.nanoTime();
			comment.add(clazz.getSimpleName() + ", " + String.valueOf(TimeUnit.NANOSECONDS.toMillis(timeEnd - timeStart)));
		}
		return outputs;
	}

}
