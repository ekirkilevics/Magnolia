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
package info.magnolia.module.delta;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.files.MD5CheckingFileExtractor;
import info.magnolia.module.files.ModuleFileExtractorTransformer;

import java.io.IOException;

/**
 * A task which extracts all files for a module.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleFilesExtraction extends AbstractTask {

    public ModuleFilesExtraction() {
        super("Files extraction", "Extracts module files");
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final String moduleName = ctx.getCurrentModuleDefinition().getName();
        // TODO use a separate workspace, or a different storage root, ... ?
        final HierarchyManager hm = ctx.getConfigHierarchyManager();
        final MD5CheckingFileExtractor extractor = new MD5CheckingFileExtractor(hm);
        try {
            extractor.extractFiles(new ModuleFileExtractorTransformer(moduleName));
        } catch (IOException e) {
            throw new TaskExecutionException("Could not extract files for module " + ctx.getCurrentModuleDefinition() + ": " + e.getMessage(), e);
        }
    }
}
