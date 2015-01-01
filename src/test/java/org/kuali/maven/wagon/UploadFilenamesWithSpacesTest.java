/**
 * Copyright 2010-2015 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.maven.wagon;

import java.io.File;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.repository.Repository;
import org.junit.Ignore;
import org.junit.Test;

import com.amazonaws.services.s3.model.PutObjectRequest;

public class UploadFilenamesWithSpacesTest {

	private static final String USERNAME = "AKIAJFD5IM7IPVVUEBNA";
	private static final String PASSWORD = System.getProperty("secret.key");

	// private static final Logger log = LoggerFactory.getLogger(S3WagonTest.class);

	@Test
	public void spaces() {
		try {
			Repository repository = new Repository("kuali.external", "s3://maven.kuali.org/external");
			S3Wagon wagon = new S3Wagon();
			wagon.basedir = wagon.getBaseDir(repository);
			wagon.bucketName = "maven.kuali.org";
			String filename = "/Users/jcaddel/ws/kuali-spaces/src/site/resources/myimages/icon with spaces.png";
			File file = new File(filename);
			PutObjectRequest request = wagon.getPutObjectRequest(file, "myimages/icon with spaces.png", null);
			System.out.println(request.getKey());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	@Ignore
	public void testPermissions() {
		try {
			AuthenticationInfo auth = new AuthenticationInfo();
			auth.setUserName(USERNAME);
			auth.setPassword(PASSWORD);
			Repository repository = new Repository("kuali.external", "s3://maven.kuali.org/external");
			S3Wagon wagon = new S3Wagon();
			wagon.connect(repository, auth);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
