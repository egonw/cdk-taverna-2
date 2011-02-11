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
package org.openscience.cdk.applications.taverna.qsar.utilities;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.qsar.AbstractAtomicDescriptor;
import org.openscience.cdk.applications.taverna.qsar.AbstractAtomicProtonDescriptor;
import org.openscience.cdk.applications.taverna.qsar.AbstractAtompairDescriptor;
import org.openscience.cdk.applications.taverna.qsar.AbstractBondDescriptor;
import org.openscience.cdk.applications.taverna.qsar.AbstractMolecularDescriptor;
import org.openscience.cdk.applications.taverna.qsar.QSARDescriptorThreadedActivity;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IAtomPairDescriptor;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.IBondDescriptor;
import org.openscience.cdk.qsar.IMolecularDescriptor;

/**
 * Class which represents the QSAR descriptor worker.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class QSARDescriptorWorker extends Thread {

	public static final String FINISHED = "Finished...";

	private QSARDescriptorThreadedActivity owner = null;
	private String currentSate = "";
	private long startTime = 0;
	private boolean done = false;

	public QSARDescriptorWorker(QSARDescriptorThreadedActivity owner) {
		this.owner = owner;
	}

	@Override
	public void run() {
		try {
			QSARDescriptorWork work = null;
			while ((work = this.owner.getWork()) != null) {
				IAtomContainer molecule = work.molecule;
				Class<? extends AbstractCDKActivity> descriptorClass = work.descriptorClass;
				try {
					AbstractCDKActivity descriptorActivity = null;
					startTime = System.nanoTime();
					currentSate = descriptorClass.getSimpleName();
					this.owner.showProgress();
					try {
						descriptorActivity = descriptorClass.newInstance();
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError(
								"Error during instantiation of descriptor: " + descriptorClass.getSimpleName(),
								this.getClass().getSimpleName(), e);
						continue;
					}
					if (descriptorActivity instanceof AbstractAtomicDescriptor) {
						IAtomicDescriptor descriptor = ((AbstractAtomicDescriptor) descriptorActivity).getDescriptor();
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
					} else if (descriptorActivity instanceof AbstractAtomicProtonDescriptor) {
						IAtomicDescriptor descriptor = ((AbstractAtomicProtonDescriptor) descriptorActivity)
								.getDescriptor();
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
					} else if (descriptorActivity instanceof AbstractAtompairDescriptor) {
						IAtomPairDescriptor descriptor = ((AbstractAtompairDescriptor) descriptorActivity)
								.getDescriptor();
						try {
							for (int j = 0; j < molecule.getAtomCount(); j++) {
								for (int i = 0; i < molecule.getAtomCount(); i++) {
									DescriptorValue value = descriptor.calculate(molecule.getAtom(j),
											molecule.getAtom(i), molecule);
									molecule.setProperty(value.getSpecification(), value);
								}
							}
						} catch (Exception e) {
							ErrorLogger.getInstance().writeError(
									"Error during calculating QSAR descriptor: " + descriptor.getClass() + "!",
									descriptor.toString(), e);
						}
					} else if (descriptorActivity instanceof AbstractBondDescriptor) {
						IBondDescriptor descriptor = ((AbstractBondDescriptor) descriptorActivity).getDescriptor();
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
					} else if (descriptorActivity instanceof AbstractMolecularDescriptor) {
						IMolecularDescriptor descriptor = ((AbstractMolecularDescriptor) descriptorActivity)
								.getDescriptor();
						try {
							DescriptorValue value = descriptor.calculate(molecule);
							molecule.setProperty(value.getSpecification(), value);
						} catch (Exception e) {
							ErrorLogger.getInstance().writeError(
									"Error during calculating QSAR descriptor: " + descriptor.getClass() + "!",
									descriptor.toString(), e);
						}
					} else {
						throw new CDKTavernaException("QSARDescreiptorWorker", "Unknown descriptor type: "
								+ descriptorActivity.getActivityName());
					}
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError("Error during calculation of QSAR descriptors!",
							this.getClass().getSimpleName(), e);
				} finally {
					long duration = System.nanoTime() - startTime;
					this.owner.setTime(descriptorClass, duration);
					this.owner.releaseDescriptor(descriptorClass);
					this.owner.publishResult(molecule);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErrorLogger.getInstance().writeError("Serious QSAR descriptor calculation error!",
					this.getClass().getSimpleName());
		} finally {
			this.currentSate = QSARDescriptorWorker.FINISHED;
			this.owner.showProgress();
			this.done = true;
			this.owner.workerDone();
		}
	}

	/**
	 * @return whether all work for this worker is done.
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * @return the current progress state.
	 */
	public String getCurrentState() {
		return this.currentSate;
	}

}
