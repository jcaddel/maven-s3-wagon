/**
 * Copyright 2010-2014 The Kuali Foundation
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * An extension to the {@link FileOutputStream} that notifies
 * a @{link TransferProgress} object as it is being written to.
 *
 * @author Ben Hale
 * @since 1.1
 */
class TransferProgressFileOutputStream extends FileOutputStream {

    private TransferProgress progress;

    public TransferProgressFileOutputStream(File file, TransferProgress progress) throws FileNotFoundException {
        super(file);
        this.progress = progress;
    }

    public void write(int b) throws IOException {
        super.write(b);
        progress.notify(new byte[]{(byte) b}, 1);
    }

    public void write(byte b[]) throws IOException {
        super.write(b);
        progress.notify(b, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        super.write(b, off, len);
        if (off == 0) {
            progress.notify(b, len);
        } else {
            byte[] bytes = new byte[len];
            System.arraycopy(b, off, bytes, 0, len);
            progress.notify(bytes, len);
        }
    }
}
