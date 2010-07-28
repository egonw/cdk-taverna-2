package org.openscience.cdk.applications.taverna.renderer;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class DrawPDF {
	 private static int width = 300;
	 private static int height = 300;
	 private static double scale = 0.9;
	 private static int ncol = 2;
	 private static int nrow = 3;

	 
	public static void drawPDF(List<IAtomContainer> molecules, String filename) throws Exception {
		Document pdf = new Document();
		PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(filename));
		pdf.open();
		PdfPTable table = new PdfPTable(ncol);
		for(IAtomContainer molecule : molecules) {
			PdfPCell cell = new PdfPCell();
			BufferedImage image = Draw2DStructure.drawMolecule(molecule, width, height);
			java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(image.getSource());
			cell.addElement(Image.getInstance(awtImage, null));
			table.addCell(cell);
		}
		int cells = molecules.size();
		while(cells % ncol != 0) {
			PdfPCell cell = new PdfPCell();
			table.addCell(cell);
			cells++;
		}
		pdf.add(table);
		pdf.close();
	}
}
