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

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.repository.Repository;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class S3WagonTest {
	private static final String USERNAME = System.getProperty("AWS_ACCESS_KEY_ID");
	private static final String PASSWORD = System.getProperty("AWS_SECRET_ACCESS_KEY");

	// private static final Logger log = LoggerFactory.getLogger(S3WagonTest.class);

	@Test
	@Ignore
	public void testPermissions() {
		try {
			AuthenticationInfo auth = new AuthenticationInfo();
			auth.setUserName(USERNAME);
			auth.setPassword(PASSWORD);
			Repository repository = new Repository("kuali.release", "s3://deletemenow.kuali.org/release");
			S3Wagon wagon = new S3Wagon();
			wagon.connect(repository, auth);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void simple() {
		try {
			AuthenticationInfo auth = new AuthenticationInfo();
			auth.setUserName(USERNAME);
			auth.setPassword(PASSWORD);
			Repository repository = new Repository("kuali.release", "s3://maven.kuali.org/release");
			S3Wagon wagon = new S3Wagon();
			wagon.connect(repository, auth);
			List<String> files = wagon.getFileList("");
			System.out.println(files.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
