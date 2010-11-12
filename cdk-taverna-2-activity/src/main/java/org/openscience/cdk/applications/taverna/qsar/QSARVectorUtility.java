package org.openscience.cdk.applications.taverna.qsar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.openscience.cdk.applications.art2aclassification.FingerprintItem;

import com.sun.corba.se.pept.transport.InboundConnectionCache;

public class QSARVectorUtility {

	private Map<UUID, Map<String, Object>> curatedVectorMap = new HashMap<UUID, Map<String, Object>>();
	private ArrayList<String> curatedDescriptorNames = new ArrayList<String>();

	/**
	 * Returns a list of the UUIDs from given the QSAR vector.
	 * 
	 * @param vectorMap
	 *            QSAR descriptor vector map.
	 * @return List of the UUIDs from given the QSAR vector.
	 */
	public List<UUID> getUUIDs(Map<UUID, Map<String, Object>> vectorMap) {
		ArrayList<UUID> uuids = new ArrayList<UUID>();
		for (Entry<UUID, Map<String, Object>> entry : vectorMap.entrySet()) {
			uuids.add(entry.getKey());
		}
		return uuids;
	}

	public void curateQSARVector(Map<UUID, Map<String, Object>> vectorMap, ArrayList<String> descriptorNames) {
		ArrayList<Integer> columsToDelete = new ArrayList<Integer>();
		List<UUID> uuids = getUUIDs(vectorMap);
		for (UUID uuid : uuids) {
			Map<String, Object> descriptorResultMap = vectorMap.get(uuid);
			for (int i = 0; i < descriptorNames.size(); i++) {
				String key = descriptorNames.get(i);
				Object result = descriptorResultMap.get(key);
				if(result == null) {
					columsToDelete.add(i);
					continue;
				}
				Double value = Double.NaN;
				if (result instanceof Double) {
					value = (Double) result;
				} else {
					value = Double.valueOf((Integer) result);
				}
				if (value.equals(Double.NaN) && !columsToDelete.contains(i)) {
					columsToDelete.add(i);
				}
			}
		}
		ArrayList<Integer> columsToKeep = new ArrayList<Integer>();
		for (int i = 0; i < descriptorNames.size(); i++) {
			if (!columsToDelete.contains(i)) {
				columsToKeep.add(i);
			}
		}
		for (int idx : columsToKeep) {
			String key = descriptorNames.get(idx);
			this.curatedDescriptorNames.add(key);
		}
		for (UUID uuid : uuids) {
			Map<String, Object> descriptorResultMap = vectorMap.get(uuid);
			Map<String, Object> curatedDescriptorResultMap = new HashMap<String, Object>();
			for (int idx : columsToKeep) {
				String key = descriptorNames.get(idx);
				Double value = Double.NaN;
				Object result = descriptorResultMap.get(key);
				if (result instanceof Double) {
					value = (Double) result;
				} else {
					value = Double.valueOf((Integer) result);
				}
				curatedDescriptorResultMap.put(key, value);
			}
			this.curatedVectorMap.put(uuid, curatedDescriptorResultMap);
		}
	}

	public List<FingerprintItem> createFingerprintItemListFRomQSARVector(Map<UUID, Map<String, Object>> vectorMap,
			ArrayList<String> descriptorNames) {
		ArrayList<FingerprintItem> itemList = new ArrayList<FingerprintItem>();
		List<UUID> uuids = getUUIDs(vectorMap);
		for (UUID uuid : uuids) {
			Map<String, Object> descriptorResultMap = vectorMap.get(uuid);
			double[] vector = new double[descriptorNames.size()];
			for (int i = 0; i < descriptorNames.size(); i++) {
				String key = descriptorNames.get(i);
				Double value = (Double) descriptorResultMap.get(key);
				vector[i] = value;
			}
			FingerprintItem item = new FingerprintItem();
			item.correspondingObject = uuid;
			item.fingerprintVector = vector;
			itemList.add(item);
		}
		return itemList;
	}

	public Map<UUID, Map<String, Object>> getCuratedVectorMap() {
		return curatedVectorMap;
	}

	public ArrayList<String> getCuratedDescriptorNames() {
		return curatedDescriptorNames;
	}

}
