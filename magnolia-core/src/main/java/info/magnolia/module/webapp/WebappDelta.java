/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.webapp;

import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaType;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.Collections;
import java.util.List;

/**
 * A hard-coded {@link Delta} for the webapp module.
 * @see info.magnolia.module.webapp.WebappVersionHandler
 * @see info.magnolia.module.webapp.WebappBootstrap
 *
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

    public List<Condition> getConditions() {
        return Collections.emptyList();
    }

    public List<Task> getTasks() {
        return Collections.<Task>singletonList(new WebappBootstrap());
    }

    public DeltaType getType() {
        return DeltaType.install;
    }
}
