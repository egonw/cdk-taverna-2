/* $RCSfile$
 * $Author: thomaskuhn $
 * $Date: 2009-03-02 21:21:33 +0100 (Mo, 02 Mrz 2009) $
 * $Revision: 14307 $
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
package org.openscience.cdk.applications.taverna.qsar.descriptors.molecular;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.openscience.cdk.applications.taverna.CDKTavernaConfig;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.scuflworkers.cdk.CDKLocalWorker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class RuleOfFiveFilter implements CDKLocalWorker {

	private static RuleOfFiveDescriptor descriptor;
	private String descriptorName = "RuleOfFive_Filter";
	private String[] inputNames  = new String[] { "inputStructures" };
	private String[] outputNames = new String[] { "matchingStructures", "otherStructures", "Comment" };

	// Region: input and output definition

	public String[] inputNames() {
		return inputNames;
	}
	public String[] inputTypes() {
		return new String[] {CDKLocalWorker.CMLChemFileList};
	}

	public String[] outputNames() {
		return outputNames;
	}
	public String[] outputTypes() {
		return new String[] {CDKLocalWorker.CMLChemFileList, CDKLocalWorker.CMLChemFileList, CDKLocalWorker.STRING_ARRAY};
	}

	// End of region

	// Region: local worker execution
	@SuppressWarnings("unchecked")
	public Map<String, DataThing> execute(Map<String, DataThing> inputs) throws TaskExecutionException {
		FileNameGenerator fileNameGenerator = new FileNameGenerator();
		List<CMLChemFile> inputList = null;
		List<CMLChemFile> matchedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> unmatchedList = new ArrayList<CMLChemFile>();
		List<String> comment = new ArrayList<String>();
		String fileNameCalculated = descriptorName + "_Matched";
		String fileNameNotCalculated = descriptorName + "_NOT_Matched";;
		if (inputs.get(inputNames[0]) != null) {
			inputList = CMLChemFileWrapper.getListOfCMLChemfileFromDataThing(inputs.get(inputNames[0]));
		} else {
			return null;
		}
		if (descriptor == null) {
			descriptor = new RuleOfFiveDescriptor();
		}
		try {
			for (CMLChemFile file : inputList) {
				List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(file);
				for (IAtomContainer molecules : moleculeList) {
					try {
						IDescriptorResult result = descriptor.calculate(
								molecules).getValue();
						if (result instanceof IntegerResult 
								&& ((IntegerResult) result).intValue() == 0) {
							file.setProperty(FileNameGenerator.FILENAME, fileNameGenerator.addFileNameToFileNameList(fileNameCalculated, (List<String>)file.getProperty(FileNameGenerator.FILENAME)));
							matchedList.add(file);
							comment.add("Matched Rule of Five;");
							break;
						}
						else { 
							file.setProperty(FileNameGenerator.FILENAME, fileNameGenerator.addFileNameToFileNameList(fileNameNotCalculated, (List<String>)file.getProperty(FileNameGenerator.FILENAME)));
							unmatchedList.add(file);
							if(result instanceof IntegerResult) {
								comment.add("NOT Matched Rule of Five within: " + String.valueOf(((IntegerResult) result).intValue()) + " rules");
							} else {
								comment.add("NOT Matched Rule of Five!");
							}
							break;
						}
					} catch (Exception e) {
						file.setProperty(FileNameGenerator.FILENAME, fileNameGenerator.addFileNameToFileNameList(fileNameNotCalculated, (List<String>)file.getProperty(FileNameGenerator.FILENAME)));
						unmatchedList.add(file);
						String molID = "";
						if (file.getProperty(CDKTavernaConfig.DATABASEID) != null) {
							molID += file.getProperty(CDKTavernaConfig.DATABASEID).toString() + " ;";
						}
						if (file.getProperty(CDKTavernaConfig.MOLECULEID) != null) {
							molID += file.getProperty(CDKTavernaConfig.MOLECULEID) + " ;";
						}
						comment.add(molID + "Error, calculation of the Descriptor for this molecule caused an error!" + e);
					}
					
					
				}	
			}
		} catch (Exception exception) {
			throw new TaskExecutionException(exception);
		}
		Map<String, DataThing> outputs = new HashMap<String, DataThing>();
		outputs.put(outputNames[0], new DataThing(matchedList));
		outputs.put(outputNames[1], new DataThing(unmatchedList));
		outputs.put(outputNames[2], new DataThing(comment));
		return outputs;
	}
	// End of region
}
