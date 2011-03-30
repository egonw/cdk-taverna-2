package org.openscience.cdk.applications.taverna.ui.view;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.classification.art2a.ART2aClassificationActivity;
import org.openscience.cdk.applications.taverna.curation.MoleculeConnectivityCheckerActivity;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileWriter;
import org.openscience.cdk.applications.taverna.interfaces.IPortNumber;
import org.openscience.cdk.applications.taverna.jchempaint.JChemPaintActivity;
import org.openscience.cdk.applications.taverna.qsar.CurateQSARVectorActivity;
import org.openscience.cdk.applications.taverna.qsar.QSARDescriptorActivity;
import org.openscience.cdk.applications.taverna.qsar.QSARDescriptorThreadedActivity;
import org.openscience.cdk.applications.taverna.reactionenumerator.ReactionEnumeratorActivity;
import org.openscience.cdk.applications.taverna.signaturescoring.AtomSignatureActivity;
import org.openscience.cdk.applications.taverna.ui.EmptyConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.art2a.ART2aClassificationConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.curation.AtomSignatureConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.curation.CuratorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.IterativeFileWriterConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.jchempaint.JChemPaintConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.miscellaneous.PortNumberConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.qsar.CurateQSARVectorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.qsar.QSARDescriptorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.reactionenumerator.ReactionEnumeratorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.AttributeEvaluationConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.GAAttributeEvaluationConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.ScatternPlotConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.SplitDatasetIntoTrainTestsetConfigurationPanelController;
import org.openscience.cdk.applications.taverna.ui.weka.WekaClusteringConfigurationPanelController;
import org.openscience.cdk.applications.taverna.ui.weka.WekaLearningConfigurationPanelController;
import org.openscience.cdk.applications.taverna.weka.clustering.WekaClusteringActivity;
import org.openscience.cdk.applications.taverna.weka.learning.AttributeEvaluationActivity;
import org.openscience.cdk.applications.taverna.weka.learning.GAAttributeEvaluationActivity;
import org.openscience.cdk.applications.taverna.weka.learning.ScatterPlotFromLearningResultAsPDFActivity;
import org.openscience.cdk.applications.taverna.weka.learning.SplitDatasetIntoTrainTestsetActivity;
import org.openscience.cdk.applications.taverna.weka.learning.WekaLearningActivity;

public class CDKConfigurationPanelFactory {

	public static ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> getConfigurationPanel(
			AbstractCDKActivity activity) {
		if (activity instanceof IIterativeFileWriter) {
			return new IterativeFileWriterConfigurationPanel(activity);
		} else if (activity instanceof IPortNumber) {
			return new PortNumberConfigurationPanel(activity);
		} else if (activity instanceof JChemPaintActivity) {
			return new JChemPaintConfigurationPanel(activity);
		} else if (activity instanceof AtomSignatureActivity) {
			return new AtomSignatureConfigurationPanel(activity);
		} else if (activity instanceof QSARDescriptorActivity || activity instanceof QSARDescriptorThreadedActivity) {
			return new QSARDescriptorConfigurationPanel(activity);
		} else if (activity instanceof MoleculeConnectivityCheckerActivity) {
			return new CuratorConfigurationPanel(activity);
		} else if (activity instanceof ReactionEnumeratorActivity) {
			return new ReactionEnumeratorConfigurationPanel(activity);
		} else if (activity instanceof ART2aClassificationActivity) {
			return new ART2aClassificationConfigurationPanel(activity);
		} else if (activity instanceof CurateQSARVectorActivity) {
			return new CurateQSARVectorConfigurationPanel(activity);
		} else if (activity instanceof WekaClusteringActivity) {
			return new WekaClusteringConfigurationPanelController(activity);
		} else if (activity instanceof WekaLearningActivity) {
			return new WekaLearningConfigurationPanelController(activity);
		} else if (activity instanceof SplitDatasetIntoTrainTestsetActivity) {
			return new SplitDatasetIntoTrainTestsetConfigurationPanelController(activity);
		} else if (activity instanceof ScatterPlotFromLearningResultAsPDFActivity) {
			return new ScatternPlotConfigurationPanel(activity);
		} else if (activity instanceof GAAttributeEvaluationActivity) {
			return new GAAttributeEvaluationConfigurationPanel(activity);
		} else if (activity instanceof AttributeEvaluationActivity) {
			return new AttributeEvaluationConfigurationPanel(activity);
		}
		return new EmptyConfigurationPanel();
	}

}
