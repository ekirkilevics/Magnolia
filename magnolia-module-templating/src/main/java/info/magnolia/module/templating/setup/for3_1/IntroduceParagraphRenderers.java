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
package info.magnolia.module.templating.setup.for3_1;

import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.InstallContext;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class IntroduceParagraphRenderers extends BootstrapResourcesTask {

    public IntroduceParagraphRenderers() {
        super("Paragraph renderers", "Paragraph renderers were introduced in Magnolia 3.1");
    }

    protected boolean acceptResource(InstallContext installContext, String name) {
        return name.startsWith("/mgnl-bootstrap/templating/config.modules.templating.paragraph-renderers.")
                && name.endsWith(".xml");
    }
}
