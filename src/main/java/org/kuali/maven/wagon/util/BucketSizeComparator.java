package org.kuali.maven.wagon.util;

import java.util.Comparator;

import org.kuali.maven.wagon.BucketSummary;

public class BucketSizeComparator implements Comparator<BucketSummary> {

	public int compare(BucketSummary one, BucketSummary two) {
		if (one.getSize() < two.getSize()) {
			return -1;
		} else if (one.getSize() == two.getSize()) {
			return 0;
		} else {
			return 1;
		}
	}

}
