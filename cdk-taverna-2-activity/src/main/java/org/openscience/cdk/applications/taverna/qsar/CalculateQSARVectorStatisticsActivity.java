/*
 * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.openscience.cdk.applications.taverna.qsar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARVectorUtility;

/**
 * Class which represents the calculate QSAR Vector statistics activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CalculateQSARVectorStatisticsActivity extends AbstractCDKActivity {

	public static final String CALCULATE_QSAR_VECTOR_STATISTICS_ACTIVITY = "Calculate QSAR Vector Statistics";

	/**
	 * Creates a new instance.
	 */
	public CalculateQSARVectorStatisticsActivity() {
		this.INPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names" };
		this.RESULT_PORTS = new String[] { "Statistic Strings" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<UUID, Map<String, Object>> vectorMap;
		byte[] vectorData = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class, context);
		try {
			vectorMap = (Map<UUID, Map<String, Object>>) CDKObjectHandler.getObject(vectorData);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during deserializing object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		ArrayList<String> descriptorNames;
		byte[] nameData = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]), byte[].class, context);
		try {
			descriptorNames = (ArrayList<String>) CDKObjectHandler.getObject(nameData);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during deserializing object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		List<String> statistics = null;
		try {
			QSARVectorUtility vectorUtility = new QSARVectorUtility();
			statistics = vectorUtility.calculateQSARVectorStatistics(vectorMap, descriptorNames);
			// HashMap<String, Integer> calculatedDescriptorDistribution = vectorUtility.getCalculatedDescritorDistribution(
			// vectorMap, descriptorNames);
			// int numberOfMolecules = vectorUtility.getUUIDs(vectorMap).size();
			// int[] distribution = new int[numberOfMolecules + 1];
			// for (String name : descriptorNames) {
			// int value = calculatedDescriptorDistribution.get(name);
			// distribution[value]++;
			// }
			// HashSet<String> tempNameSet = new HashSet<String>();
			// for (String name : descriptorNames) {
			// tempNameSet.add(name);
			// }
			// ArrayList<String> sortedDescriptorNames = new ArrayList<String>();
			// for (int i = numberOfMolecules; i >= 0; i--) {
			// LinkedList<String> tempNames = new LinkedList<String>();
			// for (String name : tempNameSet) {
			// int value = calculatedDescriptorDistribution.get(name);
			// if (value == i) {
			// tempNames.add(name);
			// }
			// }
			// for (String name : tempNames) {
			// tempNameSet.remove(name);
			// sortedDescriptorNames.add(name);
			// }
			// }
			// DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
			//
			// for (int i = 0; i < sortedDescriptorNames.size(); i++) {
			// String name = sortedDescriptorNames.get(i);
			// int value = calculatedDescriptorDistribution.get(name);
			// dataSet.addValue(value, ""+ value, Integer.valueOf(i + 1));
			//
			//
			// }
			// ChartTool chartTool = new ChartTool();
			// chartTool.setBarChartHeight(500);
			// chartTool.setBarChartWidth(840);
			// chartTool.setPlotOrientation(PlotOrientation.VERTICAL);
			// chartTool.setDescriptionYAxis("Ratio in percent");
			// chartTool.setDescriptionXAxis("(Class number/Number of Vectors/Interangle)");
			// chartTool.setRenderXAxisDescriptionDiagonal(true);
			// chartTool.setRenderLegend(false);
			// List<File> chartFiles = new LinkedList<File>();
			// chartFiles.add(chartTool.exportToAreaChart(dataSet, "QSAR Vector Statistics"));
			// chartTool.exportToChartsToPDF(chartFiles, new File("testpdf.pdf"), statistics);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during curating QSAR vector!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		try {
			T2Reference containerRef = referenceService.register(statistics, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during configurating output port!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}
		return outputs;
	}

	@Override
	public String getActivityName() {
		return CalculateQSARVectorStatisticsActivity.CALCULATE_QSAR_VECTOR_STATISTICS_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CalculateQSARVectorStatisticsActivity.CALCULATE_QSAR_VECTOR_STATISTICS_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}

}
