/*
 * Copyright (C) 2010 - 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.weka.utilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Represents a genome for the GA attribute selection activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class GAAttributeEvaluationGenome {

	private static int SCORE_POWER = 2;

	private Random rand = new Random();
	private double score = Double.MAX_VALUE;
	private double rmse = 0;
	private int attrRestriction = -1;

	private Instances dataset = null;
	private Instances optDataset = null;
	private String[] attrNames = null;
	private boolean[] attrIsUsed = null;

	/**
	 * Creates a new instance.
	 */
	public GAAttributeEvaluationGenome(Instances dataset) throws Exception {
		this.dataset = dataset;
		HashSet<String> nameSet = new HashSet<String>();
		for (int i = 1; i < dataset.numAttributes() - 1; i++) {
			nameSet.add(dataset.attribute(i).name());
		}
		this.attrNames = new String[nameSet.size()];
		this.attrNames = nameSet.toArray(this.attrNames);
		this.attrIsUsed = new boolean[this.attrNames.length];
		for (int i = 0; i < this.attrIsUsed.length; i++) {
			this.attrIsUsed[i] = this.rand.nextBoolean();
		}
	}

	/**
	 * Creates a new instance.
	 */
	private GAAttributeEvaluationGenome(Instances dataset, String[] attrNames, boolean[] attrIsUsed, double rmse,
			int attrRestriction) {// ,
		this.dataset = dataset;
		this.attrNames = attrNames;
		this.attrIsUsed = attrIsUsed;
		this.rmse = rmse;
		this.score = Math.pow(1 / this.rmse, SCORE_POWER);
		this.attrRestriction = attrRestriction;
	}

	/**
	 * Mutates the genome.
	 * 
	 * @param rate
	 *            Rate of mutating attributes.
	 */
	public void mutate(double rate) {
		// Mutate used attributes
		for (int i = 0; i < this.attrIsUsed.length; i++) {
			if (rand.nextDouble() < rate) {
				this.attrIsUsed[i] = !this.attrIsUsed[i];
			}
		}
		// Apply restriction
		if (this.attrRestriction > 0) {
			this.applyAttrRestriction();
		}
	}

	/**
	 * Performs a cross-over with given second genome.
	 * 
	 * @param genome
	 *            Second genome for performing the cross-over.
	 */
	public void crossOver(GAAttributeEvaluationGenome genome) {
		int start = this.rand.nextInt(this.attrNames.length);
		int end = this.rand.nextInt(this.attrNames.length - (start - 1)) + start;
		// Cross attributes
		boolean[] tmpIsUsed = Arrays.copyOfRange(genome.getAttrIsUsed(), start, end);
		for (int i = start; i < end; i++) {
			genome.getAttrIsUsed()[i] = this.attrIsUsed[i];
			this.attrIsUsed[i] = tmpIsUsed[i - start];
		}
		// Apply restriction
		if (this.attrRestriction > 0) {
			this.applyAttrRestriction();
			genome.applyAttrRestriction();
		}
	}

	/**
	 * Restricts the number of chosen attributes.
	 */
	public void applyAttrRestriction() {
		// count active descriptors
		LinkedList<Integer> activeDescriptors = new LinkedList<Integer>();
		for (int i = 0; i < this.attrIsUsed.length; i++) {
			if (this.attrIsUsed[i]) {
				activeDescriptors.add(i);
			}
		}
		// Apply restriction
		if (activeDescriptors.size() < this.attrRestriction) {
			while (activeDescriptors.size() < this.attrRestriction) {
				int idx = this.rand.nextInt(this.attrIsUsed.length);
				if (!activeDescriptors.contains(idx)) {
					this.attrIsUsed[idx] = true;
					activeDescriptors.add(idx);
				}
			}
		}
		if (activeDescriptors.size() > this.attrRestriction) {
			while (activeDescriptors.size() > this.attrRestriction) {
				int idx = this.rand.nextInt(activeDescriptors.size());
				this.attrIsUsed[activeDescriptors.get(idx)] = false;
				activeDescriptors.remove(idx);
			}
		}
	}

	/**
	 * Creates a dataset depending on which attributes are activated.
	 */
	public void updateDataset() throws Exception {
		WekaTools tools = new WekaTools();
		LinkedList<Integer> attributesToDelete = new LinkedList<Integer>();
		for (int i = 0; i < this.attrIsUsed.length; i++) {
			if (!this.attrIsUsed[i]) {
				String name = (String) this.attrNames[i];
				for (int j = 1; j < this.dataset.numAttributes() - 1; j++) {
					if (this.dataset.attribute(j).name().startsWith(name)) {
						attributesToDelete.add(j + 1);
					}
				}
			}
		}
		this.optDataset = Filter.useFilter(this.dataset, tools.getAttributRemover(this.dataset, attributesToDelete));
	}

	/**
	 * Calculates the score of current genome.
	 * 
	 * @param classifier
	 *            Classifier to perform the classification with.
	 * @param useCV
	 *            True when Cross Validation shall be used.
	 * @param folds
	 *            Number of folds.
	 * @throws Exception
	 */
	public void calculateScore(Classifier classifier, boolean useCV, int folds) throws Exception {
		WekaTools tools = new WekaTools();
		Instances testset = Filter.useFilter(this.optDataset, tools.getIDRemover(this.optDataset));
		Evaluation eval = new Evaluation(testset);
		if (useCV) {
			eval.crossValidateModel(classifier, testset, folds, new Random(1));
		} else {
			classifier.buildClassifier(testset);
			eval.evaluateModel(classifier, testset);
		}
		this.rmse = eval.rootMeanSquaredError();
		this.score = Math.pow(1 / this.rmse, SCORE_POWER);
	}

	@Override
	public String toString() {
		String s = "Attribute;isUsed;Weight\n";
		for (int i = 0; i < this.attrNames.length; i++) {
			s += this.attrNames[i] + ";" + this.attrIsUsed[i] + ";";
		}
		s += "Score: " + this.score + "\n";
		s += "RMSE: " + this.rmse + "\n";
		return s;
	}

	@Override
	public Object clone() {
		GAAttributeEvaluationGenome clone = null;
		clone = new GAAttributeEvaluationGenome(this.dataset, this.attrNames.clone(), this.attrIsUsed.clone(),
				this.rmse, this.attrRestriction);
		return clone;
	}

	/**
	 * @return The current configuration in CSV representation.
	 */
	public String getAttributeSetupCSV() {
		String csv = "Attribute;Selected;\n";
		for (int i = 0; i < this.attrNames.length; i++) {
			csv += this.attrNames[i] + ";" + this.attrIsUsed[i] + ";\n";
		}
		return csv;
	}

	/**
	 * @return The score.
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return Boolean array which contains the activation state for each
	 *         attribute.
	 */
	public boolean[] getAttrIsUsed() {
		return attrIsUsed;
	}

	/**
	 * @return The attribute names.
	 */
	public String[] getAttrNames() {
		return attrNames;
	}

	/**
	 * @param attrNames
	 *            The attribute names.
	 */
	public void setAttrNames(String[] attrNames) {
		this.attrNames = attrNames;
	}

	/**
	 * @return RMSE value.
	 */
	public double getRmse() {
		return rmse;
	}

	/**
	 * @return The optimized dataset.
	 * @throws Exception
	 */
	public Instances getOptDataset() throws Exception {
		this.updateDataset();
		return optDataset;
	}

	/**
	 * @param attrRestriction
	 *            Number of activated attributes. -1 for free selection.FO
	 */
	public void setAttrRestriction(int attrRestriction) {
		this.attrRestriction = attrRestriction;
	}

}
