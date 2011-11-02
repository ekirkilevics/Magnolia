/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.nodebuilder.task;

import static info.magnolia.nodebuilder.Ops.addNode;
import static info.magnolia.nodebuilder.Ops.addProperty;
import static info.magnolia.nodebuilder.Ops.getNode;
import static info.magnolia.nodebuilder.Ops.remove;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.test.RepositoryTestCase;

import org.junit.Test;

/**
 * @version $Id$
 */
public class NodeBuilderTaskTest extends RepositoryTestCase {
    @Test
    public void testUnknownRootThrowsException() throws Exception {
        ContentUtil.createPath(MgnlContext.getHierarchyManager("config"), "/hello/world", true);

        Task passing = new NodeBuilderTask("Passing", "This task should work since it uses a known root", ErrorHandling.strict, "config", "/hello/world", addProperty("foo", "bar"));
        Task failing = new NodeBuilderTask("Failing", "This task should not work since it uses an unknown root", ErrorHandling.strict, "config", "/hello/boo", addProperty("foo", "bar"));

        final InstallContextImpl ctx = new InstallContextImpl(null);
        passing.execute(ctx);
        try {
            failing.execute(ctx);
            fail("should have failed");
        } catch (TaskExecutionException e) {
            assertEquals("Could not execute task: Path not found: hello/boo", e.getMessage());
        }
    }

    @Test
    public void testSyntax() throws Exception {
        ContentUtil.createPath(MgnlContext.getHierarchyManager("config"), "/modules/stk/templates/stkSection/mainArea/opener/paragraphs/stkTeaserOpener");

        final Task configurateTemplateStkSection = new NodeBuilderTask("Config Task stkSection", "Configure stkSection template.",
                ErrorHandling.logging,
                "config", "/modules/stk/templates",

                getNode("stkSection/mainArea").then(
                        addProperty("template", "/templates/xxx/pages/section/pageIntroMainArea.ftl"),
                        addNode("floating").then(
                                addProperty("columns", "2"),
                                addProperty("enabled", "true")),
                                addNode("opener/paragraphs/xxxTeaserOpener").then(
                                        addProperty("name", "xxxTeaserOpener")),
                                        addNode("paragraphs").then(
                                                addNode("xxxSingleLink").then(
                                                        addProperty("name", "xxxSingleLink")),
                                                        addNode("stkTeaserFingerTabbed").then(
                                                                addProperty("name", "stkTeaserFingerTabbed")),
                                                                addNode("xxxTeaserNewsList").then(
                                                                        addProperty("name", "xxxTeaserNewsList")),
                                                                        addNode("xxxExternalTeaser").then(
                                                                                addProperty("name", "xxxExternalTeaser")),
                                                                                addNode("xxxChronicleTeaser").then(
                                                                                        addProperty("name", "xxxChronicleTeaser"))),
                                                                                        remove("opener/paragraphs/stkTeaserOpener"))
        );
        configurateTemplateStkSection.execute(new InstallContextImpl(null));
    }

}
