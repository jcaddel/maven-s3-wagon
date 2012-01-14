/**
 * Copyright 2010-2012 The Kuali Foundation
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
