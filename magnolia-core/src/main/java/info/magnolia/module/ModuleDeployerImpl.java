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
package info.magnolia.module;

import info.magnolia.module.delta.Delta;
import info.magnolia.module.model.Version;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleDeployerImpl implements ModuleDeployer {
    private final InstallContext ctx;

    public ModuleDeployerImpl(InstallContext ctx) {
        this.ctx = ctx;
    }

    public void install(ModuleVersionHandler moduleVersionHandler) {
        throw new IllegalStateException("not implemented yet");
    }

    public void update(ModuleVersionHandler moduleVersionHandler, Version installedVersion) {
        final List deltas = moduleVersionHandler.getDeltas(installedVersion, moduleVersionHandler.getLatestVersion());
        apply(deltas);
    }

    public void unInstall(ModuleVersionHandler moduleVersionHandler) {
        throw new IllegalStateException("not implemented yet");
    }

    protected void apply(List deltas) {
        final Iterator it = deltas.iterator();
        while (it.hasNext()) {
            final Delta d = (Delta) it.next();
            d.apply(ctx);

        }
    }
}
