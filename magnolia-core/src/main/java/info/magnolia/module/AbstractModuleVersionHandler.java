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
import info.magnolia.module.model.VersionRange;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implement this and register your deltas in the constructor using the register method.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractModuleVersionHandler implements ModuleVersionHandler {
    private final Version latestVersion;
    private final Map allDeltas; // <Version, Delta>

    protected AbstractModuleVersionHandler(Version latestVersion) {
        this.latestVersion = latestVersion;
        allDeltas = new HashMap();
    }

    /**
     * Registers the delta needed to update to version v from the previous one.
     */
    protected void register(Version v, Delta delta) {
        allDeltas.put(v, delta);
    }

    public List getDeltas(Version from, Version to) {
        final List deltas = new LinkedList();
        final VersionRange versionRange = new VersionRange(from, to);
        final Iterator it = allDeltas.keySet().iterator();
        while (it.hasNext()) {
            final Version v = (Version) it.next();
            if (versionRange.contains(v)) {
                deltas.add(allDeltas.get(v));
            }
        }
        return deltas;
    }

    public Version getLatestVersion() {
        return latestVersion;
    }
}
