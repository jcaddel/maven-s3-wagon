package org.kuali.maven.wagon;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen for events about the transfer and record timing and byte count information
 *
 * @author Jeff Caddel
 * @since May 27, 2010 5:08:12 PM
 */
public class S3Listener implements TransferListener, SessionListener {
    final Logger log = LoggerFactory.getLogger(S3Listener.class);
    SimpleFormatter formatter = new SimpleFormatter();
    SessionTracker sessionTracker = new SessionTracker();

    @Override
    public void transferCompleted(final TransferEvent transferEvent) {
        TransferTracker tt = sessionTracker.getCurrentTransfer();
        tt.setCompleted(System.currentTimeMillis());
        // System.out.println();
        // log(tt.toString());
    }

    @Override
    public void transferError(final TransferEvent transferEvent) {
        log.error("Transfer error: " + transferEvent.getException(), transferEvent.getException());
    }

    @Override
    public void transferInitiated(final TransferEvent transferEvent) {
        sessionTracker.addTransfer(new TransferTracker());
        TransferTracker tt = sessionTracker.getCurrentTransfer();
        tt.setInitiated(System.currentTimeMillis());
    }

    @Override
    public void transferProgress(final TransferEvent transferEvent, final byte[] buffer, final int length) {
        // No bytes were actually read
        if (length == -1) {
            return;
        }
        TransferTracker tt = sessionTracker.getCurrentTransfer();
        int byteCount = tt.getByteCount() + length;
        tt.setByteCount(byteCount);
    }

    @Override
    public void transferStarted(final TransferEvent transferEvent) {
        TransferTracker tt = sessionTracker.getCurrentTransfer();
        tt.setStarted(System.currentTimeMillis());
        if (transferEvent.getRequestType() == TransferEvent.REQUEST_GET) {
            log.info("Downloading: " + getURI(transferEvent));
        } else {
            log.info("Uploading: " + getURI(transferEvent));
        }
        // System.out.print("[INFO] ");
    }

    protected String getURI(final TransferEvent event) {
        return getNormalizedURI(event.getWagon().getRepository().getUrl() + "/" + event.getResource().getName());
    }

    protected String getNormalizedURI(final String uri) {
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
    @Override
    public void sessionOpening(final SessionEvent sessionEvent) {
        sessionTracker.addSessionEvent(sessionEvent);
    }

    /**
     * @see SessionListener#sessionOpened(SessionEvent)
     */
    @Override
    public void sessionOpened(final SessionEvent sessionEvent) {
        sessionTracker.addSessionEvent(sessionEvent);
        sessionTracker.setOpened(System.currentTimeMillis());
        // log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Opened  ");
    }

    /**
     * @see SessionListener#sessionDisconnecting(SessionEvent)
     */
    @Override
    public void sessionDisconnecting(final SessionEvent sessionEvent) {
        sessionTracker.addSessionEvent(sessionEvent);
        sessionTracker.setDisconnecting(System.currentTimeMillis());
        // log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Disconnecting  ");
    }

    /**
     * @see SessionListener#sessionDisconnected(SessionEvent)
     */
    @Override
    public void sessionDisconnected(final SessionEvent sessionEvent) {
        sessionTracker.addSessionEvent(sessionEvent);
        // log(sessionEvent.getWagon().getRepository().getUrl() + " - Disconnected");
        sessionTracker.setDisconnected(System.currentTimeMillis());
        int transferCount = sessionTracker.getTransfers().size();
        long byteCount = 0;
        long transferElapsed = 0;
        for (TransferTracker tt : sessionTracker.getTransfers()) {
            byteCount += tt.getByteCount();
            transferElapsed += tt.getCompleted() - tt.getStarted();
        }
        long elapsed = sessionTracker.getDisconnected() - sessionTracker.getOpened();
        StringBuilder sb = new StringBuilder();
        sb.append("Transfers: " + transferCount);
        sb.append(" Time: " + formatter.getTime(elapsed));
        sb.append(" Amount: " + formatter.getSize(byteCount));
        sb.append(" Rate: " + formatter.getRate(transferElapsed, byteCount));
        sb.append(" Throughput: " + formatter.getRate(elapsed, byteCount));
        log.info(sb.toString());
    }

    /**
     * @see SessionListener#sessionConnectionRefused(SessionEvent)
     */
    @Override
    public void sessionConnectionRefused(final SessionEvent sessionEvent) {
        sessionTracker.addSessionEvent(sessionEvent);
        log.warn(sessionEvent.getWagon().getRepository().getUrl() + " - Connection refused");
    }

    /**
     * @see SessionListener#sessionLoggedIn(SessionEvent)
     */
    @Override
    public void sessionLoggedIn(final SessionEvent sessionEvent) {
        sessionTracker.addSessionEvent(sessionEvent);
        sessionTracker.setLoggedIn(System.currentTimeMillis());
        log.info("Logged in - " + sessionEvent.getWagon().getRepository().getHost());
    }

    /**
     * @see SessionListener#sessionLoggedOff(SessionEvent)
     */
    @Override
    public void sessionLoggedOff(final SessionEvent sessionEvent) {
        sessionTracker.addSessionEvent(sessionEvent);
        sessionTracker.setLoggedOff(System.currentTimeMillis());
        log.info("Logged off - " + sessionEvent.getWagon().getRepository().getHost());
    }

    @Override
    public void sessionError(final SessionEvent sessionEvent) {
        sessionTracker.addSessionEvent(sessionEvent);
        log.error("Session error: " + sessionEvent.getException(), sessionEvent.getException());
    }

    @Override
    public void debug(final String message) {
        log.debug(message);
    }

}
