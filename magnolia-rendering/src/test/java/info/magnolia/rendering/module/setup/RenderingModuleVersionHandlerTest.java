/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.rendering.module.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

/**
 * 
 * @version $Id$
 */
public class RenderingModuleVersionHandlerTest extends ModuleVersionHandlerTestCase{

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/rendering.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new RenderingModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml"
        );
    }

    /**
     * Testing update to version 4.5.3. Testing transformation subTemplates to variations.
     */
    @Test
    public void testOnUpdateFrom452() throws ModuleManagementException, LoginException, RepositoryException{
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node printStkHomeNode = NodeUtil.createPath(session.getRootNode(), "/modules/standard-templating-kit/templates/stkHome/subTemplates/printsite", "mgnl:contentNode");
        printStkHomeNode.setProperty("extension", "print");
        printStkHomeNode.setProperty("templatePath", "/templates/print.ftl");
        printStkHomeNode.setProperty("type", "freemarker");
        Node htmlStkHomeNode = NodeUtil.createPath(session.getRootNode(), "/modules/standard-templating-kit/templates/stkHome/subTemplates/html", "mgnl:contentNode");
        htmlStkHomeNode.setProperty("templatePath", "/templates/html.ftl");
        htmlStkHomeNode.setProperty("type", "freemarker");
        Node subTempStkArticleNode = NodeUtil.createPath(session.getRootNode(), "/modules/standard-templating-kit/templates/stkArticle/subTemplates/html", "mgnl:contentNode");
        subTempStkArticleNode.setProperty("extension", "html");
        subTempStkArticleNode.setProperty("templatePath", "/templates/print.ftl");
        subTempStkArticleNode.setProperty("type", "freemarker");
        NodeUtil.createPath(session.getRootNode(), "/modules/standard-templating-kit/templates/stkArticle/variations", "mgnl:contentNode");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.5.2"));

        assertFalse(session.nodeExists("/modules/standard-templating-kit/templates/stkHome/subTemplates"));
        assertEquals("/modules/standard-templating-kit/templates/stkHome/variations/print", printStkHomeNode.getPath());
        assertTrue(printStkHomeNode.hasProperty("templateScript"));
        assertTrue(printStkHomeNode.hasProperty("renderType"));
        assertFalse(printStkHomeNode.hasProperty("extension"));
        assertEquals("/modules/standard-templating-kit/templates/stkHome/variations/html", htmlStkHomeNode.getPath());
        assertTrue(printStkHomeNode.hasProperty("templateScript"));
        assertTrue(printStkHomeNode.hasProperty("renderType"));
        assertTrue(session.nodeExists("/modules/standard-templating-kit/templates/stkArticle/subTemplates/html"));
        assertFalse(session.nodeExists("/modules/standard-templating-kit/templates/variations/html"));
    }

    /**
     * Testing update to version 4.5.4. Testing task which rename variations properties templatePath and type.
     */
    @Test
    public void testOnUpdateFrom453() throws ModuleManagementException, LoginException, RepositoryException{
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node htmlStkHomeNode = NodeUtil.createPath(session.getRootNode(), "/modules/standard-templating-kit/templates/stkHome/variations/html", "mgnl:contentNode");
        htmlStkHomeNode.setProperty("templatePath", "/templates/print.ftl");
        htmlStkHomeNode.setProperty("type", "freemarker");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.5.3"));

        assertEquals("/modules/standard-templating-kit/templates/stkHome/variations/html", htmlStkHomeNode.getPath());
        assertTrue(htmlStkHomeNode.hasProperty("templateScript"));
        assertTrue(htmlStkHomeNode.hasProperty("renderType"));
    }
}
