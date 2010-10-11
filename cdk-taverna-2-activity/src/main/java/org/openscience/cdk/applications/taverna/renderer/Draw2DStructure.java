package org.openscience.cdk.applications.taverna.renderer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReaction;
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

	public static BufferedImage drawMolecule(IAtomContainer molecule, int width, int height) throws Exception {
		return drawMolecule(molecule, width, height, 1.0);
	}

	public static BufferedImage drawMolecule(IAtomContainer molecule, int width, int height, double scale) throws Exception {
		// Init image
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(width, height);
		// Init renderer
		Renderer renderer = RendererFactory.getRendererInstance();

		Graphics2D g2 = (Graphics2D) image.getGraphics().create();
		g2.setColor(renderer.getRenderer2DModel().getBackColor());
		g2.fillRect(0, 0, width, height);
		if (scale >= 1.0) {
			molecule = Draw2DStructure.fitMoleculeGeometry(molecule, new Dimension(width, height));
			renderer.paintMolecule(molecule, new AWTDrawVisitor(g2), new Rectangle(0, 0, width, height), true);
		} else {
			int x = (int) ((width * (1.0 - scale)) / 2);
			int y = (int) ((height * (1.0 - scale)) / 2);
			int scaledWith = (int) (width * scale);
			int scaledHeight = (int) (height * scale);
			molecule = Draw2DStructure.fitMoleculeGeometry(molecule, new Dimension(scaledWith, scaledHeight));
			try {
				renderer.paintMolecule(molecule, new AWTDrawVisitor(g2), new Rectangle(x, y, scaledWith, scaledHeight), true);
			} catch (Exception e) {
				// TODO
				//e.printStackTrace();
			}
		}
		g2.dispose();
		return image;
	}

	public static BufferedImage drawReaction(IReaction reaction, int width, int height) throws Exception {
		return drawReaction(reaction, width, height, 1.0);
	}

	public static BufferedImage drawReaction(IReaction reaction, int width, int height, double scale) throws Exception {
		// Init image
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(width, height);
		// Init renderer
		Renderer renderer = RendererFactory.getRendererInstance();

		Graphics2D g2 = (Graphics2D) image.getGraphics().create();
		g2.setColor(renderer.getRenderer2DModel().getBackColor());
		g2.fillRect(0, 0, width, height);
		Rectangle drawArea;
		if (scale >= 1.0) {
			drawArea = new Rectangle(0, 0, width, height);
			renderer.setup(reaction, drawArea);
			renderer.paintReaction(reaction, new AWTDrawVisitor(g2), drawArea, true);
		} else {
			int x = (int) ((width * (1.0 - scale)) / 2);
			int y = (int) ((height * (1.0 - scale)) / 2);
			int scaledWith = (int) (width * scale);
			int scaledHeight = (int) (height * scale);
			drawArea = new Rectangle(x, y, scaledWith, scaledHeight);
			renderer.setup(reaction, drawArea);
			renderer.paintReaction(reaction, new AWTDrawVisitor(g2), drawArea, false);
		}
		g2.dispose();
		return image;
	}

}
