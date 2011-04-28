package org.openscience.cdk.applications.taverna.ui.weka;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClassificationConfigurationFrame;

public class WekaClassificationConfigurationPanelController extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = 1159635990433702962L;

	private static final String CONFIG_PACKAGE = "org.openscience.cdk.applications.taverna.ui.weka.panels.classification";

	private AbstractCDKActivity activity;
	private WekaLearningConfigurationPanelView view = null;
	private CDKActivityConfigurationBean configBean;
	private SPIRegistry<AbstractClassificationConfigurationFrame> cdkLearningConfigFramesRegistry = new SPIRegistry<AbstractClassificationConfigurationFrame>(
			AbstractClassificationConfigurationFrame.class);
	private List<AbstractClassificationConfigurationFrame> configFrames = null;
	private int currentSelection;

	private ActionListener comboBoxListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			view.remove(configFrames.get(currentSelection));
			int idx = view.getLearnerComboBox().getSelectedIndex();
			view.add(configFrames.get(idx), BorderLayout.CENTER);
			currentSelection = idx;
			view.revalidate();
			view.repaint();
		}
	};

	private ActionListener algorithmComboBoxListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			setThreadingParam();
		}
	};

	public WekaClassificationConfigurationPanelController(AbstractCDKActivity activity) {
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
		this.configFrames = new ArrayList<AbstractClassificationConfigurationFrame>();
		List<String> learnerNames = new ArrayList<String>();
		for (AbstractClassificationConfigurationFrame configFrame : this.cdkLearningConfigFramesRegistry.getInstances()) {
			if (configFrame.getClass().getName().startsWith(CONFIG_PACKAGE)) {
				learnerNames.add(configFrame.getName());
				this.configFrames.add(configFrame);
			}
		}
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(learnerNames.toArray());
		this.view.getLearnerComboBox().setModel(comboBoxModel);
		this.view.getLearnerComboBox().addActionListener(this.comboBoxListener);
		this.view.getLearnerComboBox().addActionListener(this.algorithmComboBoxListener);
		int numberOfThreads = (Integer) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
		this.view.getThreadsTextField().setText("" + numberOfThreads);
		this.refreshConfiguration();
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
		String name = (String) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_NAME);
		int idx = view.getLearnerComboBox().getSelectedIndex();
		if (name == null || !name.equals(this.configFrames.get(idx).getConfiguredClass().getName())) {
			return true;
		}
		String opts = (String) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_OPTIONS);
		if (opts == null || !opts.equals(this.getConfString())) {
			return true;
		}
		return false;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		int idx = view.getLearnerComboBox().getSelectedIndex();
		String name = this.configFrames.get(idx).getConfiguredClass().getName();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_NAME, name);
		String opts = this.getConfString();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_OPTIONS, opts);
		int threads = Integer.parseInt(this.view.getThreadsTextField().getText());
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, threads);
	}

	private String getConfString() {
		int idx = view.getLearnerComboBox().getSelectedIndex();
		String[] options = this.configFrames.get(idx).getOptions();
		String opts = "";
		for (String o : options) {
			opts += o + ";";
		}
		return opts;
	}

	private void setThreadingParam() {
		int threads = 1;
		int idx = this.view.getLearnerComboBox().getSelectedIndex();
		if (this.configFrames.get(idx).useThreading()) {
			threads = (Integer) this.configBean
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
			this.view.getThreadsTextField().setEnabled(true);
		} else {
			this.view.getThreadsTextField().setEnabled(false);
		}
		this.view.getThreadsTextField().setText("" + threads);
	}

	@Override
	public void refreshConfiguration() {
		this.setThreadingParam();
		if (this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_NAME) != null) {
			for (int i = 0; i < this.configFrames.size(); i++) {
				String name = (String) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_NAME);
				if (this.configFrames.get(i).getConfiguredClass().getName().equals(name)) {
					this.currentSelection = i;
					String options = (String) this.configBean
							.getAdditionalProperty(CDKTavernaConstants.PROPERTY_LEARNER_OPTIONS);
					this.configFrames.get(i).setOptions(options.split(";"));
					this.view.getLearnerComboBox().setSelectedIndex(i);
					break;
				}
			}
		} else {
			this.currentSelection = view.getLearnerComboBox().getSelectedIndex();
		}
		this.view.add(this.configFrames.get(this.currentSelection), BorderLayout.CENTER);
		this.view.revalidate();
		this.view.repaint();
	}

}
