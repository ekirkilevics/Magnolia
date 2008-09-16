/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.templating.setup;

import java.util.Collections;
import java.util.List;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.BootstrapSingleResourceAndOrderBefore;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.templating.setup.for3_5.IntroduceParagraphRenderers;


/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class TemplatingModuleVersionHandler extends DefaultModuleVersionHandler {

    private OrderNodeBeforeTask orderBackwardCompatibilityFilter = new OrderNodeBeforeTask("Move backward compatiblity filter","",ContentRepository.CONFIG, "/server/filters/cms/backwardCompatibility", "rendering");

    public TemplatingModuleVersionHandler() {
        DeltaBuilder delta35 = DeltaBuilder.update("3.5", "");
        delta35.addTask(new IntroduceParagraphRenderers());
        delta35.addTask(new BootstrapSingleResourceAndOrderBefore(
                "Compatibility Filter",
                "${actpage} is deprecated. Adds a compatibility filter still supporting it but writing warn messages to the log",
                "/mgnl-bootstrap/templating/config.server.filters.cms.backwardCompatibility.xml",
                "rendering"));

        // move the filter in case it is in the wrong place
        // this happened in case of a fresh install of former version
        DeltaBuilder delta354 = DeltaBuilder.update("3.5.4", "");
        delta354.addTask(orderBackwardCompatibilityFilter);

        DeltaBuilder delta37 = DeltaBuilder.update("3.7", "");
        delta37.addTask(new BootstrapSingleResource(
                "Freemarker Template Renderer", 
                "Add freemarker template renderer configuration", 
                "/mgnl-bootstrap/templating/config.modules.templating.template-renderers.freemarker.xml"));
        
        register(delta35);
        register(delta354);
        register(delta37);
    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        return Collections.singletonList(orderBackwardCompatibilityFilter);
    }

}
