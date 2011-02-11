/* $RCSfile$
 * $Author: thomaskuhn $
 * $Date: 2009-03-02 21:19:40 +0100 (Mo, 02 Mrz 2009) $
 * $Revision: 14306 $
 * 
 * Copyright (C) 2005 by Egon Willighagen <egonw@users.sf.net>
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
package org.openscience.cdk.applications.taverna.io;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLRXNReader;
import org.openscience.cdk.io.MDLRXNV3000Reader;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * @author Egon Willighagen, Andreas Truszkowski
 * 
 */
public class CDKIOReader {

	/**
	 * Reads the contents of a SMILES into a set of CMLChemFile.
	 * 
	 * @taverna.consume
	 * 
	 * @param filename
	 *            Name of the file to read
	 * @return ChemModel of read file
	 * @throws Exception
	 *             Thrown when a IO exception occur
	 */
	public static CMLChemFile[] readFromSMILESFile(String filename) throws Exception {
		SMILESReader reader = new SMILESReader(new FileReader(new File(filename)));
		IMoleculeSet som = (IMoleculeSet) reader.read(new MoleculeSet());
		IMoleculeSet som2D = new MoleculeSet();
		StructureDiagramGenerator str = new StructureDiagramGenerator();
		for (int i = 0; i < som.getMoleculeCount(); i++) {
			str.setMolecule(som.getMolecule(i));
			str.generateCoordinates();
			som2D.addMolecule(str.getMolecule());
		}
		CMLChemFile[] results = new CMLChemFile[som2D.getMoleculeCount()];
		for (int i = 0; i < results.length; i++) {
			results[i] = wrapInChemModel(som2D.getMolecule(i));
		}
		reader.close();
		return results;
	}

	/**
	 * Method which wraps a IMolecule to an CMLChemfile
	 * 
	 * @param molecule
	 * @return CMLChemFile
	 */
	public static CMLChemFile wrapInChemModel(IMolecule molecule) {
		CMLChemFile file = new CMLChemFile();
		IChemModel model = new ChemModel();
		IChemSequence sequence = new ChemSequence();
		IMoleculeSet moleculeSet = new MoleculeSet();
		moleculeSet.addMolecule(molecule);
		model.setMoleculeSet(moleculeSet);
		sequence.addChemModel(model);
		file.addChemSequence(sequence);
		return file;
	}

	/**
	 * Method which converts a cmlChemFile which contains multiple molecules to
	 * an array of cmlChemFiles which contains only one molecle
	 * 
	 * @param cmlChemFile
	 *            which can contain multiple molecules
	 * @return Array of cmlChemFiles
	 */
	public static CMLChemFile[] wrapInChemModelArray(CMLChemFile cmlChemFile) {
		List<IAtomContainer> container = ChemFileManipulator.getAllAtomContainers(cmlChemFile);
		CMLChemFile[] result = new CMLChemFile[container.size()];
		for (int i = 0; i < container.size(); i++) {
			result[i] = CMLChemFileWrapper.wrapAtomContainerInChemModel(container.get(i));
		}
		return result;
	}

	/**
	 * Reads the contents of a MDL molfile into a CMLChemFile.
	 * 
	 * @taverna.consume
	 * 
	 * @param filename
	 *            Name of the file to read
	 * @return ChemModel of read file
	 * @throws Exception
	 *             Thrown when a IO exception occured
	 */
	public static CMLChemFile[] readFromMDLFile(String filename) throws Exception {
		CMLChemFile model = new CMLChemFile();
		ISimpleChemObjectReader reader = new MDLReader(new FileReader(new File(filename)));
		reader.read(model);
		reader.close();
		return wrapInChemModelArray(model);
	}

	/**
	 * Reads the contents of a MDL SD file into a CMLChemFile.
	 * 
	 * @taverna.consume
	 */
	public static CMLChemFile[] readFromSDFile(String filename) throws Exception {
		return readFromMDLFile(filename);
	}

	/**
	 * Reads the contents of a MDL RXN file into a reaction object.
	 * 
	 * @taverna.consume
	 */
	public static Reaction readRXNFile(String fileName) throws Exception {
		MDLRXNReader reader = new MDLRXNReader(new FileReader(new File(fileName)));
		Reaction reaction = new Reaction();
		reaction = (Reaction) reader.read(reaction);
		reader.close();
		return reaction;
	}

	/**
	 * Reads the contents of a MDL RXN V3000 file into a reaction object.
	 * 
	 * @taverna.consume
	 */
	public static Reaction readRXNV3000File(String fileName) throws Exception {
		MDLRXNV3000Reader reader = new MDLRXNV3000Reader(new FileReader(new File(fileName)));
		Reaction reaction = new Reaction();
		reaction = (Reaction) reader.read(reaction);
		reader.close();
		return reaction;
	}

	/**
	 * Reads the contents of a MDL RXN V 2000a file into a reaction object.
	 * 
	 * @taverna.consume
	 */
	public static Reaction readRXNV2000File(String fileName) throws Exception {
		org.openscience.cdk.io.MDLRXNV2000Reader reader = new org.openscience.cdk.io.MDLRXNV2000Reader(new FileReader(
				new File(fileName)));
		Reaction reaction = new Reaction();
		reaction = (Reaction) reader.read(reaction);
		reader.close();
		return reaction;
	}

	/**
	 * Reads the contents of a MDL mol V2000 file into a CMLChemFile.
	 * 
	 * @taverna.consume
	 * 
	 * @param filename
	 *            Name of the file to read
	 * @return ChemModel of read file
	 * @throws Exception
	 *             Thrown when a IO exception occured
	 */
	public static CMLChemFile[] readFromMDLV2000File(String filename) throws Exception {
		CMLChemFile model = new CMLChemFile();
		ISimpleChemObjectReader reader = new MDLV2000Reader(new FileReader(new File(filename)));
		reader.read(model);
		reader.close();
		return wrapInChemModelArray(model);
	}

	/**
	 * Reads the contents of a MDL SD V2000 file into a CMLChemFile.
	 * 
	 * @taverna.consume
	 */
	public static CMLChemFile[] readFromSDV2000File(String filename) throws Exception {
		return readFromMDLV2000File(filename);
	}
}
