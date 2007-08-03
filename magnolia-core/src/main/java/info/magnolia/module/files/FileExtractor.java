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

import java.io.IOException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface FileExtractor {

    /**
     * Extracts the given resource from the classpath and stores it as absTargetPath.
     * extractFile("/foo/bar.baz", "/Users/greg/tok.tak"). Does not handle any kind
     * of logic regarding location and name of source and target files.
     */
    void extractFile(String resourcePath, String absTargetPath) throws IOException;

    /**
     * Extracts the given resource from the classpath and stores it as dictacted by
     * the given Transformer.
     */
    void extractFile(String resourcePath, Transformer transformer) throws IOException;

    /**
     * Extracts all found resources from the classpath, using the given Transformer.
     */
    void extractFiles(Transformer transformer) throws IOException;

    // TODO : find better name !
    interface Transformer {
        /**
         * A filter in the same vein as java.io.FileFilter and such, except this
         * returns the (absolute) targetPath of a file to be extracted, or null
         * if the file should not be extracted.
         */
        String accept(String resourcePath);
    }
}
