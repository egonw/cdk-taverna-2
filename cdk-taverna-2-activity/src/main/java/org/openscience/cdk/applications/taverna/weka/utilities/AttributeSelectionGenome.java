package org.openscience.cdk.applications.taverna.weka.utilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import org.openscience.cdk.applications.taverna.weka.learning.GAAttributeSelectionActivity;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;

public class AttributeSelectionGenome {

	private static int SCORE_POWER = 2;
	
	private Random rand = new Random();
	private double score = Double.MAX_VALUE;
	private double rmse = 0;
	private int attrRestriction = -1;

	private Instances dataset = null;
	private Instances optDataset = null;
	private String[] attrNames = null;
	private boolean[] attrIsUsed = null;

	public AttributeSelectionGenome(Instances dataset) throws Exception {
		this.dataset = dataset;
		HashSet<String> nameSet = new HashSet<String>();
		for (int i = 1; i < dataset.numAttributes() - 1; i++) {
			nameSet.add(dataset.attribute(i).name());
		}
		this.attrNames = new String[nameSet.size()];
		this.attrNames = nameSet.toArray(this.attrNames);
		this.attrIsUsed = new boolean[this.attrNames.length];
		for (int i = 0; i < this.attrIsUsed.length; i++) {
			if (this.rand.nextDouble() < 0.75) {
				this.attrIsUsed[i] = true;
			} else {
				this.attrIsUsed[i] = false;
			}
		}
	}

	private AttributeSelectionGenome(Instances dataset, String[] attrNames, boolean[] attrIsUsed, double rmse,
			int attrRestriction) {// ,
		this.dataset = dataset;
		this.attrNames = attrNames;
		this.attrIsUsed = attrIsUsed;
		this.rmse = rmse;
		this.score = Math.pow(1 / this.rmse, SCORE_POWER);
		this.attrRestriction = attrRestriction;
	}

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

	public void crossOver(AttributeSelectionGenome genome) {
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

	public void calculateScore(Classifier classifier) throws Exception {
		WekaTools tools = new WekaTools();
		Instances testset = Filter.useFilter(this.optDataset, tools.getIDRemover(this.optDataset));
		Evaluation eval = new Evaluation(testset);
	//	 eval.crossValidateModel(classifier, testset, 10, new Random(1));
		classifier.buildClassifier(testset);
		eval.evaluateModel(classifier, testset);
		this.rmse = eval.rootMeanSquaredError();
		this.score = Math.pow(1 / this.rmse, SCORE_POWER);
	}

	public String toString() {
		String s = "Attribute;isUsed;Weight\n";
		for (int i = 0; i < this.attrNames.length; i++) {
			s += this.attrNames[i] + ";" + this.attrIsUsed[i] + ";";
		}
		s += "Score: " + this.score + "\n";
		s += "RMSE: " + this.rmse + "\n";
		return s;
	}

	public Object clone() {
		AttributeSelectionGenome clone = null;
		clone = new AttributeSelectionGenome(this.dataset, this.attrNames.clone(), this.attrIsUsed.clone(), this.rmse,
				this.attrRestriction);
		return clone;
	}

	public String getAttributeSetupCSV() {
		String csv = "Attribute;Selected;\n";
		for (int i = 0; i < this.attrNames.length; i++) {
			csv += this.attrNames[i] + ";" + this.attrIsUsed[i] + ";\n";
		}
		return csv;
	}

	public double getScore() {
		return score;
	}

	public boolean[] getAttrIsUsed() {
		return attrIsUsed;
	}

	public String[] getAttrNames() {
		return attrNames;
	}

	public void setAttrNames(String[] attrNames) {
		this.attrNames = attrNames;
	}

	public void setAttrIsUsed(boolean[] attrIsUsed) {
		this.attrIsUsed = attrIsUsed;
	}

	public double getRmse() {
		return rmse;
	}

	public Instances getOptDataset() throws Exception {
		this.updateDataset();
		return optDataset;
	}

	public void setAttrRestriction(int attrRestriction) {
		this.attrRestriction = attrRestriction;
	}

}
