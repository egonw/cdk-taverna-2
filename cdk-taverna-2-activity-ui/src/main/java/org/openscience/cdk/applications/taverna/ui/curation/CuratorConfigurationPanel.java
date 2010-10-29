/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.cdk.applications.taverna.ui.curation;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;

/**
 * 
 * @author kalai
 */
public class CuratorConfigurationPanel extends ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = -4113765289761095130L;
	JTextField cutoffcount = null;
	private CDKActivityConfigurationBean configBean;

	public CuratorConfigurationPanel(AbstractCDKActivity activity) {
		this.configBean = activity.getConfiguration();
		JLabel label = new JLabel("Atom Count Cut-Off");
		cutoffcount = new JTextField();
		Integer oldvalue = (Integer) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF);
		cutoffcount.setText(String.valueOf(oldvalue));
		cutoffcount.setPreferredSize(new Dimension(40, 30));
		this.add(label);
		this.add(cutoffcount);

	}

	@Override
	public boolean isConfigurationChanged() {
		String text = cutoffcount.getText();
		int cutoffcountint = Integer.parseInt(text);
		Integer oldvalue = (Integer) configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF);
		if (oldvalue == null) {
			return true;
		}
		return !(oldvalue.equals(cutoffcountint));
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF, Integer.parseInt(cutoffcount
				.getText()));
	}

	@Override
	public void refreshConfiguration() {
		Integer value = (Integer) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF);
		this.cutoffcount.setText(String.valueOf(value));
		this.cutoffcount.repaint();
	}

	@Override
	public boolean checkValues() {
		try {
			Integer.parseInt(cutoffcount.getText());
		} catch (Exception exc) {
			return false;
		}
		return true;
	}
}
