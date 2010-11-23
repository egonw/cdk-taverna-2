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
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
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
 * Class which represents the QSAR descriptor worker.
 * 
 * @author Andreas Truszkowski
 *
 */
public class QSARDescriptorWorker extends Thread {

	private QSARDescriptorThreadedActivity owner = null;
	private ArrayList<Class<? extends AbstractCDKActivity>> classes = null;
	private List<byte[]> moleculeDataArray = null;

	public QSARDescriptorWorker(QSARDescriptorThreadedActivity owner, ArrayList<Class<? extends AbstractCDKActivity>> classes,
			List<byte[]> moleculeDataArray) {
		this.owner = owner;
		this.classes = classes;
		this.moleculeDataArray = moleculeDataArray;
	}

	@Override
	public void run() {
		try {
			List<CMLChemFile> chemFiles = null;
			try {
				chemFiles = CDKObjectHandler.getChemFileList(this.moleculeDataArray);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError("Error during deserializing object!", this.toString(), e);
				this.owner.workerDone(new ArrayList<byte[]>());
				return;
			}
			List<IAtomContainer> moleculeList = new ArrayList<IAtomContainer>();
			for (Iterator<CMLChemFile> iter = chemFiles.iterator(); iter.hasNext();) {
				CMLChemFile file = iter.next();
				moleculeList.addAll(ChemFileManipulator.getAllAtomContainers(file));
			}
			for (Class<? extends AbstractCDKActivity> clazz : classes) {
				AbstractCDKActivity descriptorActivity = null;
				try {
					descriptorActivity = clazz.newInstance();
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError("Error during instantiation of descriptor: " + clazz.getSimpleName(),
							this.getClass().getSimpleName(), e);
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
									DescriptorValue value = descriptor.calculate(molecule.getAtom(j), molecule.getAtom(i),
											molecule);
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
			chemFiles = CMLChemFileWrapper.wrapAtomContainerListInChemModelList(moleculeList);
				this.moleculeDataArray = CDKObjectHandler.getBytesList(chemFiles);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during calculation of QSAR descriptors!", this.getClass().getSimpleName(), e);
		}
		this.owner.workerDone(this.moleculeDataArray);
	}

}
