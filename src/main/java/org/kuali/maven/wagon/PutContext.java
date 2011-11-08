package org.kuali.maven.wagon;

import java.io.File;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.resource.Resource;

public class PutContext {
	File source;
	String destination;
	Resource resource;
	TransferProgress progress;
	TransferListenerSupport listeners;

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

}
