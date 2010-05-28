package org.kuali.maven.wagon;

public class TransferTimer {
	long initiated;
	long started;
	long completed;
	int byteCount;
	ByteFormatter byteFormatter = new ByteFormatter();
	SpeedFormatter speedFormatter = new SpeedFormatter();
	TimeFormatter timeFormatter = new TimeFormatter();

	public long getInitiated() {
		return initiated;
	}

	public void setInitiated(long initiated) {
		this.initiated = initiated;
	}

	public long getStarted() {
		return started;
	}

	public void setStarted(long started) {
		this.started = started;
	}

	public long getCompleted() {
		return completed;
	}

	public void setCompleted(long completed) {
		this.completed = completed;
	}

	public int getByteCount() {
		return byteCount;
	}

	public void setByteCount(int byteCount) {
		this.byteCount = byteCount;
	}

	public String toString() {
		long elapsed = completed - started;
		StringBuffer sb = new StringBuffer();
		sb.append("Time:" + timeFormatter.getString(elapsed));
		sb.append("  Size:" + byteFormatter.getString(byteCount));
		sb.append("  Rate:" + speedFormatter.getString(elapsed, byteCount));
		return sb.toString();
	}
}
