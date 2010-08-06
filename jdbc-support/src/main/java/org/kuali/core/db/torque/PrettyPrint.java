package org.kuali.core.db.torque;

/**
 * 
 */
public class PrettyPrint {
	public PrettyPrint() {
		this(null);
	}

	public PrettyPrint(String msg) {
		super();
		this.msg = msg;
	}

	long start;
	long stop;
	String msg;

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getStop() {
		return stop;
	}

	public void setStop(long stop) {
		this.stop = stop;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
