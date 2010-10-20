/* 
 * Copyright (C) 2006 - 2007 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
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
package org.openscience.cdk.applications.taverna.basicutilities;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which provides method to wrap in CMLChemfiles or from CMLChemfiles to other data types like atomcontianer
 * 
 * @author Thomas Kuhn & Andreas Truszkowski
 */
public class CMLChemFileWrapper {

	/**
	 * Method which converts an atomContainer to a CMLChemFile
	 * 
	 * @param atomContainer
	 * @return CMLChemFile which contains the information of the atomContainer
	 * @throws Exception
	 */
	public static IAtomContainer wrapChemModelInAtomContainer(CMLChemFile cmlChemFile) throws Exception {
		IAtomContainer atomContainer;
		List<IAtomContainer> listOfAtomContainerTemp = null;
		listOfAtomContainerTemp = ChemFileManipulator.getAllAtomContainers(cmlChemFile);
		if (listOfAtomContainerTemp.size() == 1) {
			atomContainer = listOfAtomContainerTemp.get(0);
		} else {
			throw new CDKTavernaException("CMLChemFileWrapper",
					"ConvertCMLChemFileListToAtomContainerArray: More than one molecules within one CMLChemfile");
		}
		return atomContainer;
	}

	/**
	 * Method which converts an CMLChemfile array to a CMLChemFile array with only one molecule per CMLChemFile.
	 * 
	 * @param atomContainer
	 * @return CMLChemFile which contains the information of the atomContainer
	 * @throws Exception
	 */
	public static CMLChemFile[] wrapChemModelArrayInResolvedChemModelArray(CMLChemFile[] cmlChemFiles) throws Exception {
		ArrayList<CMLChemFile> chemFiles = new ArrayList<CMLChemFile>();
		for (CMLChemFile file : cmlChemFiles) {
			if (file != null) {
				List<CMLChemFile> cfs = wrapInChemModelList(file);
				chemFiles.addAll(cfs);
			}
		}
		CMLChemFile[] fileArray = new CMLChemFile[chemFiles.size()];
		chemFiles.toArray(fileArray);
		return fileArray;
	}

	/**
	 * Method which converts an atomContainer to a CMLChemFile
	 * 
	 * @param atomContainer
	 * @return CMLChemFile which contains the information of the atomContainer
	 */
	public static CMLChemFile wrapAtomContainerInChemModel(IAtomContainer atomContainer) {
		CMLChemFile file = new CMLChemFile();
		IChemModel model = new ChemModel();
		IChemSequence sequence = new ChemSequence();
		IMoleculeSet moleculeSet = new MoleculeSet();
		moleculeSet.addAtomContainer(atomContainer);
		model.setMoleculeSet(moleculeSet);
		sequence.addChemModel(model);
		file.addChemSequence(sequence);
		return file;
	}

	/**
	 * Method which converts a atomContainer array to a CMLChemFile array
	 * 
	 * @param atomContainer
	 *            Array which will be converted
	 * @return CMLChemFileArray which contains the atomContainers
	 */
	public static CMLChemFile[] wrapAtomContainerArrayInChemModel(IAtomContainer[] atomContainer) {
		CMLChemFile[] cmlChemfile = new CMLChemFile[atomContainer.length];
		for (int i = 0; i < atomContainer.length; i++) {
			cmlChemfile[i] = wrapAtomContainerInChemModel(atomContainer[i]);
		}
		return cmlChemfile;
	}

	/**
	 * Method which converts a atomContainer list to a CMLChemFile array
	 * 
	 * @param atomContainer
	 *            List which will be converted
	 * @return CMLChemFileArray which contains the atomContainers
	 */
	public static CMLChemFile[] wrapAtomContainerListInChemModel(List<IAtomContainer> atomContainers) {
		CMLChemFile[] cmlChemfile = new CMLChemFile[atomContainers.size()];
		for (int i = 0; i < atomContainers.size(); i++) {
			cmlChemfile[i] = wrapAtomContainerInChemModel(atomContainers.get(i));
		}
		return cmlChemfile;
	}

	/**
	 * Method which adds an atomContainer to a CMLChemFile
	 * 
	 * @param atomContainer
	 * @return CMLChemFile which contains the information of the atomContainer
	 */
	public static CMLChemFile addAtomContainerToChemModel(CMLChemFile chemFile, IAtomContainer atomContainer) {
		IMoleculeSet moleculeSet = chemFile.getChemSequence(0).getChemModel(0).getMoleculeSet();
		moleculeSet.addAtomContainer(atomContainer);
		return chemFile;
	}

	/**
	 * Method which adds all atomContainer in a CMLChemfile to a CMLChemFile
	 * 
	 * @param chemFileSource
	 *            Source of the atomContainers
	 * @param chemFileTarget
	 *            Gets the atomContainers
	 * @return modified CMLChemFile
	 */
	public static CMLChemFile wrapChemModelAtomContainerInChemModel(CMLChemFile chemFileSource, CMLChemFile chemFileTarget) {
		if (chemFileTarget.getChemSequence(0) == null) {
			chemFileTarget.addChemSequence(new ChemSequence());
			chemFileTarget.getChemSequence(0).addChemModel(new ChemModel());
			chemFileTarget.getChemSequence(0).getChemModel(0).setMoleculeSet(new MoleculeSet());
		}
		IMoleculeSet moleculeSet = chemFileTarget.getChemSequence(0).getChemModel(0).getMoleculeSet();
		for (int i = 0; i < chemFileSource.getChemSequenceCount(); i++) {
			IChemSequence sequence = chemFileSource.getChemSequence(i);
			for (int j = 0; j < sequence.getChemModelCount(); j++) {
				IChemModel model = sequence.getChemModel(j);
				IMoleculeSet set = model.getMoleculeSet();
				moleculeSet.add(set);
			}
		}
		return chemFileTarget;
	}

	/**
	 * Method which converts a list of CMLChemfiles to an AtomContainer array
	 * 
	 * @param cmlChemFileList
	 *            List of CMLChemfiles
	 * @return AtomContainer array which contains the molecules of the CMLChemFileList
	 * @throws Exception
	 */
	public static IAtomContainer[] convertCMLChemFileListToAtomContainerArray(List<CMLChemFile> cmlChemFileList) throws Exception {
		IAtomContainer[] atomContainerArray = new AtomContainer[cmlChemFileList.size()];
		List<IAtomContainer> listOfAtomContainerTemp = null;
		for (int i = 0; i < atomContainerArray.length; i++) {
			listOfAtomContainerTemp = ChemFileManipulator.getAllAtomContainers(cmlChemFileList.get(i));
			if (listOfAtomContainerTemp.size() == 1) {
				atomContainerArray[i] = listOfAtomContainerTemp.get(0);
			} else {
				// FIXME Exception handling
				throw new CDKTavernaException("CMLChemFileWrapper",
						"ConvertCMLChemFileListToAtomContainerArray: More than one molecules within one CMLChemfile");
			}
		}
		return atomContainerArray;
	}

	/**
	 * Method which wraps a IMolecule to an CMLChemfile
	 * 
	 * @param molecule
	 * @return CMLChemFile
	 */
	public static CMLChemFile wrapInChemModel(IMolecule molecule) {
		// FileNameGenerator fileNameGenerator = new FileNameGenerator();
		CMLChemFile file = new CMLChemFile();
		IChemModel model = new ChemModel();
		IChemSequence sequence = new ChemSequence();
		IMoleculeSet moleculeSet = new MoleculeSet();
		moleculeSet.addMolecule(molecule);
		model.setMoleculeSet(moleculeSet);
		sequence.addChemModel(model);
		file.addChemSequence(sequence);
		// file.setProperty(FileNameGenerator.FILENAME,
		// fileNameGenerator.getNewFileNameList());
		return file;
	}

	/**
	 * Method which converts a cmlChemFile which contains multiple molecules to an array of cmlChemFiles which contains only one
	 * molecle
	 * 
	 * @param cmlChemFile
	 *            which can contain multiple molecules
	 * @return Array of cmlChemFiles
	 */
	public static CMLChemFile[] wrapInChemModelArray(CMLChemFile cmlChemFile) {
		// FileNameGenerator fileNameGenerator = new FileNameGenerator();
		int numberOfChemModels = 0;
		int chemModelNumber = 0;
		for (int i = 0; i < cmlChemFile.getChemSequenceCount(); i++) {
			numberOfChemModels += cmlChemFile.getChemSequence(i).getChemModelCount();
		}
		CMLChemFile[] result = new CMLChemFile[numberOfChemModels];
		for (int i = 0; i < cmlChemFile.getChemSequenceCount(); i++) {
			for (int j = 0; j < cmlChemFile.getChemSequence(i).getChemModelCount(); j++) {
				IChemModel chemModel = cmlChemFile.getChemSequence(i).getChemModel(j);
				result[chemModelNumber] = new CMLChemFile();
				IChemSequence sequence = new ChemSequence();
				sequence.addChemModel(chemModel);
				result[chemModelNumber].addChemSequence(sequence);
				// result[chemModelNumber].setProperty(FileNameGenerator.FILENAME,
				// fileNameGenerator.getNewFileNameList());
				chemModelNumber++;
			}
		}
		return result;
	}

	/**
	 * Method which converts a cmlChemFile which contains multiple molecules to a list of cmlChemFiles which contains only one
	 * molecule
	 * 
	 * @param cmlChemFile
	 *            which can contain multiple molecules
	 * @return List of cmlChemFiles
	 */
	public static List<CMLChemFile> wrapInChemModelList(CMLChemFile cmlChemFile) {
		List<CMLChemFile> cmlList = new ArrayList<CMLChemFile>();
		List<IAtomContainer> containers = ChemFileManipulator.getAllAtomContainers(cmlChemFile);
		if (containers.size() == 1) {
			cmlList.add(cmlChemFile);
			return cmlList;
		}
		for (IAtomContainer container : containers) {
			CMLChemFile chemFile = new CMLChemFile();
			IMoleculeSet set = new MoleculeSet();
			set.addAtomContainer(container);
			IChemModel model = new ChemModel();
			model.setMoleculeSet(set);
			IChemSequence seq = new ChemSequence();
			seq.addChemModel(model);
			chemFile.addChemSequence(seq);
			cmlList.add(chemFile);
		}
		return cmlList;
	}

	/**
	 * Method which converts a cmlChemFile which contains multiple molecules to an List of CML Strings which each represent only
	 * one molecule
	 * 
	 * @param cmlChemFile
	 *            which can contain multiple molecules
	 * @return List of cmlChemFiles
	 */
	public static List<String> convertChemModelToCMLString(CMLChemFile cmlChemFile) throws Exception {
		ArrayList<String> stringList = new ArrayList<String>();
		CMLChemFile chemFile;
		for (int i = 0; i < cmlChemFile.getChemSequenceCount(); i++) {
			for (int j = 0; j < cmlChemFile.getChemSequence(i).getChemModelCount(); j++) {
				IChemModel chemModel = cmlChemFile.getChemSequence(i).getChemModel(j);
				chemFile = new CMLChemFile();
				IChemSequence sequence = new ChemSequence();
				sequence.addChemModel(chemModel);
				chemFile.addChemSequence(sequence);
				// chemFile.setProperty(FileNameGenerator.FILENAME,
				// fileNameGenerator.getNewFileNameList());
				stringList.add(chemFile.toCML());
			}
		}
		return stringList;
	}

	/**
	 * Method which converts a CML String list to a cmlChemFile which contains multiple molecules.
	 * 
	 * @param stringList
	 *            CML String list
	 * @return cmlChemFile which can contain multiple molecules
	 */
	public List<CMLChemFile> convertStringListToChemModelList(List<String> stringList) throws Exception {
		ArrayList<CMLChemFile> chemModelList = new ArrayList<CMLChemFile>();
		for (String CMLString : stringList) {
			CMLChemFile chemModel = new CMLChemFile(CMLString);
			chemModelList.add(chemModel);
		}
		return chemModelList;
	}

}
