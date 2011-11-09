package org.kuali.maven.wagon;

import java.lang.Thread.UncaughtExceptionHandler;

public class ThreadHandler implements UncaughtExceptionHandler {

    ThreadGroup group;
    Thread[] threads;
    Throwable exception;
    boolean stopThreads;
    int requestsPerThread;

    public ThreadGroup getGroup() {
        return group;
    }

    public void setGroup(ThreadGroup group) {
        this.group = group;
    }

    public Thread[] getThreads() {
        return threads;
    }

    public void setThreads(Thread[] threads) {
        this.threads = threads;
    }

    public void executeThreads() {
        start();
        join();
    }

    protected void start() {
        for (Thread thread : threads) {
            thread.start();
        }
    }

    protected void join() {
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void uncaughtException(Thread t, Throwable e) {
        this.stopThreads = true;
        this.exception = new RuntimeException("Unexpected issue in thread [" + t.getId() + ":" + t.getName() + "]", e);
    }

    public Throwable getException() {
        return exception;
    }

    public synchronized boolean isStopThreads() {
        return stopThreads;
    }

    public int getRequestsPerThread() {
        return requestsPerThread;
    }

    public void setRequestsPerThread(int requestsPerThread) {
        this.requestsPerThread = requestsPerThread;
    }
}
