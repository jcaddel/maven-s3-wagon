/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.maven.wagon;

import java.io.File;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;

/**
 * An abstract implementation of the Wagon interface. This implementation manages listener and other common behaviors.
 *
 * @author Ben Hale
 * @author Jeff Caddel - Updates for version 1.0-beta-6 of the Wagon interface
 * @since 1.1
 */
public abstract class AbstractWagon implements Wagon {

    private int timeout;

    private boolean interactive;

    private Repository repository;

    private final boolean supportsDirectoryCopy;

    private final SessionListenerSupport sessionListeners = new SessionListenerSupport(this);

    private final TransferListenerSupport transferListeners = new TransferListenerSupport(this);

    protected AbstractWagon(final boolean supportsDirectoryCopy) {
        this.supportsDirectoryCopy = supportsDirectoryCopy;
    }

    @Override
    public final void addSessionListener(final SessionListener listener) {
        if (listener.getClass().equals(Debug.class)) {
            // This is a junky listener that spews things to System.out in an ugly way
            return;
        }
        sessionListeners.addListener(listener);
    }

    protected final SessionListenerSupport getSessionListeners() {
        return sessionListeners;
    }

    @Override
    public final boolean hasSessionListener(final SessionListener listener) {
        return sessionListeners.hasListener(listener);
    }

    @Override
    public final void removeSessionListener(final SessionListener listener) {
        sessionListeners.removeListener(listener);
    }

    @Override
    public final void addTransferListener(final TransferListener listener) {
        transferListeners.addListener(listener);
    }

    protected final TransferListenerSupport getTransferListeners() {
        return transferListeners;
    }

    @Override
    public final boolean hasTransferListener(final TransferListener listener) {
        return transferListeners.hasListener(listener);
    }

    @Override
    public final void removeTransferListener(final TransferListener listener) {
        transferListeners.removeListener(listener);
    }

    @Override
    public final Repository getRepository() {
        return repository;
    }

    @Override
    public final boolean isInteractive() {
        return interactive;
    }

    @Override
    public final void setInteractive(final boolean interactive) {
        this.interactive = interactive;
    }

    @Override
    public final void connect(final Repository source) throws ConnectionException, AuthenticationException {
        doConnect(source, null, null);
    }

    @Override
    public final void connect(final Repository source, final ProxyInfo proxyInfo) throws ConnectionException,
            AuthenticationException {
        connect(source, null, proxyInfo);
    }

    @Override
    public final void connect(final Repository source, final AuthenticationInfo authenticationInfo) throws ConnectionException,
            AuthenticationException {
        doConnect(source, authenticationInfo, null);
    }

    protected void doConnect(final Repository source, final AuthenticationInfo authenticationInfo, final ProxyInfo proxyInfo)
            throws ConnectionException, AuthenticationException {
        repository = source;
        sessionListeners.fireSessionOpening();
        try {
            connectToRepository(source, authenticationInfo, proxyInfo);
        } catch (ConnectionException e) {
            sessionListeners.fireSessionConnectionRefused();
            throw e;
        } catch (AuthenticationException e) {
            sessionListeners.fireSessionConnectionRefused();
            throw e;
        } catch (Exception e) {
            sessionListeners.fireSessionConnectionRefused();
            throw new ConnectionException("Could not connect to repository", e);
        }
        sessionListeners.fireSessionLoggedIn();
        sessionListeners.fireSessionOpened();
    }

    @Override
    public final void connect(final Repository source, final AuthenticationInfo authenticationInfo, final ProxyInfo proxyInfo)
            throws ConnectionException, AuthenticationException {
        doConnect(source, authenticationInfo, proxyInfo);
    }

    @Override
    public final void disconnect() throws ConnectionException {
        sessionListeners.fireSessionDisconnecting();
        try {
            disconnectFromRepository();
        } catch (ConnectionException e) {
            sessionListeners.fireSessionConnectionRefused();
            throw e;
        } catch (Exception e) {
            sessionListeners.fireSessionConnectionRefused();
            throw new ConnectionException("Could not disconnect from repository", e);
        }
        sessionListeners.fireSessionLoggedOff();
        sessionListeners.fireSessionDisconnected();
    }

    @Override
    public final void get(final String resourceName, final File destination) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        Resource resource = new Resource(resourceName);
        transferListeners.fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
        transferListeners.fireTransferStarted(resource, TransferEvent.REQUEST_GET);

        try {
            getResource(resourceName, destination, new TransferProgress(resource, TransferEvent.REQUEST_GET,
                    transferListeners));
            transferListeners.fireTransferCompleted(resource, TransferEvent.REQUEST_GET);
        } catch (TransferFailedException e) {
            throw e;
        } catch (ResourceDoesNotExistException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final List getFileList(final String destinationDirectory) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        try {
            return listDirectory(destinationDirectory);
        } catch (TransferFailedException e) {
            throw e;
        } catch (ResourceDoesNotExistException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            sessionListeners.fireSessionError(e);
            throw new TransferFailedException("Listing of directory " + destinationDirectory + "failed", e);
        }
    }

    @Override
    public final boolean getIfNewer(final String resourceName, final File destination, final long timestamp)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        Resource resource = new Resource(resourceName);
        try {
            if (isRemoteResourceNewer(resourceName, timestamp)) {
                get(resourceName, destination);
                return true;
            } else {
                return false;
            }
        } catch (TransferFailedException e) {
            throw e;
        } catch (ResourceDoesNotExistException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
        }
    }

    @Override
    public final void openConnection() throws ConnectionException, AuthenticationException {
        // Nothing to do here (never called by the wagon manager)
    }

    @Override
    public final void put(final File source, final String destination) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        Resource resource = new Resource(destination);
        transferListeners.fireTransferInitiated(resource, TransferEvent.REQUEST_PUT);
        transferListeners.fireTransferStarted(resource, TransferEvent.REQUEST_PUT);

        try {
            putResource(source, destination, new TransferProgress(resource, TransferEvent.REQUEST_PUT,
                    transferListeners));
            transferListeners.fireTransferCompleted(resource, TransferEvent.REQUEST_PUT);
        } catch (TransferFailedException e) {
            throw e;
        } catch (ResourceDoesNotExistException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            transferListeners.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
            throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
        }
    }

    /**
     * On S3 there are no true "directories". An S3 bucket is essentially a Hashtable of files stored by key. The
     * integration between a traditional file system and an S3 bucket is to use the path of the file on the local file
     * system as the key to the file in the bucket. The S3 bucket does not contain a separate key for the directory
     * itself.
     */
    @Override
    public final void putDirectory(final File sourceDirectory, final String destinationDirectory) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        // Cycle through all the files in this directory
        for (File f : sourceDirectory.listFiles()) {
            // We hit a directory
            if (f.isDirectory()) {
                // Recurse into the sub-directory and store any files we find
                putDirectory(f, destinationDirectory + "/" + f.getName());
            } else {
                // Normal file, store it into S3
                put(f, destinationDirectory + "/" + f.getName());
            }
        }
    }

    @Override
    public final boolean resourceExists(final String resourceName) throws TransferFailedException, AuthorizationException {
        try {
            return doesRemoteResourceExist(resourceName);
        } catch (TransferFailedException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            sessionListeners.fireSessionError(e);
            throw new TransferFailedException("Listing of resource " + resourceName + "failed", e);
        }
    }

    @Override
    public final boolean supportsDirectoryCopy() {
        return supportsDirectoryCopy;
    }

    /**
     * Subclass must implement with specific connection behavior
     *
     * @param source
     * The repository connection information
     * @param authenticationInfo
     * Authentication information, if any
     * @param proxyInfo
     * Proxy information, if any
     * @throws Exception
     * Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract void connectToRepository(Repository source, AuthenticationInfo authenticationInfo,
            ProxyInfo proxyInfo) throws Exception;

    /**
     * Subclass must implement with specific detection behavior
     *
     * @param resourceName
     * The remote resource to detect
     * @return true if the remote resource exists
     * @throws Exception
     * Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract boolean doesRemoteResourceExist(String resourceName) throws Exception;

    /**
     * Subclasses must implement with specific disconnection behavior
     *
     * @throws Exception
     * Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract void disconnectFromRepository() throws Exception;

    /**
     * Subclass must implement with specific get behavior
     *
     * @param resourceName
     * The name of the remote resource to read
     * @param destination
     * The local file to write to
     * @param progress
     * A progress notifier for the upload. It must be used or hashes will not be calculated correctly
     * @throws Exception
     * Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract void getResource(String resourceName, File destination, TransferProgress progress)
            throws Exception;

    /**
     * Subclass must implement with newer detection behavior
     *
     * @param resourceName
     * The name of the resource being compared
     * @param timestamp
     * The timestamp to compare against
     * @return true if the current version of the resource is newer than the timestamp
     * @throws Exception
     * Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract boolean isRemoteResourceNewer(String resourceName, long timestamp) throws Exception;

    /**
     * Subclass must implement with specific directory listing behavior
     *
     * @param directory
     * The directory to list files in
     * @return A collection of file names
     * @throws Exception
     * Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract List<String> listDirectory(String directory) throws Exception;

    /**
     * Subclasses must implement with specific put behavior
     *
     * @param source
     * The local source file to read from
     * @param destination
     * The name of the remote resource to write to
     * @param progress
     * A progress notifier for the upload. It must be used or hashes will not be calculated correctly
     * @throws Exception
     * Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract void putResource(File source, String destination, TransferProgress progress) throws Exception;

    @Override
    public void connect(final Repository source, final AuthenticationInfo authenticationInfo, final ProxyInfoProvider proxyInfoProvider)
            throws ConnectionException, AuthenticationException {
        doConnect(source, authenticationInfo, null);
    }

    @Override
    public void connect(final Repository source, final ProxyInfoProvider proxyInfoProvider) throws ConnectionException,
            AuthenticationException {
        doConnect(source, null, null);
    }

    @Override
    public int getTimeout() {
        return this.timeout;
    }

    @Override
    public void setTimeout(final int timeoutValue) {
        this.timeout = timeoutValue;
    }

}
