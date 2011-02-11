/*
 * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.openscience.cdk.applications.taverna;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.setup.SetupController;

/**
 * Abstract class to describe an Taverna 2 activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public abstract class AbstractCDKActivity extends AbstractAsynchronousActivity<CDKActivityConfigurationBean> implements
		AsynchronousActivity<CDKActivityConfigurationBean> {

	/**
	 * Number of current iteration.
	 */
	protected int iteration = 0;
	/**
	 * Input port names.
	 */
	public String[] INPUT_PORTS;
	/**
	 * Output port names.
	 */
	public String[] OUTPUT_PORTS;

	/**
	 * Configuration bean.
	 */
	private CDKActivityConfigurationBean configBean;

	private Map<String, T2Reference> inputs;
	protected Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();

	protected AsynchronousActivityCallback callback;

	public AbstractCDKActivity() {
		// empty
	}

	@Override
	public void configure(CDKActivityConfigurationBean configBean) throws ActivityConfigurationException {
		// Any pre-config sanity checks
		if (configBean.getActivityName().equals("invalidExample")) {
			throw new ActivityConfigurationException("Example string can't be 'invalidExample'");
		}

		this.configBean = configBean;
		configurePorts();
	}

	/**
	 * Do port configuration.
	 */
	private void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();
		this.addInputPorts();
		this.addOutputPorts();
	}

	/**
	 * Use command addInput() here to add and configure input ports.
	 */
	protected abstract void addInputPorts();

	/**
	 * Use command addOutput() here to add and configure output ports.
	 */
	protected abstract void addOutputPorts();

	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs, final AsynchronousActivityCallback callback) {
		this.inputs = inputs;
		this.callback = callback;
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {

			public void run() {
				// Do work
				try {
					AbstractCDKActivity.this.iteration++;
					AbstractCDKActivity.this.work();
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError(CDKTavernaException.NOT_CATCHED_EXCEPTION,
							"AbstractCDKActivity", e);
					callback.fail(e.getMessage());
				}
				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(AbstractCDKActivity.this.outputs, new int[0]);
			}
		});
	}

	/**
	 * Abstract method which does the work.
	 */
	public abstract void work() throws Exception;

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	/**
	 * @return The name of the activity.
	 */
	public abstract String getActivityName();

	/**
	 * @return The folder of the activity.
	 */
	public abstract String getFolderName();

	/**
	 * @return The description of the activity.
	 */
	public abstract String getDescription();

	/**
	 * @return Additional properties.
	 */
	public abstract HashMap<String, Object> getAdditionalProperties();

	// ---- Input handling -------------------------------------------------------------------

	/**
	 * Extracts file information from an input port.
	 * 
	 * @param port
	 *            The name of target port.
	 * @return The file
	 * @throws Exception
	 */
	protected File getInputAsFile(String port) throws Exception {
		ReferenceService referenceService = this.callback.getContext().getReferenceService();
		String file = null;
		T2Reference inputRef = this.inputs.get(port);
		Identified identified = referenceService.resolveIdentifier(inputRef, null, this.callback.getContext());
		if (identified instanceof IdentifiedList<?>) {
			identified = (ReferenceSet) ((IdentifiedList<?>) identified).get(0);
		}
		if (identified instanceof ReferenceSet) {
			ReferenceSet referenceSet = (ReferenceSet) identified;
			Set<ExternalReferenceSPI> externalReferences = referenceSet.getExternalReferences();
			for (ExternalReferenceSPI externalReference : externalReferences) {
				if (externalReference instanceof FileReference) {
					FileReference fileReference = (FileReference) externalReference;
					file = fileReference.getFilePath();
				} else if (externalReference instanceof InlineStringReference) {
					InlineStringReference fileReference = (InlineStringReference) externalReference;
					file = fileReference.getContents();
				}
			}
		}
		if (file == null) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
		}
		return new File(file);
	}

	/**
	 * Extracts file informations from an input port.
	 * 
	 * @param port
	 *            The name of target port.
	 * @return List of Files
	 * @throws Exception
	 */
	protected List<File> getInputAsFileList(String port) throws Exception {
		ReferenceService referenceService = this.callback.getContext().getReferenceService();
		ArrayList<File> files = new ArrayList<File>();
		T2Reference inputRef = this.inputs.get(port);
		Identified identified = referenceService.resolveIdentifier(inputRef, null, this.callback.getContext());
		if (identified instanceof IdentifiedList<?>) {
			for (int i = 0; i < ((IdentifiedList<?>) identified).size(); i++) {
				if (((IdentifiedList<?>) identified).get(i) instanceof ReferenceSet) {
					ReferenceSet referenceSet = (ReferenceSet) ((IdentifiedList<?>) identified).get(i);
					Set<ExternalReferenceSPI> externalReferences = referenceSet.getExternalReferences();
					for (ExternalReferenceSPI externalReference : externalReferences) {
						try {
							if (externalReference instanceof FileReference) {
								FileReference fileReference = (FileReference) externalReference;
								String file = fileReference.getFilePath();
								files.add(new File(file));
							} else if (externalReference instanceof InlineStringReference) {
								InlineStringReference fileReference = (InlineStringReference) externalReference;
								String file = fileReference.getContents();
								files.add(new File(file));
							}
						} catch (Exception e) {
							ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_RESOLVING_FILE_INPUT,
									this.getActivityName(), e);
						}
					}
				}
			}
		}
		if (files.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
		}
		return files;
	}

	protected Object getInputAsObject(String port) throws Exception {
		ReferenceService referenceService = this.callback.getContext().getReferenceService();
		Object obj = null;
		try {
			boolean isDataCaching = SetupController.getInstance().isDataCaching();
			if (isDataCaching) {
				byte[] uuidData = (byte[]) referenceService.renderIdentifier(inputs.get(port), byte[].class,
						this.callback.getContext());
				UUID uuid = (UUID) CDKObjectHandler.getObject(uuidData);
				obj = FileNameGenerator.uncacheByteStream(uuid);
			} else {
				byte[] data = (byte[]) referenceService.renderIdentifier(inputs.get(port), byte[].class,
						this.callback.getContext());
				obj = CDKObjectHandler.getObject(data);
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OBJECT_DESERIALIZATION_ERROR,
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	protected <T> T getInputAsObject(String port, Class<T> type) throws Exception {
		ReferenceService referenceService = this.callback.getContext().getReferenceService();
		T obj = null;
		try {
			if (type == String.class || type == Integer.class) {
				obj = (T) referenceService.renderIdentifier(inputs.get(port), type, this.callback.getContext());
			} else {
				boolean isDataCaching = SetupController.getInstance().isDataCaching();
				if (isDataCaching) {
					byte[] uuidData = (byte[]) referenceService.renderIdentifier(inputs.get(port), byte[].class,
							this.callback.getContext());
					UUID uuid = (UUID) CDKObjectHandler.getObject(uuidData);
					obj = (T) FileNameGenerator.uncacheByteStream(uuid);
				} else {
					byte[] data = (byte[]) referenceService.renderIdentifier(inputs.get(port), byte[].class,
							this.callback.getContext());
					obj = (T) CDKObjectHandler.getObject(data);
				}
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(
					CDKTavernaException.WRONG_INPUT_PORT_TYPE + " Expected type:" + type.getSimpleName(),
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		if (obj.getClass() != type) {
			throw new CDKTavernaException("CDKObjectHandler", CDKTavernaException.WRONG_INPUT_PORT_TYPE
					+ " Expected type: " + type.getSimpleName());
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> getInputAsList(String port, Class<T> type) throws Exception {
		ReferenceService referenceService = this.callback.getContext().getReferenceService();
		if (type == String.class) {
			List<T> strings = (List<T>) referenceService.renderIdentifier(inputs.get(port), String.class,
					this.callback.getContext());
			if (strings == null || strings.isEmpty()) {
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
			return strings;
		}
		if (type == byte[].class) {
			List<T> data;
			try {
				data = (List<T>) referenceService.renderIdentifier(inputs.get(port), byte[].class,
						this.callback.getContext());
			} catch (Exception e) {
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
			return data;
		} else {
			boolean isDataCaching = SetupController.getInstance().isDataCaching();
			List<T> dataList = null;
			if (isDataCaching) {
				List<byte[]> uuidDataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(port),
						byte[].class, this.callback.getContext());
				UUID uuid = (UUID) CDKObjectHandler.getObject(uuidDataArray.get(0));
				byte[] data = FileNameGenerator.uncacheByteStream(uuid);
				dataList = (List<T>) CDKObjectHandler.getObject(data);
				try {
					dataList = (List<T>) CDKObjectHandler.getObject(data);
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError(CDKTavernaException.OBJECT_DESERIALIZATION_ERROR,
							this.getActivityName(), e);
					throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
				}
			} else {
				List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(port),
						byte[].class, this.callback.getContext());
				try {
					dataList = CDKObjectHandler.getGenericList(dataArray, type);
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError(CDKTavernaException.OBJECT_DESERIALIZATION_ERROR,
							this.getActivityName(), e);
					throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
				}
			}
			return dataList;
		}
	}

	// ---- Output handling -----------------------------------------------------------------------------

	protected void setOutputAsObjectList(List<?> objectList, String port) throws Exception {
		try {
			boolean isDataCaching = SetupController.getInstance().isDataCaching();
			ReferenceService referenceService = this.callback.getContext().getReferenceService();
			T2Reference containerRef;
			if (isDataCaching) {
				byte[] data = CDKObjectHandler.getBytes(objectList);
				UUID uuid = FileNameGenerator.cacheByteStream(data);
				List<byte[]> uuidDataList = new ArrayList<byte[]>();
				uuidDataList.add(CDKObjectHandler.getBytes(uuid));
				containerRef = referenceService.register(uuidDataList, 1, true, this.callback.getContext());
			} else {
				List<byte[]> dataList = CDKObjectHandler.getBytesList(objectList);
				containerRef = referenceService.register(dataList, 1, true, this.callback.getContext());
			}
			this.outputs.put(port, containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR,
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
	}

	protected void setOutputAsObject(Object object, String port) throws Exception {
		try {
			boolean isDataCaching = SetupController.getInstance().isDataCaching();
			ReferenceService referenceService = this.callback.getContext().getReferenceService();
			byte[] data;
			if (isDataCaching) {
				byte[] objectData = CDKObjectHandler.getBytes(object);
				UUID uuid = FileNameGenerator.cacheByteStream(objectData);
				data = CDKObjectHandler.getBytes(uuid);
			} else {
				data = CDKObjectHandler.getBytes(object);
			}
			T2Reference containerRef = referenceService.register(data, 0, true, this.callback.getContext());
			this.outputs.put(port, containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR,
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
	}

	protected void setOutputAsStringList(List<String> stringList, String port) throws Exception {
		try {
			ReferenceService referenceService = this.callback.getContext().getReferenceService();
			T2Reference containerRef = referenceService.register(stringList, 1, true, this.callback.getContext());
			this.outputs.put(port, containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR,
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
	}

	protected void setOutputAsString(String s, String port) throws Exception {
		try {
			ReferenceService referenceService = this.callback.getContext().getReferenceService();
			T2Reference containerRef = referenceService.register(s, 0, true, this.callback.getContext());
			this.outputs.put(port, containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR,
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
	}

	protected void setOutputAsByteList(List<byte[]> byteList, String port) throws Exception {
		try {
			ReferenceService referenceService = this.callback.getContext().getReferenceService();
			T2Reference containerRef = referenceService.register(byteList, 1, true, this.callback.getContext());
			this.outputs.put(port, containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR,
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
	}
}
