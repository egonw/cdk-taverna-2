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
package org.openscience.cdk.applications.taverna.weka.clustering;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.basicutilities.Tools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which represents the Split Molecules Into Clusters activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class SplitMoleculesIntoClustersActivity extends AbstractCDKActivity {

	public static final String SPLIT_MOLECULES_INTO_CLUSTERS = "Split Molecules Into Clusters";
	private HashMap<Integer, File> files = null;
	private HashMap<UUID, Integer> uuidClusterMap = null;

	/**
	 * Creates a new instance.
	 */
	public SplitMoleculesIntoClustersActivity() {
		this.INPUT_PORTS = new String[] { "Structures", "UUID Cluster CSV", "File" };
		this.OUTPUT_PORTS = new String[] { "Files" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 1, true, null, String.class);
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[2], 1, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<CMLChemFile> chemFiles = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		List<String> csv = this.getInputAsList(this.INPUT_PORTS[1], String.class);
		File targetFile = this.getInputAsFile(this.INPUT_PORTS[2]);
		String directory = Tools.getDirectory(targetFile);
		String name = Tools.getFileName(targetFile);
		String extension = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
		// Do work
		if (this.files == null) {
			this.files = new HashMap<Integer, File>();
		}
		List<String> resultFiles = new ArrayList<String>();
		// Create cluster uuid map
		if (this.uuidClusterMap == null) {
			this.uuidClusterMap = new HashMap<UUID, Integer>();
			for (int i = 1; i < csv.size(); i++) {
				String[] values = csv.get(i).split(";");
				uuidClusterMap.put(UUID.fromString(values[0]), Integer.parseInt(values[1]));
			}
		}
		// Write SDFiles
		for (CMLChemFile chemFile : chemFiles) {
			List<IAtomContainer> containers = ChemFileManipulator.getAllAtomContainers(chemFile);
			for (IAtomContainer container : containers) {
				File file = null;
				try {
					if (container.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
						ErrorLogger.getInstance().writeError(CDKTavernaException.MOLECULE_NOT_TAGGED_WITH_UUID,
								this.getActivityName());
						continue;
					}
					UUID uuid = UUID.fromString((String) container.getProperty(CDKTavernaConstants.MOLECULEID));
					Integer cluster = this.uuidClusterMap.get(uuid);
					container.setProperty("Cluster ID", cluster);
					if (cluster == null) {
						ErrorLogger.getInstance().writeError(CDKTavernaException.NO_CLUSTER_INFORMATION_AVAILABLE,
								this.getActivityName());
						continue;
					}
					file = this.files.get(cluster);
					if (file == null) {
						file = FileNameGenerator.getNewFile(directory, extension, name + "_Cluster_" + cluster);
						this.files.put(cluster, file);
						resultFiles.add(file.getPath());
					}
					SDFWriter writer = new SDFWriter(new FileWriter(file, true));
					writer.write(container);
					writer.close();
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + file.getPath() + "!",
							this.getActivityName(), e);
					throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRITE_FILE_ERROR
							+ file.getPath() + "!");
				}
			}
		}
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return SplitMoleculesIntoClustersActivity.SPLIT_MOLECULES_INTO_CLUSTERS;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".sdf");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + SplitMoleculesIntoClustersActivity.SPLIT_MOLECULES_INTO_CLUSTERS;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_CLUSTERING_FOLDER_NAME;
	}
}
