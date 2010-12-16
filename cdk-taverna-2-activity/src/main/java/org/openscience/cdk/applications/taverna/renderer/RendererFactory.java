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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.jchempaint.renderer.Renderer;
import org.openscience.jchempaint.renderer.font.AWTFontManager;
import org.openscience.jchempaint.renderer.generators.BasicAtomGenerator;
import org.openscience.jchempaint.renderer.generators.IGenerator;
import org.openscience.jchempaint.renderer.generators.IReactionGenerator;
import org.openscience.jchempaint.renderer.generators.ReactionArrowGenerator;
import org.openscience.jchempaint.renderer.generators.ReactionPlusGenerator;
import org.openscience.jchempaint.renderer.generators.RingGenerator;

/**
 * This class creates the renderer object.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class RendererFactory {

	public static Renderer getRendererInstance() {
		Renderer renderer = null;
		try {
			renderer = new Renderer(makeGenerators(), makeReactionGenerators(), new AWTFontManager(), null, true);
			// any specific rendering settings defaults should go here
			renderer.getRenderer2DModel().setShowEndCarbons(false);
			renderer.getRenderer2DModel().setShowAromaticity(false);
		} catch (IOException e) {
			ErrorLogger.getInstance().writeError("Error during rendererinitializion!", "Renderer Factory", e);
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
