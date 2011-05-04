/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.templatingcomponents.freemarker;

import java.io.Writer;

import info.magnolia.cms.core.Content;
import info.magnolia.context.WebContext;
import info.magnolia.module.templating.RenderException;
import info.magnolia.module.templating.engine.RenderingEngine;
import info.magnolia.test.ComponentsTestUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Tests for RenderDirective.
 * 
 * $Id$
 */
public class RenderDirectiveTest extends AbstractDirectiveTestCase {

    @Override
    protected void setupExpectations(WebContext ctx, HttpServletRequest req) {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setInstance(RenderingEngine.class, new RenderingEngine() {
            @Override
            public void render(Content content, Writer out) throws RenderException {

            }
            @Override
            public void render(Content content, String definitionName, Writer out) throws RenderException {
            }
        });
    }

    public void testRenderSimpleBar() throws Exception {
        final String result = renderForTest("[@cms.render /]");
        assertEquals("", result);
    }
}
