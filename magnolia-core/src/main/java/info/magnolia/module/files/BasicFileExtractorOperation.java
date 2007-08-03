/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
