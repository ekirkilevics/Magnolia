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
package info.magnolia.module.webapp;

import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaType;
import info.magnolia.module.model.Version;

import java.util.Collections;
import java.util.List;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebappDelta implements Delta {
    private final Version version;

    WebappDelta(Version version) {
        this.version = version;
    }

    public String getDescription() {
        return "Bootstraps the webapp upon first deployment.";
    }

    public Version getVersion() {
        return version;
    }

    public List getConditions() {
        return Collections.EMPTY_LIST;
    }

    public List getTasks() {
        return Collections.singletonList(new WebappBootstrap());
    }

    public DeltaType getType() {
        return DeltaType.install;
    }
}