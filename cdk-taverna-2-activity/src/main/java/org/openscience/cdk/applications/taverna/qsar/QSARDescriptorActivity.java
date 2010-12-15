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
import java.util.Iterator;
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
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARDescriptorWorker;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARProgressFrame;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IAtomPairDescriptor;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.IBondDescriptor;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which represents the QSAR descriptor activity.
 * 
 * @author Andreas Trusykowski
 * 
 */
public class QSARDescriptorActivity extends AbstractCDKActivity {

	public static final String QSAR_DESCRIPTOR_ACTIVITY = "QSAR Descriptor";

	private QSARProgressFrame progressFrame = null;
	private int currentProgress = 0;

	private AsynchronousActivityCallback callback = null;

	public QSARDescriptorActivity() {
		super();
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "Calculated Structures", "Time CSV" };
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
		HashMap<String, Object> additionalProperties = new HashMap<String, Object>();
		additionalProperties.put(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS, true);
		additionalProperties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, 1);
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
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = this.callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		ArrayList<String> durationList = new ArrayList<String>();
		ArrayList<Class<? extends AbstractCDKActivity>> classes = (ArrayList<Class<? extends AbstractCDKActivity>>) this
				.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_CHOSEN_QSARDESCRIPTORS);
		List<IAtomContainer> moleculeList = new ArrayList<IAtomContainer>();
		if (classes == null || classes.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), "No QSAR descriptors chosen!");
		}
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during deserializion of object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		if (chemFileList.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), "Chemfile list is empty!");
		}

		for (Iterator<CMLChemFile> iter = chemFileList.iterator(); iter.hasNext();) {
			CMLChemFile file = iter.next();
			moleculeList.addAll(ChemFileManipulator.getAllAtomContainers(file));
		}
		durationList.add("Descriptor Name;Duration;");
		// Setup progress frame
		if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
			int numberOfCalculations = classes.size() * moleculeList.size();
			this.progressFrame = new QSARProgressFrame(1);
			this.progressFrame.setVisible(true);
			this.progressFrame.getProgressBar().setMinimum(0);
			this.progressFrame.getProgressBar().setMaximum(numberOfCalculations);
			this.progressFrame.getProgressBar().setValue(0);
			FileNameGenerator.centerWindowOnScreen(this.progressFrame);
			this.currentProgress = 0;
		}
		for (Class<? extends AbstractCDKActivity> clazz : classes) {
			AbstractCDKActivity descriptorActivity = null;
			try {
				descriptorActivity = clazz.newInstance();
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError("Error during instantiation of descriptor: " + clazz.getSimpleName(),
						this.getActivityName(), e);
				continue;
			}
			long startTime = System.currentTimeMillis();
			if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
				this.progressFrame.getStateLabels()[0].setText("Worker 1: " + clazz.getSimpleName());
				this.progressFrame.getStateLabels()[0].setBackground(Color.YELLOW);
			}
			if (descriptorActivity instanceof AbstractAtomicDescriptor) {
				IAtomicDescriptor descriptor = ((AbstractAtomicDescriptor) descriptorActivity).getDescriptor();
				for (IAtomContainer molecule : moleculeList) {
					if (molecule.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
						ErrorLogger.getInstance().writeError(
								"Molecule contains no ID! Use \"Tag Molecules With UUID\" activity!", this.getActivityName());
						throw new Exception("Molecule contains no ID!");
					}
					try {
						for (int j = 0; j < molecule.getAtomCount(); j++) {
							DescriptorValue value = descriptor.calculate(molecule.getAtom(j), molecule);
							molecule.getAtom(j).setProperty(value.getSpecification(), value);
						}
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError(
								"Error during calculation of QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
					}
					if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
						this.progressFrame.getProgressBar().setValue(this.currentProgress);
						this.currentProgress++;
					}
				}
			} else if (descriptorActivity instanceof AbstractAtomicProtonDescriptor) {
				IAtomicDescriptor descriptor = ((AbstractAtomicProtonDescriptor) descriptorActivity).getDescriptor();
				for (IAtomContainer molecule : moleculeList) {
					try {
						for (int j = 0; j < molecule.getAtomCount(); j++) {
							// Calculates only the value if the atom has the symbol H
							if (molecule.getAtom(j).getSymbol().equals("H")) {
								DescriptorValue value = descriptor.calculate(molecule.getAtom(j), molecule);
								molecule.getAtom(j).setProperty(value.getSpecification(), value);
							}
						}
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError(
								"Error during calculation of QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
					}
					if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
						this.progressFrame.getProgressBar().setValue(this.currentProgress);
						this.currentProgress++;
					}
				}
			} else if (descriptorActivity instanceof AbstractAtompairDescriptor) {
				IAtomPairDescriptor descriptor = ((AbstractAtompairDescriptor) descriptorActivity).getDescriptor();
				for (IAtomContainer molecule : moleculeList) {
					try {
						for (int j = 0; j < molecule.getAtomCount(); j++) {
							for (int i = 0; i < molecule.getAtomCount(); i++) {
								DescriptorValue value = descriptor.calculate(molecule.getAtom(j), molecule.getAtom(i), molecule);
								molecule.setProperty(value.getSpecification(), value);
							}
						}
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError(
								"Error during calculation of QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
					}
					if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
						this.progressFrame.getProgressBar().setValue(this.currentProgress);
						this.currentProgress++;
					}
				}
			} else if (descriptorActivity instanceof AbstractBondDescriptor) {
				IBondDescriptor descriptor = ((AbstractBondDescriptor) descriptorActivity).getDescriptor();
				for (IAtomContainer molecule : moleculeList) {
					try {
						for (int j = 0; j < molecule.getBondCount(); j++) {
							DescriptorValue value = descriptor.calculate(molecule.getBond(j), molecule);
							molecule.getBond(j).setProperty(value.getSpecification(), value);
						}
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError(
								"Error during calculation of QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
					}
					if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
						this.progressFrame.getProgressBar().setValue(this.currentProgress);
						this.currentProgress++;
					}
				}
			} else if (descriptorActivity instanceof AbstractMolecularDescriptor) {
				IMolecularDescriptor descriptor = ((AbstractMolecularDescriptor) descriptorActivity).getDescriptor();
				for (IAtomContainer molecule : moleculeList) {
					try {
						DescriptorValue value = descriptor.calculate(molecule);
						molecule.setProperty(value.getSpecification(), value);
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError(
								"Error during calculation of QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
					}
					if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
						this.progressFrame.getProgressBar().setValue(this.currentProgress);
						this.currentProgress++;
					}
				}
			} else {
				throw new CDKTavernaException("QSARDescreiptorWorker", "Unknown descriptor type: "
						+ descriptorActivity.getActivityName());
			}
			long duration = System.currentTimeMillis() - startTime;
			String durationString = descriptorActivity.getActivityName() + ";" + String.format("%4f", duration / 1000.0) + "s;";
			durationList.add(durationString);
		}
		if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
			this.progressFrame.getStateLabels()[0].setText("Worker 1: " + QSARDescriptorWorker.FINISHED);
			this.progressFrame.getStateLabels()[0].setBackground(Color.GREEN);
		}
		try {
			List<CMLChemFile> chemFiles = CMLChemFileWrapper.wrapAtomContainerListInChemModelList(moleculeList);
			List<byte[]> moleculeDataArray = CDKObjectHandler.getBytesList(chemFiles);
			// Congfigure output
			T2Reference containerRef = referenceService.register(moleculeDataArray, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			containerRef = referenceService.register(durationList, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during configuration of output port!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error during configuration of output port!");
		}
		if ((Boolean) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_SHOW_PROGRESS) == true) {
			this.progressFrame.dispose();
		}
		return outputs;
	}

}
