/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
        super("Files extraction", "Extracts module files.");
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
