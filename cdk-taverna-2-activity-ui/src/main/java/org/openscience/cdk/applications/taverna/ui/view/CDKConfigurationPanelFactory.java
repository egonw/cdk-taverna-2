package org.openscience.cdk.applications.taverna.ui.view;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.curation.MoleculeConnectivityCheckerActivity;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileReader;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileWriter;
import org.openscience.cdk.applications.taverna.interfaces.IPortNumber;
import org.openscience.cdk.applications.taverna.jchempaint.JChemPaintActivity;
import org.openscience.cdk.applications.taverna.qsar.QSARDescriptorActivity;
import org.openscience.cdk.applications.taverna.reactionenumerator.ReactionEnumeratorActivity;
import org.openscience.cdk.applications.taverna.ui.EmptyConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.curation.CuratorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.FileReaderConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.FileWriterConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.IterativeFileWriterConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.IterativeFileReaderConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.jchempaint.JChemPaintConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.miscellaneous.PortNumberConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.qsar.QSARDescriptorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.reactionenumerator.ReactionEnumeratorConfigurationPanel;

public class CDKConfigurationPanelFactory {

	public static ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> getConfigurationPanel(
			AbstractCDKActivity activity) {
		if (activity instanceof IFileReader) {
			return new FileReaderConfigurationPanel(activity);
		} else if (activity instanceof IFileWriter) {
			return new FileWriterConfigurationPanel(activity);
		}  else if (activity instanceof IIterativeFileWriter) {
			return new IterativeFileWriterConfigurationPanel(activity);
		} else if (activity instanceof IIterativeFileReader) {
			return new IterativeFileReaderConfigurationPanel(activity);
		} else if (activity instanceof IPortNumber) {
			return new PortNumberConfigurationPanel(activity);
		} else if (activity instanceof JChemPaintActivity) {
			return new JChemPaintConfigurationPanel(activity);
		} else if (activity instanceof QSARDescriptorActivity) {
			return new QSARDescriptorConfigurationPanel(activity);
		} else if (activity instanceof MoleculeConnectivityCheckerActivity) {
			return new CuratorConfigurationPanel(activity);
		} else if (activity instanceof ReactionEnumeratorActivity) {
			return new ReactionEnumeratorConfigurationPanel(activity);
		}
		return new EmptyConfigurationPanel();
	}

}
