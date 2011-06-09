/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.jcr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;

/**
 * Represents binary data stored in a file on the file system.
 *
 * @version $Id$
 */
public class BinaryInFile implements Binary {

    private final File file;
    private boolean disposed = false;

    public BinaryInFile(File file) {
        this.file = file;
    }

    @Override
    public InputStream getStream() throws RepositoryException {
        if (disposed) {
            throw new IllegalStateException("Binary already disposed");
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public int read(byte[] b, long position) throws IOException, RepositoryException {
        if (disposed) {
            throw new IllegalStateException("Binary already disposed");
        }
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        try {
            raf.seek(position);
            return raf.read(b);
        } finally {
            raf.close();
        }
    }

    @Override
    public long getSize() throws RepositoryException {
        if (disposed) {
            throw new IllegalStateException("Binary already disposed");
        }
        return file.length();
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public String toString() {
        return "file:" + file.toString();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BinaryInFile)) {
            return false;
        }

        BinaryInFile binary = (BinaryInFile) o;

        return !(file != null ? !file.equals(binary.file) : binary.file != null);
    }
}
