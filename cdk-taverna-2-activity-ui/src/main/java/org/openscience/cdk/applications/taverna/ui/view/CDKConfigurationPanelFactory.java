package org.openscience.cdk.applications.taverna.ui.view;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileReader;
import org.openscience.cdk.applications.taverna.interfaces.IPortNumber;
import org.openscience.cdk.applications.taverna.iterativeio.DataCollectorAcceptorActivity;
import org.openscience.cdk.applications.taverna.iterativeio.DataCollectorEmitterActivity;
import org.openscience.cdk.applications.taverna.jchempaint.JChemPaintActivity;
import org.openscience.cdk.applications.taverna.qsar.QSARDescriptorActivity;
import org.openscience.cdk.applications.taverna.ui.EmptyConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.FileReaderConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.FileWriterConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.IterativeFileReaderConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.jchempaint.JChemPaintConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.miscellaneous.DataCollectorAcceptorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.miscellaneous.DataCollectorEmitterConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.miscellaneous.PortNumberConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.qsar.QSARDescriptorConfigurationPanel;

public class CDKConfigurationPanelFactory {

	public static ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> getConfigurationPanel(
			AbstractCDKActivity activity) {
		if (activity instanceof IFileReader) {
			return new FileReaderConfigurationPanel(activity);
		} else if (activity instanceof IFileWriter) {
			return new FileWriterConfigurationPanel(activity);
		} else if (activity instanceof IIterativeFileReader) {
			return new IterativeFileReaderConfigurationPanel(activity);
		} else if (activity instanceof IPortNumber) {
			return new PortNumberConfigurationPanel(activity);
		} else if (activity instanceof JChemPaintActivity) {
			return new JChemPaintConfigurationPanel(activity);
		} else if (activity instanceof QSARDescriptorActivity) {
			return new QSARDescriptorConfigurationPanel(activity);
		}
//		else if (activity instanceof StructureDataCollectorAcceptorActivity) {
//			return new DataCollectorAcceptorConfigurationPanel(activity);
//		} else if (activity instanceof StructureDataCollectorEmitterActivity) {
//			return new DataCollectorEmitterConfigurationPanel(activity);
//		}
		return new EmptyConfigurationPanel();
	}

}
