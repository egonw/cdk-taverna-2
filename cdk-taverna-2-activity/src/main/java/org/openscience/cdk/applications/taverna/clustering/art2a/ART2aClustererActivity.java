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
package org.openscience.cdk.applications.taverna.clustering.art2a;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.art2aclassification.Art2aClassificator;
import org.openscience.cdk.applications.art2aclassification.FingerprintItem;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.basicutilities.Tools;
import org.openscience.cdk.applications.taverna.io.XMLFileIO;

/**
 * Class which implements a local worker for the cdk-taverna project. This
 * worker uses an implementation of the ART2A Clusterer to classify a given
 * set of fingerprint items.
 * 
 * @author Thomas Kuhn
 * @author Andreas Truzskowski
 * 
 */
public class ART2aClustererActivity extends AbstractCDKActivity {

	public static final String ART2A_CLUSTERER_ACTIVITY = "ART-2a Clusterer";

	/**
	 * Boolean which allows a scaling of the input fingerprint between 0 and 1
	 */
	private boolean scaleFingerprintItemToInternalZeroOne = true;
	/**
	 * The number of classificator to calculate
	 */
	private int numberOfClassifications = 10;
	/**
	 * The upper limit for the vigilance parameter
	 */
	private double upperVigilanceLimit = 0.9;
	/**
	 * The lower limit for the vigilance parameter
	 */
	private double lowerVigilanceLimit = 0.1;
	/**
	 * The maximum classification time for one epoch
	 */
	private int maximumClassificationTime = 15;
	/**
	 * The flag is used to switch between deterministic random and random
	 * random. If mDeterministicRandom = true a seed of 1 is used. If
	 * mDeterministicRandom = false no seed is set explicitly
	 */
	private boolean deterministicRandom = true;

	/**
	 * This flag enables the switch between the two convergence criteria.
	 * convergenceFlag = true means the clustering is convergent if the angels
	 * between two cluster has the required similarity between two epochs.
	 * convergenceFlag = false means the clustering is convergent if the number
	 * of cluster have the same composition as in the previous epoch.
	 */
	private boolean convergenceFlag = true;

	/**
	 * The required similarity for the use of the convergence flag. This is the
	 * required similarity used if the convergence flag is set to true.
	 */
	private double requiredSimilarity = 0.99;

	/**
	 * Creates a new instance.
	 */
	public ART2aClustererActivity() {
		this.INPUT_PORTS = new String[] { "Fingerprint Items", "File" };
		this.OUTPUT_PORTS = new String[] { "ART-2a Files" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[1], 0, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<FingerprintItem> fingerprintItemList = this.getInputAsList(this.INPUT_PORTS[0], FingerprintItem.class);
		File targetFile = this.getInputAsFile(this.INPUT_PORTS[1]);
		String directory = Tools.getDirectory(targetFile);
		String name = Tools.getFileName(targetFile);
		String extension = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
		// Do work
		FingerprintItem[] fingerprintItemArray = new FingerprintItem[fingerprintItemList.size()];
		fingerprintItemList.toArray(fingerprintItemArray);
		this.scaleFingerprintItemToInternalZeroOne = (Boolean) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_SCALE_FINGERPRINT_ITEMS);
		this.numberOfClassifications = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_NUMBER_OF_CLASSIFICATIONS);
		this.upperVigilanceLimit = (Double) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_UPPER_VIGILANCE_LIMIT);
		this.lowerVigilanceLimit = (Double) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_LOWER_VIGILANCE_LIMIT);
		this.maximumClassificationTime = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_MAXIMUM_CLASSIFICATION_TIME);

		if (this.numberOfClassifications < 1)
			throw new IllegalArgumentException("The number of clusters must be greater 0.");
		if (this.upperVigilanceLimit < 0 || this.upperVigilanceLimit > 1
				|| this.upperVigilanceLimit <= this.lowerVigilanceLimit)
			throw new IllegalArgumentException("The upper limit of the vigilance parameter is out of boundaries.");
		if (this.lowerVigilanceLimit < 0 || this.lowerVigilanceLimit > 1)
			throw new IllegalArgumentException("The lower limit of the vigilance parameter is out of boundaries.");

		double[] vigilanceParameters = new double[this.numberOfClassifications];
		double interval;
		if (this.numberOfClassifications == 1)
			interval = 0;
		else
			interval = (this.upperVigilanceLimit - this.lowerVigilanceLimit) / (this.numberOfClassifications - 1);
		for (int i = 0; i < this.numberOfClassifications; i++) {
			// round value to 2 digits after decimal point
			vigilanceParameters[i] = Math.round((this.lowerVigilanceLimit + i * interval) * 100) / 100.0;
		}
		if (this.scaleFingerprintItemToInternalZeroOne) {
			Art2aClassificator.scaleFingerprintVectorComponentsToIntervalZeroOne(fingerprintItemArray);
		}
		XMLStreamWriter writer;
		Art2aClassificator myART;
		XMLFileIO xmlFileIO = new XMLFileIO();
		List<String> resultFiles = new ArrayList<String>(this.numberOfClassifications);
		for (int i = 0; i < this.numberOfClassifications; i++) {
			try {
				// Classifiy first
				myART = new Art2aClassificator(fingerprintItemArray, vigilanceParameters[i]);
				myART.setMaximumClassificationTime(this.maximumClassificationTime);
				myART.setConvergenceFlag(this.convergenceFlag);
				myART.setRequiredSimilarity(this.requiredSimilarity);
				myART.setDeterministicRandom(this.deterministicRandom);
				myART.classify();
				// Store the results
				String desc= "_ART2a_Result" + String.valueOf(i + 1) + "of"
						+ String.valueOf(this.numberOfClassifications) + "_";
				File file = FileNameGenerator.getNewFile(directory, extension, name + desc);
				writer = xmlFileIO.getXMLStreamWriterWithCompression(file);
				writer.writeStartDocument();
				myART.saveResultToXmlWriter(writer);
				writer.writeEndDocument();
				writer.close();
				resultFiles.add(file.getPath());
				xmlFileIO.closeXMLStreamWriter();
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(
						"Error during classification step " + String.valueOf(i + 1) + " of "
								+ String.valueOf(this.numberOfClassifications) + "!", this.getActivityName(), e);
			}
		}
		// Set output
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return ART2aClustererActivity.ART2A_CLUSTERER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".art2a");
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_CLASSIFICATIONS, this.numberOfClassifications);
		properties.put(CDKTavernaConstants.PROPERTY_UPPER_VIGILANCE_LIMIT, this.upperVigilanceLimit);
		properties.put(CDKTavernaConstants.PROPERTY_LOWER_VIGILANCE_LIMIT, this.lowerVigilanceLimit);
		properties.put(CDKTavernaConstants.PROPERTY_MAXIMUM_CLASSIFICATION_TIME, this.maximumClassificationTime);
		properties
				.put(CDKTavernaConstants.PROPERTY_SCALE_FINGERPRINT_ITEMS, this.scaleFingerprintItemToInternalZeroOne);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ART2aClustererActivity.ART2A_CLUSTERER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ART2A_FOLDER_NAME;
	}
}
