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
package org.openscience.cdk.applications.taverna;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.openscience.cdk.exception.CDKException;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * Abstract class to describe an Taverna 2 activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public abstract class AbstractCDKActivity extends AbstractAsynchronousActivity<CDKActivityConfigurationBean> implements
		AsynchronousActivity<CDKActivityConfigurationBean> {

	private CDKActivityConfigurationBean configBean;

	public AbstractCDKActivity() {
		// empty
	}

	@Override
	public void configure(CDKActivityConfigurationBean configBean) throws ActivityConfigurationException {
		// Any pre-config sanity checks
		if (configBean.getActivityName().equals("invalidExample")) {
			throw new ActivityConfigurationException("Example string can't be 'invalidExample'");
		}

		this.configBean = configBean;
		configurePorts();
	}

	/**
	 * Do port configuration.
	 */
	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();
		this.addInputPorts();
		this.addOutputPorts();
	}

	/**
	 * Use command addInput() here to add and configure input ports.
	 */
	protected abstract void addInputPorts();

	/**
	 * Use command addOutput() here to add and configure output ports.
	 */
	protected abstract void addOutputPorts();

	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs, final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {

			public void run() {
				// Do work
				Map<String, T2Reference> outputs;
				try {
					outputs = AbstractCDKActivity.this.work(inputs, callback);
				} catch (CDKTavernaException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage());
					callback.fail(e.getMessage());
					return;
				}
				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
			}
		});
	}

	protected abstract Map<String, T2Reference> work(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) throws CDKTavernaException;

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	public abstract String getActivityName();

	public abstract String getFolderName();

	public abstract String getDescription();

	public abstract String getConfigurationPanelClass();

	public abstract HashMap<String, Object> getAdditionalProperties();

}
