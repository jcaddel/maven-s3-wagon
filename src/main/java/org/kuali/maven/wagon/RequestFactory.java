package org.kuali.maven.wagon;

import com.amazonaws.services.s3.model.PutObjectRequest;

public interface RequestFactory {
	PutObjectRequest getPutObjectRequest(PutContext context);
}
