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

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.Constants;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class RuleOfFiveFilter extends AbstractCDKActivity {

	private static RuleOfFiveDescriptor descriptor;

	public RuleOfFiveFilter() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "matchingStructures", "otherStructures" };
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
		return this.getClass().getSimpleName();
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + this.getClass().getSimpleName();
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return Constants.QSAR_ATOMIC_DESCRIPTOR_FOLDER_NAME;
	}

	@SuppressWarnings("unchecked")
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		List<CMLChemFile> inputList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> matchedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> unmatchedList = new ArrayList<CMLChemFile>();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		for (byte[] data : dataArray) {
			Object obj;
			try {
				obj = CDKObjectHandler.getObject(data);
			} catch (Exception e) {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(), "Error while deserializing object");
			}
			if (obj instanceof CMLChemFile) {
				inputList.add((CMLChemFile) obj);
			} else {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(),
						CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
		}
		if (descriptor == null) {
			descriptor = new RuleOfFiveDescriptor();
		}
		try {
			for (CMLChemFile file : inputList) {
				List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(file);
				for (IAtomContainer molecules : moleculeList) {
					try {
						IDescriptorResult result = descriptor.calculate(molecules).getValue();
						if (result instanceof IntegerResult && ((IntegerResult) result).intValue() == 0) {
							// file.setProperty(FileNameGenerator.FILENAME,
							// fileNameGenerator.addFileNameToFileNameList(fileNameCalculated,
							// (List<String>)file.getProperty(FileNameGenerator.FILENAME)));
							matchedList.add(file);
							// comment.add("Matched Rule of Five;");
							break;
						} else {
							// file.setProperty(FileNameGenerator.FILENAME,
							// fileNameGenerator.addFileNameToFileNameList(fileNameNotCalculated,
							// (List<String>)file.getProperty(FileNameGenerator.FILENAME)));
							unmatchedList.add(file);
							// if(result instanceof IntegerResult) {
							// comment.add("NOT Matched Rule of Five within: " + String.valueOf(((IntegerResult)
							// result).intValue()) + " rules");
							// } else {
							// comment.add("NOT Matched Rule of Five!");
							// }
							break;
						}
					} catch (Exception e) {
						// file.setProperty(FileNameGenerator.FILENAME,
						// fileNameGenerator.addFileNameToFileNameList(fileNameNotCalculated,
						// (List<String>)file.getProperty(FileNameGenerator.FILENAME)));
						unmatchedList.add(file);
						// String molID = "";
						// if (file.getProperty(CDKTavernaConfig.DATABASEID) != null) {
						// molID += file.getProperty(CDKTavernaConfig.DATABASEID).toString() + " ;";
						// }
						// if (file.getProperty(CDKTavernaConfig.MOLECULEID) != null) {
						// molID += file.getProperty(CDKTavernaConfig.MOLECULEID) + " ;";
						// }
						// comment.add(molID + "Error, calculation of the Descriptor for this molecule caused an error!" + e);
					}

				}
			}
		} catch (Exception exception) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), exception.getMessage());
		}
		// Congfigure output
		try {
			dataArray = new ArrayList<byte[]>();
			if (!matchedList.isEmpty()) {
				for (CMLChemFile c : matchedList) {
					dataArray.add(CDKObjectHandler.getBytes(c));
				}
			}
			T2Reference containerRef = referenceService.register(dataArray, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			dataArray = new ArrayList<byte[]>();
			if (!unmatchedList.isEmpty()) {
				for (CMLChemFile c : unmatchedList) {
					dataArray.add(CDKObjectHandler.getBytes(c));
				}
			}
			containerRef = referenceService.register(dataArray, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO exception handling
		}
		return outputs;
	}

}
