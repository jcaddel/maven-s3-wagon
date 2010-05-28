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
	private static final double ONE_SECOND = 1000;
	private static final double ONE_MINUTE = 60 * ONE_SECOND;
	private static final double FIFTEEN_MINUTES = 15 * ONE_MINUTE;

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
		if (kilobytesPerSecond < 1024) {
			return StringUtils.leftPad(rateFormatter.format(kilobytesPerSecond) + " kB/s", 10, " ");
		} else {
			double transferRate = kilobytesPerSecond / 1024;
			return StringUtils.leftPad(rateFormatter.format(transferRate) + " MB/s", 10, " ");
		}
	}

	/**
	 * Given milliseconds, return seconds or minutes
	 */
	public String getTime(long millis) {
		if (millis < ONE_SECOND) {
			return StringUtils.leftPad(millis + "ms", 6, " ");
		} else if (millis < 10 * ONE_SECOND) {
			return StringUtils.leftPad(timeFormatter.format(millis / ONE_SECOND) + "s", 6, " ");
		} else if (millis < FIFTEEN_MINUTES) {
			return StringUtils.leftPad(rateFormatter.format(millis / ONE_SECOND) + "s", 6, " ");
		} else {
			return StringUtils.leftPad(rateFormatter.format(millis / ONE_MINUTE) + "m", 6, " ");
		}
	}

	/**
	 * Given bytes, return kilobytes if it is less than a megabyte, megabytes if it is less than a gigabyte, otherwise
	 * gigabytes
	 */
	public String getSize(long bytes) {
		if (bytes < MB) {
			return StringUtils.leftPad(sizeFormatter.format(bytes / KB) + "k", 7, " ");
		}
		if (bytes < GB) {
			return StringUtils.leftPad(sizeFormatter.format(bytes / MB) + "m", 7, " ");
		}
		return StringUtils.leftPad(sizeFormatter.format(bytes / GB) + "g", 7, " ");
	}
}
