package org.kuali.maven.wagon;

/**
 * Holds timing and byte count information about a transfer operation
 * 
 * @author Jeff Caddel
 * 
 * @since May 27, 2010 6:51:19 PM
 */
public class TransferTracker {
	long initiated;
	long started;
	long completed;
	int byteCount;
	SimpleFormatter formatter = new SimpleFormatter();

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
		sb.append("[" + formatter.getTime(elapsed));
		sb.append(", " + formatter.getSize(byteCount));
		sb.append(", " + formatter.getRate(elapsed, byteCount));
		sb.append("]");
		return sb.toString();
	}
}
