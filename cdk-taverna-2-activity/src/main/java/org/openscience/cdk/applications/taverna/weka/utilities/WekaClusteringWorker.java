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

import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.weka.clustering.WekaClusteringActivity;

import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.OptionHandler;

/**
 * Worker for the weka clustering activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class WekaClusteringWorker extends Thread {

	private String className = null;
	private String[] options = null;
	private Instances dataset = null;
	private WekaClusteringActivity owner = null;
	private boolean done = false;

	/**
	 * Creates a new instance.
	 */
	public WekaClusteringWorker(WekaClusteringActivity owner, String className, String[] options, Instances dataset) {
		this.owner = owner;
		this.className = className;
		this.options = options;
		this.dataset = dataset;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Clusterer clusterer = null;
		try {
			Class<? extends Clusterer> clustererClass = (Class<? extends Clusterer>) Class.forName(this.className);
			clusterer = clustererClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String optionString = "";
		try {
			for (String o : this.options) {
				optionString += o;
			}
			this.options = Arrays.copyOfRange(this.options, 0, this.options.length - 2);
			((OptionHandler) clusterer).setOptions(this.options);
			// Do clustering
			clusterer.buildClusterer(dataset);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.CLUSTERING_ERROR, this.getClass().getSimpleName(),
					e);
		} finally {
			done = true;
			this.owner.workerDone(clusterer, optionString);
		}
	}

	/**
	 * @return True when worker is done.
	 */
	public boolean isDone() {
		return done;
	}

}
