/* 
 * $Author: egonw $
 * $Date: 2008-05-05 12:58:11 +0200 (Mo, 05 Mai 2008) $
 * $Revision: 10819 $
 * 
 * Copyright (C) 2008 by Thomas Kuhn <thomas.kuhn@uni-koeln.de>
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
package org.openscience.cdk.applications.art2aclassification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Implements a container class to create and store one or more
 * ART2aClassificators. It classifies objects with an ART2a algorithm to a
 * previously unknown number of classes. It also contains properties to get the
 * results of the classification. This class was ported from C# code.
 * 
 * @author original C# code: Stefan Neumann, Gesellschaft fuer
 *         naturwissenschaftliche Informatik, stefan.neumann@gnwi.de<br>
 *         porting to Java: Christian Geiger, University of Applied Sciences
 *         Gelsenkirchen, 2007
 * @author Thomas Kuhn
 * 
 */
public class Art2aCollection {

	// region CLASS VARIABLES

	/**
	 * The number of ART2aClassificators stored in the Art2ACollection.
	 */
	int mNumberOfClassificators;

	/**
	 * Contains a number of ART2aClassificators.
	 */
	Art2aClassificator[] mART2aClassificators;

	/**
	 * Contains the file names for the temporary files which stores the results
	 */
	private List<String> mTemporaryFileNames = null;

	// end

	// region CONSTRUCTORS

	/**
	 * Creates a new Art2aCollection out of the data in the reader.
	 * 
	 * @param reader
	 *            which contains the information of the collection. The current
	 *            position of the reader hast to be the start element of the XML
	 *            knod "CollectionData". This knod encloses the XML knods
	 *            containing the data of the collection. At the end of the
	 *            method the position of the reader is the end element of the
	 *            "CollectionData" knod
	 */
	public Art2aCollection(XMLStreamReader reader) {
		if (reader == null) {
			throw new IllegalArgumentException("The reader is null.");
		}
		this.loadStatusFromXmlReader(reader);
	}

	/**
	 * Classifies the objects with a linear decrease of the "vigilance". This
	 * will reach to a different number of detected classes. Remark: The
	 * vigilance paramter influences the number of classes.
	 * 
	 * @param objects
	 *            Contains the objects to be classified.
	 * @param numberOfClassificators
	 *            The number "ART2aClassificators" which will be created. E.g. a
	 *            value of 3 will create 3 ART2aClassificators with a vigilance
	 *            parameter of 0, 0.5 and 1.
	 */
	public Art2aCollection(FingerprintItem[] objects, int numberOfClassificators) {
		if (objects == null) {
			throw new IllegalArgumentException("The array 'objects' is not set to an instance of an object.");
		}
		try {
			if (numberOfClassificators < 1)
				throw new IllegalArgumentException("The number of clusters must be greater 0.");
			mNumberOfClassificators = numberOfClassificators;
			double[] vigilanceParameters = new double[numberOfClassificators];
			double lowerVigilance = 0;
			double interval;
			if (numberOfClassificators == 1)
				interval = 0;
			else
				interval = 1.0 / (numberOfClassificators - 1);
			for (int i = 0; i < numberOfClassificators; i++) {
				// round value to 2 digits after decimal point
				vigilanceParameters[i] = Math.round((lowerVigilance + i * interval) * 100) / 100.0;
			}
			createClassificators(objects, vigilanceParameters);
		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}
	}

	/**
	 * Classifies the objects with only one vigilance parameter. A
	 * vigilanceParameter near 1 forces a fine classification and the number of
	 * detected classes will increase. On the other hand a value near 0 forces a
	 * coarse classification and decreases the number of detected classes.
	 * 
	 * @param objects
	 *            Contains the objects to be classified.
	 * @param vigilanceParameter
	 *            Parameter to influence the number of classes. 0 less than
	 *            vigilanceParameter less than 1.
	 */
	public Art2aCollection(FingerprintItem[] objects, double vigilanceParameter) {
		if (objects == null) {
			throw new IllegalArgumentException("The array 'objects' is not set to an instance of an object.");
		}
		try {
			if (vigilanceParameter < 0 || vigilanceParameter > 1)
				throw new IllegalArgumentException("The value of the parameter is out of boundaries.");
			mNumberOfClassificators = 1;
			double[] vigilanceParameters = new double[] { vigilanceParameter };
			createClassificators(objects, vigilanceParameters);
		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}
	}

	/**
	 * Classifies the objects with different vigilance parameters defined in the
	 * array "vigilanceParameters".
	 * 
	 * @param objects
	 *            Contains the objects to be classified.
	 * @param vigilanceParameters
	 *            Contains the vigilance parameters which influences the number
	 *            of detected classes. 0 less than vigilanceParameter less than
	 *            1.
	 */
	public Art2aCollection(FingerprintItem[] objects, double[] vigilanceParameters) {
		if (objects == null) {
			throw new IllegalArgumentException("The array 'objects' is not set to an instance of an object.");
		}
		try {
			if (vigilanceParameters == null)
				throw new IllegalArgumentException(
						"The array vigilanceParameters is not set to an instance of an object.");
			else {
				for (int i = 0; i < vigilanceParameters.length; i++) {
					if (vigilanceParameters[i] < 0 || vigilanceParameters[i] > 1)
						throw new IllegalArgumentException(
								"The array vigilanceParameters contains at least one illegal value.");
				}
			}
			mNumberOfClassificators = vigilanceParameters.length;
			createClassificators(objects, vigilanceParameters);
		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}
	}

	/**
	 * Classifies the objects with vigilance parameters between the interval
	 * [upperVigilance - lowerVigilance]. The value decreases linear. E.g.:
	 * upperVigilance = 0.5; lowerVigilance 0.3; numberOfClassificators = 5;
	 * This configuration creates the results of 5 classifcations with the
	 * vigilance values of 0.5, 0.45, 0.4, 0.35, 0.3 .
	 * 
	 * @param objects
	 *            Contains the objects to be classified.
	 * @param lowerVigilanceLimit
	 *            The lower limit of the vigilance parameters.
	 * @param upperVigilanceLimit
	 *            The upper limit of the vigilance parameters.
	 * @param numberOfClassificators
	 *            The number "ART2aClassificators" which will be created.
	 */
	public Art2aCollection(FingerprintItem[] objects, double upperVigilanceLimit, double lowerVigilanceLimit,
			int numberOfClassificators) {
		if (objects == null) {
			throw new IllegalArgumentException("The array 'objects' is not set to an instance of an object.");
		}
		try {
			if (numberOfClassificators < 1)
				throw new IllegalArgumentException("The number of clusters must be greater 0.");
			if (upperVigilanceLimit < 0 || upperVigilanceLimit > 1 || upperVigilanceLimit <= lowerVigilanceLimit)
				throw new IllegalArgumentException("The upper limit of the vigilance parameter is out of boundaries.");
			if (lowerVigilanceLimit < 0 || lowerVigilanceLimit > 1)
				throw new IllegalArgumentException("The lower limit of the vigilance parameter is out of boundaries.");
			mNumberOfClassificators = numberOfClassificators;
			double[] vigilanceParameters = new double[numberOfClassificators];
			double interval;
			if (numberOfClassificators == 1)
				interval = 0;
			else
				interval = (upperVigilanceLimit - lowerVigilanceLimit) / (numberOfClassificators - 1);
			for (int i = 0; i < numberOfClassificators; i++) {
				// round value to 2 digits after decimal point
				vigilanceParameters[i] = Math.round((lowerVigilanceLimit + i * interval) * 100) / 100.0;
			}
			createClassificators(objects, vigilanceParameters);
		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}

	}

	/**
	 * Classifies the objects with vigilance parameters between the interval
	 * [upperVigilance - lowerVigilance]. The value decreases linear. E.g.:
	 * upperVigilance = 0.5; lowerVigilance 0.3; numberOfClassificators = 5;
	 * This configuration creates the results of 5 classifcations with the
	 * vigilance values of 0.5, 0.45, 0.4, 0.35, 0.3 .
	 * 
	 * @param objects
	 *            Contains the objects to be classified.
	 * @param lowerVigilanceLimit
	 *            The lower limit of the vigilance parameters.
	 * @param upperVigilanceLimit
	 *            The upper limit of the vigilance parameters.
	 * @param numberOfClassificators
	 *            The number "ART2aClassificators" which will be created.
	 * @param saveResultsToTempFiles
	 *            True: each classification will be stored in a temporary file.
	 *            This will reduce the memory consumption. To get the absolute
	 *            file names use the method GetTemporaryFileNamesOfResults()
	 * @param maximumClassificationTime
	 *            The maximum classification time for one epoch.
	 */
	public Art2aCollection(FingerprintItem[] objects, double upperVigilanceLimit, double lowerVigilanceLimit,
			int numberOfClassificators, boolean saveResultsToTempFiles, int maximumClassificationTime) {
		if (objects == null) {
			throw new IllegalArgumentException("The array 'objects' is not set to an instance of an object.");
		}
		try {
			if (numberOfClassificators < 1)
				throw new IllegalArgumentException("The number of clusters must be greater 0.");
			if (upperVigilanceLimit < 0 || upperVigilanceLimit > 1 || upperVigilanceLimit <= lowerVigilanceLimit)
				throw new IllegalArgumentException("The upper limit of the vigilance parameter is out of boundaries.");
			if (lowerVigilanceLimit < 0 || lowerVigilanceLimit > 1)
				throw new IllegalArgumentException("The lower limit of the vigilance parameter is out of boundaries.");
			mNumberOfClassificators = numberOfClassificators;
			double[] vigilanceParameters = new double[numberOfClassificators];
			double interval;
			if (numberOfClassificators == 1)
				interval = 0;
			else
				interval = (upperVigilanceLimit - lowerVigilanceLimit) / (numberOfClassificators - 1);
			for (int i = 0; i < numberOfClassificators; i++) {
				// round value to 2 digits after decimal point
				vigilanceParameters[i] = Math.round((lowerVigilanceLimit + i * interval) * 100) / 100.0;
			}
			if (saveResultsToTempFiles) {
				mTemporaryFileNames = createClassificatorsAndSaveResultsAsTempFiles(objects, vigilanceParameters,
						maximumClassificationTime);
			} else {
				createClassificators(objects, vigilanceParameters);
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}

	}

	/**
	 * Classifies the objects with a linear decrease of the "vigilance". This
	 * will reach to a different number of detected classes. Remark: The
	 * vigilance paramter influences the number of classes.
	 * 
	 * @param dataMatrix
	 *            The dataMatrix contains the fingerprintVectors of all objects
	 *            which should be classified. Each row codes one
	 *            fingerprintVector and each column codes an object property.<br>
	 *            IMPORTANT: ALL values of the matrix must be scaled to the
	 *            interval [0,1].
	 * @param numberOfClassificators
	 *            The number "ART2aClassificators" which will be created. E.g. a
	 *            value of 3 will create 3 ART2aClassificators with a vigilance
	 *            parameter of 0, 0.5 and 1.
	 */
	public Art2aCollection(double[][] dataMatrix, int numberOfClassificators) {
		checkDataMatrix(dataMatrix);
		try {
			if (numberOfClassificators < 1)
				throw new IllegalArgumentException("The number of clusters must be greater 0.");
			mNumberOfClassificators = numberOfClassificators;
			double[] vigilanceParameters = new double[numberOfClassificators];
			double lowerVigilance = 0;
			double interval;
			if (numberOfClassificators == 1)
				interval = 0;
			else
				interval = 1.0 / (numberOfClassificators - 1);
			for (int i = 0; i < numberOfClassificators; i++) {
				// round value to 2 digits after decimal point
				vigilanceParameters[i] = Math.round((lowerVigilance + i * interval) * 100) / 100.0;
			}
			createClassificators(dataMatrix, vigilanceParameters);
		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}
	}

	/**
	 * Classifies the objects with only one vigilance parameter. A
	 * vigilanceParameter near 1 forces a fine classification and the number of
	 * detected classes will increase. On the other hand a value near 0 forces a
	 * coarse classification and decreases the number of detected classes.
	 * 
	 * @param dataMatrix
	 *            The dataMatrix contains the fingerprintVectors of all objects
	 *            which should be classified. Each row codes one
	 *            fingerprintVector and each column codes an object property.<br>
	 *            IMPORTANT: ALL values of the matrix must be scaled to the
	 *            interval [0,1].
	 * @param vigilanceParameter
	 *            Parameter to influence the number of classes. 0 less than
	 *            vigilanceParameter less than 1.
	 */
	public Art2aCollection(double[][] dataMatrix, double vigilanceParameter) {
		checkDataMatrix(dataMatrix);
		try {
			if (vigilanceParameter < 0 || vigilanceParameter > 1)
				throw new IllegalArgumentException("The value of the parameter is out of boundaries.");
			mNumberOfClassificators = 1;
			double[] vigilanceParameters = new double[] { vigilanceParameter };
			createClassificators(dataMatrix, vigilanceParameters);
		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}
	}

	/**
	 * Classifies the objects with different vigilance parameters defined in the
	 * array "vigilanceParameters".
	 * 
	 * @param dataMatrix
	 *            The dataMatrix contains the fingerprintVectors of all objects
	 *            which should be classified. Each row codes one
	 *            fingerprintVector and each column codes an object property.<br>
	 *            IMPORTANT: ALL values of the matrix must be scaled to the
	 *            interval [0,1].
	 * @param vigilanceParameters
	 *            Contains the vigilance parameters which influences the number
	 *            of detected classes. 0 less than vigilanceParameter less than
	 *            1.
	 */
	public Art2aCollection(double[][] dataMatrix, double[] vigilanceParameters) {
		checkDataMatrix(dataMatrix);
		try {
			if (vigilanceParameters == null)
				throw new IllegalArgumentException(
						"The array vigilanceParameters is not set to an instance of an object.");
			else {
				for (int i = 0; i < vigilanceParameters.length; i++) {
					if (vigilanceParameters[i] < 0 || vigilanceParameters[i] > 1)
						throw new IllegalArgumentException(
								"The array vigilanceParameters contains at least one illegal value.");
				}
			}
			mNumberOfClassificators = vigilanceParameters.length;
			createClassificators(dataMatrix, vigilanceParameters);
		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}
	}

	/**
	 * Classifies the objects with vigilance parameters between the intervall
	 * [upperVigilance - lowerVigilance]. The value decreases linear. E.g.:
	 * upperVigilance = 0.5; lowerVigilance 0.3; numberOfClassificators = 5;
	 * This configuration creates the results of 5 classifcations with the
	 * vigilance values of 0.5, 0.45, 0.4, 0.35, 0.3 .
	 * 
	 * @param dataMatrix
	 *            The dataMatrix contains the fingerprintVectors of all objects
	 *            which should be classified. Each row codes one
	 *            fingerprintVector and each column codes an object property.<br>
	 *            IMPORTANT: ALL values of the matrix must be scaled to the
	 *            interval [0,1].
	 * @param lowerVigilanceLimit
	 *            The lower limit of the vigilance parameters.
	 * @param upperVigilanceLimit
	 *            The upper limit of the vigilance parameters.
	 * @param numberOfClassificators
	 *            The number "ART2aClassificators" which will be created.
	 */
	public Art2aCollection(double[][] dataMatrix, double upperVigilanceLimit, double lowerVigilanceLimit,
			int numberOfClassificators) {
		checkDataMatrix(dataMatrix);
		try {
			if (numberOfClassificators < 1)
				throw new IllegalArgumentException("The number of clusters must be greater 0.");
			if (upperVigilanceLimit < 0 || upperVigilanceLimit > 1 || upperVigilanceLimit <= lowerVigilanceLimit)
				throw new IllegalArgumentException("The upper limit of the vigilance parameter is out of boundaries.");
			if (lowerVigilanceLimit < 0 || lowerVigilanceLimit > 1)
				throw new IllegalArgumentException("The lower limit of the vigilance parameter is out of boundaries.");
			mNumberOfClassificators = numberOfClassificators;
			double[] vigilanceParameters = new double[numberOfClassificators];
			double interval;
			if (numberOfClassificators == 1)
				interval = 0;
			else
				interval = (upperVigilanceLimit - lowerVigilanceLimit) / (numberOfClassificators - 1);
			for (int i = 0; i < numberOfClassificators; i++) {
				// round value to 2 digits after decimal point
				vigilanceParameters[i] = Math.round((lowerVigilanceLimit + i * interval) * 100) / 100.0;
			}
			createClassificators(dataMatrix, vigilanceParameters);
		} catch (Exception exception) {
			throw new RuntimeException("Unable to create an instance of the ART2aCollection.", exception);
		}

	}

	// end

	// region PUBLIC METHODS

	/**
	 * Adds a new classificator to the collection. The new classificator gets
	 * its data to classify from the first classificator in the array of
	 * classificators.
	 * 
	 * @param vigilanceParameter
	 *            The vigilance parameter of the new classificator
	 */
	public void addClassificator(double vigilanceParameter) {
		if (vigilanceParameter < 0 || vigilanceParameter > 1)
			throw new IllegalArgumentException("The value of the parameter is out of boundaries.");
		Art2aClassificator newClassificator = new Art2aClassificator(mART2aClassificators[0].mObjectArray,
				vigilanceParameter);
		newClassificator.classify();
		mNumberOfClassificators = mNumberOfClassificators + 1;
		mART2aClassificators = Arrays.copyOf(this.mART2aClassificators, mNumberOfClassificators);
		mART2aClassificators[mNumberOfClassificators - 1] = newClassificator;
	}

	/**
	 * Gets all information of the collection from a XmlReader
	 * 
	 * @param reader
	 *            which contains the information of the collection. The current
	 *            position of the reader hast to be the start element of the XML
	 *            knod "CollectionData". This knod encloses the XML knods
	 *            containing the data of the collection. At the end of the
	 *            method the position of the reader is the end element of the
	 *            "CollectionData" knod
	 * @throws RuntimeException
	 *             when the information of the classificator cannot be loaded
	 *             from the xml-reader.
	 * 
	 */
	public void loadStatusFromXmlReader(XMLStreamReader reader) throws RuntimeException {
		try {
			if (reader.getLocalName() != "CollectionData") {
				throw new IllegalArgumentException("Reader postition was wrong.");
			}
			reader.next();
			reader.next();
			this.mNumberOfClassificators = Integer.valueOf(reader.getText());
			this.mART2aClassificators = new Art2aClassificator[this.mNumberOfClassificators];
			reader.nextTag();
			int i = 0;
			while (reader.nextTag() == 1) {
				if (reader.getLocalName() != "ClassificatorData") {
					throw new RuntimeException("Unexpected start tag");
				}
				this.mART2aClassificators[i] = new Art2aClassificator(reader);
				i++;

			}
		} catch (Exception e) {
			throw new RuntimeException("Information for the collection cannot be loaded from the XML reader.", e);
		}
	}

	/**
	 * Removes a classificator from the collection. The collection has to
	 * contain at least one classificator.
	 * 
	 * @param index
	 *            Index of the classificator in the array of classificators.
	 * @throws IllegalStateException
	 *             if the collection does not contain at least two
	 *             classificators
	 * @throws IllegalArgumentException
	 *             if the index is not within the bounds of the array of
	 *             classificators
	 */
	public void removeClassificator(int index) throws IllegalStateException, IllegalArgumentException {
		if (mNumberOfClassificators < 2) {
			throw new IllegalStateException(
					"Removing of classificator not possible. The collection has to contain at least one classificator.");
		}
		if (index < 0 || index >= mNumberOfClassificators) {
			throw new IllegalArgumentException("Wrong index.");
		}
		Art2aClassificator[] tmpClassificators = new Art2aClassificator[mNumberOfClassificators - 1];
		for (int i = 0; i < index; i++) {
			tmpClassificators[i] = mART2aClassificators[i];
		}
		for (int i = index; i < mNumberOfClassificators; i++) {
			mART2aClassificators[i] = mART2aClassificators[i + 1];
		}
	}

	/**
	 * Saves all information of the ART2aCollection to a XmlWriter.
	 * 
	 * @param writer
	 *            The XmlWriter to write the data in. All XML knods containing
	 *            data are enclosed by the "CollectionData" knod. At the end of
	 *            the method the end element of the "CollectionData" knod will
	 *            be written.
	 * @throws RuntimeException
	 *             when the information of the collection cannot be saved to the
	 *             xml-writer.
	 */
	public void saveStatusToXmlWriter(XMLStreamWriter writer) throws RuntimeException {
		try {
			writer.writeStartElement("CollectionData");
			writer.writeStartElement("NumberOfClassificators");
			writer.writeCharacters(Integer.toString(this.mNumberOfClassificators));
			writer.writeEndElement();
			for (int i = 0; i < this.mNumberOfClassificators; i++) {
				this.mART2aClassificators[i].saveStatusToXmlWriter(writer);
			}
			writer.writeEndElement();
		} catch (Exception e) {
			throw new RuntimeException("Information of the classificator cannot be saved to the xml-writer.", e);
		}
	}

	/**
	 * Get the file names of the temporary files which stores the results of the
	 * classification.
	 * 
	 * @return
	 */
	public List<String> getTemporaryFileNamesOfResults() {
		return mTemporaryFileNames;
	}

	// end

	// region PROPERTIES

	/**
	 * Get-property
	 * 
	 * @return The ART2aClassificators created by the ART2aCollection.
	 */
	public Art2aClassificator[] getClassificators() {
		return mART2aClassificators;
	}

	/**
	 * Get-property
	 * 
	 * @return The number of ART2aClassificators stored in the ART2aCollection.
	 */
	public int getNumberOfClassificators() {
		return mNumberOfClassificators;
	}

	// end

	// region PRIVATE METHODS

	/**
	 * Checks the validity of the dataMatrix.
	 * 
	 * @param dataMatrix
	 *            The double array to be checked.
	 * @throws IllegalArgumentException
	 *             when the dataMatrix is invalid.
	 */
	private void checkDataMatrix(double[][] dataMatrix) {
		if (dataMatrix == null) {
			throw new IllegalArgumentException("The datamatrix is not set to an instance of an object.");
		}
		int numberOfVectors;
		int numberOfComponents;
		boolean valueNotInRangeFlag = false;

		numberOfVectors = dataMatrix.length;
		if (numberOfVectors <= 0) {
			throw new IllegalArgumentException("The numberof vectors must be greater 0.");
		}
		numberOfComponents = dataMatrix[0].length;
		for (int i = 0; i < numberOfVectors; i++) {
			if (numberOfComponents != dataMatrix[i].length) {
				throw new IllegalArgumentException("The vectors differ in length.");
			}
			numberOfComponents = dataMatrix[i].length;
			for (int j = 0; j < numberOfComponents; j++) {
				if (dataMatrix[i][j] > 1 || dataMatrix[i][j] < 0)
					valueNotInRangeFlag = true;
			}
		}
		if (valueNotInRangeFlag == true) {
			throw new IllegalArgumentException("At least one value of the dataMatrix is out of interval [0, 1].");
		}
	}

	/**
	 * Creates the ART2aClassificators.
	 * 
	 * @param objects
	 *            The objects to be classified.
	 * @param vigilanceParameters
	 *            The vigilance parameters.
	 */
	private void createClassificators(FingerprintItem[] objects, double[] vigilanceParameters) {
		Art2aClassificator myART;
		try {
			mART2aClassificators = new Art2aClassificator[mNumberOfClassificators];
			for (int i = 0; i < mNumberOfClassificators; i++) {
				myART = new Art2aClassificator(objects, vigilanceParameters[i]);
				myART.classify();
				mART2aClassificators[i] = myART;
			}
		} catch (Exception exp) {
			throw new RuntimeException("An error occured while creating the classificators.", exp);
		}
	}

	/**
	 * Creates the ART2aClassificators.
	 * 
	 * @param objects
	 *            The objects to be classified.
	 * @param vigilanceParameters
	 *            The vigilance parameters.
	 * @param maximumClassificationTime
	 *            The maximum classification time for one epoch.
	 */
	private List<String> createClassificatorsAndSaveResultsAsTempFiles(FingerprintItem[] objects,
			double[] vigilanceParameters, int maximumClassificationTime) {
		Art2aClassificator myART;
		try {
			File tempFile;
			FileWriter fwriter;
			XMLStreamWriter writer;
			BufferedWriter bwriter;
			XMLOutputFactory ouputFactory;
			List<String> fileNames = new ArrayList<String>(mNumberOfClassificators);
			mART2aClassificators = new Art2aClassificator[mNumberOfClassificators];
			for (int i = 0; i < mNumberOfClassificators; i++) {
				// Classifiy first
				myART = new Art2aClassificator(objects, vigilanceParameters[i]);
				myART.setMaximumClassificationTime(maximumClassificationTime);
				myART.setConvergenceFlag(true);
				myART.classify();
				// Store the results
				tempFile = File.createTempFile(
						"ART2a_Result" + String.valueOf(i) + "of" + String.valueOf(mNumberOfClassificators)
								+ "Classifications", ".xml");
				fwriter = new FileWriter(tempFile);
				bwriter = new BufferedWriter(fwriter);
				ouputFactory = XMLOutputFactory.newInstance();
				writer = ouputFactory.createXMLStreamWriter(bwriter);
				writer.writeStartDocument();
				myART.saveResultToXmlWriter(writer);
				writer.writeEndDocument();
				writer.close();
				fileNames.add(tempFile.getAbsolutePath());
				fwriter.close();

			}
			return fileNames;
		} catch (Exception exp) {
			throw new RuntimeException("An error occured while creating the classificators.", exp);
		}
	}

	/**
	 * Creates the ART2aClassificators.
	 * 
	 * @param dataMatrix
	 *            The data matrix containing the vectors to be classified.
	 * @param vigilanceParameters
	 *            The vigilance parameters.
	 */
	private void createClassificators(double[][] dataMatrix, double[] vigilanceParameters) {
		Art2aClassificator myART;
		try {
			mART2aClassificators = new Art2aClassificator[mNumberOfClassificators];
			for (int i = 0; i < mNumberOfClassificators; i++) {
				myART = new Art2aClassificator(dataMatrix, vigilanceParameters[i]);
				myART.classify();
				mART2aClassificators[i] = myART;
			}
		} catch (Exception exp) {
			throw new RuntimeException("An error occured while creating the classificators.", exp);
		}
	}

	// end

}
