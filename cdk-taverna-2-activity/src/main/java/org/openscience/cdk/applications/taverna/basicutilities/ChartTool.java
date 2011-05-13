/*
 * Copyright (C) 2010-2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.basicutilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.openscience.cdk.applications.taverna.CDKTavernaException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Helper class for creating different types of charts.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ChartTool {

	private boolean drawLegend = true;
	private PlotOrientation orientation = PlotOrientation.VERTICAL;
	private int width = 1024;
	private int height = 724;

	/**
	 * Creates an area chart.
	 * 
	 * @param title
	 * @param categoryAxisLabel
	 *            (X-Axis label)
	 * @param valueAxisLabel
	 *            (Y-Axis label)
	 * @param dataset
	 * @return JfreeChart instance.
	 */
	public JFreeChart createAreaChart(String title, String categoryAxisLabel, String valueAxisLabel,
			CategoryDataset dataset) {
		JFreeChart chart = ChartFactory.createAreaChart(title, categoryAxisLabel, valueAxisLabel, dataset,
				this.orientation, this.drawLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);
		CategoryPlot plot = chart.getCategoryPlot();
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0);
		domainAxis.setUpperMargin(0);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		AreaRenderer renderer = (AreaRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.red);
		renderer.setSeriesPaint(2, Color.green);
		renderer.setSeriesPaint(3, Color.darkGray);
		renderer.setSeriesPaint(4, Color.yellow);
		return chart;
	}

	/**
	 * Creates a scatter plot.
	 * 
	 * @param dataset
	 * @param header
	 * @param xAxis
	 * @param yAxis
	 * @return
	 */
	public JFreeChart createScatterPlot(XYDataset dataset, String header, String xAxis, String yAxis) {
		JFreeChart chart = ChartFactory.createScatterPlot(header, xAxis, yAxis, dataset, PlotOrientation.VERTICAL,
				true, false, false);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("NO DATA");
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);
		XYLineAnnotation annotation = new XYLineAnnotation(-1000000, -1000000, 1000000, 1000000);
		plot.addAnnotation(annotation);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesOutlinePaint(0, Color.black);
		renderer.setUseOutlinePaint(true);
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
		domainAxis.setTickMarkInsideLength(2.0f);
		domainAxis.setTickMarkOutsideLength(0.0f);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setTickMarkInsideLength(2.0f);
		rangeAxis.setTickMarkOutsideLength(0.0f);

		return chart;
	}

	/**
	 * Creates a residue plot.
	 * 
	 * @param yValues
	 * @param header
	 * @param xAxis
	 * @param yAxis
	 * @param seriesNames
	 * @return
	 */
	public JFreeChart createResiduePlot(List<Double[]> yValues, String header, String xAxis, String yAxis,
			List<String> seriesNames) {
		LinkedList<XYLineAnnotation> lines = new LinkedList<XYLineAnnotation>();
		DefaultXYDataset xyDataSet = new DefaultXYDataset();
		for (int j = 0; j < yValues.size(); j++) {
			XYSeries series = new XYSeries(seriesNames.get(j));
			for (int i = 0; i < yValues.get(j).length; i++) {
				series.add(i + 1, yValues.get(j)[i]);
				float dash[] = { 10.0f };
				BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash,
						0.0f);
				XYLineAnnotation annotation = new XYLineAnnotation(i + 1, 0, i + 1, yValues.get(j)[i], stroke,
						Color.BLUE);
				lines.add(annotation);
			}
			xyDataSet.addSeries(seriesNames.get(j), series.toArray());
		}
		JFreeChart chart = ChartFactory.createScatterPlot(header, xAxis, yAxis, xyDataSet, PlotOrientation.VERTICAL,
				true, false, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("NO DATA");
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);
		for (int i = 0; i < lines.size(); i++) {
			plot.addAnnotation(lines.get(i));
		}
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesOutlinePaint(0, Color.black);
		renderer.setUseOutlinePaint(true);
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
		domainAxis.setTickMarkInsideLength(2.0f);
		domainAxis.setTickMarkOutsideLength(0.0f);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setTickMarkInsideLength(2.0f);
		rangeAxis.setTickMarkOutsideLength(0.0f);

		return chart;
	}

	/**
	 * Creates a bar chart.
	 * 
	 * @param title
	 * @param categoryAxisLabel
	 *            (X-Axis label)
	 * @param valueAxisLabel
	 *            (Y-Axis label)
	 * @param dataset
	 * @return JfreeChart instance.
	 */
	public JFreeChart createBarChart(String title, String categoryAxisLabel, String valueAxisLabel,
			CategoryDataset dataset) {
		JFreeChart chart = ChartFactory.createBarChart(title, categoryAxisLabel, valueAxisLabel, dataset,
				this.orientation, this.drawLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);
		CategoryPlot plot = chart.getCategoryPlot();
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.01);
		domainAxis.setUpperMargin(0.01);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.red);
		renderer.setSeriesPaint(2, Color.green);
		renderer.setSeriesPaint(3, Color.darkGray);
		renderer.setSeriesPaint(4, Color.yellow);
		return chart;
	}

	/**
	 * Creates a line chart.
	 * 
	 * @param title
	 * @param categoryAxisLabel
	 *            (X-Axis label)
	 * @param valueAxisLabel
	 *            (Y-Axis label)
	 * @param dataset
	 * @return JfreeChart instance.
	 */
	public JFreeChart createLineChart(String title, String categoryAxisLabel, String valueAxisLabel,
			CategoryDataset dataset) {
		return this.createLineChart(title, categoryAxisLabel, valueAxisLabel, dataset, true, true);
	}

	/**
	 * Creates a line chart.
	 * 
	 * @param title
	 * @param categoryAxisLabel
	 *            (X-Axis label)
	 * @param valueAxisLabel
	 *            (Y-Axis label)
	 * @param dataset
	 * @param includeZero
	 *            True when zero shall be included to the axis range.
	 * @return JfreeChart instance.
	 */
	public JFreeChart createLineChart(String title, String categoryAxisLabel, String valueAxisLabel,
			CategoryDataset dataset, boolean includeZero, boolean drawShapes) {
		JFreeChart chart = ChartFactory.createLineChart(title, categoryAxisLabel, valueAxisLabel, dataset,
				this.orientation, this.drawLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);
		CategoryPlot plot = chart.getCategoryPlot();
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.025);
		domainAxis.setUpperMargin(0.025);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(includeZero);
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.red);
		renderer.setSeriesPaint(2, Color.green);
		renderer.setSeriesPaint(3, Color.darkGray);
		renderer.setSeriesPaint(4, Color.yellow);
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);
		renderer.setBaseShapesVisible(drawShapes);
		renderer.setBaseShapesFilled(true);
		return chart;
	}

	/**
	 * Creates a line chart.
	 * 
	 * @param title
	 * @param categoryAxisLabel
	 *            (X-Axis label)
	 * @param valueAxisLabel
	 *            (Y-Axis label)
	 * @param dataset
	 * @param includeZero
	 *            True when zero shall be included to the axis range.
	 * @return JfreeChart instance.
	 */
	public JFreeChart createXYLineChart(String title, String categoryAxisLabel, String valueAxisLabel,
			XYDataset dataset, boolean includeZero, boolean drawShapes) {
		JFreeChart chart = ChartFactory.createXYLineChart(title, categoryAxisLabel, valueAxisLabel, dataset,
				this.orientation, this.drawLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);
		XYPlot plot = chart.getXYPlot();
		ValueAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.025);
		domainAxis.setUpperMargin(0.025);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(includeZero);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.red);
		renderer.setSeriesPaint(2, Color.green);
		renderer.setSeriesPaint(3, Color.darkGray);
		renderer.setSeriesPaint(4, Color.yellow);
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);
		renderer.setBaseShapesVisible(drawShapes);
		renderer.setBaseShapesFilled(true);
		return chart;
	}
	
	/**
	 * Creates a line chart.
	 * 
	 * @param title
	 * @param categoryAxisLabel
	 *            (X-Axis label)
	 * @param valueAxisLabel
	 *            (Y-Axis label)
	 * @param dataset
	 * @param includeZero
	 *            True when zero shall be included to the axis range.
	 * @return JfreeChart instance.
	 */
	public JFreeChart createXYLineSplineChart(String title, String categoryAxisLabel, String valueAxisLabel,
			XYDataset dataset, boolean includeZero, boolean drawShapes) {
		JFreeChart chart = ChartFactory.createXYLineChart(title, categoryAxisLabel, valueAxisLabel, dataset,
				this.orientation, this.drawLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);
		XYPlot plot = chart.getXYPlot();
		plot.ssetBackcolor(Color.WHITE); 
		ValueAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.025);
		domainAxis.setUpperMargin(0.025);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(includeZero);
		plot.setRenderer(new XYSplineRenderer(100));
		XYSplineRenderer renderer = (XYSplineRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.red);
		renderer.setSeriesPaint(2, Color.green);
		renderer.setSeriesPaint(3, Color.darkGray);
		renderer.setSeriesPaint(4, Color.yellow);
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);
		renderer.setBaseShapesVisible(drawShapes);
		renderer.setBaseShapesFilled(true);
		return chart;
	}

	/**
	 * Creates a XY bar chart.
	 * 
	 * @param title
	 * @param categoryAxisLabel
	 * @param valueAxisLabel
	 * @param dataset
	 * @param includeZero
	 * @param shadow
	 * @return
	 */
	public JFreeChart createXYBarChart(String title, String categoryAxisLabel, String valueAxisLabel,
			IntervalXYDataset dataset, boolean includeZero, boolean shadow) {
		JFreeChart chart = ChartFactory.createXYBarChart(title, categoryAxisLabel, false, valueAxisLabel, dataset,
				this.orientation, this.drawLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);
		XYPlot plot = chart.getXYPlot();
		ValueAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.025);
		domainAxis.setUpperMargin(0.025);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(includeZero);
		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(shadow);
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.red);
		renderer.setSeriesPaint(2, Color.green);
		renderer.setSeriesPaint(3, Color.darkGray);
		renderer.setSeriesPaint(4, Color.yellow);
		return chart;
	}

	/**
	 * Writes given charts into target PDF file.
	 * 
	 * @param file
	 * @param charts
	 * @param annotations
	 * @throws IOException
	 */
	public synchronized void writeChartAsPDF(File file, List<Object> chartObjects) throws IOException {
		Rectangle pagesize = new Rectangle(this.width, this.height);
		Document document = new Document(pagesize, 50, 50, 50, 50);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
			document.addAuthor("CDK-Taverna 2.0");
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			for (int i = 0; i < chartObjects.size(); i++) {
				Object obj = chartObjects.get(i);
				if (obj instanceof JFreeChart) {
					JFreeChart chart = (JFreeChart) obj;
					this.addChartPageToPDF(chart, cb);
				} else if (obj instanceof String) {
					String annotation = (String) obj;
					this.addAnnotationToPDF(annotation, document);
				}
				document.newPage();
			}
		} catch (DocumentException e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.CANT_CREATE_PDF_FILE + file.getPath(),
					this.getClass().getSimpleName(), e);
		}
		document.close();
	}

	/**
	 * Adds a chart to the pdf file.
	 * 
	 * @param chart
	 * @param cb
	 */
	private void addChartPageToPDF(JFreeChart chart, PdfContentByte cb) {
		PdfTemplate tp = cb.createTemplate(this.width, this.height);
		Graphics2D g2 = tp.createGraphics(this.width, this.height, new DefaultFontMapper());
		Rectangle2D r2D = new Rectangle2D.Double(0, 0, this.width, this.height);
		chart.draw(g2, r2D);
		g2.dispose();
		cb.addTemplate(tp, 0, 0);
	}

	/**
	 * Writes an annotation into the pdf file.
	 * 
	 * @param annotation
	 * @param document
	 * @throws DocumentException
	 */
	private void addAnnotationToPDF(String annotation, Document document) throws DocumentException {
		Paragraph para = new Paragraph();
		Font font = FontFactory.getFont(FontFactory.HELVETICA, 12);
		para.setFont(font);
		para.setLeading(14F);
		para.add(annotation);
		document.add(para);
	}

	/**
	 * @return The page size in pixel.
	 */
	public Dimension getPageSize() {
		return new Dimension(this.width, this.height);
	}

	/**
	 * @param size
	 *            The page size in pixel.
	 */
	public void setPageSize(Dimension size) {
		this.width = size.width;
		this.height = size.height;
	}

}
