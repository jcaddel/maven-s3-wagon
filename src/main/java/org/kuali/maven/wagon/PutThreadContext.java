package org.kuali.maven.wagon;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3Client;

/**
 * This is the context needed by a PutThread to successfully upload a list of files to S3
 */
public class PutThreadContext {

    int id;
    ThreadHandler handler;
    List<PutFileContext> contexts;
    AmazonS3Client client;
    RequestFactory factory;
    int length;
    int offset;

    public ThreadHandler getHandler() {
        return handler;
    }

    public void setHandler(ThreadHandler handler) {
        this.handler = handler;
    }

    public List<PutFileContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<PutFileContext> contexts) {
        this.contexts = contexts;
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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
