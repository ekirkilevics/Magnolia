/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.module.cache.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>VersionHandler</code> implementation for the cache module.
 *
 * @version $Id$
 */
public class CacheModuleVersionHandler extends DefaultModuleVersionHandler {

    private final FilterOrderingTask placeGzipFitler = new FilterOrderingTask("gzip", new String[]{"context", "multipartRequest", "activation"});
    private final FilterOrderingTask placeCacheFilter = new FilterOrderingTask("cache", new String[] { "gzip", "range", "i18n" });

    public CacheModuleVersionHandler() {
        register(DeltaBuilder.checkPrecondition("4.4.6", "4.5"));

        register(DeltaBuilder.update("4.5", "")
                .addTask(placeGzipFitler)
                .addTask(placeCacheFilter)
                .addTask(new BootstrapSingleResource(
                    "Register Virtual Uri Mapping",
                    "Add Virtual Uri for static resources",
                    "/mgnl-bootstrap/cache/config.modules.cache.virtualURIMapping.xml"))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(placeGzipFitler);
        tasks.add(placeCacheFilter);
        return tasks;
    }
}
