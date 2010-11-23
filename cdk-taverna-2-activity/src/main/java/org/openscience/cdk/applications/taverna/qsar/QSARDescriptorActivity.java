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

	private AsynchronousActivityCallback callback = null;

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
		HashMap<String, Object> additionalProperties = new HashMap<String, Object>();
		additionalProperties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
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
		ArrayList<Class<? extends AbstractCDKActivity>> classes = (ArrayList<Class<? extends AbstractCDKActivity>>) this
				.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_CHOSEN_QSARDESCRIPTORS);
		if (classes == null || classes.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), "No QSAR descriptors chosen!");
		}
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during deserializing object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		if (chemFileList.isEmpty()) {
			return null;
		}
		List<IAtomContainer> moleculeList = new ArrayList<IAtomContainer>();
		for (Iterator<CMLChemFile> iter = chemFileList.iterator(); iter.hasNext();) {
			CMLChemFile file = iter.next();
			moleculeList.addAll(ChemFileManipulator.getAllAtomContainers(file));
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
			if (descriptorActivity instanceof AbstractAtomicDescriptor) {
				IAtomicDescriptor descriptor = ((AbstractAtomicDescriptor) descriptorActivity).getDescriptor();
				for (IAtomContainer molecule : moleculeList) {
					try {
						for (int j = 0; j < molecule.getAtomCount(); j++) {
							DescriptorValue value = descriptor.calculate(molecule.getAtom(j), molecule);
							molecule.getAtom(j).setProperty(value.getSpecification(), value);
						}
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError(
								"Error during calculating QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
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
								"Error during calculating QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
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
								"Error during calculating QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
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
								"Error during calculating QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
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
								"Error during calculating QSAR descriptor: " + descriptor.getClass() + "!",
								descriptor.toString(), e);
					}
				}
			} else {
				throw new CDKTavernaException("QSARDescreiptorWorker", "Unknown descriptor type: "
						+ descriptorActivity.getActivityName());
			}
		}
		try {
			List<CMLChemFile> chemFiles = CMLChemFileWrapper.wrapAtomContainerListInChemModelList(moleculeList);
			List<byte[]> moleculeDataArray = CDKObjectHandler.getBytesList(chemFiles);
			// Congfigure output
			T2Reference containerRef = referenceService.register(moleculeDataArray, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during configurating output port!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}
		return outputs;
	}

}
