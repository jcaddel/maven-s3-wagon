package org.kuali.maven.wagon;

import java.io.PrintStream;

public class ProgressTracker {

    int count;
    int total;
    PrintStream out = System.out;
    String startToken = "[INFO] Progress: ";
    String completeToken = "\n";
    String progressToken = ".";

    public synchronized int getCount() {
        return count;
    }

    public synchronized void increment() {
        if (count == 0) {
            showProgressStart();
        }
        showProgress(++count, total);
        if (count == total) {
            showProgressComplete();
        }
    }

    protected void showProgressComplete() {
        out.print(completeToken);
    }

    protected void showProgressStart() {
        out.print(startToken);
    }

    protected void showProgress(int count, int total) {
        out.print(progressToken);
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public String getStartToken() {
        return startToken;
    }

    public void setStartToken(String startToken) {
        this.startToken = startToken;
    }

    public String getCompleteToken() {
        return completeToken;
    }

    public void setCompleteToken(String completeToken) {
        this.completeToken = completeToken;
    }

    public String getProgressToken() {
        return progressToken;
    }

    public void setProgressToken(String progressToken) {
        this.progressToken = progressToken;
    }

}
