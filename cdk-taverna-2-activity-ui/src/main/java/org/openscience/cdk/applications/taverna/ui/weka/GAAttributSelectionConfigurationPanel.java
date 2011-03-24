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
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractLearningConfigurationFrame;
import org.openscience.cdk.applications.taverna.ui.weka.panels.LearningDatasetClassifierFrame;

public class GAAttributSelectionConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {
	private static final long serialVersionUID = 4885493705007067285L;
	private static final String CONFIG_PACKAGE = "org.openscience.cdk.applications.taverna.ui.weka.panels.learning";

	private SPIRegistry<AbstractLearningConfigurationFrame> cdkLearningConfigFramesRegistry = new SPIRegistry<AbstractLearningConfigurationFrame>(
			AbstractLearningConfigurationFrame.class);
	private List<AbstractLearningConfigurationFrame> configFrames = null;

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;
	private GAAttributSelectionConfigurationPanelView view = new GAAttributSelectionConfigurationPanelView();

	private ActionListener configureClassifierAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			int idx = view.getAlgorithmComboBox().getSelectedIndex();
			LearningDatasetClassifierFrame dialog = new LearningDatasetClassifierFrame();
			dialog.getContentPanel().add(configFrames.get(idx));
			dialog.pack();
			dialog.setVisible(true);
		}
	};

	public GAAttributSelectionConfigurationPanel(AbstractCDKActivity activity) {
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
			this.view.getAlgorithmComboBox().setModel(comboBoxModel);
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

	@Override
	public boolean isConfigurationChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void noteConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkValues() {
		// TODO Auto-generated method stub
		return false;
	}

}
