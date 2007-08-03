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

import info.magnolia.module.InstallContext;

/**
 * A task that does nothing, i.e is only a name and a description.
 * Can be used to describe manual operations users have to take, for
 * instance.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NullTask extends AbstractTask {

    public NullTask(String name, String description) {
        super(name, description);
    }

    public void execute(InstallContext installContext) {
    }
}
