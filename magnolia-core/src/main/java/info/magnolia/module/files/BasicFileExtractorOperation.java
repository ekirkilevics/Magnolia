/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.files;

import info.magnolia.cms.util.ClasspathResourcesUtil;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple FileExtractorOperation which just extracts a resource from the class path
 * to the filesystem.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BasicFileExtractorOperation implements FileExtractorOperation {
    protected final String resourcePath;
    protected final String absoluteTargetPath;

    BasicFileExtractorOperation(String resourcePath, String absoluteTargetPath) {
        this.resourcePath = resourcePath;
        this.absoluteTargetPath = absoluteTargetPath;
    }

    public void extract() throws IOException {
        final InputStream resourceStream = checkInput();
        final File checkedTargetFile = checkOutput();
        if (checkedTargetFile != null) {
            final OutputStream out = openOutput(checkedTargetFile);

            copyAndClose(resourceStream, out);
        }
    }

    protected InputStream checkInput() throws IOException {
        final InputStream resourceStream = ClasspathResourcesUtil.getStream(resourcePath, false);
        if (resourceStream == null) {
            throw new IOException("Can't open " + resourcePath);
        }
        return resourceStream;
    }

    /**
     * @return a java.io.File corresponding to the absoluteTargetPath if the path is validated,
     *         or null if the file should not be extracted. TODO ugly.
     */
    protected File checkOutput() throws IOException {
        final File targetFile = new File(absoluteTargetPath);
        final File parent = targetFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Can't create directories for " + targetFile.getAbsolutePath());
        } else if (!parent.canWrite()) {
            throw new IOException("Can't write to " + targetFile.getAbsolutePath());
        }
        return targetFile;
    }

    protected OutputStream openOutput(File targetFile) throws IOException {
        return new FileOutputStream(targetFile);
    }

    protected void copyAndClose(InputStream in, OutputStream out) throws IOException {
        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}
