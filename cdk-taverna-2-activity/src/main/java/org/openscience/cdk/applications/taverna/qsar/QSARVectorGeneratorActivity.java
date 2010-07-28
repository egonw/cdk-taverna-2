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
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.qsar.result.IntegerArrayResult;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which implements a local worker for the cdk-taverna-2 project which provides the possibility to generate a vector from
 * descriptor values which are calculated and stored within each molecule
 * 
 * @author Thomas Kuhn, Andreas Truszkowski
 * 
 */
public class QSARVectorGeneratorActivity extends AbstractCDKActivity implements IFileWriter {

	public static final String QSAR_VECTOR_GENERATOR_ACTIVITY = "QSAR Vector Generator";

	public QSARVectorGeneratorActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "Descriptor Vector", "Structures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 0);
		addOutput(this.RESULT_PORTS[1], 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		Map<UUID, Map<String, Object>> vectorMap = new HashMap<UUID, Map<String, Object>>();
		Map<String, Object> descritorResultMap;
		SortedSet<String> descriptorNames = new TreeSet<String>();
		String descriptorSpecificationSplitter = "#";
		String descriptorName;
		IDescriptorResult result;
		DescriptorValue dValue;
		Object[] keys;
		try {
			for (CMLChemFile chemFile : chemFileList) {
				List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(chemFile);
				for (IAtomContainer atomContainer : moleculeList) {
					if (atomContainer.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
						throw new Exception("Molecule contains no ID!");
					}
					descritorResultMap = new HashMap<String, Object>();
					// Add the bond descriptor to the descriptor map
					for (int i = 0; i < atomContainer.getBondCount(); i++) {
						IBond bond = atomContainer.getBond(i);
						keys = bond.getProperties().keySet().toArray();
						for (Object key : keys) {
							if (bond.getProperty(key) instanceof DescriptorValue) {
								dValue = (DescriptorValue) bond.getProperty(key);
								descriptorName = dValue.getSpecification().getSpecificationReference().split(
										descriptorSpecificationSplitter)[1];
								descriptorName += "." + i;
								result = dValue.getValue();
								if (result instanceof DoubleResult) {
									descriptorNames.add(descriptorName);
									descritorResultMap.put(descriptorName, ((DoubleResult) result).doubleValue());
								} else if (result instanceof IntegerResult) {
									descriptorNames.add(descriptorName);
									descritorResultMap.put(descriptorName, ((IntegerResult) result).intValue());
								} else if (result instanceof IntegerArrayResult) {
									for (int j = 0; j < ((IntegerArrayResult) result).length(); j++) {
										descriptorNames.add(descriptorName + "." + j);
										descritorResultMap.put(descriptorName + "." + j, ((IntegerArrayResult) result).get(j));
									}
								} else if (result instanceof DoubleArrayResult) {
									for (int j = 0; j < ((DoubleArrayResult) result).length(); j++) {
										descriptorNames.add(descriptorName + "." + j);
										descritorResultMap.put(descriptorName + "." + j, ((DoubleArrayResult) result).get(j));
									}
								}
							}
						}
					}
					// Add the atom descriptor to the descriptor map
					for (int i = 0; i < atomContainer.getAtomCount(); i++) {
						IAtom atom = atomContainer.getAtom(i);
						keys = atom.getProperties().keySet().toArray();
						for (Object key : keys) {
							if (atom.getProperty(key) instanceof DescriptorValue) {
								dValue = (DescriptorValue) atom.getProperty(key);
								descriptorName = dValue.getSpecification().getSpecificationReference().split(
										descriptorSpecificationSplitter)[1];
								result = dValue.getValue();
								descriptorName += "." + i;
								if (result instanceof DoubleResult) {
									descriptorNames.add(descriptorName);
									descritorResultMap.put(descriptorName, ((DoubleResult) result).doubleValue());
								} else if (result instanceof IntegerResult) {
									descriptorNames.add(descriptorName);
									descritorResultMap.put(descriptorName, ((IntegerResult) result).intValue());
								} else if (result instanceof IntegerArrayResult) {
									for (int j = 0; j < ((IntegerArrayResult) result).length(); j++) {
										descriptorNames.add(descriptorName + "." + j);
										descritorResultMap.put(descriptorName + "." + j, ((IntegerArrayResult) result).get(j));
									}
								} else if (result instanceof DoubleArrayResult) {
									for (int j = 0; j < ((DoubleArrayResult) result).length(); j++) {
										descriptorNames.add(descriptorName + "." + j);
										descritorResultMap.put(descriptorName + "." + j, ((DoubleArrayResult) result).get(j));
									}
								}
							}
						}
					}
					// Add the molecular Descriptors to the Descriptor map
					keys = atomContainer.getProperties().keySet().toArray();
					for (Object key : keys) {
						if (atomContainer.getProperty(key) instanceof DescriptorValue) {
							dValue = (DescriptorValue) atomContainer.getProperty(key);
							descriptorName = dValue.getSpecification().getSpecificationReference().split(
									descriptorSpecificationSplitter)[1];
							result = dValue.getValue();
							if (result instanceof DoubleResult) {
								descriptorNames.add(descriptorName);
								descritorResultMap.put(descriptorName, ((DoubleResult) result).doubleValue());
							} else if (result instanceof IntegerResult) {
								descriptorNames.add(descriptorName);
								descritorResultMap.put(descriptorName, ((IntegerResult) result).intValue());
							} else if (result instanceof IntegerArrayResult) {
								for (int i = 0; i < ((IntegerArrayResult) result).length(); i++) {
									descriptorNames.add(descriptorName + "." + i);
									descritorResultMap.put(descriptorName + "." + i, ((IntegerArrayResult) result).get(i));
								}
							} else if (result instanceof DoubleArrayResult) {
								for (int i = 0; i < ((DoubleArrayResult) result).length(); i++) {
									descriptorNames.add(descriptorName + "." + i);
									descritorResultMap.put(descriptorName + "." + i, ((DoubleArrayResult) result).get(i));
								}
							}
						}
					}
					vectorMap.put((UUID) atomContainer.getProperty(CDKTavernaConstants.MOLECULEID), descritorResultMap);
				}
			}

			byte[] vectorData = CDKObjectHandler.getBytes(vectorMap);
			T2Reference containerRef = referenceService.register(vectorData, 0, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			containerRef = referenceService.register(dataArray, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
			comment.add("done");
		} catch (Exception e) {
			// TODO: handle exception
		}
		// Return results
		return outputs;

	}

	@Override
	public String getActivityName() {
		return QSARVectorGeneratorActivity.QSAR_VECTOR_GENERATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + QSAR_VECTOR_GENERATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}
}
