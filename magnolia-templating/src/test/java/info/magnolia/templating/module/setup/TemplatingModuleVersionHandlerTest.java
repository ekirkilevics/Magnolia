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
package info.magnolia.templating.module.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.templating.freemarker.RenderableDefinitionModel;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @version $Revision: $ ($Author: $)
 */
public class TemplatingModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {
    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/rendering.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/rendering.xml",
                "/META-INF/magnolia/core.xml"
        );
    }
    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new TemplatingModuleVersionHandler();
    }

    @Test
    public void testInstallFrom4_4_6() throws Exception {
        // GIVEN
        setupConfigNode("/server/filters/cms/backwardCompatibility");
        setupConfigNode("/modules/templating/template-renderers");
        setupConfigNode("/modules/templating/paragraph-renderers");
        setupConfigNode("/modules/test/templates/myTemplate");
        setupConfigNode("/server/rendering/freemarker/modelFactories/renderable");
        setupConfigProperty("/server/rendering/freemarker/modelFactories/renderable", "class", "info.magnolia.module.templating.freemarker.RenderableDefinitionModel$Factory");

        final String tplPath = "/modules/test/templates/myTemplate";
        setupConfigProperty(tplPath, "templatePath","/some/path.ftl");

        // WHEN
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.4.6"));

        // THEN
        assertConfig("/some/path.ftl", tplPath + "/templateScript");
        assertConfig(RenderableDefinitionModel.Factory.class.getName(), "/server/rendering/freemarker/modelFactories/renderable/class");
        assertNoMessages(ctx);
    }
}
