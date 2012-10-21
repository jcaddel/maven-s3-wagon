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
package org.kuali.maven.wagon;

import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;

/**
 * Very simple formatter for formatting a few things - transfer rate, elapsed time, bytes
 *
 * @author Jeff Caddel
 * @since May 27, 2010 6:46:17 PM
 */
public class SimpleFormatter {
	private static final double KB = 1024;
	private static final double MB = 1024 * KB;
	private static final double GB = 1024 * MB;
	private static final double TB = 1024 * GB;
	private static final double PB = 1024 * TB;
	private static final double EB = 1024 * PB;
	private static final double SECOND = 1000;
	private static final double MINUTE = 60 * SECOND;
	private static final double HOUR = 60 * MINUTE;
	private static final double DAY = 24 * HOUR;
	private static final double YEAR = 365 * DAY;
	private static final double DECADE = 10 * YEAR;
	private static final double CENTURY = 10 * DECADE;

	NumberFormat sizeFormatter = NumberFormat.getInstance();
	NumberFormat timeFormatter = NumberFormat.getInstance();
	NumberFormat rateFormatter = NumberFormat.getInstance();
	int pad = 1;

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
	 * Given a number of bytes and the number of milliseconds it took to transfer that number of bytes, return KB/s, MB/s, or GB/s as
	 * appropriate
	 */
	public String getRate(long elapsed, long bytes) {
		double seconds = elapsed / SECOND;
		double bytesPerSecond = bytes / seconds;
		if (bytesPerSecond < MB) {
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / KB) + " KB/s", pad, " ");
		} else if (bytesPerSecond < GB) {
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / MB) + " MB/s", pad, " ");
		} else {
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / GB) + " GB/s", pad, " ");
		}
	}

	/**
	 * Given milliseconds, return seconds, minutes, hours, days, years, decades, or centuries as appropriate
	 */
	public String getTime(long millis) {
		if (millis < SECOND) {
			return StringUtils.leftPad(millis + " ms", pad, " ");
		} else if (millis < MINUTE) {
			return StringUtils.leftPad(timeFormatter.format(millis / SECOND) + " seconds", pad, " ");
		} else if (millis < HOUR) {
			return StringUtils.leftPad(timeFormatter.format(millis / MINUTE) + " minutes", pad, " ");
		} else if (millis < DAY) {
			return StringUtils.leftPad(timeFormatter.format(millis / HOUR) + " hours", pad, " ");
		} else if (millis < YEAR) {
			return StringUtils.leftPad(timeFormatter.format(millis / DAY) + " days", pad, " ");
		} else if (millis < DECADE) {
			return StringUtils.leftPad(timeFormatter.format(millis / YEAR) + " years", pad, " ");
		} else if (millis < CENTURY) {
			return StringUtils.leftPad(timeFormatter.format(millis / DECADE) + " decades", pad, " ");
		} else {
			return StringUtils.leftPad(timeFormatter.format(millis / CENTURY) + " centuries", pad, " ");
		}
	}

	/**
	 * Given a number of bytes return kilobytes, megabytes, gigabytes, terabytes, petabytes, or exabytes as appropriate.
	 */
	public String getSize(long bytes) {
		if (bytes < MB) {
			return StringUtils.leftPad(sizeFormatter.format(bytes / KB) + "k", pad, " ");
		} else if (bytes < GB) {
			return StringUtils.leftPad(sizeFormatter.format(bytes / MB) + "m", pad, " ");
		} else if (bytes < TB) {
			return StringUtils.leftPad(sizeFormatter.format(bytes / GB) + "g", pad, " ");
		} else if (bytes < PB) {
			// A terabyte. Nice.
			return StringUtils.leftPad(sizeFormatter.format(bytes / TB) + "t", pad, " ");
		} else if (bytes < EB) {
			// A petabyte!!!!!! Holy crap.
			return StringUtils.leftPad(sizeFormatter.format(bytes / PB) + "p", pad, " ");
		} else {
			// An exabyte?????? Get outta here.
			return StringUtils.leftPad(sizeFormatter.format(bytes / EB) + "e", pad, " ");
		}
	}

	public NumberFormat getSizeFormatter() {
		return sizeFormatter;
	}

	public void setSizeFormatter(NumberFormat sizeFormatter) {
		this.sizeFormatter = sizeFormatter;
	}

	public NumberFormat getTimeFormatter() {
		return timeFormatter;
	}

	public void setTimeFormatter(NumberFormat timeFormatter) {
		this.timeFormatter = timeFormatter;
	}

	public NumberFormat getRateFormatter() {
		return rateFormatter;
	}

	public void setRateFormatter(NumberFormat rateFormatter) {
		this.rateFormatter = rateFormatter;
	}

	public int getPad() {
		return pad;
	}

	public void setPad(int pad) {
		this.pad = pad;
	}
}
