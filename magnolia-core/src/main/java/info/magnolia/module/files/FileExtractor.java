/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
