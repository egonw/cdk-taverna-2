/* $RCSfile$
 * $Author:  $
 * $Date: $
 * $Revision:  $
 * 
 * Copyright (C) 2008 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Class which creates charts using the open source library jfreechart
 * 
 * @author Thomas Kuhn
 */
public class ChartTool {
	// region Variables
	/**
	 * Width of the bar chart
	 */
	private int barChartWidth = 0;
	/**
	 * Height of the bar chart
	 */
	private int barChartHeight = 0;
	/**
	 * Set the plotOrientation for the chart
	 */
	private PlotOrientation plotOrientation = PlotOrientation.HORIZONTAL;
	/**
	 * The description of the xAxis
	 */
	private String descriptionXAxis = "";
	/**
	 * The description of the YAxix
	 */
	private String descriptionYAxis = "";
	/**
	 * Switch between portrait and landscape for the pdf document
	 */
	private boolean pdfPageInPortrait = true;
	/**
	 * Render the description of the x-axis diagonal
	 */
	private boolean renderXAxisDescriptionDiagonal = false;
	/**
	 * Render the legend of the chart.
	 */
	private boolean renderLegend = true;
	/**
	 * Map which stores for each file (containing the diagram) a table with some information about the diagram
	 */
	private Map<File, PdfPTable> tableForFileMap = new HashMap<File, PdfPTable>();

	// end

	// region Properties

	/**
	 * @return the plotOrientation
	 */
	public PlotOrientation getPlotOrientation() {
		return plotOrientation;
	}

	/**
	 * @param plotOrientation
	 *            the plotOrientation to set
	 */
	public void setPlotOrientation(PlotOrientation plotOrientation) {
		this.plotOrientation = plotOrientation;
	}

	/**
	 * @return the descriptionXAxis
	 */
	public String getDescriptionXAxis() {
		return descriptionXAxis;
	}

	/**
	 * @param descriptionXAxis
	 *            the descriptionXAxis to set
	 */
	public void setDescriptionXAxis(String descriptionXAxis) {
		this.descriptionXAxis = descriptionXAxis;
	}

	/**
	 * @return the descriptionYAxis
	 */
	public String getDescriptionYAxis() {
		return descriptionYAxis;
	}

	/**
	 * @param descriptionYAxis
	 *            the descriptionYAxis to set
	 */
	public void setDescriptionYAxis(String descriptionYAxis) {
		this.descriptionYAxis = descriptionYAxis;
	}

	/**
	 * @return the barChartWidth
	 */
	public int getBarChartWidth() {
		return barChartWidth;
	}

	/**
	 * @param barChartWidth
	 *            the barChartWidth to set
	 */
	public void setBarChartWidth(int barChartWidth) {
		this.barChartWidth = barChartWidth;
	}

	/**
	 * @return the barChartHeight
	 */
	public int getBarChartHeight() {
		return barChartHeight;
	}

	/**
	 * @param barChartHeight
	 *            the barChartHeight to set
	 */
	public void setBarChartHeight(int barChartHeight) {
		this.barChartHeight = barChartHeight;
	}

	/**
	 * @return the pdfPageInPortrait
	 */
	public boolean isPdfPageInPortrait() {
		return pdfPageInPortrait;
	}

	/**
	 * @param pdfPageInPortrait
	 *            the pdfPageInPortrait to set
	 */
	public void setPdfPageInPortrait(boolean pdfPageInPortrait) {
		this.pdfPageInPortrait = pdfPageInPortrait;
	}

	/**
	 * @return the isRenderLegend
	 */
	public boolean isRenderLegend() {
		return renderLegend;
	}

	/**
	 * @param renderLegend
	 *            true if the legend should be drawn.
	 */
	public void setRenderLegend(boolean renderLegend) {
		this.renderLegend = renderLegend;
	}

	/**
	 * @return the renderXAxisDescriptionDiagonal
	 */
	public boolean isRenderXAxisDescriptionDiagonal() {
		return renderXAxisDescriptionDiagonal;
	}

	/**
	 * @param renderXAxisDescriptionDiagonal
	 *            the renderXAxisDescriptionDiagonal to set
	 */
	public void setRenderXAxisDescriptionDiagonal(boolean renderXAxisDescriptionDiagonal) {
		this.renderXAxisDescriptionDiagonal = renderXAxisDescriptionDiagonal;
	}

	/**
	 * @return the tableForFileMap
	 */
	public Map<File, PdfPTable> getTableForFileMap() {
		return tableForFileMap;
	}

	public void addTableForFileMap(File file, PdfPTable table) {
		this.tableForFileMap.put(file, table);
	}

	// end

	// region Public Methods

	/**
	 * Method which exports a dataset to a chart. This chart gets stored temporarily as jpg.
	 * 
	 * @param dataSet
	 *            Dataset from which a chart will be generated
	 * @param title
	 *            Title of the chart
	 * @return File which contains the jpg
	 * @throws IOException
	 * @throws DocumentException
	 */
	public File exportToAreaChart(DefaultCategoryDataset dataSet, String title) throws IOException, DocumentException {
		File file = File.createTempFile("ClusterChart", ".jpg");
		JFreeChart chart = ChartFactory.createAreaChart(title, this.descriptionXAxis, this.descriptionYAxis, dataSet,
				this.plotOrientation, this.renderLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);
		// get a reference to the plot for further customization...
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		AreaRenderer renderer = (AreaRenderer) plot.getRenderer();

		// set up gradient paints for series...
		// GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.red,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.yellow,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// GradientPaint gp3 = new GradientPaint(0.0f, 0.0f, Color.green,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// GradientPaint gp4 = new GradientPaint(0.0f, 0.0f, Color.pink,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.yellow);
		renderer.setSeriesPaint(2, Color.red);
		renderer.setSeriesPaint(3, Color.darkGray);
		renderer.setSeriesPaint(4, Color.green);

		if (renderXAxisDescriptionDiagonal) {
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
		}
		ChartUtilities.saveChartAsJPEG(file, chart, this.barChartWidth, this.barChartHeight);

		// DOMImplementation domImpl
		// = GenericDOMImplementation.getDOMImplementation();
		// // Create an instance of org.w3c.dom.Document
		// org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);
		// // Create an instance of the SVG Generator
		// SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		// // set the precision to avoid a null pointer exception in Batik 1.5
		// svgGenerator.getGeneratorContext().setPrecision(6);
		// // Ask the chart to render into the SVG Graphics2D implementation
		// chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, this.barChartWidth, this.barChartHeight), null);
		// // Finally, stream out SVG to a file using UTF-8 character to
		// // byte encoding
		// boolean useCSS = true;
		// Writer out = new OutputStreamWriter(new FileOutputStream(svgfile), "UTF-8");
		// svgGenerator.stream(out, useCSS);

		return file;
	}

	/**
	 * Method which exports a dataset to a chart. This chart gets stored temporarily as jpg.
	 * 
	 * @param dataSet
	 *            Dataset from which a chart will be generated
	 * @param title
	 *            Title of the chart
	 * @return File which contains the jpg
	 * @throws IOException
	 * @throws DocumentException
	 */
	public File exportToBarChart(DefaultCategoryDataset dataSet, String title) throws IOException, DocumentException {
		File file = File.createTempFile("ClusterChart", ".jpg");
		JFreeChart chart = ChartFactory.createBarChart(title, this.descriptionXAxis, this.descriptionYAxis, dataSet,
				this.plotOrientation, this.renderLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customization...
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		// set up gradient paints for series...
		// GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.red,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.yellow,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// GradientPaint gp3 = new GradientPaint(0.0f, 0.0f, Color.green,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// GradientPaint gp4 = new GradientPaint(0.0f, 0.0f, Color.pink,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.yellow);
		renderer.setSeriesPaint(2, Color.red);
		renderer.setSeriesPaint(3, Color.darkGray);
		renderer.setSeriesPaint(4, Color.green);
		renderer.setItemMargin(0.01);
		if (renderXAxisDescriptionDiagonal) {
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
		}
		ChartUtilities.saveChartAsJPEG(file, chart, this.barChartWidth, this.barChartHeight);

		// DOMImplementation domImpl
		// = GenericDOMImplementation.getDOMImplementation();
		// // Create an instance of org.w3c.dom.Document
		// org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);
		// // Create an instance of the SVG Generator
		// SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		// // set the precision to avoid a null pointer exception in Batik 1.5
		// svgGenerator.getGeneratorContext().setPrecision(6);
		// // Ask the chart to render into the SVG Graphics2D implementation
		// chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, this.barChartWidth, this.barChartHeight), null);
		// // Finally, stream out SVG to a file using UTF-8 character to
		// // byte encoding
		// boolean useCSS = true;
		// Writer out = new OutputStreamWriter(new FileOutputStream(svgfile), "UTF-8");
		// svgGenerator.stream(out, useCSS);

		return file;
	}

	/**
	 * Method which exports a dataset to a chart. This chart gets stored temporarily as jpg.
	 * 
	 * @param dataSet
	 *            Dataset from which a chart will be generated
	 * @param title
	 *            Title of the chart
	 * @return File which contains the jpg
	 * @throws IOException
	 * @throws DocumentException
	 */
	public File exportToHistogrammChart(HistogramDataset dataSet, String title) throws IOException, DocumentException {
		File file = File.createTempFile("HistogrammChart", ".jpg");
		JFreeChart chart = ChartFactory.createHistogram(title, this.descriptionXAxis, this.descriptionYAxis, dataSet,
				this.plotOrientation, this.renderLegend, false, false);
		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customization...
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);
		// renderer.setDrawBarOutline(false);

		// set up gradient paints for series...
		// GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue,
		// 0.0f, 0.0f, new Color(0, 0, 64));
		// renderer.setSeriesPaint(0, gp0);
		// if (renderXAxisDescriptionDiagonal) {
		// CategoryAxis domainAxis = plot.getDomainAxis();
		// domainAxis.setCategoryLabelPositions(
		// CategoryLabelPositions.createUpRotationLabelPositions(
		// Math.PI / 6.0));
		// }
		ChartUtilities.saveChartAsJPEG(file, chart, this.barChartWidth, this.barChartHeight);

		return file;
	}

	/**
	 * Method which exports a list of jpg files to a pdf.
	 * 
	 * @param chartFileList
	 *            List of jpg files to export to the new PDF file
	 * @param pdfFile
	 *            The PDF file to create
	 * @param csv
	 *            List of strings which will be printed as header information within the PDF
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void exportToChartsToPDF(List<File> chartFileList, File pdfFile, List<String> header) throws IOException,
			DocumentException {
		Document document;
		float width;
		float height;
		if (pdfPageInPortrait) {
			document = new Document(PageSize.A4, 50, 50, 50, 50);
			width = PageSize.A4.getWidth() - 110;
			height = PageSize.A4.getHeight() - 110;
		} else {
			document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
			width = PageSize.A4.rotate().getWidth() - 110;
			height = PageSize.A4.rotate().getHeight() - 110;
		}
		PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
		document.open();
		// Add the header
		for (String line : header) {
			document.add(new Paragraph(new Chunk(line)));
		}
		document.add(new Paragraph());
		int size;
		byte[] imext;
		RandomAccessFile rf;
		Image image;

		for (File file : chartFileList) {
			rf = new RandomAccessFile(file, "r");
			size = (int) rf.length();
			imext = new byte[size];
			rf.readFully(imext);
			rf.close();
			image = Image.getInstance(imext);
			image.scaleToFit(width, height);
			document.add(image);
			document.add(new Phrase());
			file.delete();
		}
		document.close();
	}

	/**
	 * Method which exports a list of jpg files and the corresponding tables to a pdf.
	 * 
	 * @param chartFileList
	 *            List of jpg files to export to the new PDF file
	 * @param pdfFile
	 *            The PDF file to create
	 * @param csv
	 *            List of strings which will be printed as header information within the PDF
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void exportToChartsWithTablesToPDF(List<File> chartFileList, File pdfFile, List<String> header) throws IOException,
			DocumentException {
		Document document;
		float width;
		float height;
		if (pdfPageInPortrait) {
			document = new Document(PageSize.A4, 50, 50, 50, 50);
			width = PageSize.A4.getWidth() - 110;
			height = PageSize.A4.getHeight() - 110;
		} else {
			document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
			width = PageSize.A4.rotate().getWidth() - 110;
			height = PageSize.A4.rotate().getHeight() - 110;
		}
		PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
		document.open();
		// Add the header
		for (String line : header) {
			document.add(new Paragraph(new Chunk(line)));
		}
		document.add(new Paragraph());
		int size;
		byte[] imext;
		RandomAccessFile rf;
		Image image;
		for (File file : chartFileList) {
			rf = new RandomAccessFile(file, "r");
			size = (int) rf.length();
			imext = new byte[size];
			rf.readFully(imext);
			rf.close();
			image = Image.getInstance(imext);
			image.scaleToFit(width, height);
			document.add(image);
			document.add(new Phrase());
			document.newPage();
			if (tableForFileMap.containsKey(file)) {
				document.add(tableForFileMap.get(file));
				document.add(new Phrase());
			}
			document.newPage();
			file.delete();
		}
		document.close();
	}

	// end

}
