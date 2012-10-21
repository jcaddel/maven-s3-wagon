package org.kuali.maven.wagon;
public class BucketDisplay implements Comparable<BucketDisplay> {

	String prefix;
	long count;
	String size;

	public int compareTo(BucketDisplay other) {
		String prefix1 = getPrefix();
		String prefix2 = other.getPrefix();
		return prefix1.compareTo(prefix2);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
