package org.kuali.maven.wagon;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;

/**
 * 
 * Listen for events about the transfer and record timing and byte count information
 * 
 * @author Jeff Caddel
 * 
 * @since May 27, 2010 5:08:12 PM
 */
public class S3Listener implements TransferListener, SessionListener {
	SimpleFormatter formatter = new SimpleFormatter();
	SessionTracker sessionTracker = new SessionTracker();
	PrintStream out = null;

	public S3Listener() {
		super();
		this.out = System.out;
	}

	protected void log(String message) {
		out.println("[INFO] " + message);
	}

	@Override
	public void debug(String message) {
	}

	@Override
	public void transferCompleted(TransferEvent transferEvent) {
		TransferTracker tt = sessionTracker.getCurrentTransfer();
		tt.setCompleted(System.currentTimeMillis());
		out.println();
		log(tt.toString());
	}

	@Override
	public void transferError(TransferEvent transferEvent) {
		log(" - Transfer error: " + transferEvent.getException());
	}

	@Override
	public void transferInitiated(TransferEvent transferEvent) {
		sessionTracker.addTransfer(new TransferTracker());
		TransferTracker tt = sessionTracker.getCurrentTransfer();
		tt.setInitiated(System.currentTimeMillis());
	}

	@Override
	public void transferProgress(TransferEvent transferEvent, byte[] buffer, int length) {
		// No bytes were actually read
		if (length == -1) {
			return;
		}
		TransferTracker tt = sessionTracker.getCurrentTransfer();
		int byteCount = tt.getByteCount() + length;
		tt.setByteCount(byteCount);
		out.print("#");
	}

	@Override
	public void transferStarted(TransferEvent transferEvent) {
		TransferTracker tt = sessionTracker.getCurrentTransfer();
		tt.setStarted(System.currentTimeMillis());
		if (transferEvent.getRequestType() == TransferEvent.REQUEST_GET) {
			log("Downloading: " + transferEvent.getResource().getName() + " from " + transferEvent.getWagon().getRepository().getUrl());
		} else {
			String uri = transferEvent.getWagon().getRepository().getUrl() + "/" + transferEvent.getResource().getName();
			log("Uploading: " + getNormalizedURI(uri));
		}
		out.print("[INFO] ");
	}

	protected String getNormalizedURI(String uri) {
		try {
			URI rawUri = new URI(uri);
			return rawUri.normalize().toString();
		} catch (URISyntaxException e) {
			return uri;
		}
	}

	/**
	 * @see SessionListener#sessionOpening(SessionEvent)
	 */
	public void sessionOpening(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
	}

	/**
	 * @see SessionListener#sessionOpened(SessionEvent)
	 */
	public void sessionOpened(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		sessionTracker.setOpened(System.currentTimeMillis());
		log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Opened  ");
	}

	/**
	 * @see SessionListener#sessionDisconnecting(SessionEvent)
	 */
	public void sessionDisconnecting(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		sessionTracker.setDisconnecting(System.currentTimeMillis());
		log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Disconnecting  ");
	}

	/**
	 * @see SessionListener#sessionDisconnected(SessionEvent)
	 */
	public void sessionDisconnected(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Disconnected");
		sessionTracker.setDisconnected(System.currentTimeMillis());
		int transferCount = sessionTracker.getTransfers().size();
		long byteCount = 0;
		long transferElapsed = 0;
		for (TransferTracker tt : sessionTracker.getTransfers()) {
			byteCount += tt.getByteCount();
			transferElapsed += tt.getCompleted() - tt.getStarted();
		}
		long elapsed = sessionTracker.getDisconnected() - sessionTracker.getOpened();
		log("Total transfers: " + transferCount);
		log("Total transfer time: " + formatter.getTime(elapsed));
		log("Total amount transferred: " + formatter.getSize(byteCount));
		log("Average transfer rate: " + formatter.getRate(transferElapsed, byteCount));
		log("Overall session throughput: " + formatter.getRate(elapsed, byteCount));
	}

	/**
	 * @see SessionListener#sessionConnectionRefused(SessionEvent)
	 */
	public void sessionConnectionRefused(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Connection refused");
	}

	/**
	 * @see SessionListener#sessionLoggedIn(SessionEvent)
	 */
	public void sessionLoggedIn(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		sessionTracker.setLoggedIn(System.currentTimeMillis());
		log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Logged in");
	}

	/**
	 * @see SessionListener#sessionLoggedOff(SessionEvent)
	 */
	public void sessionLoggedOff(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		sessionTracker.setLoggedOff(System.currentTimeMillis());
		log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Logged off");
	}

	@Override
	public void sessionError(SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		log(" - Session error: " + sessionEvent.getException());
	}

}
