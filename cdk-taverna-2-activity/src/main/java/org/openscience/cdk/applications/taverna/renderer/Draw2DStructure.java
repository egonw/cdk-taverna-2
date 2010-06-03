package org.openscience.cdk.applications.taverna.renderer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.jchempaint.renderer.Renderer;
import org.openscience.jchempaint.renderer.visitor.AWTDrawVisitor;

public class Draw2DStructure {
	/**
	 * Transforms the molecule geometry to fit target margins.
	 * 
	 * @param aMolecule
	 *            Molecule to transform.
	 * @param aPrefferedSize
	 *            Size to fit.
	 * @return Fitted molecule. 
	 */
	public static IAtomContainer fitMoleculeGeometry(IAtomContainer aMolecule, Dimension aPrefferedSize) {
		if (aMolecule == null) {
			return null;
		}
		if (!ConnectivityChecker.isConnected(aMolecule)) {
			IMoleculeSet fragments = ConnectivityChecker.partitionIntoMolecules(aMolecule);
			int biggest = 0;
			for (int i = 1; i < fragments.getAtomContainerCount(); i++) {
				if (fragments.getAtomContainer(i).getAtomCount() > fragments.getAtomContainer(biggest).getAtomCount()) {
					biggest = i;
				}
			}
			aMolecule = fragments.getAtomContainer(biggest);
			// TODO render rest too
		}
		GeometryTools.translateAllPositive(aMolecule);
		GeometryTools.scaleMolecule(aMolecule, aPrefferedSize, 0.8f);
		GeometryTools.center(aMolecule, aPrefferedSize);
		return aMolecule;
	}

	public static BufferedImage drawMolecule(IAtomContainer molecule, int width, int height) {
		// Init image
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(width, height);
		// Init renderer
		Renderer renderer = RendererFactory.getRendererInstance();
		molecule = Draw2DStructure.fitMoleculeGeometry(molecule, new Dimension(width, height));
		Graphics2D g2 = (Graphics2D) image.getGraphics().create();
		g2.setColor(renderer.getRenderer2DModel().getBackColor());
		g2.fillRect(0, 0, width, height);
		renderer.paintMolecule(molecule, new AWTDrawVisitor(g2), new Rectangle(0, 0, width, height), true);
		g2.dispose();
		return image;
	}

}
