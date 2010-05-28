package org.kuali.maven.wagon;

import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;

/**
 * Very simple formatter for formatting a few things - transfer rate, elapsed time, bytes
 * 
 * 
 * @author Jeff Caddel
 * 
 * 
 * @since May 27, 2010 6:46:17 PM
 */
public class SimpleFormatter {
	private static final double KB = 1024;
	private static final double MB = 1024 * KB;
	private static final double GB = 1024 * MB;

	NumberFormat sizeFormatter = NumberFormat.getInstance();
	NumberFormat timeFormatter = NumberFormat.getInstance();
	NumberFormat rateFormatter = NumberFormat.getInstance();

	public SimpleFormatter() {
		super();
		sizeFormatter.setGroupingUsed(false);
		sizeFormatter.setMaximumFractionDigits(1);
		sizeFormatter.setMinimumFractionDigits(1);
		timeFormatter.setGroupingUsed(false);
		timeFormatter.setMaximumFractionDigits(3);
		timeFormatter.setMinimumFractionDigits(3);
		rateFormatter.setGroupingUsed(false);
		rateFormatter.setMaximumFractionDigits(1);
		rateFormatter.setMinimumFractionDigits(1);
	}

	/**
	 * Given milliseconds and bytes return kilobytes per second
	 */
	public String getRate(long millis, long bytes) {
		double seconds = millis / 1000D;
		double kilobytes = bytes / 1024D;
		double kilobytesPerSecond = kilobytes / seconds;
		return rateFormatter.format(kilobytesPerSecond) + " kB/s";
	}

	/**
	 * Given milliseconds, return seconds
	 */
	public String getTime(long millis) {
		if (millis < 1000) {
			return StringUtils.leftPad(millis + "ms", 6, " ");
		} else {
			return StringUtils.leftPad(timeFormatter.format(millis / 1000D) + "s", 6, " ");
		}
	}

	/**
	 * Given bytes, return kilobytes if it is less than a megabyte, megabytes if it is less than a gigabyte, otherwise
	 * gigabytes
	 */
	public String getSize(long bytes) {
		if (bytes < MB) {
			return sizeFormatter.format(bytes / KB) + "k";
		}
		if (bytes < GB) {
			return sizeFormatter.format(bytes / MB) + "m";
		}
		return sizeFormatter.format(bytes / GB) + "g";
	}
}
