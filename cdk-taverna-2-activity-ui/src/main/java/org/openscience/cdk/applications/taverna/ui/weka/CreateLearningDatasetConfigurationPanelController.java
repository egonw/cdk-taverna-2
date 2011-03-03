package org.openscience.cdk.applications.taverna.ui.weka;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.weka.learning.CreateWekaLearningDatasetActivity;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.M5P;

public class CreateLearningDatasetConfigurationPanelController extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final String[] CLASSIFIERS = new String[] { "Multiple Lineare Regression",
			"Multilayer Perceptron Neural Network", "Regression Tree", "Support Vector Machine" };
	private static final Class<?>[] classifierClasses = new Class<?>[] { LinearRegression.class,
			MultilayerPerceptron.class, M5P.class, LibSVM.class };

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;
	private CreateLearningDatasetConfigurationPanelView view = new CreateLearningDatasetConfigurationPanelView();

	public CreateLearningDatasetConfigurationPanelController(AbstractCDKActivity activity) {
		try {
			this.activity = activity;
			this.configBean = this.activity.getConfiguration();
			DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(CLASSIFIERS);
			this.view.getClassifierComboBox().setModel(comboBoxModel);
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
		if (this.view.getRandomRadioButton().isSelected()) {
			options += CreateWekaLearningDatasetActivity.METHODS[0] + ";";
		} else if (this.view.getClusterRadioButton().isSelected()) {
			options += CreateWekaLearningDatasetActivity.METHODS[1] + ";";
		} else if (this.view.getSingleGlobalMaxRadioButton().isSelected()) {
			options += CreateWekaLearningDatasetActivity.METHODS[2] + ";";
			int idx = this.view.getClassifierComboBox().getSelectedIndex();
			options += classifierClasses[idx].getName() + ";";
			options += this.view.getIterationsTextField().getText() + ";";
			options += this.view.getUseBlacklistingCheckBox().isSelected() + ";";
		}
		return options;

	}

	@Override
	public boolean checkValues() {
		if (this.view.getSingleGlobalMaxRadioButton().isSelected()) {
			Integer value = null;
			try {
				value = Integer.parseInt(this.view.getIterationsTextField().getText());
			} catch (Exception e) {
			}
			if (value == null || value < 1) {
				JOptionPane.showMessageDialog(this.view, "Please set a valid naumber of iterations!",
						"Illegal Argument", JOptionPane.ERROR_MESSAGE);
				return false;
			}
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
		if (options[0].equals(CreateWekaLearningDatasetActivity.METHODS[0])) {
			this.view.getRandomRadioButton().setSelected(true);
			this.view.getIterationsTextField().setEnabled(false);
			this.view.getClassifierComboBox().setEnabled(false);
			this.view.getUseBlacklistingCheckBox().setEnabled(false);
		} else if (options[0].equals(CreateWekaLearningDatasetActivity.METHODS[1])) {
			this.view.getClusterRadioButton().setSelected(true);
			this.view.getIterationsTextField().setEnabled(false);
			this.view.getClassifierComboBox().setEnabled(false);
			this.view.getUseBlacklistingCheckBox().setEnabled(false);
		} else if (options[0].equals(CreateWekaLearningDatasetActivity.METHODS[2])) {
			this.view.getSingleGlobalMaxRadioButton().setSelected(true);
			if (options[1].equals(classifierClasses[0].getName())) {
				this.view.getClassifierComboBox().setSelectedIndex(0);
			} else if (options[1].equals(classifierClasses[1].getName())) {
				this.view.getClassifierComboBox().setSelectedIndex(1);
			} else if (options[1].equals(classifierClasses[2].getName())) {
				this.view.getClassifierComboBox().setSelectedIndex(2);
			} else if (options[1].equals(classifierClasses[3].getName())) {
				this.view.getClassifierComboBox().setSelectedIndex(3);
			}
			this.view.getIterationsTextField().setText(options[2]);
			this.view.getUseBlacklistingCheckBox().setSelected(Boolean.parseBoolean(options[3]));
			this.view.getIterationsTextField().setEnabled(true);
			this.view.getClassifierComboBox().setEnabled(true);
			this.view.getUseBlacklistingCheckBox().setEnabled(true);
		}
	}

}
