package org.springframework.aws.maven;

import java.io.File;
import java.io.FileInputStream;

import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

/**
 * 
 */
public class Foo {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println("Starting up");
            String accessKey = "AKIAJZ72UQ5ZCVEDMAPQ";
            String secretKey = "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";
            AWSCredentials credentials = new AWSCredentials(accessKey, secretKey);
            RestS3Service service = new RestS3Service(credentials);
            S3Bucket bucket = service.getOrCreateBucket("foobar.ks.kuali.org");
            S3Object object1 = getS3Object("C:/temp/cloudfront/dir6.htm", "ks/");
            S3Object object2 = getS3Object("C:/temp/cloudfront/dir6.htm", "ks");
            S3Object object3 = getS3Object("C:/temp/cloudfront/dir6.htm", "ks/cloudfront");
            S3Object object4 = getS3Object("C:/temp/cloudfront/dir6.htm", "ks/cloudfront/");

            // Store the file on S3
            service.putObject(bucket, object1);
            service.putObject(bucket, object2);
            service.putObject(bucket, object3);
            service.putObject(bucket, object4);

            System.out.println("All done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static S3Object getS3Object(String filename, String key) throws Exception {

        File file = new File(filename);

        // Create an S3 object
        S3Object object = new S3Object(key);

        // Make it available to the public
        object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        object.setDataInputStream(new FileInputStream(filename));
        object.setContentLength(file.length());

        // Set the mime type according to the extension of the destination file
        object.setContentType("text/html");
        return object;
    }

}
