/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openscience.cdk.applications.taverna.ui.curation;

/**
 *
 * @author kalai
 */

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;

public class AtomSignatureConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = -4113765289761095130L;
	JTextField Atom_Signature_Height;
	private CDKActivityConfigurationBean configBean;

	public AtomSignatureConfigurationPanel(AbstractCDKActivity activity) {
		this.configBean = activity.getConfiguration();
		JLabel label = new JLabel("Atom Signature height");
		Atom_Signature_Height = new JTextField();
		Integer oldvalue = (Integer) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT);
		Atom_Signature_Height.setText(String.valueOf(oldvalue));
		Atom_Signature_Height.setPreferredSize(new Dimension(40, 30));
		this.add(label);
		this.add(Atom_Signature_Height);

	}

	@Override
	public boolean isConfigurationChanged() {
		String text = Atom_Signature_Height.getText();
		int Atom_Signature_Height_INT = Integer.parseInt(text);
		Integer oldvalue = (Integer) configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT);
		if (oldvalue == null) {
			return true;
		}
		return !(oldvalue.equals(Atom_Signature_Height_INT));
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT,
				Integer.parseInt(Atom_Signature_Height.getText()));
	}

	@Override
	public void refreshConfiguration() {
		Integer value = (Integer) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT);
		this.Atom_Signature_Height.setText(String.valueOf(value));
		this.Atom_Signature_Height.repaint();
	}

	@Override
	public boolean checkValues() {
		try {
			Integer.parseInt(Atom_Signature_Height.getText());
		} catch (Exception exc) {
			return false;
		}
		return true;
	}
}