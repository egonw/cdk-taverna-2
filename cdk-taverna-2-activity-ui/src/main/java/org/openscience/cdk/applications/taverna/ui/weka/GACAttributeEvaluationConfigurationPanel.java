package org.openscience.cdk.applications.taverna.ui.weka;

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
import org.openscience.cdk.applications.taverna.ui.UITools;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClassificationConfigurationFrame;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractRegressionConfigurationFrame;
import org.openscience.cdk.applications.taverna.ui.weka.panels.LearningDatasetClassifierFrame;

public class GACAttributeEvaluationConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {
	private static final long serialVersionUID = 4885493705007067285L;
	private static final String CONFIG_PACKAGE = "org.openscience.cdk.applications.taverna.ui.weka.panels.classification";

	private SPIRegistry<AbstractClassificationConfigurationFrame> cdkLearningConfigFramesRegistry = new SPIRegistry<AbstractClassificationConfigurationFrame>(
			AbstractClassificationConfigurationFrame.class);
	private List<AbstractClassificationConfigurationFrame> configFrames = null;

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;
	private GAAttributeEvaluationConfigurationPanelView view = new GAAttributeEvaluationConfigurationPanelView();

	private ActionListener configureClassifierAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			int idx = view.getAlgorithmComboBox().getSelectedIndex();
			LearningDatasetClassifierFrame dialog = new LearningDatasetClassifierFrame();
			dialog.getContentPanel().add(configFrames.get(idx));
			dialog.pack();
			dialog.setVisible(true);
		}
	};

	private ActionListener algorithmComboBoxListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			setThreadingParam();
		}
	};

	public GACAttributeEvaluationConfigurationPanel(AbstractCDKActivity activity) {
		try {
			this.activity = activity;
			this.configBean = this.activity.getConfiguration();
			this.configFrames = new ArrayList<AbstractClassificationConfigurationFrame>();
			List<String> learnerNames = new ArrayList<String>();
			for (AbstractClassificationConfigurationFrame configFrame : this.cdkLearningConfigFramesRegistry.getInstances()) {
				if (configFrame.getClass().getName().startsWith(CONFIG_PACKAGE)) {
					learnerNames.add(configFrame.getName());
					configFrame.makeSingleOption();
					this.configFrames.add(configFrame);
				}
			}
			DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(learnerNames.toArray());
			this.view.getAlgorithmComboBox().setModel(comboBoxModel);
			this.view.getAlgorithmComboBox().addActionListener(this.algorithmComboBoxListener);
			this.view.getBtnConfigure().addActionListener(this.configureClassifierAction);
			this.add(this.view);
			this.refreshConfiguration();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_DURING_ACTIVITY_CONFIGURATION,
					this.getClass().getSimpleName(), e);
			JOptionPane.showMessageDialog(this, CDKTavernaException.ERROR_DURING_ACTIVITY_CONFIGURATION, "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private String createOptionsString() {
		String options = "";
		options += this.view.getAttrRateTextField().getText() + ";";
		options += this.view.getCoRateTextField().getText() + ";";
		options += this.view.getNumIndTextField().getText() + ";";
		options += this.view.getIterationsTextField().getText() + ";";
		options += this.view.getMinAttrTextField().getText() + " " + this.view.getMaxAttrTextField().getText() + " "
				+ this.view.getStepSizeTextField().getText() + ";";
		int idx = this.view.getAlgorithmComboBox().getSelectedIndex();
		options += this.configFrames.get(idx).getConfiguredClass().getName() + ";";
		options += this.configFrames.get(idx).getOptions()[0] + ";";
		options += this.view.getCvCheckBox().isSelected() + ";";
		options += this.view.getCvTextField().getText() + ";";
		return options;
	}

	@Override
	public boolean isConfigurationChanged() {
		String currentOptions = (String) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_GA_ATTRIBUTE_SELECTION_OPTIONS);
		if (currentOptions == null) {
			return true;
		}
		String options = this.createOptionsString();
		return !options.equals(currentOptions);
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		String newOptions = this.createOptionsString();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_GA_ATTRIBUTE_SELECTION_OPTIONS, newOptions);
		int threads = Integer.parseInt(this.view.getThreadsTextField().getText());
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, threads);
	}

	private void setThreadingParam() {
		int threads = 1;
		int idx = this.view.getAlgorithmComboBox().getSelectedIndex();
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
		String currentOptions = (String) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_GA_ATTRIBUTE_SELECTION_OPTIONS);
		if (currentOptions == null) {
			return;
		}
		String[] opt = currentOptions.split(";");
		this.view.getAttrRateTextField().setText(opt[0]);
		this.view.getCoRateTextField().setText(opt[1]);
		this.view.getNumIndTextField().setText(opt[2]);
		this.view.getIterationsTextField().setText(opt[3]);
		String[] attrOpt = opt[4].split(" ");
		if (attrOpt.length < 3) {
			this.view.getMinAttrTextField().setText("-1");
			this.view.getMaxAttrTextField().setText("");
			this.view.getStepSizeTextField().setText("");
		} else {
			this.view.getMinAttrTextField().setText(attrOpt[0]);
			this.view.getMaxAttrTextField().setText(attrOpt[1]);
			this.view.getStepSizeTextField().setText(attrOpt[2]);
		}
		for (int i = 0; i < this.configFrames.size(); i++) {
			if (opt[5].equals(this.configFrames.get(i).getConfiguredClass().getName())) {
				this.configFrames.get(i).setOptions(new String[] { opt[6] });
				this.view.getAlgorithmComboBox().setSelectedIndex(i);
				break;
			}
		}
		this.view.getCvCheckBox().setSelected(Boolean.parseBoolean(opt[7]));
		this.view.getCvTextField().setText(opt[8]);
	}

	@Override
	public boolean checkValues() {
		int idx = view.getAlgorithmComboBox().getSelectedIndex();
		if (!this.configFrames.get(idx).checkValues()) {
			return false;
		}
		if (!UITools.checkTextFieldValueInt(this, "Number of Threads", this.view.getThreadsTextField(), 1,
				Integer.MAX_VALUE)
				|| !UITools.checkTextFieldValueInt(this, "Number of Folds", this.view.getCvTextField(), 2,
						Integer.MAX_VALUE)) {
			return false;
		}
		return true;
	}

}
