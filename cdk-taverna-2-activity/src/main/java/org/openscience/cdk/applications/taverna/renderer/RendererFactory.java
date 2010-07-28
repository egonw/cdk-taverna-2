package org.openscience.cdk.applications.taverna.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.jchempaint.renderer.Renderer;
import org.openscience.jchempaint.renderer.font.AWTFontManager;
import org.openscience.jchempaint.renderer.generators.BasicAtomGenerator;
import org.openscience.jchempaint.renderer.generators.IGenerator;
import org.openscience.jchempaint.renderer.generators.IReactionGenerator;
import org.openscience.jchempaint.renderer.generators.ReactionArrowGenerator;
import org.openscience.jchempaint.renderer.generators.ReactionPlusGenerator;
import org.openscience.jchempaint.renderer.generators.RingGenerator;

import com.itextpdf.text.Phrase;

public class RendererFactory {

	public static Renderer getRendererInstance() {
		Renderer renderer = null;
		try {
			renderer = new Renderer(makeGenerators(), makeReactionGenerators(), new AWTFontManager(), null, true);
			// any specific rendering settings defaults should go here
			renderer.getRenderer2DModel().setShowEndCarbons(false);
			renderer.getRenderer2DModel().setShowAromaticity(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return renderer;

	}

	private static List<IReactionGenerator> makeReactionGenerators() {
		List<IReactionGenerator> generators = new ArrayList<IReactionGenerator>();
		generators.add(new ReactionArrowGenerator());
		generators.add(new ReactionPlusGenerator());
		return generators;
	}

	private static List<IGenerator> makeGenerators() throws IOException {
		List<IGenerator> generators = new ArrayList<IGenerator>();
		generators.add(new RingGenerator());
		generators.add(new BasicAtomGenerator());
		return generators;
	}

}
