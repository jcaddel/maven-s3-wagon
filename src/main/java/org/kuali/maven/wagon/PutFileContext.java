package org.kuali.maven.wagon;

import java.io.File;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.resource.Resource;

import com.amazonaws.services.s3.AmazonS3Client;

/**
 * This is the context needed by the Wagon for uploading a file and tracking its progress as it goes
 */
public class PutFileContext {
    File source;
    String destination;
    Resource resource;
    TransferProgress progress;
    TransferListenerSupport listeners;
    AmazonS3Client client;
    RequestFactory factory;

    public void fireStart() {
        listeners.fireTransferInitiated(getResource(), TransferEvent.REQUEST_PUT);
        listeners.fireTransferStarted(getResource(), TransferEvent.REQUEST_PUT);
    }

    public void fireComplete() {
        listeners.fireTransferCompleted(getResource(), TransferEvent.REQUEST_PUT);
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public TransferProgress getProgress() {
        return progress;
    }

    public void setProgress(TransferProgress progress) {
        this.progress = progress;
    }

    public TransferListenerSupport getListeners() {
        return listeners;
    }

    public void setListeners(TransferListenerSupport listeners) {
        this.listeners = listeners;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public AmazonS3Client getClient() {
        return client;
    }

    public void setClient(AmazonS3Client client) {
        this.client = client;
    }

    public RequestFactory getFactory() {
        return factory;
    }

    public void setFactory(RequestFactory factory) {
        this.factory = factory;
    }

}
