/* Copyright (C) 2005,2008-2011  Egon Willighagen <egonw@users.sf.net>
 *                    2008-2009  Rajarshi Guha <rajarshi@users.sf.net>
 *                    2010-2011  Andreas Truszkowski <atruszkowski@gmx.de>
 *                    2005-2009  Thomas Kuhn <thomas.kuhn@gnwi.de>
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
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.RotatableBondsCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IntegerResult;
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
					boolean fail = hasOneOrMoreFails(molecule);
					if (fail) {
						unmatchedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));
					} else {
						matchedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));
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

	private boolean hasOneOrMoreFails(IAtomContainer mol) {
        IMolecularDescriptor xlogP = new XLogPDescriptor();
        Object[] xlogPparams = {
        	Boolean.TRUE,
            Boolean.TRUE,
        };

        try {
            IMolecularDescriptor acc = new HBondAcceptorCountDescriptor();
            Object[] hBondparams = {true};
            acc.setParameters(hBondparams);
            int acceptors = ((IntegerResult) acc.calculate(mol).getValue()).intValue();
            if (acceptors > 10) return true;

            IMolecularDescriptor don = new HBondDonorCountDescriptor();
            don.setParameters(hBondparams);
            int donors = ((IntegerResult) don.calculate(mol).getValue()).intValue();
            if (donors > 5) return true;

            IMolecularDescriptor mw = new WeightDescriptor();
            Object[] mwparams = {""};
            mw.setParameters(mwparams);
            double mwvalue = ((DoubleResult) mw.calculate(mol).getValue()).doubleValue();
            if (mwvalue > 500.0) return true;

            IMolecularDescriptor rotate = new RotatableBondsCountDescriptor();
            rotate.setParameters(hBondparams);
            int rotatablebonds = ((IntegerResult) rotate.calculate(mol).getValue()).intValue();
            if (rotatablebonds > 10.0) return true;

            xlogP.setParameters(xlogPparams);
            double xlogPvalue = ((DoubleResult) xlogP.calculate(mol).getValue()).doubleValue();
            if (xlogPvalue > 5.0) return true;
        } catch (CDKException e) {
            return true;
        }
        
        return false;
	}

}
