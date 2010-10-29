package org.openscience.cdk.applications.taverna.ui.view;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.ui.config.CDKConfigureAction;

@SuppressWarnings("serial")
public class CDKContextualView extends ContextualView {
	private final AbstractCDKActivity activity;
	private JLabel description = new JLabel("ads");
	private JLabel info = new JLabel();

	public CDKContextualView(AbstractCDKActivity activity) {
		this.activity = activity;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		JPanel jPanel = new JPanel();
		jPanel.add(this.description);
		jPanel.add(this.info);
		refreshView();
		return jPanel;
	}

	@Override
	public String getViewTitle() {
		CDKActivityConfigurationBean configuration = this.activity.getConfiguration();
		return CDKTavernaConstants.CDK_TAVERNA_FOLDER_NAME + " - " + configuration.getActivityName();
	}

	/**
	 * Typically called when the activity configuration has changed.
	 */
	@Override
	public void refreshView() {
		CDKActivityConfigurationBean configuration = this.activity.getConfiguration();
		description.setText(CDKTavernaConstants.CDK_TAVERNA_FOLDER_NAME + " - " + configuration.getFolderName() + " - "
				+ configuration.getActivityName());
	}

	/**
	 * View position hint
	 */
	@Override
	public int getPreferredPosition() {
		// We want to be on top
		return 100;
	}

	@Override
	public Action getConfigureAction(final Frame owner) {
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> configurationPanel = CDKConfigurationPanelFactory
				.getConfigurationPanel(this.activity);
		return new CDKConfigureAction(activity, owner, configurationPanel);
	}

}
