package org.kuali.maven.wagon;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;

/**
 * 
 * 
 * 
 * @author Jeff Caddel
 * 
 * @since May 27, 2010 5:08:12 PM
 */
public class SimpleTransferListener implements TransferListener {
	TransferTimer tt = null;

	@Override
	public void debug(String message) {
	}

	@Override
	public void transferCompleted(TransferEvent transferEvent) {
		tt.setCompleted(System.currentTimeMillis());
		System.out.println("\n[INFO] " + tt.toString());
		tt = null;
	}

	@Override
	public void transferError(TransferEvent transferEvent) {
	}

	@Override
	public void transferInitiated(TransferEvent transferEvent) {
		tt = new TransferTimer();
		tt.setInitiated(System.currentTimeMillis());
	}

	@Override
	public void transferProgress(TransferEvent transferEvent, byte[] buffer, int length) {
		// No bytes were actually read
		if (length == -1) {
			return;
		}
		int byteCount = tt.getByteCount() + length;
		tt.setByteCount(byteCount);
	}

	@Override
	public void transferStarted(TransferEvent transferEvent) {
		tt.setStarted(System.currentTimeMillis());
	}

}
