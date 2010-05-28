package org.kuali.maven.wagon;

import java.text.NumberFormat;

public class ByteFormatter {
	private static final double KB = 1024;
	private static final double MB = 1024 * KB;
	private static final double GB = 1024 * MB;

	NumberFormat nf = NumberFormat.getInstance();

	public ByteFormatter() {
		super();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
	}

	public String getString(long bytes) {
		if (bytes < MB) {
			return nf.format(bytes / KB) + "k";
		}
		if (bytes < GB) {
			return nf.format(bytes / MB) + "m";
		}
		return nf.format(bytes / GB) + "g";
	}
}
