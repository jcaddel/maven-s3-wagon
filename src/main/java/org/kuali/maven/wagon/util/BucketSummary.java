/**
 * Copyright 2010-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.maven.wagon.util;

public class BucketSummary implements Comparable<BucketSummary> {

	String prefix;
	long count;
	long size;

	public int compareTo(BucketSummary other) {
		String prefix1 = getPrefix();
		String prefix2 = other.getPrefix();
		if (prefix1 == null) {
			return -1;
		} else {
			return prefix1.compareTo(prefix2);
		}
	}

	public BucketSummary() {
		this(null);
	}

	public BucketSummary(String prefix) {
		super();
		this.prefix = prefix;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long objectCount) {
		this.count = objectCount;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long objectSize) {
		this.size = objectSize;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
