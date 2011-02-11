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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Implements an ART-2A-Algorithm of rapid, stable and unsupervised clustering
 * for open categorical problems.<br>
 * LITERATURE SOURCE:<br>
 * Original : G.A. Carpenter etal., Neural Networks 4 (1991) 493-504<br>
 * Secondary : Wienke etal., Chemometrics and Intelligent Laboratory Systems 24
 * (1994), 367-387<br>
 * This class was ported from C# code.
 * 
 * @author original C# code: Stefan Neumann, Gesellschaft fuer
 *         naturwissenschaftliche Informatik, stefan.neumann@gnwi.de<br>
 *         porting to Java: Christian Geiger, University of Applied Sciences
 *         Gelsenkirchen, 2007
 * @author Thomas Kuhn
 */
public class Art2aClassificator {

	// region CONSTANTS

	/**
	 * The default value of the LearningParameter.
	 */
	final double mDEFAULT_LEARNING_PARAMETER = 0.01;

	/**
	 * The default value of the RequiredSimilarity.
	 */
	final double mDEFAULT_EXIT_THRESHOLD = 0.99;

	/**
	 * The default value of the MaximumNumberOfEpochs.
	 */
	final int mDEFAULT_MAXIMUM_NUMBER_OF_EPOCHS = Integer.MAX_VALUE;

	/**
	 * The default value of the MaximumClassificationTime in seconds.
	 */
	final int mDEFAULT_MAXIMUM_CLASSIFICATION_TIME = 15;

	// end

	// region CLASS VARIABLES

	/**
	 * Array of objecs which should be classified.
	 */
	FingerprintItem[] mObjectArray;

	/**
	 * Contains the objects of the selected classes.
	 */
	FingerprintItem[][] mObjectsOfSelectedClasses;

	/**
	 * The objects in all classes: mObjectsInClasses[i][] corresponds to
	 * mNumberOfObjectsInClasses[i]
	 */
	Object[][] mObjectsOfAllClasses;

	/**
	 * True: Nullclass is selected.<br>
	 * False: Nullclass is not selected.
	 */
	boolean mNullclassSelectFlag;

	/**
	 * True: The classification was successful and the results have been
	 * successfully calculated.<br>
	 * False: The classification ended abnormaly and some or all results have
	 * not been calculated.
	 */
	boolean mClassificationCompleteFlag;

	/**
	 * Parameter to influence the number of classes. 0 less than or equal to
	 * vigilanceParameter less than or equal to 1.
	 */
	double mVigilanceParameter;

	/**
	 * Parameter to define the final similarity between the class vectors of the
	 * current and the previous epoche. 0 less than or equal to
	 * RequiredSimilarity less than or equal to 1 Default: 0.99
	 */
	double mRequiredSimilarity;

	/**
	 * Parameter to define the intensity of keeping the old class vector in mind
	 * before the system adapts it to the new sample vector.
	 */
	double mLearningParameter;

	/**
	 * All matrixvalues less than this threshold are set to zero. An increase of
	 * the threshold leads to greater contrastenhancement and to a faster
	 * stability of the classvectors. 0 less than or equal to
	 * ThresholdForContrastEnhancement less than or equal to 1 /
	 * Sqr(NumberOfComponents);<br>
	 * Default: 1 / Sqr(NumberOfComponents - 1)
	 */
	double mThresholdForContrastEnhancement;

	/**
	 * The value influences the number of detected classes. A high value
	 * increase the number of classes. 0 less than or equal to
	 * InputScalingFactor less than or equal to 1 / Sqr(NumberOfComponents);<br>
	 * Default: 1 / Sqr(NumberOfComponents - 1)
	 */
	double mInputScalingFactor;

	/**
	 * The dataMatrix contains the fingerprintVectors of all object which should
	 * be classified. Each row codes one fingerprintVector and each column codes
	 * an objectproperty.
	 */
	double[][] mDataMatrix;

	/**
	 * The matrix contains the class vectors.
	 */
	double[][] mClassMatrix;

	/**
	 * Classmatrix of the previous epoch. Is needed to check the convergence of
	 * the system.
	 */
	double[][] mClassMatrixOld;

	/**
	 * The angles between all classes.
	 */
	double[][] mInterAngleBetweenClasses;

	/**
	 * The weight of component in classes
	 */
	double[][] mComponentWeightInClasses;

	/**
	 * The current randomly selected sample vector from the datamatrix.
	 */
	double[] mSampleVector;

	/**
	 * Represents the class information of each vector. mClassView[3]=5 means
	 * vector no. 4 is in class no.6.
	 */
	int[] mClassView;

	/**
	 * Contains the indices of the objects which are in the nullclass.
	 */
	int[] mVectorsInNullClass;

	/**
	 * ClassView of the previous epoch. Is needed to check the convergence of
	 * the system.
	 */
	int[] mClassViewOld;

	/**
	 * The vector indices in random order. No doublets.
	 */
	int[] mSampleVectorsInRandomOrder;

	/**
	 * mNumberOfVectorsInClass[i] : get the number of datavectors in the class
	 * of index i
	 */
	int[] mNumberOfVectorsInClass;

	/**
	 * Index for descending sort of detected classes according to the number of
	 * vectors in class
	 */
	int[] mClassDescentSortIndex;

	/**
	 * Number of selected data vectors.
	 */
	int[] mSelectedClass;

	/**
	 * Number of non selected data vectors.
	 */
	int[] mNonSelectedClass;

	/**
	 * mSelectClassFlag[i] = true : Class i is selected ;<br>
	 * mSelectClassFlag[i] = false : Class i is NOT selected
	 */
	boolean[] mSelectClassFlag;

	/**
	 * true: System will converge if the scalar product of the classes of the
	 * current and the previous epoch is less than RequiredSimilarity.<br>
	 * false: System will be converge if the classification does not change
	 * after one epoch.
	 */
	boolean mConvergenceFlag;

	/**
	 * The number of components of a data vector.
	 */
	int mNumberOfComponents;

	/**
	 * The number of vectors int the data matrix.
	 */
	int mNumberOfVectors;

	/**
	 * The number of epochs. One epoch is finished when all sample vectors have
	 * been looped through the algortihm.
	 */
	int mNumberOfEpochs;

	/**
	 * The number of selected classes.
	 */
	int mNumberOfSelectedClass;

	/**
	 * The number of detected classes.
	 */
	int mNumberOfDetectedClasses;

	/**
	 * The number of selected data vectors.
	 */
	int mNumberOfSelectedVectors;

	/**
	 * The number of non selected data vectors.
	 */
	int mNumberOfNonSelectedClass;

	/**
	 * The maximum number of data vectors of all detected classes.
	 */
	int mMaximumNumberOfVectorsInDetectedClasses;

	/**
	 * Limit of the number of classes which are allowed to detect.
	 */
	int mMaximumNumberOfClasses;

	/**
	 * The number of data vectors in the nullclass.
	 */
	int mNumberOfVectorsInNullClass;

	/**
	 * The limit of the number of epochs. MaximumNumberOfEpochs greater than 1
	 */
	int mMaximumNumberOfEpochs;

	/**
	 * The timelimit of the classification in seconds.
	 */
	int mMaximumClassificationTime;

	/**
	 * The seed value for permutation of the vector field.
	 */
	int mSeedValue = 1;

	/**
	 * The default value for the "InputScalingFactor".
	 */
	double mDefaultInputScalingFactor;

	/**
	 * The default value for the "ThresholdForContrastEnhancement".
	 */
	double mDefaultThresholdForContrastEnhancement;

	/**
	 * The flag is used to switch between deterministic random and random
	 * random. If mDeterministicRandom = true a seed of 1 is used. If
	 * mDeterministicRandom = false no seed is set explicitly
	 */
	boolean mDeterministicRandom = true;

	/**
	 * The mCorrespondingObjectArray contain the corresponding objects from
	 * input fingerprint item array.
	 */
	Object[] mCorrespondingObjectArray;

	private Art2aClassificatorResult mART2AClassificationResult;

	// end

	// region CONSTRUCTORS

	/**
	 * Implements an ART-2A-Algorithm which classifies a set of
	 * fingerprintVectors coded in a datamatrix without defining the number of
	 * classes. <br>
	 * Remark: A vigilanceParameter near 1 forces a fine classification and the
	 * number of detected classes will increase.On the other hand a value near 0
	 * forces a coarse classification and decreases the number of detected
	 * classes.<br>
	 * 
	 * @param dataMatrix
	 *            The dataMatrix contains the fingerprintVectors of all objects
	 *            which should be classified. Each row codes one
	 *            fingerprintVector and each column codes an object property.<br>
	 *            IMPORTANT: ALL values of the matrix must be scaled to the
	 *            interval [0,1].
	 * @param vigilanceParameter
	 *            Parameter to influence the number of classes. 0 less than or
	 *            equal to vigilanceParameter less than or equal to 1.
	 */
	public Art2aClassificator(double[][] dataMatrix, double vigilanceParameter) {
		if (dataMatrix == null) {
			throw new IllegalArgumentException("The data matrix is not set to an instance of an object.");
		}
		checkDataMatrix(dataMatrix);
		mDataMatrix = dataMatrix;
		this.setVigilanceParameter(vigilanceParameter);
		mNumberOfVectors = mDataMatrix.length;
		mNumberOfComponents = mDataMatrix[0].length;
		mDefaultThresholdForContrastEnhancement = 1 / Math.sqrt(mNumberOfComponents + 1);
		mDefaultInputScalingFactor = 1 / Math.sqrt(mNumberOfComponents + 1);
		mClassificationCompleteFlag = false;
		resetClassificator();
		mObjectArray = null;
	}

	/**
	 * Implements an ART-2A-Algorithm which classifies a set of
	 * fingerprintVectors coded in a datamatrix without defining the number of
	 * classes. <br>
	 * A vigilanceParameter near 1 forces a fine classification and the number
	 * of detected classes will increase. On the other hand a value near 0
	 * forces a coarse classification and decreases the number of detected
	 * classes.<br>
	 * HINT: Use the method "scaleFingerprintVectorComponentsToIntervalZeroOne"
	 * to scale the fingerprintVectors of the objects.
	 * 
	 * @param objects
	 *            Array of objects to be classified. All objects must implement
	 *            the FingerprintItem interface. Each component of the vector
	 *            codes one "property" of the object.<br>
	 *            IMPORTANT: All components must be scaled to the interval [0,
	 *            1].
	 * @param vigilanceParameter
	 *            Parameter to influence the number of classes. 0 less than or
	 *            equal to vigilanceParameter less than or equal to 1.
	 */
	public Art2aClassificator(FingerprintItem[] objects, double vigilanceParameter) {
		if (objects == null) {
			throw new IllegalArgumentException("The array 'objects' is not set to an instance of an object.");
		}
		mObjectArray = objects;
		this.setVigilanceParameter(vigilanceParameter);
		double[][] dataMatrix = getDataMatrixFromClassificationObject(objects);
		checkDataMatrix(dataMatrix);
		mDataMatrix = dataMatrix;
		mNumberOfVectors = mDataMatrix.length;
		mNumberOfComponents = mDataMatrix[0].length;
		mDefaultThresholdForContrastEnhancement = 1 / Math.sqrt(mNumberOfComponents + 1);
		mDefaultInputScalingFactor = 1 / Math.sqrt(mNumberOfComponents + 1);
		mClassificationCompleteFlag = false;
		resetClassificator();
	}

	/**
	 * Gets an ART2aClassificator from a XmlReader which contains a xml stream
	 * created by "saveStatusToXmlWriter".
	 * 
	 * @param xmlReader
	 *            The XmlStreamReader which contains the xml file.
	 */
	public Art2aClassificator(XMLStreamReader xmlReader) {
		loadStatusFromXmlReader(xmlReader);
	}

	/**
	 * Gets an ART2aClassificator from a XmlReader which contains a xml stream
	 * created by "saveStatusToXmlWriter".
	 * 
	 * @param xmlReader
	 *            The XmlStreamReader which contains the xml file.
	 * @param loadResultOnly
	 *            If true: load only the result xml stream. This one do NOT
	 *            contain the original data matrix.
	 */
	public Art2aClassificator(XMLStreamReader xmlReader, boolean loadResultOnly) {
		if (loadResultOnly) {
			loadResultFromXmlReader(xmlReader);
		} else {
			loadStatusFromXmlReader(xmlReader);
		}
	}

	// end

	// region PUBLIC METHODS

	/**
	 * Starts the clustering ART-2A-Algorithm.<br>
	 * Classifies the "mNumberOfVectors" sample vectors (with
	 * "mNumberOfComponents" features in each case)of data matrix "mDataMatrix"
	 * by grouping into classes according to the vigilance parameter
	 * "mVigilanceParameter". If all features of a data vector are "0", the
	 * vector is put into the null-class.
	 * 
	 * @throws RuntimeException
	 *             when the classification failed.
	 */
	public Art2aClassificatorResult classify() throws RuntimeException {
		int indexOfSampleVector;
		int indexOfWinnerClass;
		double rhoForExistingClasses;
		long timerStart;
		long classificationTime = 0;
		boolean convergenceFlag = false;
		boolean nullVectorFlag = true;
		double normOfSampleVector;
		double sumOfComponentsOfSample;
		double rho;
		mNumberOfDetectedClasses = 0;
		mNumberOfEpochs = 0;
		mClassificationCompleteFlag = false;
		initialzeMatrices();

		try {
			// Initializing datamatrices
			double initValue = 1 / Math.sqrt(mNumberOfComponents);
			for (int i = 0; i < mMaximumNumberOfClasses; i++) {
				for (int j = 0; j < mNumberOfComponents; j++) {
					mClassMatrix[i][j] = initValue;
					mClassMatrixOld[i][j] = initValue;
				}
			}
			// deleting the rescue-array
			for (int k = 0; k < mNumberOfVectors; k++) {
				mClassViewOld[k] = 0;
			}
			timerStart = System.currentTimeMillis();
			// Begin Classification
			int numberOfIterations = 0;
			while (!convergenceFlag && mNumberOfEpochs <= mMaximumNumberOfEpochs
					&& classificationTime / 1000 < mMaximumClassificationTime) {
				mNumberOfEpochs++;
				System.out.println("NumberOfEpochs: " + String.valueOf(mNumberOfEpochs));
				System.out.println("NumberOfClasses: " + String.valueOf(mNumberOfDetectedClasses));
				// initialzes an array with values from 1 to mNumverOfVectors in
				// random order
				randomizeVectorIndices();
				mNumberOfVectorsInNullClass = 0;
				// loop over all sample-vectors
				for (int i = 0; i < mNumberOfVectors; i++) {
					indexOfSampleVector = mSampleVectorsInRandomOrder[i];
					nullVectorFlag = true;
					mSampleVector = new double[mNumberOfComponents];
					for (int j = 0; j < mNumberOfComponents; j++) {
						// get the sample-vector from the datamatrix
						mSampleVector[j] = mDataMatrix[indexOfSampleVector][j];
						if (mSampleVector[j] != 0) {
							nullVectorFlag = false;
						}
					}
					if (nullVectorFlag) {
						// sample is a nullvector
						// put sample in nullclass
						mVectorsInNullClass[mNumberOfVectorsInNullClass] = indexOfSampleVector;
						mClassView[indexOfSampleVector] = -1;
						mNumberOfVectorsInNullClass++;
					} else {
						// sample is not a null-vector
						// normalize Vector and set values, which are smaller
						// than the threshold to null
						normOfSampleVector = getLengthOfVector(mSampleVector);
						normOfSampleVector = 1 / normOfSampleVector;
						for (int k = 0; k < mNumberOfComponents; k++) {
							// normalizing the vector
							mSampleVector[k] *= normOfSampleVector;
							// setting components to 0, if they are beneath the
							// threshold
							if (mSampleVector[k] <= mThresholdForContrastEnhancement) {
								mSampleVector[k] = 0;
							}
						}
						// normalize the vector after contastenhancement
						normalizeVector(mSampleVector);

						// evaluate the winner-class

						// if no class are present, the samplevector is the
						// first class
						// put the samplevector into the classmatrix
						if (mNumberOfDetectedClasses == 0) {
							mClassMatrix[0] = mSampleVector;
							mClassView[indexOfSampleVector] = mNumberOfDetectedClasses;
							mNumberOfDetectedClasses++;
						} else {
							// a least one class exists
							sumOfComponentsOfSample = 0;
							// calculate the sum of all components of the
							// samplevector
							for (int j = 0; j < mSampleVector.length; j++) {
								sumOfComponentsOfSample += mSampleVector[j];
							}
							// calculate rho for uncomitted nodes
							rho = mInputScalingFactor * sumOfComponentsOfSample;
							indexOfWinnerClass = mNumberOfDetectedClasses;
							// evaluate rho for existing classes
							for (int j = 0; j < mNumberOfDetectedClasses; j++) {
								rhoForExistingClasses = 0;
								for (int k = 0; k < mNumberOfComponents; k++) {
									rhoForExistingClasses += (mSampleVector[k] * mClassMatrix[j][k]);
								}
								// if rho of the existing class is greater than
								// rho for uncomitted nodes, replace the second
								// one
								if (rhoForExistingClasses > rho) {
									rho = rhoForExistingClasses;
									indexOfWinnerClass = j;
								}
							}
							// if the samplevector did not fit with one of the
							// existing classes, a novelty is detected and the
							// samplevector builds a new class
							if (indexOfWinnerClass >= mNumberOfDetectedClasses || rho < mVigilanceParameter) {
								if (mNumberOfDetectedClasses == mMaximumNumberOfClasses) {
									break;
								} else {
									mNumberOfDetectedClasses++;
									// save the number of the detected class
									// into the ClassView
									mClassView[indexOfSampleVector] = mNumberOfDetectedClasses - 1;
									int classindex = mNumberOfDetectedClasses - 1;
									// copy samplevector to the classmatrix

									mClassMatrix[classindex] = mSampleVector;
								}
							}
							// the winner-class ist one of the existing
							// class
							else {
								// set sample-components to zero, if they are
								// smaller than the treshhold
								for (int j = 0; j < mNumberOfComponents; j++) {
									if (mClassMatrix[indexOfWinnerClass][j] <= mThresholdForContrastEnhancement) {
										mSampleVector[j] = 0;
									}
								}
								normOfSampleVector = getLengthOfVector(mSampleVector);
								// modify the samplevector
								// speed optimized
								double factor1 = mLearningParameter / normOfSampleVector;
								double factor2 = 1 - mLearningParameter;

								for (int j = 0; j < mNumberOfComponents; j++) {
									mSampleVector[j] = mSampleVector[j] * factor1 + factor2
											* mClassMatrix[indexOfWinnerClass][j];
								}

								normOfSampleVector = 1 / getLengthOfVector(mSampleVector);
								;
								// put the modified and normalized samplevector
								// into the classmatrix
								for (int j = 0; j < mNumberOfComponents; j++) {
									mSampleVector[j] *= normOfSampleVector;
								}
								mClassMatrix[indexOfWinnerClass] = mSampleVector;
								mClassView[indexOfSampleVector] = indexOfWinnerClass;
							}
						}
					}
				}
				classificationTime = System.currentTimeMillis() - timerStart;
				// check whether the classification-vectors are converged
				convergenceFlag = isNetworkConvergent();
				numberOfIterations++;
			}
		} catch (Exception exception) {
			throw new RuntimeException("The classification failed!", exception);
		}
		calculateClassificationResults();
		freeResources();
		mClassificationCompleteFlag = true;
		if (mNumberOfEpochs == mMaximumNumberOfEpochs) {
			mART2AClassificationResult = Art2aClassificatorResult.NumberOfEpochsLimitReached;
			return Art2aClassificatorResult.NumberOfEpochsLimitReached;
		} else if (classificationTime / 1000 == mMaximumClassificationTime) {
			mART2AClassificationResult = Art2aClassificatorResult.TimeLimitReached;
			return Art2aClassificatorResult.TimeLimitReached;
		} else {
			mART2AClassificationResult = Art2aClassificatorResult.ConvergedSuccessfully;
			return Art2aClassificatorResult.ConvergedSuccessfully;
		}
	}

	/**
	 * Sets all parameters back to their default values.
	 */
	public void resetClassificator() {
		this.setMaximumNumberOfEpochs(mDEFAULT_MAXIMUM_NUMBER_OF_EPOCHS);
		this.setLearningParameter(mDEFAULT_LEARNING_PARAMETER);
		this.setMaximumClassificationTime(mDEFAULT_MAXIMUM_CLASSIFICATION_TIME);
		this.setRequiredSimilarity(mDEFAULT_EXIT_THRESHOLD);
		this.setMaximumNumberOfClasses(mNumberOfVectors);
		mThresholdForContrastEnhancement = mDefaultThresholdForContrastEnhancement;
		this.setInputScalingFactor(mDefaultInputScalingFactor);
	}

	// region IO methods

	/**
	 * Saves all information of the ART2aClassificator to a XmlWriter.
	 * 
	 * @param writer
	 *            The XmlWriter to write the data in. All XML knods containing
	 *            data are enclosed by the "ClassificatorData" knod. At the end
	 *            of the method the end element of the "ClassificatorData" knod
	 *            will be written.
	 * @throws RuntimeException
	 *             when the information of the classificator cannot be saved to
	 *             the xml-writer.
	 */
	public void saveStatusToXmlWriter(XMLStreamWriter writer) throws RuntimeException {
		try {
			writer.writeStartElement("ClassificatorData");

			writer.writeStartElement("ConvergenceFlag");
			writer.writeCharacters(Boolean.toString(mConvergenceFlag));
			writer.writeEndElement();

			writer.writeStartElement("ClassificationCompleteFlag");
			writer.writeCharacters(Boolean.toString(mClassificationCompleteFlag));
			writer.writeEndElement();

			writer.writeStartElement("InputScalingFactor");
			writer.writeCharacters(Double.toString(mInputScalingFactor));
			writer.writeEndElement();

			writer.writeStartElement("LearningParameter");
			writer.writeCharacters(Double.toString(mLearningParameter));
			writer.writeEndElement();

			writer.writeStartElement("MaximumClassificationTime");
			writer.writeCharacters(Integer.toString(mMaximumClassificationTime));
			writer.writeEndElement();

			writer.writeStartElement("MaximumNumberOfClasses");
			writer.writeCharacters(Integer.toString(mMaximumNumberOfClasses));
			writer.writeEndElement();

			writer.writeStartElement("MaximumNumberOfEpochs");
			writer.writeCharacters(Integer.toString(mMaximumNumberOfEpochs));
			writer.writeEndElement();

			writer.writeStartElement("MaximumNumberOfVectorsInDetectedClasses");
			writer.writeCharacters(Integer.toString(mMaximumNumberOfVectorsInDetectedClasses));
			writer.writeEndElement();

			writer.writeStartElement("NullclassSelectFlag");
			writer.writeCharacters(Boolean.toString(mNullclassSelectFlag));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfComponents");
			writer.writeCharacters(Integer.toString(mNumberOfComponents));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfDetectedClasses");
			writer.writeCharacters(Integer.toString(mNumberOfDetectedClasses));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfEpochs");
			writer.writeCharacters(Integer.toString(mNumberOfEpochs));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfNonSelectedClass");
			writer.writeCharacters(Integer.toString(mNumberOfNonSelectedClass));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfSelectedClass");
			writer.writeCharacters(Integer.toString(mNumberOfSelectedClass));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfVectors");
			writer.writeCharacters(Integer.toString(mNumberOfVectors));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfVectorsInNullClass");
			writer.writeCharacters(Integer.toString(mNumberOfVectorsInNullClass));
			writer.writeEndElement();

			writer.writeStartElement("RequiredSimilarity");
			writer.writeCharacters(Double.toString(mRequiredSimilarity));
			writer.writeEndElement();

			writer.writeStartElement("ThresholdForContrastEnhancement");
			writer.writeCharacters(Double.toString(mThresholdForContrastEnhancement));
			writer.writeEndElement();

			writer.writeStartElement("VigilanceParameter");
			writer.writeCharacters(Double.toString(mVigilanceParameter));
			writer.writeEndElement();

			// saving int[] "mClassDescentSortIndex"
			writer.writeStartElement("ClassDescentSortIndex");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				writer.writeCharacters(Integer.toString(mClassDescentSortIndex[i]));
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving double[][] "mClassMatrix"
			if (mClassMatrix != null) {
				writer.writeStartElement("ClassMatrix");
				for (int i = 0; i < mMaximumNumberOfClasses; i++) {
					writer.writeStartElement("index" + Integer.toString(i));
					for (int j = 0; j < mNumberOfComponents; j++) {
						writer.writeStartElement("index" + Integer.toString(j));
						writer.writeCharacters(Double.toString(mClassMatrix[i][j]));
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			// saving int[] "mClassView"
			if (mClassView != null) {
				writer.writeStartElement("ClassView");
				for (int i = 0; i < mNumberOfVectors; i++) {
					writer.writeStartElement("index" + Integer.toString(i));
					writer.writeCharacters(Integer.toString(mClassView[i]));
					writer.writeEndElement();
					;
				}
				writer.writeEndElement();
			}

			// saving double[][] "mComponentWeightInClasses"
			if (mComponentWeightInClasses != null) {
				writer.writeStartElement("ComponentWeightInClasses");
				for (int i = 0; i < mMaximumNumberOfClasses; i++) {
					writer.writeStartElement("index" + Integer.toString(i));
					for (int j = 0; j < mNumberOfComponents; j++) {
						writer.writeStartElement("index" + Integer.toString(j));
						writer.writeCharacters(Double.toString(mComponentWeightInClasses[i][j]));
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			// saving double[][] "mDataMatrix"
			writer.writeStartElement("DataMatrix");
			for (int i = 0; i < mNumberOfVectors; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				for (int j = 0; j < mNumberOfComponents; j++) {
					writer.writeStartElement("index" + Integer.toString(j));
					writer.writeCharacters(Double.toString(mDataMatrix[i][j]));
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving double[][] "mInterAngleBetweenClasses"
			writer.writeStartElement("InterAngleBetweenClasses");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				for (int j = 0; j < mNumberOfDetectedClasses; j++) {
					writer.writeStartElement("index" + Integer.toString(j));
					writer.writeCharacters(Double.toString(mInterAngleBetweenClasses[i][j]));
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving int[] "mNumberOfVectorsInClass"
			writer.writeStartElement("NumberOfVectorsInClass");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				writer.writeCharacters(Integer.toString(mNumberOfVectorsInClass[i]));
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving boolean[] "mSelectClassFlag"
			writer.writeStartElement("SelectClassFlag");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				writer.writeCharacters(Boolean.toString(mSelectClassFlag[i]));
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving int[] "mSelectedClass"
			writer.writeStartElement("SelectedClass");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				writer.writeCharacters(Integer.toString(mSelectedClass[i]));
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeEndElement();
		} catch (Exception e) {
			throw new RuntimeException("Information of the classificator cannot be saved to the xml-writer.", e);
		}
	}

	/**
	 * Saves all information of the ART2aClassificator to a XmlWriter.
	 * 
	 * @param writer
	 *            The XmlWriter to write the data in. All XML knods containing
	 *            data are enclosed by the "ClassificatorData" knod. At the end
	 *            of the method the end element of the "ClassificatorData" knod
	 *            will be written.
	 * @throws RuntimeException
	 *             when the information of the classificator cannot be saved to
	 *             the xml-writer.
	 */
	public void saveResultToXmlWriter(XMLStreamWriter writer) throws RuntimeException {
		try {
			writer.writeStartElement("ClassificatorData");

			writer.writeStartElement("ConvergenceFlag");
			writer.writeCharacters(Boolean.toString(mConvergenceFlag));
			writer.writeEndElement();

			writer.writeStartElement("ClassificationCompleteFlag");
			writer.writeCharacters(Boolean.toString(mClassificationCompleteFlag));
			writer.writeEndElement();

			writer.writeStartElement("InputScalingFactor");
			writer.writeCharacters(Double.toString(mInputScalingFactor));
			writer.writeEndElement();

			writer.writeStartElement("LearningParameter");
			writer.writeCharacters(Double.toString(mLearningParameter));
			writer.writeEndElement();

			writer.writeStartElement("MaximumClassificationTime");
			writer.writeCharacters(Integer.toString(mMaximumClassificationTime));
			writer.writeEndElement();

			writer.writeStartElement("MaximumNumberOfClasses");
			writer.writeCharacters(Integer.toString(mMaximumNumberOfClasses));
			writer.writeEndElement();

			writer.writeStartElement("MaximumNumberOfEpochs");
			writer.writeCharacters(Integer.toString(mMaximumNumberOfEpochs));
			writer.writeEndElement();

			writer.writeStartElement("MaximumNumberOfVectorsInDetectedClasses");
			writer.writeCharacters(Integer.toString(mMaximumNumberOfVectorsInDetectedClasses));
			writer.writeEndElement();

			writer.writeStartElement("NullclassSelectFlag");
			writer.writeCharacters(Boolean.toString(mNullclassSelectFlag));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfComponents");
			writer.writeCharacters(Integer.toString(mNumberOfComponents));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfDetectedClasses");
			writer.writeCharacters(Integer.toString(mNumberOfDetectedClasses));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfEpochs");
			writer.writeCharacters(Integer.toString(mNumberOfEpochs));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfNonSelectedClass");
			writer.writeCharacters(Integer.toString(mNumberOfNonSelectedClass));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfSelectedClass");
			writer.writeCharacters(Integer.toString(mNumberOfSelectedClass));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfVectors");
			writer.writeCharacters(Integer.toString(mNumberOfVectors));
			writer.writeEndElement();

			writer.writeStartElement("NumberOfVectorsInNullClass");
			writer.writeCharacters(Integer.toString(mNumberOfVectorsInNullClass));
			writer.writeEndElement();

			writer.writeStartElement("RequiredSimilarity");
			writer.writeCharacters(Double.toString(mRequiredSimilarity));
			writer.writeEndElement();

			writer.writeStartElement("ThresholdForContrastEnhancement");
			writer.writeCharacters(Double.toString(mThresholdForContrastEnhancement));
			writer.writeEndElement();

			writer.writeStartElement("VigilanceParameter");
			writer.writeCharacters(Double.toString(mVigilanceParameter));
			writer.writeEndElement();

			writer.writeStartElement("ART2AClassificationResult");
			if (mART2AClassificationResult == Art2aClassificatorResult.ConvergedSuccessfully) {
				writer.writeCharacters("ConvergedSuccessfully");
			} else if (mART2AClassificationResult == Art2aClassificatorResult.NumberOfEpochsLimitReached) {
				writer.writeCharacters("NumberOfEpochsLimitReached");
			} else {
				writer.writeCharacters("TimeLimitReached");
			}
			writer.writeEndElement();

			// saving int[] "mClassDescentSortIndex"
			writer.writeStartElement("ClassDescentSortIndex");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				writer.writeCharacters(Integer.toString(mClassDescentSortIndex[i]));
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving double[][] "mClassMatrix"
			if (mClassMatrix != null) {
				writer.writeStartElement("ClassMatrix");
				for (int i = 0; i < mNumberOfDetectedClasses; i++) {
					writer.writeStartElement("index" + Integer.toString(i));
					for (int j = 0; j < mNumberOfComponents; j++) {
						writer.writeStartElement("index" + Integer.toString(j));
						writer.writeCharacters(Double.toString(mClassMatrix[i][j]));
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			// saving int[] "mClassView"
			if (mClassView != null) {
				writer.writeStartElement("ClassView");
				for (int i = 0; i < mNumberOfVectors; i++) {
					writer.writeStartElement("index" + Integer.toString(i));
					writer.writeCharacters(Integer.toString(mClassView[i]));
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			// saving double[][] "mComponentWeightInClasses"
			if (mComponentWeightInClasses != null) {
				writer.writeStartElement("ComponentWeightInClasses");
				for (int i = 0; i < mNumberOfDetectedClasses; i++) {
					writer.writeStartElement("index" + Integer.toString(i));
					for (int j = 0; j < mNumberOfComponents; j++) {
						writer.writeStartElement("index" + Integer.toString(j));
						writer.writeCharacters(Double.toString(mComponentWeightInClasses[i][j]));
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			// saving double[][] "mInterAngleBetweenClasses"
			writer.writeStartElement("InterAngleBetweenClasses");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				for (int j = 0; j < mNumberOfDetectedClasses; j++) {
					writer.writeStartElement("index" + Integer.toString(j));
					writer.writeCharacters(Double.toString(mInterAngleBetweenClasses[i][j]));
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving int[] "mNumberOfVectorsInClass"
			writer.writeStartElement("NumberOfVectorsInClass");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				writer.writeCharacters(Integer.toString(mNumberOfVectorsInClass[i]));
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving boolean[] "mSelectClassFlag"
			writer.writeStartElement("SelectClassFlag");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				writer.writeCharacters(Boolean.toString(mSelectClassFlag[i]));
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// saving int[] "mSelectedClass"
			writer.writeStartElement("SelectedClass");
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				writer.writeStartElement("index" + Integer.toString(i));
				writer.writeCharacters(Integer.toString(mSelectedClass[i]));
				writer.writeEndElement();
			}

			// saving the string value of the corresponding object of the fingerprint item"
			writer.writeStartElement("CorrespondingObjectFromFingerpring");
			if (mObjectArray != null) {
				for (int i = 0; i < mObjectArray.length; i++) {
					writer.writeStartElement("index" + Integer.toString(i));
					writer.writeCharacters(String.valueOf(mObjectArray[i].correspondingObject));
					writer.writeEndElement();
				}
			}

			writer.writeEndElement();
			writer.writeEndElement();
		} catch (Exception e) {
			throw new RuntimeException("Information of the classificator cannot be saved to the xml-writer.", e);
		}
	}

	/**
	 * Gets all information of a classification from a XmlReader
	 * 
	 * @param reader
	 *            which contains the information of the classificator. The
	 *            current position of the reader hast to be the start element of
	 *            the XML knod "ClassificatorData". This knod encloses the XML
	 *            knods containing the data of the classificator. At the end of
	 *            the method the position of the reader is the end element of
	 *            the "ClassificatorData" knod
	 * @throws RuntimeException
	 *             when the information of the classificator cannot be loaded
	 *             from the xml-reader.
	 * 
	 */
	public void loadStatusFromXmlReader(XMLStreamReader reader) throws RuntimeException {
		try {

			// loading boolean "mConvergenceFlag"
			reader.nextTag();
			reader.next();
			mConvergenceFlag = Boolean.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading boolean "mClassificationCompleteResult"
			reader.next();
			mClassificationCompleteFlag = Boolean.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading double "mInputScalingFactor"
			reader.next();
			this.setInputScalingFactor(Double.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading double "mLearningParameter"
			reader.next();
			this.setLearningParameter(Double.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading int "mLearningParameter"
			reader.next();
			this.setMaximumClassificationTime(Integer.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading int "mMaximumNumberOfClasses"
			reader.next();
			this.setMaximumNumberOfClasses(Integer.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading int "mMaximumNumberOfEpochs"
			reader.next();
			this.setMaximumNumberOfEpochs(Integer.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading int "mMaximumNumberOfVectorsInDetectedClasses"
			reader.next();
			mMaximumNumberOfVectorsInDetectedClasses = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading boolean "mNullclassSelectFlag"
			reader.next();
			mNullclassSelectFlag = Boolean.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfComponents"
			reader.next();
			mNumberOfComponents = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfDetectedClasses"
			reader.next();
			mNumberOfDetectedClasses = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfEpochs"
			reader.next();
			mNumberOfEpochs = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfNonSelectedClass"
			reader.next();
			mNumberOfNonSelectedClass = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfSelectedClass"
			reader.next();
			mNumberOfSelectedClass = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfVectors"
			reader.next();
			mNumberOfVectors = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfVectorsInNullClass"
			reader.next();
			mNumberOfVectorsInNullClass = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading double "mRequiredSimilarity"
			reader.next();
			this.setRequiredSimilarity(Double.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading double "mThresholdForContrastEnhancement"
			reader.next();
			mThresholdForContrastEnhancement = Double.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading double "mVigilanceParameter"
			reader.next();
			this.setVigilanceParameter(Double.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// call "initalMatrices" to initialize all arrays
			initialzeMatrices();
			mNumberOfVectorsInClass = new int[mNumberOfDetectedClasses];
			mClassDescentSortIndex = new int[mNumberOfDetectedClasses];
			mSelectClassFlag = new boolean[mNumberOfDetectedClasses];
			mInterAngleBetweenClasses = new double[mNumberOfDetectedClasses][];

			// loading int[] "mClassDescentSortIndex"
			reader.next();
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				mInterAngleBetweenClasses[i] = new double[mNumberOfDetectedClasses];
				reader.next();
				mClassDescentSortIndex[i] = Integer.valueOf(reader.getText());
				reader.next();
				reader.nextTag();
			}
			reader.nextTag();

			// loading double[][] "mClassMatrix"
			if (reader.getLocalName() == "ClassMatrix") {
				reader.next();
				for (int i = 0; i < mMaximumNumberOfClasses; i++) {
					reader.next();
					for (int j = 0; j < mNumberOfComponents; j++) {
						reader.next();
						mClassMatrix[i][j] = Double.valueOf(reader.getText());
						reader.next();
						reader.nextTag();
					}
					reader.nextTag();
				}
				;
				reader.nextTag();
			}

			// loading int[] "mClassView"
			if (reader.getLocalName() == "ClassView") {
				reader.next();
				for (int i = 0; i < mNumberOfVectors; i++) {
					reader.next();
					mClassView[i] = Integer.valueOf(reader.getText());
					reader.next();
					reader.nextTag();
				}
				reader.nextTag();
			}

			// loading double[][] "mComponentWeightInClasses"
			if (reader.getLocalName() == "ComponentWeightInClasses") {
				reader.next();
				for (int i = 0; i < mMaximumNumberOfClasses; i++) {
					reader.next();
					for (int j = 0; j < mNumberOfComponents; j++) {
						reader.next();
						mComponentWeightInClasses[i][j] = Double.valueOf(reader.getText());
						reader.next();
						reader.nextTag();
					}
					reader.nextTag();

				}
				reader.nextTag();
			}

			// loading double[][] "mDataMatrix"
			mDataMatrix = new double[mNumberOfVectors][];
			for (int i = 0; i < mNumberOfVectors; i++) {
				mDataMatrix[i] = new double[mNumberOfComponents];
			}
			reader.next();
			for (int i = 0; i < mNumberOfVectors; i++) {
				reader.next();
				for (int j = 0; j < mNumberOfComponents; j++) {
					reader.next();
					mDataMatrix[i][j] = Double.valueOf(reader.getText());
					reader.next();
					reader.nextTag();
				}
				reader.nextTag();
			}
			reader.nextTag();

			// loading double[][] "mInterAngleBetweenClasses"
			reader.next();
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				reader.next();
				for (int j = 0; j < mNumberOfDetectedClasses; j++) {
					reader.next();
					mInterAngleBetweenClasses[i][j] = Double.valueOf(reader.getText());
					reader.next();
					reader.nextTag();
				}
				reader.nextTag();
			}
			reader.next();
			reader.nextTag();

			// loading int[] "mNumberOfVectorsInClass"
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				reader.next();
				mNumberOfVectorsInClass[i] = Integer.valueOf(reader.getText());
				reader.next();
				reader.nextTag();
			}
			reader.next();
			reader.nextTag();

			// loading boolean[] "mSelectClassFlag"
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				reader.next();
				mSelectClassFlag[i] = Boolean.valueOf(reader.getText());
				reader.next();
				reader.nextTag();
			}
			reader.next();
			reader.nextTag();

			// loading int[] "mSelectedClass"
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				reader.next();
				mSelectedClass[i] = Integer.valueOf(reader.getText());
				reader.next();
				reader.nextTag();
			}
			reader.next();
			// reader.nextTag();

			// loading int[] "mCorrespondingObjectArray"
			// mCorrespondingObjectArray = new Object[mNumberOfVectors];
			// for (int i = 0; i < mNumberOfVectors; i++) {
			// reader.next();
			// mCorrespondingObjectArray[i] = Integer.valueOf(reader.getText());
			// reader.next();
			// reader.nextTag();
			// }
			// reader.next();
		} catch (Exception e) {
			throw new RuntimeException("Information for the classificator cannot be loaded from the XML reader.", e);
		}
	}

	/**
	 * Gets all information of a classification from a XmlReader
	 * 
	 * @param reader
	 *            which contains the information of the classificator. The
	 *            current position of the reader hast to be the start element of
	 *            the XML knod "ClassificatorData". This knod encloses the XML
	 *            knods containing the data of the classificator. At the end of
	 *            the method the position of the reader is the end element of
	 *            the "ClassificatorData" knod
	 * @throws RuntimeException
	 *             when the information of the classificator cannot be loaded
	 *             from the xml-reader.
	 * 
	 */
	public void loadResultFromXmlReader(XMLStreamReader reader) throws RuntimeException {
		try {

			// loading boolean "mConvergenceFlag"
			reader.nextTag();
			reader.next();
			mConvergenceFlag = Boolean.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading boolean "mClassificationCompleteResult"
			reader.next();
			mClassificationCompleteFlag = Boolean.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading double "mInputScalingFactor"
			reader.next();
			this.setInputScalingFactor(Double.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading double "mLearningParameter"
			reader.next();
			this.setLearningParameter(Double.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading int "mLearningParameter"
			reader.next();
			this.setMaximumClassificationTime(Integer.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading int "mMaximumNumberOfClasses"
			reader.next();
			this.setMaximumNumberOfClasses(Integer.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading int "mMaximumNumberOfEpochs"
			reader.next();
			this.setMaximumNumberOfEpochs(Integer.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading int "mMaximumNumberOfVectorsInDetectedClasses"
			reader.next();
			mMaximumNumberOfVectorsInDetectedClasses = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading boolean "mNullclassSelectFlag"
			reader.next();
			mNullclassSelectFlag = Boolean.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfComponents"
			reader.next();
			mNumberOfComponents = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfDetectedClasses"
			reader.next();
			mNumberOfDetectedClasses = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfEpochs"
			reader.next();
			mNumberOfEpochs = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfNonSelectedClass"
			reader.next();
			mNumberOfNonSelectedClass = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfSelectedClass"
			reader.next();
			mNumberOfSelectedClass = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfVectors"
			reader.next();
			mNumberOfVectors = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading int "mNumberOfVectorsInNullClass"
			reader.next();
			mNumberOfVectorsInNullClass = Integer.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading double "mRequiredSimilarity"
			reader.next();
			this.setRequiredSimilarity(Double.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading double "mThresholdForContrastEnhancement"
			reader.next();
			mThresholdForContrastEnhancement = Double.valueOf(reader.getText());
			reader.next();
			reader.nextTag();

			// loading double "mVigilanceParameter"
			reader.next();
			this.setVigilanceParameter(Double.valueOf(reader.getText()));
			reader.next();
			reader.nextTag();

			// loading the ART2AClassificationResult
			reader.next();
			String text = reader.getText();
			if (text.equalsIgnoreCase("ConvergedSuccessfully")) {
				mART2AClassificationResult = Art2aClassificatorResult.ConvergedSuccessfully;
			} else if (text.equalsIgnoreCase("NumberOfEpochsLimitReached")) {
				mART2AClassificationResult = Art2aClassificatorResult.NumberOfEpochsLimitReached;
			} else {
				mART2AClassificationResult = Art2aClassificatorResult.TimeLimitReached;
			}
			reader.next();
			reader.nextTag();

			// call "initalMatrices" to initialize all arrays
			initialzeMatrices();
			mNumberOfVectorsInClass = new int[mNumberOfDetectedClasses];
			mClassDescentSortIndex = new int[mNumberOfDetectedClasses];
			mSelectClassFlag = new boolean[mNumberOfDetectedClasses];
			mInterAngleBetweenClasses = new double[mNumberOfDetectedClasses][];

			// loading int[] "mClassDescentSortIndex"
			reader.next();
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				mInterAngleBetweenClasses[i] = new double[mNumberOfDetectedClasses];
				reader.next();
				mClassDescentSortIndex[i] = Integer.valueOf(reader.getText());
				reader.next();
				reader.nextTag();
			}
			reader.nextTag();

			// loading double[][] "mClassMatrix"
			if (reader.getLocalName() == "ClassMatrix") {
				reader.next();
				for (int i = 0; i < mNumberOfDetectedClasses; i++) {
					reader.next();
					for (int j = 0; j < mNumberOfComponents; j++) {
						reader.next();
						mClassMatrix[i][j] = Double.valueOf(reader.getText());
						reader.next();
						reader.nextTag();
					}
					reader.nextTag();
				}
				;
				reader.nextTag();
			}

			// loading int[] "mClassView"
			if (reader.getLocalName() == "ClassView") {
				reader.next();
				for (int i = 0; i < mNumberOfVectors; i++) {
					reader.next();
					mClassView[i] = Integer.valueOf(reader.getText());
					reader.next();
					reader.nextTag();
				}
				reader.nextTag();
			}

			// loading double[][] "mComponentWeightInClasses"
			if (reader.getLocalName() == "ComponentWeightInClasses") {
				reader.next();
				for (int i = 0; i < mNumberOfDetectedClasses; i++) {
					reader.next();
					for (int j = 0; j < mNumberOfComponents; j++) {
						reader.next();
						mComponentWeightInClasses[i][j] = Double.valueOf(reader.getText());
						reader.next();
						reader.nextTag();
					}
					reader.nextTag();

				}
				reader.nextTag();
			}

			// loading double[][] "mInterAngleBetweenClasses"
			reader.next();
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				reader.next();
				for (int j = 0; j < mNumberOfDetectedClasses; j++) {
					reader.next();
					mInterAngleBetweenClasses[i][j] = Double.valueOf(reader.getText());
					reader.next();
					reader.nextTag();
				}
				reader.nextTag();
			}
			reader.next();
			reader.nextTag();

			// loading int[] "mNumberOfVectorsInClass"
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				reader.next();
				mNumberOfVectorsInClass[i] = Integer.valueOf(reader.getText());
				reader.next();
				reader.nextTag();
			}
			reader.next();
			reader.nextTag();

			// loading boolean[] "mSelectClassFlag"
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				reader.next();
				mSelectClassFlag[i] = Boolean.valueOf(reader.getText());
				reader.next();
				reader.nextTag();
			}
			reader.next();
			reader.nextTag();

			// loading int[] "mSelectedClass"
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				reader.next();
				mSelectedClass[i] = Integer.valueOf(reader.getText());
				reader.next();
				reader.nextTag();
			}
			reader.next();
			// reader.nextTag();

			// loading int[] "mCorrespondingObjectArray"
			mCorrespondingObjectArray = new Object[mNumberOfVectors];

			for (int i = 0; i < mNumberOfVectors; i++) {
				if (!reader.isEndElement()) {
					reader.next();
					mCorrespondingObjectArray[i] = reader.getText();
					reader.next();
					reader.nextTag();
				} else {
					break;
				}

			}
			// reader.next();
		} catch (Exception e) {
			throw new RuntimeException("Information for the classificator cannot be loaded from the XML reader.", e);
		}
	}

	// end

	// region Selection related methods

	/**
	 * Selects a detected class.
	 * 
	 * @param classIndex
	 *            Class index (less than mNumberOfDetectedClasses)
	 * @throws IllegalArgumentException
	 *             when an illegal index was delivered
	 */
	public void selectClass(int classIndex) {
		try {
			boolean classSelectFlag = mSelectClassFlag[mClassDescentSortIndex[classIndex]];
			if (!classSelectFlag) {
				mNumberOfSelectedClass++;
				mNumberOfSelectedVectors += getNumberOfVectorsInClass(classIndex);
				mSelectClassFlag[mClassDescentSortIndex[classIndex]] = true;
				createArrayOfSelectedObjects();
			}
		} catch (Exception exception) {
			throw new IllegalArgumentException("The index is invalid", exception);
		}
	}

	/**
	 * Deselects a detected class.
	 * 
	 * @param classIndex
	 *            Class index (less than mNumberOfDetectedClasses)
	 * @throws IllegalArgumentException
	 *             when an illegal index was delivered
	 */
	public void deselectClass(int classIndex) {
		try {
			boolean classSelectFlag = mSelectClassFlag[mClassDescentSortIndex[classIndex]];
			if (classSelectFlag) {
				mNumberOfSelectedClass--;
				mNumberOfSelectedVectors -= getNumberOfVectorsInClass(classIndex);
				mSelectClassFlag[mClassDescentSortIndex[classIndex]] = false;
				createArrayOfSelectedObjects();
			}
		} catch (Exception exception) {
			throw new IllegalArgumentException("The index is invalid", exception);
		}
	}

	/**
	 * Selects or deselects the nullclass.
	 * 
	 * @param selectionFlag
	 *            True: Select the nullclass. False: Deselect the nullclass.
	 */
	public void selectNullClass(boolean selectionFlag) {
		if (selectionFlag) {
			mNullclassSelectFlag = true;
		} else {
			mNullclassSelectFlag = false;
		}
		createArrayOfSelectedObjects();
	}

	/**
	 * Deselects all detected classes.
	 * 
	 * @throws RuntimeException
	 *             when an error occurs while deselecting all classes.
	 */
	public void deselectAllClasses() {
		try {
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				mSelectClassFlag[i] = false;
			}
		} catch (Exception exception) {
			throw new RuntimeException("Unable to deselect all classes", exception);
		}
	}

	/**
	 * True: Class is selected. False: Class is not selected.
	 * 
	 * @param classIndex
	 *            Class index (less than mNumberOfDetectedClasses)
	 * @return true: Class is selected. false: Class is not selected.
	 * @throws IllegalArgumentException
	 *             when an illegal index was delivered
	 */
	public boolean isClassSelected(int classIndex) {
		try {
			return mSelectClassFlag[mClassDescentSortIndex[classIndex]];
		} catch (Exception exception) {
			throw new IllegalArgumentException("The index is invalid", exception);
		}
	}

	/**
	 * True: Nullclass is selected. False: Nullclass is not selected.
	 * 
	 * @return True if the nullclass is selected, otherwise false.
	 */
	public boolean isNullClassSelected() {
		return mNullclassSelectFlag;
	}

	/**
	 * Contains the information whether a class is selected or not. No
	 * information about the null class is returned.
	 * 
	 * @return The selectionflag array.
	 * @throws RuntimeException
	 *             when an error occured while getting the class selection
	 *             flags.
	 */
	public boolean[] getClassSelectionFlags() {
		try {
			boolean[] classSelectionFlags = new boolean[mNumberOfDetectedClasses];
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				classSelectionFlags[i] = isClassSelected(i);
			}
			return classSelectionFlags;
		} catch (Exception exception) {
			throw new RuntimeException("Unable to get the class selection flags", exception);
		}
	}

	/**
	 * Gets the index of a selected fingerprintVector.
	 * 
	 * @param index
	 *            Index of the vector.
	 * @return The index of the selected fingerprintVector.
	 * @throws IllegalArgumentException
	 *             when an illegal index was delivered
	 */
	public int getSelectedVector(int index) {
		try {
			return mSelectedClass[index];
		} catch (Exception exception) {
			throw new IllegalArgumentException("The index is invalid", exception);
		}
	}

	/**
	 * Gets the index of a nonselected datavector.
	 * 
	 * @param index
	 *            Index of the vector.
	 * @throws IllegalArgumentException
	 *             when an illegal index was delivered
	 */
	public int getNonSelectedVector(int index) {
		try {
			return mNonSelectedClass[index];
		} catch (Exception exception) {
			throw new IllegalArgumentException("The index is invalid", exception);
		}
	}

	// end

	// region Get methods

	/**
	 * Gets the angle between classI and classJ in degree. 0 less than or equal
	 * to angle less than or equal to 90
	 * 
	 * @param classI
	 *            Index of first class (less than mNumberOfDetectedClasses)
	 * @param classJ
	 *            Index of second class (less than mNumberOfDetectedClasses)
	 * @return The angle in degree. "getIndexOfMaximumDataVectors" = 0 : The
	 *         vectors are identicaly; "getIndexOfMaximumDataVectors" = 90 : The
	 *         vectors are perpendicular to each other.
	 * @throws IllegalArgumentException
	 *             when one of the delivered indices is illegal.
	 */
	public double getInterAngleOfClasses(int classI, int classJ) {
		try {
			return mInterAngleBetweenClasses[mClassDescentSortIndex[classI]][mClassDescentSortIndex[classJ]];
		} catch (Exception exception) {
			throw new IllegalArgumentException("One of the indices was invalid", exception);
		}
	}

	/**
	 * Gets an array of all objects of "classIndex".
	 * 
	 * @param classIndex
	 *            Index of the class.
	 * @return An array of objects belonging to class of "classIndex".
	 * @throws IllegalArgumentException
	 *             when an illegal index was delivered
	 */
	public FingerprintItem[] getClassificationObjectsFromClass(int classIndex) {
		try {
			FingerprintItem[] array = (FingerprintItem[]) Array.newInstance(FingerprintItem.class,
					getNumberOfVectorsInClass(classIndex));
			if (mObjectArray != null) {
				for (int i = 0; i < getNumberOfVectorsInClass(classIndex); i++) {
					array[i] = mObjectArray[getIndexOfVectorInClass(classIndex, i)];
				}
			}
			return array;
		} catch (Exception exception) {
			throw new IllegalArgumentException("The index is invalid", exception);
		}
	}

	/**
	 * The objects in all classes: mObjectsInClasses[i][] corresponds to
	 * mNumberOfObjectsInClasses[i]
	 * 
	 * @return An jagged array with all objects.
	 * @throws IllegalArgumentException
	 *             when an error occures while getting the objects from
	 *             allclasses.
	 */
	public Object[][] getClassificationObjectsFromAllClasses() {
		try {
			if (mObjectsOfAllClasses == null) {
				mObjectsOfAllClasses = (Object[][]) Array.newInstance(Object[].class, mNumberOfDetectedClasses);
				int count = 0;
				for (int i = 0; i < mNumberOfDetectedClasses; i++) {
					int numberOfObjectsInClass = getNumberOfVectorsInClass(i);
					mObjectsOfAllClasses[count] = new Object[numberOfObjectsInClass];
					if (mObjectArray != null) {
						for (int j = 0; j < numberOfObjectsInClass; j++) {
							mObjectsOfAllClasses[count][j] = mObjectArray[getIndexOfVectorInClass(i, j)].correspondingObject;
						}
					}
					count++;
				}
			}
			return mObjectsOfAllClasses;
		} catch (Exception exception) {
			throw new RuntimeException("Cannot create the array of all objects.", exception);
		}
	}

	/**
	 * Gets an array of all ClassificationObjects in the nullclass.
	 * 
	 * @return The objects in the nullclass.
	 * @throws RuntimeException
	 *             when an error occures while getting the objects from the null
	 *             class.
	 */
	public Object[] getClassificationObjectsFromNullClass() {
		try {
			Object[] array = (Object[]) Array.newInstance(Object.class, mNumberOfVectorsInNullClass);
			if (mObjectArray != null) {
				for (int i = 0; i < mNumberOfVectorsInNullClass; i++) {
					array[i] = mObjectArray[getIndexOfVectorInNullclass(i)].correspondingObject;
				}
			}
			return array;
		} catch (Exception exception) {
			throw new RuntimeException("Cannot get the objects from the null class.", exception);
		}
	}

	/**
	 * Gets the classified objects in the selected classes.
	 * 
	 * @return The classified objects in the selected classes.
	 * @throws RuntimeException
	 *             when the an error occures while getting the objects from the
	 *             selected classes.
	 */
	public FingerprintItem[] getObjectsFromSelectedClasses() {
		int numberOfSelectedObjectsInClass = mNumberOfSelectedVectors;
		if (mNullclassSelectFlag)
			numberOfSelectedObjectsInClass = mNumberOfSelectedVectors + mNumberOfVectorsInNullClass;
		FingerprintItem[] selectedObjects = new FingerprintItem[numberOfSelectedObjectsInClass];
		if (numberOfSelectedObjectsInClass > 0) {
			try {
				int k = 0;
				for (int i = 0; i < mObjectsOfSelectedClasses.length; i++) {
					for (int j = 0; j < mObjectsOfSelectedClasses[i].length; j++) {
						selectedObjects[k] = mObjectsOfSelectedClasses[i][j];
						k++;
					}
				}
			} catch (Exception exception) {
				throw new RuntimeException("Cannot get the objects from the selected classes.", exception);
			}
		}
		return selectedObjects;
	}

	/**
	 * Gets the number of data vectors in class "classIndex".
	 * 
	 * @param classIndex
	 *            Class index (less than mNumberOfDetectedClasses)
	 * @return The number of vectors in the class.
	 * @throws IllegalArgumentException
	 *             when an illegal index was delivered
	 */
	public int getNumberOfVectorsInClass(int classIndex) {
		try {
			return mNumberOfVectorsInClass[mClassDescentSortIndex[classIndex]];
		} catch (Exception exception) {
			throw new IllegalArgumentException("The index is invalid", exception);
		}
	}

	/**
	 * Gets the componentweight of class "classIndex".
	 * 
	 * @param classI
	 *            Class index (less than mNumberOfDetectedClasses)
	 * @param classJ
	 *            Class index (less than mNumberOfDetectedClasses)
	 * @return The componentweight of the class.
	 * @throws IllegalArgumentException
	 *             when one of the delivered indices is illegal.
	 */
	public double getComponentWeightInClasses(int classI, int classJ) {
		try {
			return mComponentWeightInClasses[mClassDescentSortIndex[classI]][classJ];
		} catch (Exception exception) {
			throw new IllegalArgumentException("At least one of the indices is invalid", exception);
		}

	}

	/**
	 * Creates the datamatrix from the objects.
	 * 
	 * @param objects
	 *            Contains the datavectors
	 * @return The datamatrix.
	 * @throws RuntimeException
	 *             when an error occured while getting the DataMatrix from the
	 *             fingerprint array.
	 */
	public double[][] getDataMatrixFromClassificationObject(FingerprintItem[] objects) {
		try {
			// Check the length of the inputvectors. If one vector differs from
			// the others in length,
			// an exception will be thrown.
			int minimumLength = Integer.MAX_VALUE;
			int maximumLength = Integer.MIN_VALUE;
			for (int i = 0; i < objects.length; i++) {
				if (objects[i].fingerprintVector != null) {
					if (objects[i].fingerprintVector.length <= minimumLength) {
						minimumLength = objects[i].fingerprintVector.length;
					}
					if (objects[i].fingerprintVector.length >= maximumLength) {
						maximumLength = objects[i].fingerprintVector.length;
					}
				} else {
					throw new IllegalArgumentException("The datavector of at least one ClassificationObject is null.");
				}
			}
			if (maximumLength - minimumLength != 0) {
				throw new IllegalArgumentException("The objectarray is invalid. At least one datavector of at least "
						+ "one ClassificationObject differs in length.");
			}
			// Check for double null values and for double.NaN values
			System.out.println("Art2A Check fingerprint for null and double.NaN");
			for (int i = 0; i < objects.length; i++) {
				for (int j = 0; j < objects[i].fingerprintVector.length; j++) {
					if (Double.isNaN(objects[i].fingerprintVector[j])) {
						throw new IllegalArgumentException("The fingerprint array contains a Double.NaN value");
					}
				}
			}
			// building the datamatrix
			int numberOfVectors = objects.length;
			int numberOfComponents = objects[0].fingerprintVector.length;
			double[][] matrix = new double[numberOfVectors][];
			mCorrespondingObjectArray = new Object[numberOfVectors];
			for (int i = 0; i < numberOfVectors; i++) {
				matrix[i] = new double[numberOfComponents];
				for (int j = 0; j < numberOfComponents; j++) {
					matrix[i][j] = objects[i].fingerprintVector[j];
				}
				mCorrespondingObjectArray[i] = objects[i].correspondingObject;
			}
			return matrix;
		} catch (Exception exception) {
			throw new RuntimeException("An error occured while calculating the datamatrix from the objectarray",
					exception);
		}
	}

	/**
	 * Gets the class information of each vector. mClassView[3]=5 means vector
	 * no. 4 is in class no.6.
	 * 
	 * @return A copy of the mClassView array, containing the class information
	 *         of each vector.
	 */
	public int[] getClassView() {
		int[] copyOfClassView = new int[mClassView.length];
		for (int i = 0; i < mClassView.length; i++) {
			copyOfClassView[i] = mClassView[i];
		}
		return copyOfClassView;
	}

	public List<Object> getCorrespondingObjectsOfClass(int classNumber) {
		List<Object> correspondingObjectsOfClass = new ArrayList<Object>(getMaximumNumberOfVectorsInDetectedClasses());
		for (int i = 0; i < mClassView.length; i++) {
			if (mClassView[i] == mClassDescentSortIndex[classNumber]) {
				correspondingObjectsOfClass.add(mCorrespondingObjectArray[i]);
			}
		}
		return correspondingObjectsOfClass;
	}

	// end

	// region Scale methods

	/**
	 * Scales the components of the fingerprintVector of the classification
	 * objects in the object array to the interval [0, 1] within a column.
	 * 
	 * @param objects
	 *            Contains the classification objects.
	 * @throws RuntimeException
	 *             when the fingerprint vector components cannot be scaled to
	 *             the intervall [0,1].
	 */
	public static void scaleFingerprintVectorComponentsToIntervalZeroOne(FingerprintItem[] objects) {
		try {
			double minimum;
			double maximum;
			double maxMinDifference;
			double value;
			for (int i = 0; i < objects[0].fingerprintVector.length; i++) {
				minimum = objects[0].fingerprintVector[i];
				maximum = objects[0].fingerprintVector[i];
				for (int j = 1; j < objects.length; j++) {
					double currentValue = objects[j].fingerprintVector[i];
					if (currentValue < minimum) {
						minimum = currentValue;
					}
					if (currentValue > maximum) {
						maximum = currentValue;
					}
				}
				maxMinDifference = maximum - minimum;
				for (int j = 0; j < objects.length; j++) {
					value = (objects[j].fingerprintVector[i] - minimum) / maxMinDifference;
					if (Double.isNaN(value)) {
						throw new Exception("The scaling of the fingerprint item created a Double.NaN value.");
					}
					objects[j].fingerprintVector[i] = value;
				}
			}
		} catch (Exception exception) {
			throw new RuntimeException(
					"An error occured while scaling the fingerprintVectors of the IClassification objects", exception);
		}
	}

	// end

	// end

	// region PROPERTIES

	/**
	 * Get-Property
	 * 
	 * @return Parameter to influence the number of classes. 0 less than or
	 *         equal to vigilanceParameter less than or equal to 1.
	 */
	public double getVigilanceParameter() {
		return mVigilanceParameter;
	}

	/**
	 * Set-Property
	 * 
	 * @param vigilanceParameter
	 *            Parameter to influence the number of classes. 0 less than or
	 *            equal to vigilanceParameter less than or equal to 1.
	 * @throws IllegalArgumentException
	 *             when trying to set an illegal value.
	 */
	public void setVigilanceParameter(double vigilanceParameter) {
		if (vigilanceParameter >= 0 && vigilanceParameter <= 1) {
			mVigilanceParameter = vigilanceParameter;
		} else {
			throw new IllegalArgumentException("This is not a valid value for the parameter");
		}
	}

	/**
	 * Get-Property
	 * 
	 * @return Parameter to define the intensity of keeping the old class vector
	 *         in mindbefore the system adapts it to the new sample vector.
	 */
	public double getLearningParameter() {
		return mLearningParameter;
	}

	/**
	 * Set-Property
	 * 
	 * @param learningParameter
	 *            Parameter to define the intensity of keeping the old class
	 *            vector in mindbefore the system adapts it to the new sample
	 *            vector.
	 * @throws IllegalArgumentException
	 *             when trying to set an illegal value.
	 */
	public void setLearningParameter(double learningParameter) {
		if (learningParameter >= 0 && learningParameter <= 1) {
			mLearningParameter = learningParameter;
		} else {
			throw new IllegalArgumentException("This is not a valid value for the parameter");
		}
	}

	/**
	 * GetProperty
	 * 
	 * @return Parameter to define the final similarity between the classvectors
	 *         of the current and the previous epoche. 0 less than or equal to
	 *         RequiredSimilarity less than or equal to 1 Default: 0.99<br>
	 *         Remark: E.g. a value of 0.99 means that the classification will
	 *         end if the classvectors of the current and the previous epoche
	 *         have a similarity of 99 percent.
	 */
	public double getRequiredSimilarity() {
		return mRequiredSimilarity;
	}

	/**
	 * Set-Property
	 * 
	 * @param requiredSimilarity
	 *            Parameter to define the final similarity between the
	 *            classvectors of the current and the previous epoche. 0 less
	 *            than or equal to RequiredSimilarity less than or equal to 1
	 *            Default: 0.99<br>
	 *            Remark: E.g. a value of 0.99 means that the classification
	 *            will end if the classvectors of the current and the previous
	 *            epoche have a similarity of 99 percent.
	 * @throws IllegalArgumentException
	 *             when trying to set an illegal value.
	 */
	public void setRequiredSimilarity(double requiredSimilarity) {
		if (requiredSimilarity >= 0 && requiredSimilarity <= 1) {
			mRequiredSimilarity = requiredSimilarity;
		} else {
			throw new IllegalArgumentException("This is not a valid value for the parameter");
		}
	}

	/**
	 * Get--Property
	 * 
	 * @return true: System will converge if the scalar product of the classes
	 *         of the current and the previous epoch is less than
	 *         RequiredSimilarity.<br>
	 *         false: System will converge if the classification does not change
	 *         after one epoch.
	 */
	public boolean getConvergenceFlag() {
		return mConvergenceFlag;
	}

	/**
	 * Set-Property
	 * 
	 * @param convergenceFlag
	 *            true: System will converge if the scalar product of the
	 *            classes of the current and the previous epoch is less than
	 *            RequiredSimilarity.<br>
	 *            false: System will converge if the classification does not
	 *            change after one epoch.
	 */
	public void setConvergenceFlag(boolean convergenceFlag) {
		mConvergenceFlag = convergenceFlag;
	}

	/**
	 * Get-Property
	 * 
	 * @return True: The classification was successful and the results have been
	 *         successfully calculated.<br>
	 *         False: The classification ended abnormaly and some or all results
	 *         have not been calculated.
	 */
	public boolean getClassificationCompleteFlag() {
		return mClassificationCompleteFlag;
	}

	/**
	 * Get--Property
	 * 
	 * @return Limit of the number of epochs. MaximumNumberOfEpochs greater than
	 *         1
	 */
	public int getMaximumNumberOfEpochs() {
		return mMaximumNumberOfEpochs;
	}

	/**
	 * Set-Property
	 * 
	 * @param maximumNumberOfEpochs
	 *            Limit of the number of epochs. MaximumNumberOfEpochs greater
	 *            than 1
	 * @throws IllegalArgumentException
	 *             when trying to set an illegal value.
	 */
	public void setMaximumNumberOfEpochs(int maximumNumberOfEpochs) {
		if (maximumNumberOfEpochs >= 1 && maximumNumberOfEpochs <= Integer.MAX_VALUE) {
			mMaximumNumberOfEpochs = maximumNumberOfEpochs;
		} else {
			throw new IllegalArgumentException("This is not a valid value for the parameter");
		}
	}

	/**
	 * Get-Property
	 * 
	 * @return Timelimit of the classification in seconds.
	 */
	public int getMaximumClassificationTime() {
		return mMaximumClassificationTime;
	}

	/**
	 * Set-Property
	 * 
	 * @param maximumClassificationTime
	 *            Timelimit of the classification in seconds.
	 * @throws IllegalArgumentException
	 *             when trying to set an illegal value.
	 */
	public void setMaximumClassificationTime(int maximumClassificationTime) {
		if (maximumClassificationTime > 0 && maximumClassificationTime < Integer.MAX_VALUE) {
			mMaximumClassificationTime = maximumClassificationTime;
		} else {
			throw new IllegalArgumentException("This is not a valid value for the parameter");
		}
	}

	/**
	 * Get-Property
	 * 
	 * @return Limit of the number of classes which are allowed to detect.
	 */
	public int getMaximumNumberOfClasses() {
		return mMaximumNumberOfClasses;
	}

	/**
	 * Set-Property
	 * 
	 * @param maximumNumberOfClasses
	 *            Limit of the number of classes which are allowed to detect.
	 * @throws IllegalArgumentException
	 *             when trying to set an illegal value.
	 */
	public void setMaximumNumberOfClasses(int maximumNumberOfClasses) {
		if (maximumNumberOfClasses > 0) {
			mMaximumNumberOfClasses = maximumNumberOfClasses;
		} else {
			throw new IllegalArgumentException("This is not a valid value for the parameter");
		}
	}

	/**
	 * Get-Property
	 * 
	 * @return The number of detected classes.
	 */
	public int getNumberOfDetectedClasses() {
		return mNumberOfDetectedClasses;
	}

	/**
	 * Get-Property
	 * 
	 * @return The maximum number of data vectors in one of the detected
	 *         classes.
	 */
	public int getMaximumNumberOfVectorsInDetectedClasses() {
		return mMaximumNumberOfVectorsInDetectedClasses;
	}

	/**
	 * Get-Property
	 * 
	 * @return Number of data vectors in the nullclass.
	 */
	public int getNumberOfVectorsInNullClass() {
		return mNumberOfVectorsInNullClass;
	}

	/**
	 * Get-Property
	 * 
	 * @return All matrix values less than this threshold are set to zero. 0
	 *         less than or equal to ThresholdForContrastEnhancement less than
	 *         or equal to 1 / Sqr(NumberOfComponents);<br>
	 *         Default: 1 / Sqr(NumberOfComponents - 1)<br>
	 *         Remark: An increase of the threshold leads to greater
	 *         contrastenhancement and to a faster stability of the classvectors
	 *         but forces a wrong classification.
	 */
	public double getThresholdForContrastEnhancement() {
		return mThresholdForContrastEnhancement;
	}

	/**
	 * Set-Property
	 * 
	 * @param thresholdForContrastEnhancement
	 *            All matrix values less than this threshold are set to zero. 0
	 *            less than or equal to ThresholdForContrastEnhancement less
	 *            than or equal to 1 / Sqr(NumberOfComponents);<br>
	 *            Default: 1 / Sqr(NumberOfComponents - 1)<br>
	 *            Remark: An increase of the threshold leads to greater
	 *            contrastenhancement and to a faster stability of the
	 *            classvectors but forces a wrong classification.
	 * @throws IllegalArgumentException
	 *             when trying to set an illegal value.
	 */
	public void setThresholdForContrastEnhancement(double thresholdForContrastEnhancement) {
		if (thresholdForContrastEnhancement >= 0
				&& thresholdForContrastEnhancement <= 1 / Math.sqrt(mNumberOfComponents))
			mThresholdForContrastEnhancement = thresholdForContrastEnhancement;
		else
			throw new IllegalArgumentException("This is not a valid value for the parameter");
	}

	/**
	 * Get-property
	 * 
	 * @return The value influences the number of detected classes. A high value
	 *         increases the number of classes. 0 less than or equal to
	 *         InputScalingFactor less than or equal to 1 /
	 *         Sqr(NumberOfComponents); Default: 1 / Sqr(NumberOfComponents - 1)
	 */
	public double getInputScalingFactor() {
		return mInputScalingFactor;
	}

	/**
	 * Set-property
	 * 
	 * @param inputScalingFactor
	 *            The value influences the number of detected classes. A high
	 *            value increases the number of classes. 0 less than or equal to
	 *            InputScalingFactor less than or equal to 1 /
	 *            Sqr(NumberOfComponents); Default: 1 / Sqr(NumberOfComponents -
	 *            1)
	 * @throws IllegalArgumentException
	 *             when trying to set an illegal value.
	 */
	public void setInputScalingFactor(double inputScalingFactor) {
		if (inputScalingFactor >= 0 && inputScalingFactor <= 1 / Math.sqrt(mNumberOfComponents))
			mInputScalingFactor = inputScalingFactor;
		else
			throw new IllegalArgumentException("This is not a valid value for the parameter");
	}

	/**
	 * Get-Property
	 * 
	 * @return Number of selected classes.
	 */
	public int getNumberOfSelectedClass() {
		int counter = 0;
		for (int i = 0; i < mNumberOfDetectedClasses; i++) {
			if (mSelectClassFlag[i] == true) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * Get-Property
	 * 
	 * @return The objects of the selected classes. The first index defines the
	 *         class and the second one the object in the class.
	 */
	public FingerprintItem[][] getObjectsFromSelectedClass() {
		return mObjectsOfSelectedClasses;
	}

	/**
	 * Get-Property
	 * 
	 * @return Get all objects of the classificator.
	 */
	public FingerprintItem[] getClassificationObjects() {
		return mObjectArray;
	}

	/**
	 * Get-Property
	 * 
	 * @return Get the number of epochs.
	 */
	public int getNumberOfEpochs() {
		return mNumberOfEpochs;
	}

	/**
	 * Get-Property
	 * 
	 * @return True / false, depending on whether the random should be
	 *         deterministic or not.
	 */
	public boolean getDeterministicRandom() {
		return mDeterministicRandom;
	}

	/**
	 * Set-Property
	 * 
	 * @param deterministicRandom
	 *            Set the use of a deterministic random to true or false
	 */
	public void setDeterministicRandom(boolean deterministicRandom) {
		mDeterministicRandom = deterministicRandom;
	}

	// end

	// region PRIVATE METHODS

	/**
	 * Puts the selected objects from the object array into a jagged array.
	 * 
	 * @throws RuntimeException
	 *             when an error occurs while creating the array containing the
	 *             selected objects.
	 */
	private void createArrayOfSelectedObjects() {
		try {
			int numberOfSelectedClassIncludingNullclass = mNumberOfSelectedClass;
			if (mNullclassSelectFlag)
				numberOfSelectedClassIncludingNullclass++;
			mObjectsOfSelectedClasses = (FingerprintItem[][]) Array.newInstance(FingerprintItem[].class,
					numberOfSelectedClassIncludingNullclass);
			int count = 0;
			if (mNullclassSelectFlag) {

				mObjectsOfSelectedClasses[count] = (FingerprintItem[]) Array.newInstance(FingerprintItem[].class,
						mNumberOfVectorsInNullClass);
				if (mObjectArray != null) {
					for (int j = 0; j < mNumberOfVectorsInNullClass; j++) {
						mObjectsOfSelectedClasses[0][j] = mObjectArray[getIndexOfVectorInNullclass(j)];
					}
				}
				count++;
			}
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				if (isClassSelected(i)) {
					int numberOfObjectsInClass = getNumberOfVectorsInClass(i);
					mObjectsOfSelectedClasses[count] = (FingerprintItem[]) Array.newInstance(FingerprintItem[].class,
							numberOfObjectsInClass);

					if (mObjectArray != null) {
						for (int j = 0; j < numberOfObjectsInClass; j++) {
							mObjectsOfSelectedClasses[count][j] = mObjectArray[getIndexOfVectorInClass(i, j)];
						}
					}
					count++;
				}
			}
		} catch (Exception exception) {
			throw new RuntimeException("Cannot create array of selected objects.", exception);
		}
	}

	/**
	 * Evaluates the results of the classification. Is invoked if the
	 * classification suceeded.
	 * 
	 * @throws RuntimeException
	 *             when the calculation of the classifcation results fails.
	 */
	private void calculateClassificationResults() {
		try {
			System.out.println("Calculate Classification Resulst Start");
			mNumberOfVectorsInClass = new int[mNumberOfDetectedClasses];
			mClassDescentSortIndex = new int[mNumberOfDetectedClasses];
			mSelectClassFlag = new boolean[mNumberOfDetectedClasses];
			mInterAngleBetweenClasses = new double[mNumberOfDetectedClasses][];
			// initialize mNumberOfVectorsInClass
			System.out.print("Number of detected Classes:" + mNumberOfDetectedClasses);
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				mInterAngleBetweenClasses[i] = new double[mNumberOfDetectedClasses];
				mNumberOfVectorsInClass[i] = 0;
			}
			for (int i = 0; i < mNumberOfVectors; i++) {
				if (mClassView[i] < 0) {
					continue;
				} else {
					mNumberOfVectorsInClass[mClassView[i]]++;
				}
			}
			// calculating angles between all classes
			double factor = 180 / Math.PI;
			double product;
			for (int i = 1; i < mNumberOfDetectedClasses; i++) {
				for (int j = 0; j < i; j++) {
					product = 0;
					for (int k = 0; k < mNumberOfComponents; k++) {
						product += mClassMatrix[i][k] * mClassMatrix[j][k];
					}
					mInterAngleBetweenClasses[i][j] = factor * Math.acos(product);
					// the angle between i and j, equals the angle between j and
					// i, so getting a symetric matrix
					mInterAngleBetweenClasses[j][i] = mInterAngleBetweenClasses[i][j];
				}
			}
			// getting the componentweights of each class
			double max;
			for (int i = 0; i < mMaximumNumberOfClasses; i++) {
				// initialize mComponentWeightInClasses
				for (int j = 0; j < mNumberOfComponents; j++) {
					mComponentWeightInClasses[i][j] = 0;
				}
			}
			// adding all components of the datamatrix to
			// mComponentWeightInClasses
			for (int i = 0; i < mNumberOfVectors; i++) {
				if (mClassView[i] < 0)
					continue;
				for (int j = 0; j < mNumberOfComponents; j++) {
					mComponentWeightInClasses[mClassView[i]][j] += mDataMatrix[i][j];
				}
			}
			// evaluate the maximum-component of each vector
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				max = mComponentWeightInClasses[i][0];
				for (int j = 1; j < mNumberOfComponents; j++) {
					if (mComponentWeightInClasses[i][j] > max) {
						max = mComponentWeightInClasses[i][j];
					}
				}
				// devide each component by the maximum
				for (int j = 0; j < mNumberOfComponents; j++) {
					mComponentWeightInClasses[i][j] /= max;
				}
			}
			int[] swap = new int[mNumberOfDetectedClasses];
			// Copy array
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				swap[i] = mNumberOfVectorsInClass[i];
			}
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				mClassDescentSortIndex[i] = i;
			}
			int tmp;
			for (int left = 0, right = mNumberOfDetectedClasses - 1; left < right; left++, right--) {
				tmp = mClassDescentSortIndex[left];
				mClassDescentSortIndex[left] = mClassDescentSortIndex[right];
				mClassDescentSortIndex[right] = tmp;
			}
			// calculate the max. number of data vectors in a detected class
			mMaximumNumberOfVectorsInDetectedClasses = 0;
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				if (mNumberOfVectorsInClass[i] > mMaximumNumberOfVectorsInDetectedClasses) {
					mMaximumNumberOfVectorsInDetectedClasses = mNumberOfVectorsInClass[i];
				}
			}
		} catch (Exception exception) {
			throw new RuntimeException("An error occured while evaluating the results of the classification", exception);
		}
	}

	/**
	 * Checks the convergence of the classification at the end of each epoch.
	 * The type of convergence-check depends on the convergence-flag.
	 * 
	 * @return true: Classvectors converged. Classification ends.<br>
	 *         /* false: Classvectors are still not convergent. Classification
	 *         continues.
	 * @throws RuntimeException
	 *             when an error occurs while checking the convergence of the
	 *             current classification.
	 */
	private boolean isNetworkConvergent() {
		try {
			System.out.println("Check network convergents");
			// Check for cluster which do not contain any vector!
			// This cluster will be removed!
			mNumberOfVectorsInClass = new int[mNumberOfDetectedClasses];
			// initialize mNumberOfVectorsInClass
			for (int i = 0; i < mNumberOfDetectedClasses; i++) {
				mNumberOfVectorsInClass[i] = 0;
			}
			System.out.println("Check network convergents 2");
			for (int i = 0; i < mNumberOfVectors; i++) {
				// Count the number of vectors for each class
				if (mClassView[i] < 0) {
					continue;
				} else {
					mNumberOfVectorsInClass[mClassView[i]]++;
				}
			}
			// select the classes which do not contain vectors
			List<Integer> listOfClassesWithoutVectors = new ArrayList<Integer>(mNumberOfDetectedClasses);
			// This integer array contains the number which is needed for the calculation of the new class number
			int[] calculateTheNewClassNumber = new int[mNumberOfDetectedClasses];
			System.out.println("Check network convergents 3");
			for (int i = 0; i < mNumberOfVectorsInClass.length; i++) {
				calculateTheNewClassNumber[i] = listOfClassesWithoutVectors.size();
				if (mNumberOfVectorsInClass[i] == 0) {
					listOfClassesWithoutVectors.add(i);
				}
			}
			System.out.println("Check network convergents 4");
			if (!listOfClassesWithoutVectors.isEmpty()) {
				// calculate the new class number
				// new class number = oldClassNumber - (number of classes which contains no vectors AND the class number is
				// smaller than the oldClassNumber)
				System.out.println("Check network convergents 5");
				for (int i = 0; i < mClassView.length; i++) {
					mClassView[i] = mClassView[i] - calculateTheNewClassNumber[mClassView[i]];
				}
				System.out.println("Check network convergents 6");
				for (int i = 0; i < mNumberOfDetectedClasses; i++) {
					mClassMatrix[i - calculateTheNewClassNumber[i]] = mClassMatrix[i];
				}
				// reduce the number of detected classes (mNumberOfDetectedClasses - classes without vectors)
				mNumberOfDetectedClasses = mNumberOfDetectedClasses - listOfClassesWithoutVectors.size();
			}
			System.out.println("Check network convergents 7");
			boolean convergenceFlag = false;
			if (mConvergenceFlag) {
				System.out.println("Check network convergents 8");
				// region Check convergence by evaluating the similarity of the
				// classvectors of this and the previous epoch

				convergenceFlag = true;
				double scalarProductOfClassVector;
				for (int i = 0; i < mNumberOfDetectedClasses; i++) {
					scalarProductOfClassVector = 0;
					for (int j = 0; j < mNumberOfComponents; j++) {
						scalarProductOfClassVector += mClassMatrix[i][j] * mClassMatrixOld[i][j];
					}
					if (scalarProductOfClassVector < mRequiredSimilarity) {
						convergenceFlag = false;
						break;
					}
				}
				System.out.println("Check network convergents 9");
				if (!convergenceFlag) {
					for (int i = 0; i < mNumberOfDetectedClasses; i++) {
						for (int j = 0; j < mNumberOfComponents; j++) {
							mClassMatrixOld[i][j] = mClassMatrix[i][j];
						}
					}
				}
				System.out.println("Check network convergents 10");
				// end
			} else {
				System.out.println("Check network convergents 11");
				// region Check convergence by classification comparison of this
				// and the previous epoch

				convergenceFlag = true;
				for (int i = 0; i < mNumberOfVectors; i++) {
					if (mClassView[i] != mClassViewOld[i]) {
						convergenceFlag = false;
						break;
					}
				}
				System.out.println("Check network convergents 12");
				if (!convergenceFlag) {
					for (int i = 0; i < mNumberOfVectors; i++) {
						mClassViewOld[i] = mClassView[i];
					}
				}
				System.out.println("Check network convergents 13");
				// end

			}
			System.out.println("Check network convergents end");
			return convergenceFlag;
		} catch (Exception exception) {
			throw new RuntimeException(
					"The classification failed! Unable to check the convergence of the current classification.",
					exception);
		}
	}

	/**
	 * Calculates the length of the vector.
	 * 
	 * @param vector
	 *            The vector to get the length from.
	 * @return The length (norm) of the vector.
	 * @throws RuntimeException
	 *             when the calculation of the vectorlength fails.
	 */
	private double getLengthOfVector(double[] vector) {
		double sumOfAllSquaredComponents = 0;
		try {
			for (int i = 0; i < vector.length; i++) {
				sumOfAllSquaredComponents += vector[i] * vector[i];
			}
			if (sumOfAllSquaredComponents == 0) {
				return 0;
			} else {
				return Math.sqrt(sumOfAllSquaredComponents);
			}
		} catch (Exception exception) {
			throw new RuntimeException("Unable to calculate the length of the vector!", exception);
		}
	}

	/**
	 * Normalizes a vector.
	 * 
	 * @param vector
	 *            The vector to be normalized.
	 * @throws RuntimeException
	 *             when the normalization of the vector fails.
	 */
	private void normalizeVector(double[] vector) {
		try {
			double reziprokeVectorLength = 1 / getLengthOfVector(vector);
			for (int i = 0; i < vector.length; i++) {
				vector[i] *= reziprokeVectorLength;
			}
		} catch (Exception exception) {
			throw new RuntimeException("Cannot normalize the current vector!", exception);
		}
	}

	/**
	 * Fills the "mSampleVectorsInRandomOrder" array with vectorindices in a
	 * random order. <br>
	 * Restriction: Every vector index is allowed to occur just one time in the
	 * array.
	 * 
	 * @throws RuntimeException
	 *             when an error occurs while randomizing the vector indices of
	 *             the array "mSampleVectorsInRandomOrder".
	 */
	private void randomizeVectorIndices() {
		try {
			// fills array with values from 0 - mNumberOfVectors -1
			for (int i = 0; i < mNumberOfVectors; i++) {
				mSampleVectorsInRandomOrder[i] = i;
			}
			Random rnd;
			if (mDeterministicRandom) {
				rnd = new Random(mSeedValue);
				mSeedValue++;
			} else {
				rnd = new Random();
			}

			int numberOfIterations = (mNumberOfVectors / 2) + 1;
			int randomIndex1;
			int randomIndex2;
			int buffer;

			for (int j = 0; j < numberOfIterations; j++) {
				randomIndex1 = (int) (mNumberOfVectors * rnd.nextDouble());
				randomIndex2 = (int) (mNumberOfVectors * rnd.nextDouble());

				buffer = mSampleVectorsInRandomOrder[randomIndex1];
				mSampleVectorsInRandomOrder[randomIndex1] = mSampleVectorsInRandomOrder[randomIndex2];
				mSampleVectorsInRandomOrder[randomIndex2] = buffer;
			}
		} catch (Exception exception) {
			throw new RuntimeException("Unable to initialize the array for selection of a random vector!", exception);
		}
	}

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
	 * Initializes some important arrays.
	 */
	private void initialzeMatrices() {
		mClassMatrix = new double[mMaximumNumberOfClasses][];
		mClassMatrixOld = new double[mMaximumNumberOfClasses][];
		mComponentWeightInClasses = new double[mMaximumNumberOfClasses][];
		for (int i = 0; i < mMaximumNumberOfClasses; i++) {
			mClassMatrix[i] = new double[mNumberOfComponents];
			mClassMatrixOld[i] = new double[mNumberOfComponents];
			mComponentWeightInClasses[i] = new double[mNumberOfComponents];
		}
		mClassView = new int[mNumberOfVectors];
		mClassViewOld = new int[mNumberOfVectors];
		mVectorsInNullClass = new int[mNumberOfVectors];
		mSampleVectorsInRandomOrder = new int[mNumberOfVectors];
		mSelectedClass = new int[mMaximumNumberOfClasses];
		mNonSelectedClass = new int[mMaximumNumberOfClasses];
	}

	/**
	 * Frees unnecessary resources.
	 */
	private void freeResources() {
		// mDataMatrix = null;
		mClassMatrixOld = null;
		mSampleVectorsInRandomOrder = null;
		mSampleVector = null;
		mClassViewOld = null;
	}

	/**
	 * Gets index of data vector in class "classIndex" at position
	 * "vectorInClassIndex".
	 * 
	 * @param classIndex
	 *            Class index (less than mNumberOfDetectedClasses)
	 * @param vectorInClassIndex
	 *            The vector index to define the vector in the class (less than
	 *            mNumberOfVectorsInClass).
	 * @return The index of the classvector in the classmatrix.
	 * @throws IllegalArgumentException
	 *             when one of the delivered indices is illegal.
	 */
	private int getIndexOfVectorInClass(int classIndex, int vectorInClassIndex) {
		try {
			int counter = -1;
			for (int i = 0; i < mNumberOfVectors; i++) {
				if (mClassView[i] >= 0) {
					if (mClassView[i] == mClassDescentSortIndex[classIndex]) {
						counter++;
						if (counter == vectorInClassIndex) {
							return i;
						}
					}
				}
			}
			return 0;
		} catch (Exception exception) {
			throw new IllegalArgumentException("Cannot get the vectorindex.", exception);
		}
	}

	/**
	 * Gets the index of the data vector in the null class at position
	 * "vectorInNullClassIndex" in the datamatrix.
	 * 
	 * @param vectorInNullClassIndex
	 *            / The vectorindex to define the vector in the nullclass (less
	 *            than the number of vectors in nullclass)
	 * @return The index in the datamatrix.
	 * @throws IllegalArgumentException
	 *             when the delivered index is invalid.
	 */
	private int getIndexOfVectorInNullclass(int vectorInNullClassIndex) {
		try {
			return mVectorsInNullClass[vectorInNullClassIndex];
		} catch (Exception exception) {
			throw new IllegalArgumentException("The index is invalid.", exception);
		}
	}

	// end

}