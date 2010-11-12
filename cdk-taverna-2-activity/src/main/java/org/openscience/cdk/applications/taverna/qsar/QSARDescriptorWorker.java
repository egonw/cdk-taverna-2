package org.openscience.cdk.applications.taverna.qsar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

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

public class QSARDescriptorWorker extends Thread {

	private QSARDescriptorActivity owner = null;
	private ArrayList<Class<? extends AbstractCDKActivity>> classes = null;
	private List<byte[]> moleculeDataArray = null;

	public QSARDescriptorWorker(QSARDescriptorActivity owner, ArrayList<Class<? extends AbstractCDKActivity>> classes,
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
				ErrorLogger.getInstance().writeError("Error while deserializing object!", this.toString(), e);
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
					e.printStackTrace();
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
							ErrorLogger.getInstance().writeError("Error while calculating QSAR descriptor!",
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
							ErrorLogger.getInstance().writeError("Error while calculating QSAR descriptor!",
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
							ErrorLogger.getInstance().writeError("Error while calculating QSAR descriptor!",
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
							ErrorLogger.getInstance().writeError("Error while calculating QSAR descriptor!",
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
							ErrorLogger.getInstance().writeError("Error while calculating QSAR descriptor!",
									descriptor.toString(), e);
						}
					}
				} else {
					System.out.println(descriptorActivity.toString());
				}
			}
			chemFiles = CMLChemFileWrapper.wrapAtomContainerListInChemModelList(moleculeList);
			try {
				this.moleculeDataArray = CDKObjectHandler.getBytesList(chemFiles);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.owner.workerDone(this.moleculeDataArray);
	}

}
