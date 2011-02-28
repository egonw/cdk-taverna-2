package org.openscience.cdk.applications.taverna.ui.weka;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClusteringConfigurationFrame;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractLearningConfigurationFrame;

public class WekaLearningConfigurationPanelController extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final String CONFIG_PACKAGE = "org.openscience.cdk.applications.taverna.ui.weka.panels.learning";

	private AbstractCDKActivity activity;
	private WekaLearningConfigurationPanelView view = null;
	private CDKActivityConfigurationBean configBean;
	private SPIRegistry<AbstractLearningConfigurationFrame> cdkLearningConfigFramesRegistry = new SPIRegistry<AbstractLearningConfigurationFrame>(
			AbstractLearningConfigurationFrame.class);
	private List<AbstractLearningConfigurationFrame> configFrames = null;

	private ActionListener comboBoxListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}
	};

	public WekaLearningConfigurationPanelController(AbstractCDKActivity activity) {
		try {
			this.activity = activity;
			this.configBean = this.activity.getConfiguration();
			this.initGUI();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_DURING_ACTIVITY_CONFIGURATION,
					this.getClass().getSimpleName(), e);
			JOptionPane.showMessageDialog(this, CDKTavernaException.ERROR_DURING_ACTIVITY_CONFIGURATION, "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initGUI() {
		this.view = new WekaLearningConfigurationPanelView();
		this.configFrames = new ArrayList<AbstractLearningConfigurationFrame>();
		List<String> learnerNames = new ArrayList<String>();
		for (AbstractLearningConfigurationFrame configFrame : this.cdkLearningConfigFramesRegistry.getInstances()) {
			if (configFrame.getClass().getName().startsWith(CONFIG_PACKAGE)) {
				learnerNames.add(configFrame.getName());
				this.configFrames.add(configFrame);
			}
		}
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(learnerNames.toArray());
		this.view.getLearnerComboBox().setModel(comboBoxModel);
		int idx = view.getLearnerComboBox().getSelectedIndex();
		this.view.add(this.configFrames.get(idx), BorderLayout.CENTER);
		this.add(this.view);
	}

	@Override
	public boolean checkValues() {
		int idx = view.getLearnerComboBox().getSelectedIndex();
		return this.configFrames.get(idx).checkValues();
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public boolean isConfigurationChanged() {
		int idx = view.getLearnerComboBox().getSelectedIndex();
		return this.configFrames.get(idx).isConfigurationChanged();
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		int idx = view.getLearnerComboBox().getSelectedIndex();
		String name = this.configFrames.get(idx).getConfiguredClass().getName();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_NAME, name);
		String[] options = this.configFrames.get(idx).getOptions();
		String opts = "";
		for (String o : options) {
			opts += o + ";";
		}
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_OPTIONS, opts);
	}

	@Override
	public void refreshConfiguration() {
		// TODO Auto-generated method stub

	}

}
