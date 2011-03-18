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
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractLearningConfigurationFrame;
import org.openscience.cdk.applications.taverna.ui.weka.panels.LearningDatasetClassifierFrame;
import org.openscience.cdk.applications.taverna.weka.learning.CreateWekaLearningDatasetActivity;

public class CreateLearningDatasetConfigurationPanelController extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = 4885493705007067285L;
	private static final String CONFIG_PACKAGE = "org.openscience.cdk.applications.taverna.ui.weka.panels.learning";

	private SPIRegistry<AbstractLearningConfigurationFrame> cdkLearningConfigFramesRegistry = new SPIRegistry<AbstractLearningConfigurationFrame>(
			AbstractLearningConfigurationFrame.class);
	private List<AbstractLearningConfigurationFrame> configFrames = null;

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;
	private CreateLearningDatasetConfigurationPanelView view = new CreateLearningDatasetConfigurationPanelView();

	private ActionListener configureClassifierAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			int idx = view.getClassifierComboBox().getSelectedIndex();
			LearningDatasetClassifierFrame dialog = new LearningDatasetClassifierFrame();
			dialog.getContentPanel().add(configFrames.get(idx));
			dialog.pack();
			dialog.setVisible(true);
		}
	};

	public CreateLearningDatasetConfigurationPanelController(AbstractCDKActivity activity) {
		try {
			this.activity = activity;
			this.configBean = this.activity.getConfiguration();
			this.configFrames = new ArrayList<AbstractLearningConfigurationFrame>();
			List<String> learnerNames = new ArrayList<String>();
			for (AbstractLearningConfigurationFrame configFrame : this.cdkLearningConfigFramesRegistry.getInstances()) {
				if (configFrame.getClass().getName().startsWith(CONFIG_PACKAGE)) {
					learnerNames.add(configFrame.getName());
					configFrame.makeSingleOption();
					this.configFrames.add(configFrame);
				}
			}
			DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(learnerNames.toArray());
			this.view.getClassifierComboBox().setModel(comboBoxModel);
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

	private String generateOptionsString() {
		String options = "";
		options += this.view.getLowerRatioTextField().getText() + ";";
		options += this.view.getHigherRatioTextField().getText() + ";";
		options += this.view.getStepsTextField().getText() + ";";
		if (this.view.getRandomRadioButton().isSelected()) {
			options += CreateWekaLearningDatasetActivity.METHODS[0] + ";";
		} else if (this.view.getClusterRadioButton().isSelected()) {
			options += CreateWekaLearningDatasetActivity.METHODS[1] + ";";
		} else if (this.view.getSingleGlobalMaxRadioButton().isSelected()) {
			options += CreateWekaLearningDatasetActivity.METHODS[2] + ";";
			int idx = this.view.getClassifierComboBox().getSelectedIndex();
			options += this.configFrames.get(idx).getConfiguredClass().getName() + ";";
			options += this.configFrames.get(idx).getOptions()[0] + ";";
			options += this.view.getIterationsTextField().getText() + ";";
			options += this.view.getUseBlacklistingCheckBox().isSelected() + ";";
			options += this.view.getChooseBestCheckBox().isSelected() + ";";
		}
		return options;
	}

	@Override
	public boolean checkValues() {
		if (this.view.getSingleGlobalMaxRadioButton().isSelected()
				&& !UITools.checkTextFieldValueInt(this, "# of Iterations", this.view.getIterationsTextField(), 1,
						Integer.MAX_VALUE)) {
			return false;
		}
		if (!UITools
				.checkTextFieldValueDouble(this, "Lower fraction limit", this.view.getLowerRatioTextField(), 1, 100)
				|| !UITools.checkTextFieldValueDouble(this, "Higher fraction limit",
						this.view.getHigherRatioTextField(), 1, 100)
				|| !UITools.checkTextFieldValueInt(this, "Number of steps", this.view.getStepsTextField(), 1,
						Integer.MAX_VALUE)) {
			return false;
		}
		double lower = Double.parseDouble(this.view.getLowerRatioTextField().getText());
		double higher = Double.parseDouble(this.view.getHigherRatioTextField().getText());
		if (higher <= lower) {
			JOptionPane.showMessageDialog(this, "The higher fraction limit has to be greater than the lower limit!",
					"Illegal Argument", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public boolean isConfigurationChanged() {
		String currentOption = (String) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_CREATE_SET_OPTIONS);
		String newOptions = this.generateOptionsString();
		if (currentOption == null) {
			return true;
		}
		return !currentOption.equals(newOptions);
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		String newOptions = this.generateOptionsString();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_CREATE_SET_OPTIONS, newOptions);

	}

	@Override
	public void refreshConfiguration() {
		if (this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_CREATE_SET_OPTIONS) == null) {
			return;
		}
		String currentOption = (String) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_CREATE_SET_OPTIONS);
		String[] options = currentOption.split(";");
		this.view.getLowerRatioTextField().setText(options[0]);
		this.view.getHigherRatioTextField().setText(options[1]);
		this.view.getStepsTextField().setText(options[2]);
		if (options[3].equals(CreateWekaLearningDatasetActivity.METHODS[0])) {
			this.view.getRandomRadioButton().setSelected(true);
			this.view.getIterationsTextField().setEnabled(false);
			this.view.getClassifierComboBox().setEnabled(false);
			this.view.getUseBlacklistingCheckBox().setEnabled(false);
			this.view.getChooseBestCheckBox().setEnabled(false);
			this.view.getBtnConfigure().setEnabled(false);
		} else if (options[3].equals(CreateWekaLearningDatasetActivity.METHODS[1])) {
			this.view.getClusterRadioButton().setSelected(true);
			this.view.getIterationsTextField().setEnabled(false);
			this.view.getClassifierComboBox().setEnabled(false);
			this.view.getUseBlacklistingCheckBox().setEnabled(false);
			this.view.getChooseBestCheckBox().setEnabled(false);
			this.view.getBtnConfigure().setEnabled(false);
		} else if (options[3].equals(CreateWekaLearningDatasetActivity.METHODS[2])) {
			this.view.getSingleGlobalMaxRadioButton().setSelected(true);
			for (int i = 0; i < this.configFrames.size(); i++) {
				if (options[4].equals(this.configFrames.get(i).getConfiguredClass().getName())) {
					this.configFrames.get(i).setOptions(new String[] { options[5] });
					this.view.getClassifierComboBox().setSelectedIndex(i);
					break;
				}
			}
			this.view.getIterationsTextField().setText(options[6]);
			this.view.getUseBlacklistingCheckBox().setSelected(Boolean.parseBoolean(options[7]));
			this.view.getChooseBestCheckBox().setSelected(Boolean.parseBoolean(options[8]));
			this.view.getIterationsTextField().setEnabled(true);
			this.view.getClassifierComboBox().setEnabled(true);
			this.view.getUseBlacklistingCheckBox().setEnabled(true);
			this.view.getChooseBestCheckBox().setEnabled(true);
			this.view.getBtnConfigure().setEnabled(true);
		}
	}

}
