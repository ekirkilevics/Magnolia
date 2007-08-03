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
package info.magnolia.module.templating.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.BasicDelta;
import info.magnolia.module.templating.setup.for3_1.IntroduceParagraphRenderers;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class TemplatingModuleVersionHandler extends DefaultModuleVersionHandler {

    private final BasicDelta delta31 = new BasicDelta("Updating to 3.1", "", new IntroduceParagraphRenderers());

    public TemplatingModuleVersionHandler() {
        super();
        register("3.1.0", delta31);
    }

}
