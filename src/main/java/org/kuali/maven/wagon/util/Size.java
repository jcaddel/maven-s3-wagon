package org.kuali.maven.wagon.util;

public enum Size {

	BYTE(1, "bytes", "bytes/s"), //
	KB(1024, "k", "KB/s"), //
	MB(1024 * Size.KB.getValue(), "m", "MB/s"), //
	GB(1024 * Size.MB.getValue(), "g", "GB/s"), //
	TB(1024 * Size.GB.getValue(), "terabytes", "terabytes/s"), //
	PB(1024 * Size.TB.getValue(), "petabytes", "petabyte/s"), //
	EB(1024 * Size.PB.getValue(), "exabytes", "exabytes/s");

	private long value;
	private String sizeLabel;
	private String rateLabel;

	private Size(long value, String sizeLabel, String rateLabel) {
		this.value = value;
		this.sizeLabel = sizeLabel;
		this.rateLabel = rateLabel;
	}

	public long getValue() {
		return value;
	}

	public String getSizeLabel() {
		return sizeLabel;
	}

	public String getRateLabel() {
		return rateLabel;
	}

}
