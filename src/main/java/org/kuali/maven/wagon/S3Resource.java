/**
 * Copyright 2014 Adobe Systems Incorporated
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

import org.apache.maven.wagon.resource.Resource;

/**
 * @author <a href="mailto:kuppe@adobe.com>Markus A. Kuppe</a>
 */
public class S3Resource extends Resource {

	private String url;

	public S3Resource(String resourceName) {
		super(resourceName);
	}

	public String getUrl() {
		return url;
	}
	
	public void setURL(String anUrl) {
		this.url = anUrl;
	}
}
