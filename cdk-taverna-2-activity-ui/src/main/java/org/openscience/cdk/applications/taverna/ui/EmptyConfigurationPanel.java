package org.openscience.cdk.applications.taverna.ui;

import javax.swing.JLabel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;

public class EmptyConfigurationPanel extends ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = -4481753217310493421L;

	public EmptyConfigurationPanel() {
		this.initGui();
	}

	private void initGui() {
		JLabel label = new JLabel("Nothing to configure!");
		this.add(label);
	}

	@Override
	public boolean checkValues() {
		return false;
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return null;
	}

	@Override
	public boolean isConfigurationChanged() {
		return false;
	}

	@Override
	public void noteConfiguration() {
	}

	@Override
	public void refreshConfiguration() {
	}

}
