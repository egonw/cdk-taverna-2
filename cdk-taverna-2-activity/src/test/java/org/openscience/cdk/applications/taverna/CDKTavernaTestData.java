/* 
 * $Author: thomaskuhn $
 * $Date: 2009-03-02 23:02:58 +0100 (Mo, 02 Mrz 2009) $
 * $Revision: 14345 $
 * 
 * Copyright (C) 2006 - 2008 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
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
import java.util.HashMap;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.io.CDKIOReader;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * 
 * Class which provides default test data for the junit tests of cdk-taverna It also provides methods which can be useful for
 * debugging
 * 
 */
public class CDKTavernaTestData {

	private static IAtomContainer[] atomContainer;
	private static CMLChemFile[] cmlChemFile;
	private static CMLChemFile[] cmlChemFileWith3DCoordinates;
	private static StringBuffer logTests = new StringBuffer();
	private static String pathToWriteInUnitTestFiles = "";
	private static StringBuffer descriptorValues = new StringBuffer();
	private static String[] smiles;
	private static int[] numberOfAtomsFromSmilesTest;
	private static boolean defaultDataLoaded = false;
	private static CMLChemFile[] reactionEvaluationEductOne;
	private static CMLChemFile[] reactionEvaluationEductTwo;
	private static CMLChemFile[] reactionEvaluationResult;
	private static Reaction reactionEvaluationReaction;
	public static final String JUNITTESTINGFILENAME = "JunitTesting";

	public CDKTavernaTestData() throws Exception {
	}

	/**
	 * Returns an atomContainer-Array for the junit test.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static IAtomContainer[] getAtomContainerArray() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		}
		return atomContainer;
	}

	/**
	 * Sets the atomContainer-array for the junit-tests
	 * 
	 * @param atomContainerArray
	 * @throws Exception
	 */
	public static void setAtomContainerArray(IAtomContainer[] atomContainerArray) throws Exception {
		atomContainer = atomContainerArray;
	}

	/**
	 * Sets the CMLChemFile for the junit-tests
	 * 
	 * @param cmlChemFileArray
	 * @throws Exception
	 */
	public static void setCMLChemFile(CMLChemFile[] cmlChemFileArray) throws Exception {
		cmlChemFile = cmlChemFileArray;
	}

	/**
	 * Returns the CMLChemFile for the junit-test
	 * 
	 * @return
	 * @throws Exception
	 */
	public static CMLChemFile[] getCMLChemFile() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		} else {
			for (int i = 0; i < cmlChemFile.length; i++) {
				// cmlChemFile[i].setProperty(FileNameGenerator.FILENAME,
				// fileNameGenerator.addFileNameToFileNameList(JUNITTESTINGFILENAME, fileNameGenerator.getNewFileNameList()));
			}
		}
		return cmlChemFile;
	}

	/**
	 * Method which reloads the test data
	 * 
	 * @throws Exception
	 */
	public static void reloadTestData() throws Exception {
		loadDefaultDataForTests();
	}

	/**
	 * Method which loads the default Data for the junit-tests
	 * 
	 * @throws Exception
	 */
	public static void loadDefaultDataForTests() throws Exception {
		pathToWriteInUnitTestFiles = "target" + File.separator + "unit_tests";
		createTestData();
		defaultDataLoaded = true;
	}

	/**
	 * Loads the default data for the junit-tests
	 * 
	 * @throws Exception
	 */
	private static void createTestData() throws Exception {
		try {
			smiles = new String[] { "CC", "OCC", "O(C)CCC", "c1=cc=cc=c1", "C(=C)=C", "OCC=CCc1=cc=cc=c1(C=C)", "O(CC=C)CCN",
					"CCCCCCCCCCCCCCC", "OCC=CCO", "NCCCCN" };
			numberOfAtomsFromSmilesTest = new int[] { 2, 3, 5, 6, 3, 13, 7, 15, 6, 6 };
			// String[] smiles = new String[] {"OCC=CCc1ccccc1(C=C)"};
			SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			atomContainer = new IAtomContainer[smiles.length];
			cmlChemFile = new CMLChemFile[smiles.length];

			for (int i = 0; i < smiles.length; i++) {
				atomContainer[i] = sp.parseSmiles(smiles[i]);
				cmlChemFile[i] = CMLChemFileWrapper.wrapAtomContainerInChemModel(atomContainer[i]);
				// cmlChemFile[i].setProperty(FileNameGenerator.FILENAME,
				// fileNameGenerator.addFileNameToFileNameList(JUNITTESTINGFILENAME, fileNameGenerator.getNewFileNameList()));
			}
			String path = "." + File.separator + "target" + File.separator + "test-classes" + File.separator + "data"
					+ File.separator + "mol" + File.separator + "heptane.mol";
			File file = new File(path);
			if (file.exists()) {
				cmlChemFileWith3DCoordinates = CDKIOReader.readFromMDLFile(path);
				for (int i = 0; i < cmlChemFileWith3DCoordinates.length; i++) {
					// cmlChemFileWith3DCoordinates[i].setProperty(FileNameGenerator.FILENAME,
					// fileNameGenerator.addFileNameToFileNameList(JUNITTESTINGFILENAME, fileNameGenerator.getNewFileNameList()));
				}
			}
			path = "src\\test\\resources\\data\\rxn\\reactionevaluation-reaction.rxn";
			file = new File(path);
			if (file.exists()) {
				reactionEvaluationReaction = CDKIOReader.readRXNV2000File(path);
			}
			path = "src\\test\\resources\\data\\sdf\\reactionevaluationedukt1a1-3.sdf";
			file = new File(path);
			if (file.exists()) {
				reactionEvaluationEductOne = CDKIOReader.readFromSDV2000File(path);
				reactionEvaluationEductOne = CMLChemFileWrapper.wrapChemModelArrayInResolvedChemModelArray(reactionEvaluationEductOne);
		//		for (int i = 0; i < reactionEvaluationEductOne.length; i++) {
					// reactionEvaluationEductOne[i].setProperty(FileNameGenerator.FILENAME,
					// fileNameGenerator.addFileNameToFileNameList(JUNITTESTINGFILENAME, fileNameGenerator.getNewFileNameList()));
			//	}
			}
			path = "src\\test\\resources\\data\\sdf\\reactionevaluationedukt2B1-3.sdf";
			file = new File(path);
			if (file.exists()) {
				reactionEvaluationEductTwo = CDKIOReader.readFromSDV2000File(path);
				reactionEvaluationEductTwo = CMLChemFileWrapper.wrapChemModelArrayInResolvedChemModelArray(reactionEvaluationEductTwo);
				for (int i = 0; i < reactionEvaluationEductTwo.length; i++) {
					// reactionEvaluationEductTwo[i].setProperty(FileNameGenerator.FILENAME,
					// fileNameGenerator.addFileNameToFileNameList(JUNITTESTINGFILENAME, fileNameGenerator.getNewFileNameList()));
				}
			}
			path = "." + File.separator + "target" + File.separator + "test-classes" + File.separator + "data" + File.separator
					+ "sdf" + File.separator + "reactionevaluationresult-a1-3b1-3.sdf";
			file = new File(path);
			if (file.exists()) {
				reactionEvaluationResult = CDKIOReader.readFromSDV2000File(path);
				for (int i = 0; i < reactionEvaluationResult.length; i++) {
					// reactionEvaluationResult[i].setProperty(FileNameGenerator.FILENAME,
					// fileNameGenerator.addFileNameToFileNameList(JUNITTESTINGFILENAME, fileNameGenerator.getNewFileNameList()));
				}
			}
			pathToWriteInUnitTestFiles = pathToWriteInUnitTestFiles + File.separator + "original_Data.txt";
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Test data could not be created: " + e);
		}
	}

	public static String[] getAromaticityOfDefaultTestData() {
		return new String[] { "false", "false", "false", "true", "false", "true", "false", "false", "false", "false" };
	}

	/**
	 * Method which returns a test set of smiles which is used to test the cdk-taverna processors
	 * 
	 * @return list 10 smiles
	 * @throws Exception
	 */
	public static String[] getSMILES() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		}
		return smiles;
	}

	/**
	 * Method which returns the number of atoms of the smiles for the tests
	 * 
	 * @return
	 * @throws Exception
	 */
	public static int[] getNumberOfAtomsForTestSmiles() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		}
		return numberOfAtomsFromSmilesTest;
	}

	/**
	 * Method which compares two CMLChemFiles after conversions to cml-strings
	 * 
	 * @param chemFileOriginal
	 *            The original CMLChemFile
	 * @param chemFileToCompare
	 *            The CMLChemFile to compare
	 * @return A string which contains all lines of the of the cml which were missing in the compared file or in the original file
	 * @throws Exception
	 */
	public static String diffCMLChemFile(CMLChemFile chemFileOriginal, CMLChemFile chemFileToCompare) throws Exception {
		if (!chemFileOriginal.compare(chemFileToCompare)) {
			String[] cmlChemFileOriginalStringArray = chemFileOriginal.toCML().split("\n");
			String[] cmlChemFileToCompareStringArray = chemFileToCompare.toCML().split("\n");
			StringBuilder differ = new StringBuilder();
			HashMap<String, String> hashChemSequenceOriginal = new HashMap<String, String>();
			HashMap<String, String> hashChemSequenceToCompare = new HashMap<String, String>();
			for (int i = 0; i < cmlChemFileOriginalStringArray.length; i++) {
				hashChemSequenceOriginal.put(cmlChemFileOriginalStringArray[i], cmlChemFileOriginalStringArray[i]);
				hashChemSequenceToCompare.put(cmlChemFileToCompareStringArray[i], cmlChemFileToCompareStringArray[i]);
			}
			for (int i = 0; i < cmlChemFileToCompareStringArray.length; i++) {
				if (!hashChemSequenceOriginal.containsKey(cmlChemFileToCompareStringArray[i])) {
					differ.append("+" + cmlChemFileToCompareStringArray[i] + "\n");
				}
			}
			for (int i = 0; i < cmlChemFileOriginalStringArray.length; i++) {
				if (!hashChemSequenceToCompare.containsKey(cmlChemFileOriginalStringArray[i])) {
					differ.append("-" + cmlChemFileOriginalStringArray[i] + "\n");
				}
			}
			return differ.toString();
		} else {
			return null;
		}
	}

	public static CMLChemFile[] getCMLChemFileWith3DCoordinates() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		}
		for (int i = 0; i < cmlChemFileWith3DCoordinates.length; i++) {
			// cmlChemFileWith3DCoordinates[i].setProperty(FileNameGenerator.FILENAME,
			// fileNameGenerator.addFileNameToFileNameList("JunitTesting", fileNameGenerator.getNewFileNameList()));
		}
		return cmlChemFileWith3DCoordinates;
	}

	public static void addLogMessage(String[] log) {
		for (int i = 0; i < log.length; i++) {
			logTests.append(log);
		}
	}

	public static String getLogFileOfJUnitTests() {
		return logTests.toString();
	}

	public static String getPathForWritingFilesOfUnitTests(boolean successful) {
		if (successful) {
			pathToWriteInUnitTestFiles = "target" + File.separator + "test_files" + File.separator;
		} else {
			pathToWriteInUnitTestFiles = "target" + File.separator + "test_file_errors" + File.separator;
		}
		return pathToWriteInUnitTestFiles;
	}

	public static void setDescpitorValue(String descriptorValue) {
		descriptorValues.append(descriptorValue + ";");
	}

	public static String getDescriptorValue() {
		return descriptorValues.toString();
	}

	/**
	 * @return the reactoinEvaluationEductOne
	 * @throws Exception
	 */
	public static CMLChemFile[] getReactionEvaluationEductOne() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		}
		return reactionEvaluationEductOne;
	}

	/**
	 * @return the reactoinEvaluationEductTwo
	 * @throws Exception
	 */
	public static CMLChemFile[] getReactionEvaluationEductTwo() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		}
		return reactionEvaluationEductTwo;
	}

	/**
	 * @return the reactoinEvaluationReaction
	 * @throws Exception
	 */
	public static Reaction getReactionEvaluationReaction() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		}
		return reactionEvaluationReaction;
	}

	/**
	 * @return the reactoinEvaluationResult
	 * @throws Exception
	 */
	public static CMLChemFile[] getReactionEvaluationResult() throws Exception {
		if (!defaultDataLoaded) {
			loadDefaultDataForTests();
		}
		return reactionEvaluationResult;
	}

	// public static DBConnector getDatabaseConnector(String sqlStatement) {
	// String url = "jdbc:postgresql://localhost:5433/mol";
	// DBConnector connector = new DBConnector();
	// connector.setDbURL(url);
	// connector.setUserName("postgres");
	// // Here add the password for your postgres installation
	// connector.setUserPassword("");
	// connector.setSqlStatement(sqlStatement);
	// return connector;
	// }

}
