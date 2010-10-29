package org.openscience.cdk.applications.taverna.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;

@SuppressWarnings("serial")
public class CDKConfigureAction extends ActivityConfigurationAction<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> configurationPanel = null;

	public CDKConfigureAction(AbstractCDKActivity activity, Frame owner,
			ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> configurationPanel) {
		super(activity);
		this.configurationPanel = configurationPanel;
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog<AbstractCDKActivity, CDKActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		ActivityConfigurationDialog<AbstractCDKActivity, CDKActivityConfigurationBean> dialog = new ActivityConfigurationDialog<AbstractCDKActivity, CDKActivityConfigurationBean>(
				getActivity(), this.configurationPanel);
		ActivityConfigurationAction.setDialog(getActivity(), dialog);
	}

}
