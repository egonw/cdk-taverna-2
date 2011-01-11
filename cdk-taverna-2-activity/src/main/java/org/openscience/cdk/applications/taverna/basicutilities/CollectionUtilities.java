package org.openscience.cdk.applications.taverna.basicutilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class for java.util.maps
 * 
 * @author Andreas Truszkowski
 * 
 */
public class CollectionUtilities {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized static void sortTwoLists(List<Integer> queryList, List secondList) {
		if (queryList.size() != secondList.size()) {
			throw new IllegalArgumentException("The lists have to be of equal size!");
		}
		int n = queryList.size();
		for (int i = 0; i < n; i++) {
			for (int j = n - 1; j > i; j--) {
				int one = ((Number) queryList.get(j - 1)).intValue();
				int two = ((Number) queryList.get(j)).intValue();
				if (one > two) {
					queryList.set(j - 1, two);
					queryList.set(j, one);

					Object temp = secondList.get(j - 1);
					secondList.set(j - 1, secondList.get(j));
					secondList.set(j, temp);
				}
			}
		}
	}

	/**
	 * Returns a sorted entry set of given map.
	 * 
	 * @param <K>
	 *            key type
	 * @param <V>
	 *            value type
	 * @param map
	 *            Map to be sorted
	 * @return Sorted entry set
	 */
	public synchronized static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
		Collections.sort(entries, new ByValue<K, V>());
		return entries;
	}

	private static class ByValue<K, V extends Comparable<V>> implements Comparator<Entry<K, V>> {
		public int compare(Entry<K, V> o1, Entry<K, V> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}

}