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

	public CDKConfigureAction(AbstractCDKActivity activity, Frame owner) {
		super(activity);

	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog<AbstractCDKActivity, CDKActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> panel;
		try {
			panel = (ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean>) getActivity()
					.getConfiguration().getConfigurationPanelClass().getConstructor(AbstractCDKActivity.class).newInstance(
							getActivity());
			ActivityConfigurationDialog<AbstractCDKActivity, CDKActivityConfigurationBean> dialog = new ActivityConfigurationDialog<AbstractCDKActivity, CDKActivityConfigurationBean>(
					getActivity(), panel);
			ActivityConfigurationAction.setDialog(getActivity(), dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
