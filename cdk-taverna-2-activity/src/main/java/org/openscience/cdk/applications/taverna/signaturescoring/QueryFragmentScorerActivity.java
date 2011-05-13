/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.cdk.applications.taverna.signaturescoring;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.CollectionUtilities;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.signature.MoleculeFromSignatureBuilder;

import signature.AbstractVertexSignature;
import signature.ColoredTree;

/**
 * 
 * @author kalai
 */
public class QueryFragmentScorerActivity extends AbstractCDKActivity {

	public static final String Query_Fragment_Scorer_ACTIVITY = "Query fragments scorer";
	HashMap<String, HashSet<String>> NP_set = new HashMap<String, HashSet<String>>();
	HashMap<String, HashSet<String>> SM_set = new HashMap<String, HashSet<String>>();
	HashMap<String, List<String>> Query_signatures = new HashMap<String, List<String>>();
	HashSet<String> total_NP_count = new HashSet<String>();
	HashSet<String> total_SM_count = new HashSet<String>();
	List<CMLChemFile> fragmentStructuresChemFileArray = new ArrayList<CMLChemFile>();

	public QueryFragmentScorerActivity() {
		this.INPUT_PORTS = new String[] { "NP_file", "SM_file", "QueryFile_Signatures" };
		this.OUTPUT_PORTS = new String[] { "Structure_scores", "Fragment_structures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
		addInput(this.INPUT_PORTS[1], 1, true, null, String.class);
		addInput(this.INPUT_PORTS[2], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.OUTPUT_PORTS) {
			addOutput(name, 1);
		}
	}

	public void work() throws Exception {

		// Get inputs
		List<File> np_files = this.getInputAsFileList(this.INPUT_PORTS[0]);
		List<File> sm_files = this.getInputAsFileList(this.INPUT_PORTS[1]);
		List<File> query_files = this.getInputAsFileList(this.INPUT_PORTS[2]);
		// Do work
		// Processing Natural products signature files
		for (File file : np_files) {
			LineNumberReader np_file_reader = new LineNumberReader(new FileReader(file));
			String line;
			while ((line = np_file_reader.readLine()) != null) {
				String[] uuid_signature = line.split(";");
				String uuid = uuid_signature[0];
				String signature = uuid_signature[1];
				HashSet<String> uuid_values = null;
				if (!NP_set.containsKey(signature)) {
					uuid_values = new HashSet<String>();
					uuid_values.add(uuid);
					NP_set.put(signature, uuid_values);
					total_NP_count.addAll(uuid_values);
				} else {
					uuid_values = NP_set.get(signature);
					uuid_values.add(uuid);
					total_NP_count.addAll(uuid_values);
				}
			}
		}
		// Processing Synthetic molecules signature files
		for (File file : sm_files) {
			LineNumberReader sm_file_reader = new LineNumberReader(new FileReader(file));
			String line;
			while ((line = sm_file_reader.readLine()) != null) {
				String[] uuid_signature = line.split(";");
				String uuid = uuid_signature[0];
				String signature = uuid_signature[1];
				HashSet<String> uuid_values = null;
				if (!SM_set.containsKey(signature)) {
					uuid_values = new HashSet<String>();
					uuid_values.add(uuid);
					SM_set.put(signature, uuid_values);
					total_SM_count.addAll(uuid_values);
				} else {
					uuid_values = SM_set.get(signature);
					uuid_values.add(uuid);
					total_SM_count.addAll(uuid_values);
				}
			}
		}
		// Processing query molecule's signature files
		if (query_files == null || query_files.size() == 0) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_FILE_CHOSEN);
		}
		for (File file : query_files) {
			LineNumberReader query_file_reader = new LineNumberReader(new FileReader(file));
			String line;

			while ((line = query_file_reader.readLine()) != null) {
				String[] uuid_signature = line.split(";");

				String uuid = uuid_signature[0];
				String signature = uuid_signature[1];

				if (!Query_signatures.containsKey(uuid)) {
					List<String> signatures = new ArrayList<String>();
					signatures.add(signature);
					Query_signatures.put(uuid, signatures);
				} else {
					List<String> signatures = Query_signatures.get(uuid);
					signatures.add(signature);
				}
			}

		}
		Map<String, Double> score_list = new HashMap<String, Double>();
		List<String> result_score = new ArrayList<String>();
		result_score.add("Molecule_ID;Score");
		for (String molecule : Query_signatures.keySet()) {

			List<String> fragments = Query_signatures.get(molecule);
			int molecule_size = fragments.size();
			String molecule_id = molecule;
			double score = 0.0;
			for (String fragment : fragments) {
				double fragment_Weight = npScorer(fragment);
				score = score + fragment_Weight;

				IAtomContainer fragment_container = reconstruct(fragment);
				IMolecule fragment_molecule = new Molecule(fragment_container);

				if (fragment_molecule.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
					fragment_molecule.setProperty(CDKTavernaConstants.MOLECULEID, molecule_id);
				}

				if (fragment_molecule.getProperty(CDKTavernaConstants.FRAGMENT_SCORE) == null) {
					if (fragment_Weight != 0.0 && molecule_size != 0) {
						double normalized_fragment_score = fragment_Weight / molecule_size;
						fragment_molecule.setProperty(CDKTavernaConstants.FRAGMENT_SCORE, normalized_fragment_score);

					} else {
						fragment_molecule.setProperty(CDKTavernaConstants.FRAGMENT_SCORE, fragment_Weight);
					}

				}
				if (fragment_molecule.getProperty(CDKTavernaConstants.SIGNATURE) == null) {
					fragment_molecule.setProperty(CDKTavernaConstants.SIGNATURE, fragment);
				}
				fragmentStructuresChemFileArray.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(fragment_molecule));
			}
			if (score != 0.0 && molecule_size != 0) {
				double normalized_score = score / molecule_size;
				score_list.put(molecule, normalized_score);
			} else {
				score_list.put(molecule, 0.0);
			}
			// result_score.add(molecule + " = " + score);
		}
		List<Entry<String, Double>> sorted_score_list = CollectionUtilities.sortByValue(score_list);
		for (Entry<String, Double> entry : sorted_score_list) {
			String molecule_name = entry.getKey();
			Double score_ = entry.getValue();
			String molecule_score = molecule_name + ";" + score_;
			result_score.add(molecule_score);
		}
		// Set output
		this.setOutputAsStringList(result_score, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(fragmentStructuresChemFileArray, this.OUTPUT_PORTS[1]);
	}

	private double npScorer(String signature) {

		double fragment_weight = 0;
		double NPCount = 0;
		double SMCount = 0;
		double TotalNPCount = 0;
		double TotalSMCount = 0;

		TotalNPCount = total_NP_count.size();
		TotalSMCount = total_SM_count.size();

		if (NP_set.containsKey(signature)) {
			HashSet<String> uuid_values = NP_set.get(signature);
			NPCount = uuid_values.size();
		} else {
			NPCount = 0.0;
		}

		if (SM_set.containsKey(signature)) {
			HashSet<String> uuid_values = SM_set.get(signature);
			SMCount = uuid_values.size();
		} else {
			SMCount = 0;
		}

		if (NPCount != 0 && SMCount != 0) {
			fragment_weight = Math.log10((NPCount / SMCount) * (TotalSMCount / TotalNPCount));
			return fragment_weight;
		} else {
			return 0.0;
		}
	}

	public IAtomContainer reconstruct(String signature) {
		ColoredTree tree = AbstractVertexSignature.parse(signature);
		MoleculeFromSignatureBuilder builder = new MoleculeFromSignatureBuilder(DefaultChemObjectBuilder.getInstance());
		builder.makeFromColoredTree(tree);
		return builder.getAtomContainer();
	}

	@Override
	public String getActivityName() {
		return QueryFragmentScorerActivity.Query_Fragment_Scorer_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.SIGNATURE_SCORING_FOLDER_NAME;
	}

	@Override
	public String getDescription() {
		return "Description: " + QueryFragmentScorerActivity.Query_Fragment_Scorer_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

}
