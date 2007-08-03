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

import info.magnolia.cms.core.Path;
import org.apache.commons.lang.StringUtils;

/**
 * A FileExtractor.Transformer which accepts files with paths like
 * /mgnl-files/.../moduleName/... and stores them in
 * ${magnolia.app.rootdir}/.../moduleName/...
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleFileExtractorTransformer implements FileExtractor.Transformer {
    private final String moduleName;

    public ModuleFileExtractorTransformer(String moduleName) {
        this.moduleName = moduleName;
    }

    public String accept(String resourcePath) {
        final boolean thisIsAFileWeWant = resourcePath.startsWith("/mgnl-files/") && StringUtils.contains(resourcePath, "/" + moduleName + "/");
        if (!thisIsAFileWeWant) {
            return null;
        }
        final String relTargetPath = StringUtils.removeStart(resourcePath, "/mgnl-files/");
        return Path.getAbsoluteFileSystemPath(relTargetPath);
    }
}
