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
 * Format time, bytes, and transfer rate into human friendly form
 *
 * @author Jeff Caddel
 * @since May 27, 2010 6:46:17 PM
 */
public class SimpleFormatter {
	private static final double KB = 1024; // kilobyte
	private static final double MB = 1024 * KB; // megabyte
	private static final double GB = 1024 * MB; // gigabyte
	private static final double TB = 1024 * GB; // terabyte
	private static final double PB = 1024 * TB; // petabyte
	private static final double EB = 1024 * PB; // exabyte
	private static final double SECOND = 1000;
	private static final double MINUTE = 60 * SECOND;
	private static final double HOUR = 60 * MINUTE;
	private static final double DAY = 24 * HOUR;
	private static final double YEAR = 365 * DAY;
	private static final double DECADE = 10 * YEAR;
	private static final double CENTURY = 10 * DECADE;

	NumberFormat sizeFormatter = NumberFormat.getInstance();
	NumberFormat smallSizeFormatter = NumberFormat.getInstance();
	NumberFormat timeFormatter = NumberFormat.getInstance();
	NumberFormat rateFormatter = NumberFormat.getInstance();
	int leftPad = 1;

	public SimpleFormatter() {
		super();
		sizeFormatter.setGroupingUsed(false);
		sizeFormatter.setMaximumFractionDigits(3);
		sizeFormatter.setMinimumFractionDigits(3);
		smallSizeFormatter.setGroupingUsed(false);
		smallSizeFormatter.setMaximumFractionDigits(1);
		smallSizeFormatter.setMinimumFractionDigits(1);
		timeFormatter.setGroupingUsed(false);
		timeFormatter.setMaximumFractionDigits(3);
		timeFormatter.setMinimumFractionDigits(3);
		rateFormatter.setGroupingUsed(false);
		rateFormatter.setMaximumFractionDigits(3);
		rateFormatter.setMinimumFractionDigits(3);
	}

	/**
	 * Given a number of bytes and the number of milliseconds it took to transfer that number of bytes, return bytes/s, KB/s, MB/s, GB/s,
	 * TB/s, PB/s, or EB/s as appropriate
	 */
	public String getRate(long millis, long bytes) {
		double seconds = millis / SECOND;
		double bytesPerSecond = bytes / seconds;
		if (bytesPerSecond < KB) {
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond) + " bytes/s", leftPad, " ");
		} else if (bytesPerSecond < MB) {
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / KB) + " KB/s", leftPad, " ");
		} else if (bytesPerSecond < GB) {
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / MB) + " MB/s", leftPad, " ");
		} else if (bytesPerSecond < TB) {
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / GB) + " GB/s", leftPad, " ");
		} else if (bytesPerSecond < PB) {
			// Terabytes per second. Wow
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / TB) + " terabytes/s", leftPad, " ");
		} else if (bytesPerSecond < EB) {
			// Petabytes per second!!! Holy smokes.
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / PB) + " petabytes/s", leftPad, " ");
		} else {
			// Exabytes per second!!! Get outta here.
			return StringUtils.leftPad(rateFormatter.format(bytesPerSecond / EB) + " exabytes/s", leftPad, " ");
		}
	}

	/**
	 * Given milliseconds, return milliseconds, seconds, minutes, hours, days, years, decades, or centuries as appropriate
	 */
	public String getTime(long millis) {
		if (millis < SECOND) {
			return StringUtils.leftPad(millis + "ms", leftPad, " ");
		} else if (millis < MINUTE) {
			return StringUtils.leftPad(timeFormatter.format(millis / SECOND) + "s", leftPad, " ");
		} else if (millis < HOUR) {
			return StringUtils.leftPad(timeFormatter.format(millis / MINUTE) + "m", leftPad, " ");
		} else if (millis < DAY) {
			return StringUtils.leftPad(timeFormatter.format(millis / HOUR) + " hours", leftPad, " ");
		} else if (millis < YEAR) {
			return StringUtils.leftPad(timeFormatter.format(millis / DAY) + " days", leftPad, " ");
		} else if (millis < DECADE) {
			return StringUtils.leftPad(timeFormatter.format(millis / YEAR) + " years", leftPad, " ");
		} else if (millis < CENTURY) {
			return StringUtils.leftPad(timeFormatter.format(millis / DECADE) + " decades", leftPad, " ");
		} else {
			return StringUtils.leftPad(timeFormatter.format(millis / CENTURY) + " centuries", leftPad, " ");
		}
	}

	/**
	 * Given a number of bytes return bytes, kilobytes, megabytes, gigabytes, terabytes, petabytes, or exabytes as appropriate.
	 */
	public String getSize(long bytes) {
		if (bytes < KB) {
			return StringUtils.leftPad(bytes + " bytes", leftPad, " ");
		} else if (bytes < MB) {
			return StringUtils.leftPad(smallSizeFormatter.format(bytes / KB) + "k", leftPad, " ");
		} else if (bytes < GB) {
			return StringUtils.leftPad(smallSizeFormatter.format(bytes / MB) + "m", leftPad, " ");
		} else if (bytes < TB) {
			return StringUtils.leftPad(sizeFormatter.format(bytes / GB) + "g", leftPad, " ");
		} else if (bytes < PB) {
			// A terabyte. Nice.
			return StringUtils.leftPad(sizeFormatter.format(bytes / TB) + " terabytes", leftPad, " ");
		} else if (bytes < EB) {
			// A petabyte!!!!!! Wow.
			return StringUtils.leftPad(sizeFormatter.format(bytes / PB) + " petabytes", leftPad, " ");
		} else {
			// An exabyte?????? Get outta here.
			return StringUtils.leftPad(sizeFormatter.format(bytes / EB) + " exabytes", leftPad, " ");
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

	public int getLeftPad() {
		return leftPad;
	}

	public void setLeftPad(int pad) {
		this.leftPad = pad;
	}

	public NumberFormat getSmallSizeFormatter() {
		return smallSizeFormatter;
	}

	public void setSmallSizeFormatter(NumberFormat smallSizeFormatter) {
		this.smallSizeFormatter = smallSizeFormatter;
	}
}
