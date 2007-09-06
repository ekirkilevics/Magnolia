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
 * A task which does nothing else than logging a warning message.
 * Can be used to remind users about some manual operations to be
 * done after installation/update.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WarnTask extends AbstractTask {

    public WarnTask(String name, String warningMessage) {
        super(name, warningMessage);
    }

    public void execute(InstallContext installContext) throws TaskExecutionException {
        installContext.warn(getDescription());
    }
}
