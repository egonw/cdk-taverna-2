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
package org.openscience.cdk.applications.taverna.renderer;

import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.tools.manipulator.ReactionManipulator;
import org.openscience.jchempaint.renderer.BoundsCalculator;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Class for creating PDFs containing images of reactions or molecules.
 * 
 * @author Andreas Truszkowski
 * 
 */
@SuppressWarnings("unused")
// TODO
public class DrawPDF {
	private static int width = 300;
	private static int height = 300;
	private static double scale = 0.9;
	private static int ncol = 2;
	private static int nrow = 3;

	public static void drawMoleculesAsPDF(List<IAtomContainer> molecules, File file) throws Exception {
		Document pdf = new Document();
		PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(file));
		pdf.open();
		PdfPTable table = new PdfPTable(ncol);
		for (IAtomContainer molecule : molecules) {
			PdfPCell cell = new PdfPCell();
			BufferedImage image = Draw2DStructure.drawMolecule(molecule, width, height, 0.95);
			java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(image.getSource());
			cell.addElement(Image.getInstance(awtImage, null));
			table.addCell(cell);
		}
		int cells = molecules.size();
		while (cells % ncol != 0) {
			PdfPCell cell = new PdfPCell();
			cell.disableBorderSide(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP);
			table.addCell(cell);
			cells++;
		}
		pdf.add(table);
		pdf.close();
	}

	public static void drawReactionAsPDF(List<IReaction> reactions, File file) throws Exception {
		Document pdf = new Document();
		PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(file));
		pdf.open();
		PdfPTable table = new PdfPTable(1);
		int reactionWitdh = width * (reactions.get(0).getReactantCount() + reactions.get(0).getProductCount() + 1);
		if (reactionWitdh > width * 5) {
			reactionWitdh = width * 5;
		}
		for (IReaction reaction : reactions) {
			// shift the molecules not to overlap
			// TODO remove here
			Rectangle2D usedBounds = null;
			usedBounds = null;
			for (IAtomContainer container : ReactionManipulator.getAllAtomContainers(reaction)) {
				// now move it so that they don't overlap
				Rectangle2D bounds = BoundsCalculator.calculateBounds(container);
				if (usedBounds != null) {
					double bondLength = GeometryTools.getBondLengthAverage(container);
					Rectangle2D shiftedBounds = GeometryTools.shiftContainer(container, bounds, usedBounds, bondLength);
					usedBounds = usedBounds.createUnion(shiftedBounds);
				} else {
					usedBounds = bounds;
				}
			}
			PdfPCell cell = new PdfPCell();
			BufferedImage image = Draw2DStructure.drawReaction(reaction, reactionWitdh, height);
			java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(image.getSource());
			cell.addElement(Image.getInstance(awtImage, null));
			table.addCell(cell);
		}
		pdf.add(table);
		pdf.close();
	}
}
