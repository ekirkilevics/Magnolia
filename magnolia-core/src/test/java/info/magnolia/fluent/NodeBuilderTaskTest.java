/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.fluent;

import info.magnolia.context.MgnlContext;
import static info.magnolia.fluent.Ops.*;
import info.magnolia.module.delta.Task;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallStatus;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NodeBuilderTaskTest extends RepositoryTestCase {
    public void testUnknownRootThrowsException() throws Exception {
        MgnlContext.getHierarchyManager("config").getRoot().createContent("hello").createContent("world");

        MgnlContext.getHierarchyManager("config").getContent("/hello");
        MgnlContext.getHierarchyManager("config").getContent("/hello/world");

        assertPathNotFoundExceptionFor("boo");
        assertPathNotFoundExceptionFor("hello/woohoo");
        assertPathNotFoundExceptionFor("hello/world/blah");
    }

    public void testSyntax() throws Exception {
        ContentUtil.createPath(MgnlContext.getHierarchyManager("config"), "/modules/stk/templates/stkSection/mainArea/opener/paragraphs/stkTeaserOpener");

        final Task configurateTemplateStkSection = new NodeBuilderTask("Config Task stkSection", "Configure stkSection template.",
                "config", "/modules/stk/templates",

                getNode("stkSection/mainArea").then(
                        addProperty("template", "/templates/xxx/pages/section/pageIntroMainArea.ftl"),
                        add("floating").then(
                                addProperty("columns", "2"),
                                addProperty("enabled", "true")),
                        add("opener/paragraphs/xxxTeaserOpener").then(
                                addProperty("name", "xxxTeaserOpener")),
                        add("paragraphs").then(
                                add("xxxSingleLink").then(
                                        addProperty("name", "xxxSingleLink")),
                                add("stkTeaserFingerTabbed").then(
                                        addProperty("name", "stkTeaserFingerTabbed")),
                                add("xxxTeaserNewsList").then(
                                        addProperty("name", "xxxTeaserNewsList")),
                                add("xxxExternalTeaser").then(
                                        addProperty("name", "xxxExternalTeaser")),
                                add("xxxChronicleTeaser").then(
                                        addProperty("name", "xxxChronicleTeaser"))),
                        remove("opener/paragraphs/stkTeaserOpener"))
        );
        configurateTemplateStkSection.execute(new InstallContextImpl());
    }

    private void assertPathNotFoundExceptionFor(final String path) throws RepositoryException {
        try {
            MgnlContext.getHierarchyManager("config").getContent(path);
            fail("should have failed");
        } catch (PathNotFoundException e) {
            assertEquals(path, e.getMessage());
        }
    }


}
