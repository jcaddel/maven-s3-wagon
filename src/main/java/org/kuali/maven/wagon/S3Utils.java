package org.kuali.maven.wagon;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class S3Utils {
	private static final int KILOBYTE = 1024;
	private static final int MEGABYTE = 1024 * KILOBYTE;
	private static final int MULTI_PART_UPLOAD_THRESHOLD = 100 * MEGABYTE;

	public static final void upload(long fileSize, PutObjectRequest request, AmazonS3Client client, TransferManager manager) {

		// Store the file on S3
		if (fileSize > MULTI_PART_UPLOAD_THRESHOLD) {
			// Use multi-part upload for large files
			Upload upload = manager.upload(request);
			try {
				// Block and wait for the upload to finish
				upload.waitForCompletion();
			} catch (Exception e) {
				throw new AmazonS3Exception("Unexpected error uploading file", e);
			}
		} else {
			// Use normal upload for small files
			client.putObject(request);
		}

	}

}
