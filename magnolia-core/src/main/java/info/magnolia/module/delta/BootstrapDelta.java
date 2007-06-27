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
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RegisterException;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class BootstrapDelta implements Delta {

    // TODO : check if nodes were already there
    public void apply(InstallContext ctx) throws RepositoryException {
        try {
            ModuleUtil.bootstrap(getResourcesToBootstrap());
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        } catch (RegisterException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    protected abstract String[] getResourcesToBootstrap();
}
