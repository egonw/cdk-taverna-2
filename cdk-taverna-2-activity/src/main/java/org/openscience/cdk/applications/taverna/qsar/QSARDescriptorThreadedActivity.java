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
package org.openscience.cdk.applications.taverna.qsar;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.swing.JLabel;

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
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARDescriptorWork;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARDescriptorWorker;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARProgressFrame;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which represents the QSAR descriptor threaded *Experimental* activity.
 * 
 * @author Andreas Trusykowski
 * 
 */
public class QSARDescriptorThreadedActivity extends AbstractCDKActivity {

	public static final String QSAR_DESCRIPTOR_ACTIVITY = "QSAR Descriptor Threaded";

	private QSARDescriptorWorker[] workers = null;
	private AsynchronousActivityCallback callback = null;
	private HashMap<UUID, IAtomContainer> resultMap = new HashMap<UUID, IAtomContainer>();
	private QSARProgressFrame progressFrame = null;
	private int currentProgress = 0;
	private int moleculesCalculated = 0;
	private HashMap<Class<? extends AbstractCDKActivity>, Double> timeMap = new HashMap<Class<? extends AbstractCDKActivity>, Double>();
	private ArrayList<Class<? extends AbstractCDKActivity>> descriptorsToDo = null;
	private HashMap<Class<? extends AbstractCDKActivity>, LinkedList<IAtomContainer>> workToDoMap = new HashMap<Class<? extends AbstractCDKActivity>, LinkedList<IAtomContainer>>();
	private HashSet<Class<? extends AbstractCDKActivity>> availableDescriptorsSet = new HashSet<Class<? extends AbstractCDKActivity>>();

	public QSARDescriptorThreadedActivity() {
		super();
		this.INPUT_PORTS = new String[] { "Structures" };
		this.OUTPUT_PORTS = new String[] { "Calculated Structures", "Time CSV" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.OUTPUT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	public String getActivityName() {
		return QSARDescriptorThreadedActivity.QSAR_DESCRIPTOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> additionalProperties = new HashMap<String, Object>();
		additionalProperties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
		additionalProperties.put(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS, true);
		return additionalProperties;
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
		this.workToDoMap.clear();
		this.availableDescriptorsSet.clear();
		this.resultMap.clear();
		ArrayList<String> durationList = new ArrayList<String>();
		InvocationContext context = this.callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		this.descriptorsToDo = (ArrayList<Class<? extends AbstractCDKActivity>>) ((ArrayList<Class<? extends AbstractCDKActivity>>) this
				.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_CHOSEN_QSARDESCRIPTORS)).clone();
		if (this.descriptorsToDo == null || this.descriptorsToDo.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), "No QSAR descriptors chosen!");
		}
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.getINPUT_PORTS()[0]),
				byte[].class, context);
		// Prepare work
		int numberOfCalculations = 0;
		LinkedList<IAtomContainer> molecules = null;
		try {
			List<CMLChemFile> chemFiles = CDKObjectHandler.getChemFileList(dataArray);
			molecules = new LinkedList<IAtomContainer>();
			for (CMLChemFile chemFile : chemFiles) {
				molecules.addAll(ChemFileManipulator.getAllAtomContainers(chemFile));
			}
			if(molecules.isEmpty()) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.DATA_CONTAINS_NO_MOLECULE, this.getActivityName());
				return new HashMap<String, T2Reference>(); // FIXME Is it good to return only a empty list?
			}
			// Check for ID
			for (IAtomContainer atomContainer : molecules) {
				if (atomContainer.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
					ErrorLogger.getInstance().writeError("Molecule contains no ID!", this.getClass().getSimpleName());
					throw new CDKTavernaException(this.getClass().getSimpleName(),
							CDKTavernaException.MOLECULE_NOT_TAGGED_WITH_UUID);
				} else {
					UUID uuid = (UUID) atomContainer.getProperty(CDKTavernaConstants.MOLECULEID);
					this.resultMap.put(uuid, atomContainer);
				}
			}
			// Clone molecules
			for (Class<? extends AbstractCDKActivity> descriptor : this.descriptorsToDo) {
				this.availableDescriptorsSet.add(descriptor);
				LinkedList<IAtomContainer> moleculesClone = new LinkedList<IAtomContainer>();
				for (IAtomContainer atomContainer : molecules) {
					moleculesClone.add((IAtomContainer) atomContainer.clone());
					numberOfCalculations++;
				}
				this.workToDoMap.put(descriptor, moleculesClone);
			}

		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OBJECT_DESERIALIZATION_ERROR, this.toString(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OBJECT_DESERIALIZATION_ERROR);
		}
		// Show progress frame
		int numberOfThreads = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
		if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
			this.progressFrame = new QSARProgressFrame(numberOfThreads);
			this.progressFrame.setVisible(true);
			this.progressFrame.getProgressBar().setMinimum(0);
			this.progressFrame.getProgressBar().setMaximum(numberOfCalculations);
			this.progressFrame.getProgressBar().setValue(0);
			FileNameGenerator.centerWindowOnScreen(this.progressFrame);
			this.currentProgress = 0;
		}
		// Prepare worker
		if (dataArray.size() < numberOfThreads) {
			numberOfThreads = dataArray.size();
		}
		this.workers = new QSARDescriptorWorker[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++) {
			this.workers[i] = new QSARDescriptorWorker(this);
			this.workers[i].start();
		}
		// Wait for workers
		synchronized (this) {
			this.wait();
		}
		// Congfigure output
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		try {
			durationList.add("Descriptor Name;Duration(in s);");
			for (Entry<Class<? extends AbstractCDKActivity>, Double> entry : this.timeMap.entrySet()) {
				String name = entry.getKey().getSimpleName();
				double duration = entry.getValue();
				String value = String.format("%4f", duration / 1000.0);
				String durationString = name + ";" + value + ";";
				durationList.add(durationString);
			}
			ArrayList<CMLChemFile> results = new ArrayList<CMLChemFile>();
			for (Entry<UUID, IAtomContainer> entry : this.resultMap.entrySet()) {
				results.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(entry.getValue()));
			}

			List<byte[]> resultData = CDKObjectHandler.getBytesList(results);
			T2Reference containerRef = referenceService.register(resultData, 1, true, context);
			outputs.put(this.OUTPUT_PORTS[0], containerRef);
			containerRef = referenceService.register(durationList, 1, true, context);
			outputs.put(this.OUTPUT_PORTS[1], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
		if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
			this.progressFrame.dispose();
		}
		return outputs;
	}


	/**
	 * Updates the duration of target descriptor.
	 * 
	 * @param clazz
	 *            DescriptorClass
	 * @param time
	 *            Duration
	 */
	public synchronized void setTime(Class<? extends AbstractCDKActivity> clazz, long time) {
		if (this.timeMap.get(clazz) == null) {
			double millis = time / Math.pow(10, 6);
			this.timeMap.put(clazz, millis);
		} else {
			double millis = time / Math.pow(10, 6);
			double currentTime = (Double) this.timeMap.get(clazz) + millis;
			this.timeMap.put(clazz, currentTime);
		}
	}

	/**
	 * Final point for the workers.
	 * 
	 * @param results
	 *            Described molecules.
	 */
	public synchronized void workerDone() {
		boolean allDone = true;
		for (QSARDescriptorWorker worker : this.workers) {
			if (!worker.isDone()) {
				allDone = false;
				break;
			}
		}
		if (allDone) {
			synchronized (this) {
				this.notify();
			}
		}
	}

	/**
	 * Manages the work for the worker and locks used descriptors.
	 * 
	 * @return Work for the Workers.
	 */
	public synchronized QSARDescriptorWork getWork() {
		QSARDescriptorWork work = null;
		LinkedList<Class<? extends AbstractCDKActivity>> emptyWork = new LinkedList<Class<? extends AbstractCDKActivity>>();

		for (Class<? extends AbstractCDKActivity> descriptor : this.descriptorsToDo) {
			boolean available = false;
			if (this.availableDescriptorsSet.contains(descriptor)) {
				available = true;
			}
			if (available) {
				LinkedList<IAtomContainer> molecules = this.workToDoMap.get(descriptor);
				if (molecules.isEmpty()) {
					emptyWork.add(descriptor);
					continue;
				}
				this.availableDescriptorsSet.remove(descriptor);
				work = new QSARDescriptorWork();
				work.descriptorClass = descriptor;
				work.molecule = molecules.remove();
				break;
			}
		}
		for (Class<? extends AbstractCDKActivity> descriptor : emptyWork) {
			this.descriptorsToDo.remove(descriptor);
		}
		if (work != null) {
			this.currentProgress++;
			this.moleculesCalculated++;
		}
		return work;
	}

	/**
	 * Releases the lock on descriptors.
	 * 
	 * @param descriptorClass
	 */
	public synchronized void releaseDescriptor(Class<? extends AbstractCDKActivity> descriptorClass) {
		this.availableDescriptorsSet.add(descriptorClass);
	}

	/**
	 * Updates the result map.
	 * 
	 * @param result
	 *            Calculated molecule.
	 */
	public synchronized void publishResult(IAtomContainer result) {
		// Merge result
		UUID uuid = (UUID) result.getProperty(CDKTavernaConstants.MOLECULEID);
		IAtomContainer target = this.resultMap.get(uuid);
		for (Entry<Object, Object> property : result.getProperties().entrySet()) {
			target.setProperty(property.getKey(), property.getValue());
		}
	}

	/**
	 * Displays the progress in the progress frame.
	 */
	public void showProgress() {
		if (!(Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
			return;
		}
		for (int i = 0; i < this.workers.length; i++) {
			JLabel progressLabel = this.progressFrame.getStateLabels()[i];
			String state = this.workers[i].getCurrentState();
			progressLabel.setText("Worker " + (i + 1) + ": " + state);
			if (state.equals(QSARDescriptorWorker.FINISHED)) {
				progressLabel.setBackground(Color.GREEN);
			} else {
				progressLabel.setBackground(Color.YELLOW);
			}
		}
		this.progressFrame.getProgressBar().setValue(this.currentProgress);
		this.progressFrame.getProgressLabel().setText(this.moleculesCalculated + " descriptor values calculated!");
	}

}
