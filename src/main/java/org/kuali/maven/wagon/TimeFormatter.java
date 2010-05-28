package org.kuali.maven.wagon;

import java.text.NumberFormat;

public class TimeFormatter {
	NumberFormat nf = NumberFormat.getInstance();

	public TimeFormatter() {
		super();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
	}

	public String getString(long millis) {
		return nf.format(millis / 1000D) + "s";
	}
}
