package org.kuali.core.db.torque;

import java.util.Set;
import java.util.TreeSet;

/**
 * Utility methods for Set operations
 */
public class SetUtils {

	/**
	 * Returns the combined elements from both
	 */
	public static <T> Set<T> union(Set<T> a, Set<T> b) {
		Set<T> tmp = new TreeSet<T>(a);
		tmp.addAll(b);
		return tmp;
	}

	/**
	 * Returns only those elements that are present in both A and B
	 */
	public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
		Set<T> tmp = new TreeSet<T>();
		for (T x : a) {
			if (b.contains(x)) {
				tmp.add(x);
			}
		}
		return tmp;
	}

	/**
	 * Returns elements from A that are not in B
	 */
	public static <T> Set<T> difference(Set<T> a, Set<T> b) {
		Set<T> tmp = new TreeSet<T>(a);
		tmp.removeAll(b);
		return tmp;
	}

	/**
	 * 
	 */
	public static <T> Set<T> symDifference(Set<T> a, Set<T> b) {
		Set<T> tmpA = union(a, b);
		Set<T> tmpB = intersection(a, b);
		return difference(tmpA, tmpB);
	}

	/**
	 * Return true if every element in A is also present in B
	 */
	public static <T> boolean isSubset(Set<T> a, Set<T> b) {
		return b.containsAll(a);
	}

	/**
	 * Return true if every element in B is also present in A
	 */
	public static <T> boolean isSuperset(Set<T> a, Set<T> b) {
		return a.containsAll(b);
	}
}
