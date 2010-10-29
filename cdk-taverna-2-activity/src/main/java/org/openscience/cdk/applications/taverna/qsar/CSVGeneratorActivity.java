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
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which implements a local worker for the cdk-taverna-2 project which provides the possibility to generate a CSV from
 * descriptor values which are calculated and stored within each molecule
 * 
 * @author Thomas Kuhn, Andreas Truszkowski
 * 
 */
public class CSVGeneratorActivity extends AbstractCDKActivity implements IFileWriter {

	public static final String CSV_GENERATOR_ACTIVITY = "CSV Generator";

	/**
	 * Creates a new instance.
	 */
	public CSVGeneratorActivity() {
		this.INPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names", "Structures" };
		this.RESULT_PORTS = new String[] { "CSV String" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[2], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<UUID, Map<String, Object>> vectorMap;
		byte[] vectorData = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class, context);
		try {
			vectorMap = (Map<UUID, Map<String, Object>>) CDKObjectHandler.getObject(vectorData);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while deserializing object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		SortedSet<String> descriptorNames;
		byte[] nameData = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]), byte[].class, context);
		try {
			descriptorNames = (SortedSet<String>) CDKObjectHandler.getObject(nameData);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while deserializing object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[2]), byte[].class,
				context);
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while deserializing object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		List<String> csv = null;
		try {
			List<UUID> uuids = this.getUUIDs(chemFileList);
			csv = this.createCSV(vectorMap, descriptorNames, uuids);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while creating csv!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		T2Reference containerRef = referenceService.register(csv, 1, true, context);
		outputs.put(this.RESULT_PORTS[0], containerRef);
		// Return results
		return outputs;

	}

	/**
	 * Returns a list of the UUIDs from given the chem files.
	 * 
	 * @param chemFileList
	 * @return List of the UUIDs from given the chem files.
	 */
	private List<UUID> getUUIDs(List<CMLChemFile> chemFileList) {
		ArrayList<UUID> uuids = new ArrayList<UUID>();
		for (CMLChemFile chemFile : chemFileList) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(chemFile);
			for (IAtomContainer atomContainer : moleculeList) {
				if (atomContainer.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
					ErrorLogger.getInstance().writeMessage("Molecule contains no UUID in " + this.getActivityName());
				} else {
					uuids.add((UUID) atomContainer.getProperty(CDKTavernaConstants.MOLECULEID));
				}
			}
		}
		return uuids;
	}

	/**
	 * Creates a csv list of strings for a given vector of qsar descriptors
	 * 
	 * @param results
	 *            Vector of qsar descriptor results
	 * @return List of strings which contains the descriptor values as CSV
	 */
	private List<String> createCSV(Map<UUID, Map<String, Object>> results, SortedSet<String> descriptorNames, List<UUID> uuids) {
		List<String> csv = new ArrayList<String>();
		StringBuffer buffer;
		String separator = ";";
		String quotationMark = "\"";
		Map<String, Object> descriptorValueMap;
		buffer = new StringBuffer();
		buffer.append("ID;");
		for (String descriptorToken : descriptorNames) {
			buffer.append(quotationMark);
			buffer.append(descriptorToken);
			buffer.append(quotationMark);
			buffer.append(separator);
		}
		csv.add(buffer.toString());
		for (UUID id : uuids) {
			buffer = new StringBuffer();
			descriptorValueMap = results.get(id);
			if (descriptorValueMap != null) {
				buffer.append(quotationMark);
				buffer.append(id.toString());
				buffer.append(quotationMark);
				buffer.append(separator);
				for (String descriptorToken : descriptorNames) {
					Object obj = descriptorValueMap.get(descriptorToken);
					if (obj != null) {
						buffer.append(quotationMark);
						if (obj instanceof Integer) {
							buffer.append((Integer) obj);
						}
						if (obj instanceof Double) {
							buffer.append((Double) obj);
						}
						buffer.append(quotationMark);
						buffer.append(separator);
					} else {
						buffer.append(separator);
					}
				}
				csv.add(buffer.toString());
			}
		}
		return csv;
	}

	@Override
	public String getActivityName() {
		return CSVGeneratorActivity.CSV_GENERATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CSV_GENERATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}
}
