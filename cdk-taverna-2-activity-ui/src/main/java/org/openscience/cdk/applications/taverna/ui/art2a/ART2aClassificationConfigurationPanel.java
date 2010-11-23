package org.openscience.cdk.applications.taverna.ui.art2a;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.Preferences;
import org.openscience.cdk.applications.taverna.basicutilities.CDKFileFilter;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.ui.io.FileReaderConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.FileWriterConfigurationPanel;

public class ART2aClassificationConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = 2709664402538725557L;
	private ART2aView view = null;
	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;
	private File file = null;

	private AbstractAction chooseFileAction = new AbstractAction() {

		private static final long serialVersionUID = 2594222854083984583L;

		public void actionPerformed(ActionEvent e) {
			JFileChooser openDialog = new JFileChooser(new File(Preferences.getInstance().getCurrentDirectory()));
			openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (openDialog.showOpenDialog(ART2aClassificationConfigurationPanel.this) == JFileChooser.APPROVE_OPTION) {
				Preferences.getInstance().setCurrentDirectory(openDialog.getCurrentDirectory().getPath());
				ART2aClassificationConfigurationPanel.this.file = openDialog.getSelectedFile();
				ART2aClassificationConfigurationPanel.this.showValue();
			}
		}
	};

	private void showValue() {
		this.view.getPathTextField().setText(this.file.getPath());
		this.view.getPathTextField().repaint();
		this.revalidate();
	}

	public ART2aClassificationConfigurationPanel(AbstractCDKActivity activity) {
		this.activity = activity;
		this.configBean = this.activity.getConfiguration();
		this.initGUI();
	}

	private void initGUI() {
		try {
			this.view = new ART2aView();
			int numberOfClassifications = (Integer) this.configBean
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_CLASSIFICATIONS);
			this.view.getNumberOfClassificationsTextField().setText(String.valueOf(numberOfClassifications));
			double upperVigilanceLimit = (Double) this.configBean
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_UPPER_VIGILANCE_LIMIT);
			this.view.getUpperVigilanceLimitTextField().setText(String.valueOf(upperVigilanceLimit));
			double lowerVigilanceLimit = (Double) this.configBean
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_LOWER_VIGILANCE_LIMIT);
			this.view.getLowerVigilanceLimitTextField().setText(String.valueOf(lowerVigilanceLimit));
			int maximumClassificationTime = (Integer) this.configBean
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_MAXIMUM_CLASSIFICATION_TIME);
			this.view.getMaximumClassificationTimeTextField().setText(String.valueOf(maximumClassificationTime));
			boolean scaleFingerprintItems = (Boolean) this.configBean
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_SCALE_FINGERPRINT_ITEMS);
			this.view.getScaleFingerprintItemsCheckBox().setSelected(scaleFingerprintItems);
			this.view.getPathButton().addActionListener(this.chooseFileAction);
			ClassLoader cld = getClass().getClassLoader();
			URL url = cld.getResources("icons/open.gif").nextElement();
			ImageIcon icon = new ImageIcon(url);
			this.view.getPathButton().setIcon(icon);
			this.add(this.view);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during setting up configuration panel!", this.getClass().getSimpleName(),
					e);
		}
	}

	@Override
	public boolean isConfigurationChanged() {
		int numberOfClassificationsCurrent = (Integer) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_CLASSIFICATIONS);
		int numberOfClassifications = Integer.parseInt(this.view.getNumberOfClassificationsTextField().getText());
		if (numberOfClassificationsCurrent != numberOfClassifications) {
			return true;
		}
		double upperVigilanceLimitCurrent = (Double) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_UPPER_VIGILANCE_LIMIT);
		double upperVigilanceLimit = Double.parseDouble(this.view.getUpperVigilanceLimitTextField().getText());
		if (upperVigilanceLimitCurrent != upperVigilanceLimit) {
			return true;
		}
		double lowerVigilanceLimitCurrent = (Double) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_LOWER_VIGILANCE_LIMIT);
		double lowerVigilanceLimit = Double.parseDouble(this.view.getLowerVigilanceLimitTextField().getText());
		if (lowerVigilanceLimitCurrent != lowerVigilanceLimit) {
			return true;
		}
		int maximumClassificationTimeCurrent = (Integer) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_MAXIMUM_CLASSIFICATION_TIME);
		int maximumClassificationTime = Integer.parseInt(this.view.getMaximumClassificationTimeTextField().getText());
		if (maximumClassificationTimeCurrent != maximumClassificationTime) {
			return true;
		}
		boolean scaleFingerprintItemsCurrent = (Boolean) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_SCALE_FINGERPRINT_ITEMS);
		boolean scaleFingerprintItems = this.view.getScaleFingerprintItemsCheckBox().isSelected();
		if (scaleFingerprintItemsCurrent != scaleFingerprintItems) {
			return true;
		}
		if (this.file == null) {
			return false;
		}
		File file = (File) configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		return !this.file.equals(file);
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		int numberOfClassifications = Integer.parseInt(this.view.getNumberOfClassificationsTextField().getText());
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_CLASSIFICATIONS, numberOfClassifications);
		double upperVigilanceLimit = Double.parseDouble(this.view.getUpperVigilanceLimitTextField().getText());
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_UPPER_VIGILANCE_LIMIT, upperVigilanceLimit);
		double lowerVigilanceLimit = Double.parseDouble(this.view.getLowerVigilanceLimitTextField().getText());
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_LOWER_VIGILANCE_LIMIT, lowerVigilanceLimit);
		int maximumClassificationTime = Integer.parseInt(this.view.getMaximumClassificationTimeTextField().getText());
		this.configBean
				.addAdditionalProperty(CDKTavernaConstants.PROPERTY_MAXIMUM_CLASSIFICATION_TIME, maximumClassificationTime);
		boolean scaleFingerprintItems = this.view.getScaleFingerprintItemsCheckBox().isSelected();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_SCALE_FINGERPRINT_ITEMS, scaleFingerprintItems);
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE, this.file);
	}

	@Override
	public void refreshConfiguration() {
		int numberOfClassifications = (Integer) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_CLASSIFICATIONS);
		this.view.getNumberOfClassificationsTextField().setText(String.valueOf(numberOfClassifications));
		double upperVigilanceLimit = (Double) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_UPPER_VIGILANCE_LIMIT);
		this.view.getUpperVigilanceLimitTextField().setText(String.valueOf(upperVigilanceLimit));
		double lowerVigilanceLimit = (Double) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_LOWER_VIGILANCE_LIMIT);
		this.view.getLowerVigilanceLimitTextField().setText(String.valueOf(lowerVigilanceLimit));
		int maximumClassificationTime = (Integer) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_MAXIMUM_CLASSIFICATION_TIME);
		this.view.getMaximumClassificationTimeTextField().setText(String.valueOf(maximumClassificationTime));
		boolean scaleFingerprintItems = (Boolean) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_SCALE_FINGERPRINT_ITEMS);
		this.view.getScaleFingerprintItemsCheckBox().setSelected(scaleFingerprintItems);
		this.file = (File) configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (this.file != null) {
			this.view.getPathTextField().setText(this.file.getAbsolutePath());
			this.view.getPathTextField().repaint();
		}
	}

	@Override
	public boolean checkValues() {
		try {
			int numberOfClassifications = Integer.parseInt(this.view.getNumberOfClassificationsTextField().getText());
			if (numberOfClassifications < 0) {
				throw new CDKTavernaException(this.getClass().getSimpleName(), "Invalid Number");
			}
			int maximumClassificationTime = Integer.parseInt(this.view.getMaximumClassificationTimeTextField().getText());
			if (maximumClassificationTime < 0) {
				throw new CDKTavernaException(this.getClass().getSimpleName(), "Invalid Number");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number > 0!", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try {
			double upperVigilanceLimit = Double.parseDouble(this.view.getUpperVigilanceLimitTextField().getText());
			if (upperVigilanceLimit < 0 || upperVigilanceLimit > 1) {
				throw new CDKTavernaException(this.getClass().getSimpleName(), "Invalid Number");
			}
			double lowerVigilanceLimit = Double.parseDouble(this.view.getLowerVigilanceLimitTextField().getText());
			if (lowerVigilanceLimit < 0 || lowerVigilanceLimit > 1) {
				throw new CDKTavernaException(this.getClass().getSimpleName(), "Invalid Number");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Please enter a valid floating point number number beetween 0.0 and 1.0!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (this.file != null && this.file.exists()) {
			return true;
		}
		JOptionPane.showMessageDialog(this, "Chosen directory is not valid!", "Invalid directory", JOptionPane.ERROR_MESSAGE);
		// Not valid, return false
		return false;
	}

}
