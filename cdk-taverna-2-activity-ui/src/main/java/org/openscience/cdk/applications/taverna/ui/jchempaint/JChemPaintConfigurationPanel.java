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
package org.openscience.cdk.applications.taverna.ui.jchempaint;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.Constants;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.jchempaint.JChemPaintPanel;

/**
 * Configuration panel for MDL file reading activities.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class JChemPaintConfigurationPanel extends ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = 8171127307831390262L;

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;

	private JChemPaintPanel jcpPanel = null;
	private CMLChemFile file = null;

	public JChemPaintConfigurationPanel(AbstractCDKActivity activity) {
		this.activity = activity;
		this.configBean = this.activity.getConfiguration();
		this.initGUI();
	}

	protected void initGUI() {
		try {
			this.removeAll();
			this.setLayout(new GridLayout());
			this.setPreferredSize(new Dimension(800, 600));
			this.file = (CMLChemFile) configBean.getAdditionalProperty(Constants.PROPERTY_CMLCHEMFILE);
			IChemModel model = null;
			if (file == null) {
				model = new ChemModel();
				IMoleculeSet molSet = new MoleculeSet();
				molSet.addAtomContainer(new Molecule());
				model.setMoleculeSet(molSet);
			} else {
				model = this.file.getChemSequence(0).getChemModel(0);
			}
			this.jcpPanel = new JChemPaintPanel(model, "applet", false, null);
			this.add(this.jcpPanel);
			this.revalidate();
			this.refreshConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkValues() {
		this.file = new CMLChemFile();
		ChemSequence sequence = new ChemSequence();
		sequence.addChemModel(JChemPaintConfigurationPanel.this.jcpPanel.getChemModel());
		this.file.addChemSequence(sequence);
		if (this.file != null) {
			return true;
		}
		JOptionPane.showMessageDialog(this, "No Molecule found!", "Invalid Molecule", JOptionPane.ERROR_MESSAGE);
		// Not valid, return false
		return false;
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public boolean isConfigurationChanged() {
		if (this.file == null) {
			return false;
		}
		CMLChemFile file = (CMLChemFile) configBean.getAdditionalProperty(Constants.PROPERTY_CMLCHEMFILE);
		return !this.file.equals(file);
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		this.configBean.addAdditionalProperty(Constants.PROPERTY_CMLCHEMFILE, this.file);
	}

	@Override
	public void refreshConfiguration() {
		// Nothing to do
	}

}
