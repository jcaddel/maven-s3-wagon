package org.kuali.maven.wagon;

import java.text.NumberFormat;

/**
 * 
 * @author Jeff Caddel
 * 
 * @since May 27, 2010 5:29:10 PM
 */
public class SpeedFormatter {
	NumberFormat nf = NumberFormat.getInstance();

	public SpeedFormatter() {
		super();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
	}

	public String getString(long millis, long bytes) {
		double seconds = millis / 1000D;
		double kilobytes = bytes / 1024D;
		double kilobytesPerSecond = kilobytes / seconds;
		return nf.format(kilobytesPerSecond) + " kB/second";
	}

}
