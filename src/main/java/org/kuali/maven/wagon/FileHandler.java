package org.kuali.maven.wagon;

import org.kuali.common.threads.ElementHandler;
import org.kuali.common.threads.ListIteratorContext;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class FileHandler implements ElementHandler<PutFileContext> {

    public void handleElement(ListIteratorContext<PutFileContext> context, int index, PutFileContext element) {
        RequestFactory factory = element.getFactory();
        AmazonS3Client client = element.getClient();
        PutObjectRequest request = factory.getPutObjectRequest(element);
        client.putObject(request);
    }

}
