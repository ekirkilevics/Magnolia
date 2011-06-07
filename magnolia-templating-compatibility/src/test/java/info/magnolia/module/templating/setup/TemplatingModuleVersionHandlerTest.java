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
package info.magnolia.module.templating.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.filters.BackwardCompatibilityFilter;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;

import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class TemplatingModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {
    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/templating.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/templating.xml",
                "/META-INF/magnolia/core.xml"
        );
    }
    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new TemplatingModuleVersionHandler();
    }

    // fixed by 4.0.3 or 4.1.1
    public void testMisfixedTemplatesFrom402AreFixed() throws Exception {
        // fake pre-install
        setupConfigProperty("/server/filters/cms/rendering", "class", "info.magnolia.cms.filters.RenderingFilter"); // old RenderingFilter fqn
        setupConfigProperty("/server/filters/cms/backwardCompatibility", "class", BackwardCompatibilityFilter.class.getName());
        setupConfigNode("/server/rendering/freemarker/sharedVariables"); // this is assumed to have been created by CoreModuleVersionHandler

        // setup a template, with pre-4.0 properties
        final String tplPath = "/modules/test/templates/myTemplate";
        setupTemplateNode("test", "myTemplate");
        setupConfigProperty(tplPath, "title", "My Test Template");
        // 4.0.2 and 4.0 wrongly moved the "templatePath" property under /parameters
        setupConfigProperty(tplPath + "/parameters", "templatePath", "/some/path.ftl");
        setupConfigProperty(tplPath + "/parameters", "someProperty", "someValue");

        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.2"));

        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        assertConfig("My Test Template", tplPath + "/title");

        // 5.0 renames templatePath to templateScript
        assertConfig("/some/path.ftl", tplPath + "/templateScript");
        // non-standard props are supposed to be moved to the "parameters" subnode
        assertConfig("someValue", tplPath + "/parameters/someProperty");
        assertFalse("path property should have been removed", hm.isExist(tplPath + "/path"));
        assertFalse("path property should have been removed", hm.isExist(tplPath + "/parameters/path"));
        assertFalse("path property should have been removed", hm.isExist(tplPath + "/parameters/templatePath"));
        assertFalse("path property should have been renamed to templatePath and be in the template's def. node", hm.isExist(tplPath + "/parameters/templatePath"));

        assertNoMessages(ctx);
    }

    public void testSilentIfUserFixedTemplatesHimself() throws Exception {
        // fake pre-install
        setupConfigProperty("/server/filters/cms/rendering", "class", "info.magnolia.cms.filters.RenderingFilter"); // old RenderingFilter fqn
        setupConfigProperty("/server/filters/cms/backwardCompatibility", "class", BackwardCompatibilityFilter.class.getName());
        setupConfigNode("/server/rendering/freemarker/sharedVariables"); // this is assumed to have been created by CoreModuleVersionHandler

        // setup a template, with pre-4.0 properties
        final String tplPath = "/modules/test/templates/myTemplate";
        setupTemplateNode("test", "myTemplate");
        setupConfigProperty(tplPath, "title", "My Test Template");
        // 4.0.2 and 4.0 wrongly moved the "templatePath" property under /parameters - here the user fixed it already
        setupConfigProperty(tplPath, "templatePath", "/some/path.ftl");
        setupConfigProperty(tplPath + "/parameters", "someProperty", "someValue");

        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.2"));

        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        assertConfig("My Test Template", tplPath + "/title");

        // 5.0 renames templatePath to templateScript
        assertConfig("/some/path.ftl", tplPath + "/templateScript");
        // non-standard props are supposed to be moved to the "parameters" subnode
        assertConfig("someValue", tplPath + "/parameters/someProperty");
        assertFalse("path property should have been removed", hm.isExist(tplPath + "/path"));
        assertFalse("path property should have been removed", hm.isExist(tplPath + "/parameters/path"));
        assertFalse("path property should have been removed", hm.isExist(tplPath + "/parameters/templatePath"));
        assertFalse("path property should have been renamed to templatePath and be in the template's def. node", hm.isExist(tplPath + "/parameters/templatePath"));

        assertNoMessages(ctx);
    }

    private void setupTemplateNode(String moduleName, String templateNodeName) throws RepositoryException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content content = ContentUtil.createPath(hm, "/modules/" + moduleName + "/templates", ItemType.CONTENT);
        content.createContent(templateNodeName, ItemType.CONTENTNODE);
        hm.save();
    }

}
