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

import org.apache.maven.wagon.resource.Resource;

/**
 * A class that encapsulates the notification of the transfer listeners for
 * Maven Wagon.
 *
 * @author Ben Hale
 * @since 1.1
 */
class TransferProgress {

    private Resource resource;

    private int requestType;

    private TransferListenerSupport transferListeners;

    public TransferProgress(Resource resource, int requestType, TransferListenerSupport listeners) {
        this.resource = resource;
        this.requestType = requestType;
        this.transferListeners = listeners;
    }

    protected void notify(byte[] buffer, int length) {
        transferListeners.fireTransferProgress(resource, requestType, buffer, length);
    }

}
