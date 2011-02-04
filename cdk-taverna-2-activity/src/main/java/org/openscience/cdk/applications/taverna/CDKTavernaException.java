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
package org.openscience.cdk.applications.taverna;

public class CDKTavernaException extends Exception {

	private static final long serialVersionUID = 4460231083206706250L;

	public static final String WRONG_INPUT_PORT_TYPE = "Incorrect input port type!";
	public static final String NOT_CATCHED_EXCEPTION = "Not catched exception!";
	public static final String FILE_CREATION_ERROR = "Error during fille creation!";
	public static final String OBJECT_DESERIALIZATION_ERROR = "Error during object deseralization!";
	public static final String NO_OUTPUT_DIRECTORY_CHOSEN = "Error, no output directory chosen!";
	public static final String NO_FILE_CHOSEN = "Error, no file chosen!";
	public static final String PROCESS_ART2A_RESULT_ERROR = "Could not process ART-2a results!";
	public static final String PROCESS_WEKA_RESULT_ERROR = "Could not process Weka results!";
	public static final String OUTPUT_PORT_CONFIGURATION_ERROR = "Error during output port configuration!";
	public static final String CML_FILE_CONVERSION_ERROR = "Error during CML file conversion!";
	public static final String WRITE_FILE_ERROR = "Error writing file: ";
	public static final String CREATE_DIRECTORY_ERROR = "Could not create the directory: ";
	public static final String READ_FILE_ERROR = "Error reading file: ";
	public static final String GENERATE_2D_COORDINATES_ERROR = "Error during 2D Coordinate generation!";
	public static final String SERIALIZING_OUTPUT_DATA_ERROR = "Error during output data creation/serializion!";
	public static final String WRAPPING_ATOMCONTAINER_IN_CHEMMODEL_ERROR = "Error wrapping IAtomContainer in ChemModel!";
	public static final String STREAM_INITIALIZATION_ERROR = "Error during data stream initializion!";
	public static final String WRITE_CACHE_DATA_ERROR = "Error writing cache data!";
	public static final String READ_CACHE_DATA_ERROR = "Error reading cache data!";
	public static final String DESCRIPTOR_INITIALIZION_ERROR = "The descriptor could not be initialized!";
	public static final String DESCRIPTOR_CALCULATION_ERROR = "Error during QSAR descriptor calculation!";
	public static final String MOLECULE_NOT_TAGGED_WITH_UUID = "Molecule contains no ID! Use \"Tag Molecules With UUID\" activity!";
	public static final String DATA_CONTAINS_NO_MOLECULE = "Data contains no molecules!";
	public static final String CONVERTION_ERROR = "Error during data convertion!";
	public static final String CLUSTERING_ERROR = "Error during clustering process!";
	public static final String NO_CLUSTERING_DATA_AVAILABLE = "Error, no clustering data available!";
	public static final String LOADING_CLUSTERING_DATA_ERROR = "Error loadin clustering data!";
	public static final String NO_CLUSTER_INFORMATION_AVAILABLE = "Error, no cluster information for molecule available!";
	public static final String CLUSTER_MODEL_HAS_NO_ID = "Error, cluster model contains no job ID!";
	public static final String CANT_CREATE_PDF_FILE = "Error can't create PDF file: ";
	public static final String ERROR_DURING_ACTIVITY_CONFIGURATION = "Error during activity configuration!";
	public static final String ERROR_PROVIDING_SERVICES = "Error while providing services!";
	public static final String ERROR_INVOKING_WORKERS = "Error while invoking workers!";
	public static final String ERROR_DURING_SUBSTRUCTURE_SEARCH = "Error during substructure search!";
	public static final String ERROR_WHILE_PARSING_SMILES = "Error while parsing SMILES!";

	public CDKTavernaException(String activityName, String type) {
		super(type + " Class: " + activityName + ".");
	}

}
