package org.openscience.cdk.applications.taverna.ui.weka;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.weka.learning.ScatterPlotFromLearningResultAsPDFActivity;

public class ScatternPlotConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = 5507784095907970008L;
	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;

	private JRadioButton testTrainingSetButton;
	private JRadioButton singleSetButton;
	private JCheckBox useCVCheckBox;

	public ScatternPlotConfigurationPanel(AbstractCDKActivity activity) {
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
		this.setLayout(new GridLayout(3, 1));
		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
		ButtonGroup setSelectionGroup = new ButtonGroup();
		this.testTrainingSetButton = new JRadioButton("Add training- and testset port");
		setSelectionGroup.add(this.testTrainingSetButton);
		this.add(testTrainingSetButton);
		this.singleSetButton = new JRadioButton("Add single dataset port");
		setSelectionGroup.add(this.singleSetButton);
		this.add(this.singleSetButton);
		this.useCVCheckBox = new JCheckBox("Perform Cross-validation");
		this.useCVCheckBox.setSelected(false);
		this.add(this.useCVCheckBox);
		this.refreshConfiguration();
	}

	private String createOptionString() {
		String opt = "";
		if (this.testTrainingSetButton.isSelected()) {
			opt += ScatterPlotFromLearningResultAsPDFActivity.TEST_TRAININGSET_PORT + ";";
		} else {
			opt += ScatterPlotFromLearningResultAsPDFActivity.SINGLE_DATASET_PORT + ";";
		}
		opt += this.useCVCheckBox.isSelected() + ";";
		return opt;
	}

	@Override
	public boolean isConfigurationChanged() {
		String currentOpt = this.createOptionString();
		String opt = (String) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_SCATTER_PLOT_OPTIONS);
		if (opt == null) {
			return true;
		}
		return !currentOpt.equals(opt);
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		String newOptions = this.createOptionString();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_SCATTER_PLOT_OPTIONS, newOptions);
	}

	@Override
	public void refreshConfiguration() {
		String options = (String) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_SCATTER_PLOT_OPTIONS);
		if (options != null) {
			String[] tmp = options.split(";");
			if (tmp[0].equals("" + ScatterPlotFromLearningResultAsPDFActivity.TEST_TRAININGSET_PORT)) {
				this.testTrainingSetButton.setSelected(true);
			} else {
				this.singleSetButton.setSelected(true);
			}
			this.useCVCheckBox.setSelected(Boolean.parseBoolean(tmp[1]));
		}
	}

	@Override
	public boolean checkValues() {
		return true;
	}

}
