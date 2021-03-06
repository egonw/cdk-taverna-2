/* $RCSfile$
 * $Author: thomaskuhn $
 * $Date: 2009-03-02 21:21:33 +0100 (Mo, 02 Mrz 2009) $
 * $Revision: 14307 $
 * 
 * Copyright (C) 2005 by Egon Willighagen <egonw@users.sf.net>
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
package org.openscience.cdk.applications.taverna.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.reaction.enumerator.tools.ErrorLogger;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which represents the Rule OF Five filter activity.
 * 
 * @author Thomas Kuhn
 * 
 */
public class RuleOfFiveFilter extends AbstractCDKActivity {

	private static RuleOfFiveDescriptor descriptor;

	public static final String RULE_OF_FIVE_FILTER_ACTIVITY = "Rule Of Five Filter";

	public RuleOfFiveFilter() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.OUTPUT_PORTS = new String[] { "matchingStructures", "discardedStructures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.OUTPUT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	public String getActivityName() {
		return RuleOfFiveFilter.RULE_OF_FIVE_FILTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + RuleOfFiveFilter.RULE_OF_FIVE_FILTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.FILTER_FOLDER_NAME;
	}

	public void work() throws Exception {
		// Get input
		List<CMLChemFile> inputList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		// Do work
		List<CMLChemFile> matchedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> unmatchedList = new ArrayList<CMLChemFile>();
		if (descriptor == null) {
			descriptor = new RuleOfFiveDescriptor();
		}
		try {
			for (CMLChemFile file : inputList) {
				List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(file);
				for (IAtomContainer molecule : moleculeList) {
					try {
						DescriptorValue value = descriptor.calculate(molecule);
						molecule.setProperty(value.getSpecification(), value);
						if (value.getValue() instanceof IntegerResult
								&& ((IntegerResult) value.getValue()).intValue() == 0) {
							matchedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));
						} else {
							unmatchedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));
						}
					} catch (Exception e) {
						unmatchedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));
						ErrorLogger.getInstance().writeError(
								"Error, calculation of the Descriptor for this molecule caused an error!",
								this.getActivityName(), e);
					}
				}
			}
		} catch (Exception exception) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), exception.getMessage());
		}
		// Set output
		this.setOutputAsObjectList(matchedList, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(unmatchedList, this.OUTPUT_PORTS[1]);
	}

}
