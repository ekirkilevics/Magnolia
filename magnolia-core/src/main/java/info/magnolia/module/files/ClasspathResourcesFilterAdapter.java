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

/**
 * A ClasspathResourcesUtil.Filter which delegates to a FileExtractor.Transformer
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ClasspathResourcesFilterAdapter extends ClasspathResourcesUtil.Filter {
    private final FileExtractor.Transformer transformer;

    public ClasspathResourcesFilterAdapter(FileExtractor.Transformer transformer) {
        this.transformer = transformer;
    }

    public boolean accept(String name) {
        return transformer.accept(name) != null;
    }
}
