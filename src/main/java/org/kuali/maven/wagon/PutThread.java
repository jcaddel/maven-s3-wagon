package org.kuali.maven.wagon;

import static org.apache.commons.lang.StringUtils.leftPad;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Thread implementation for uploading a list of files to S3
 */
public class PutThread implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(PutThread.class);

    PutThreadContext context;

    public PutThread() {
        this(null);
    }

    public PutThread(PutThreadContext context) {
        super();
        this.context = context;
    }

    public void run() {
        logger.debug("[Thread-" + context.getId() + "] Starting");
        int offset = context.getOffset();
        int length = context.getLength();
        List<PutFileContext> list = context.getContexts();
        RequestFactory factory = context.getFactory();
        AmazonS3Client client = context.getClient();
        for (int i = offset; i < offset + length; i++) {
            if (context.getHandler().isStopThreads()) {
                break;
            }
            PutFileContext pc = list.get(i);
            pc.setProgress(null);
            PutObjectRequest request = factory.getPutObjectRequest(pc);
            // pc.fireStart();
            logger.debug(leftPad(context.getId() + "", 2, " ") + " - " + pc.getSource().getAbsolutePath());
            client.putObject(request);
            context.getTracker().increment();
            // pc.fireComplete();
        }
        logger.debug("Thread " + context.getId() + " stopping");
    }

    public PutThreadContext getContext() {
        return context;
    }

    public void setContext(PutThreadContext context) {
        this.context = context;
    }
}
