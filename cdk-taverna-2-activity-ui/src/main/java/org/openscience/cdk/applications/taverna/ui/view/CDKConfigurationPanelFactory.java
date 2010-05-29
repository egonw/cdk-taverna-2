package org.openscience.cdk.applications.taverna.ui.view;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.applications.taverna.reactionenumerator.ReactionEnumeratorActivity;
import org.openscience.cdk.applications.taverna.ui.EmptyConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.MDLFileReaderConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.MDLFileWriterConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.reactionenumerator.ReactionEnumeratorConfigurationPanel;

public class CDKConfigurationPanelFactory {

	public static ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> getConfigurationPanel(
			AbstractCDKActivity activity) {
		if (activity instanceof IFileReader) {
			return new MDLFileReaderConfigurationPanel(activity);
		} else if (activity instanceof IFileWriter) {
			return new MDLFileWriterConfigurationPanel(activity);
		} else if (activity instanceof ReactionEnumeratorActivity) {
			return new ReactionEnumeratorConfigurationPanel(activity);
		}
		return new EmptyConfigurationPanel();
	}

}
