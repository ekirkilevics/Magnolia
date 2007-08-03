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

import info.magnolia.cms.core.HierarchyManager;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MD5CheckingFileExtractor extends BasicFileExtractor {
    private final HierarchyManager hm;

    public MD5CheckingFileExtractor(HierarchyManager hm) {
        this.hm = hm;
    }

    protected FileExtractorOperation newOperation(String resourcePath, String absTargetPath) {
        return new MDChecking5FileExtractorOperation(hm, resourcePath, absTargetPath);
    }
}
