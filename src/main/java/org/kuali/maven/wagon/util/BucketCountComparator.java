package org.kuali.maven.wagon.util;

import java.util.Comparator;


public class BucketCountComparator implements Comparator<BucketSummary> {

	public int compare(BucketSummary one, BucketSummary two) {
		if (one.getCount() < two.getCount()) {
			return -1;
		} else if (one.getCount() == two.getCount()) {
			return 0;
		} else {
			return 1;
		}
	}

}
