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
package info.magnolia.link;

import info.magnolia.cms.filters.WebContainerResources;
import info.magnolia.cms.filters.WebContainerResourcesImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @version $id$
 */
public class EditorLinkTransformerTest {

    private Link link;
    private LinkTransformer editorTransformer;
    private WebContext ctx;

    @Before
    public void setUp(){
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);
        ctx = mock(WebContext.class);
        MgnlContext.setInstance(ctx);
        link = new Link();
        editorTransformer = new EditorLinkTransformer();
    }

    @Test
    public void testEditorLinkTransformation(){
        link.setHandle("/path");
        link.setNodeDataName("nodeData");
        link.setFileName("fileName");
        link.setExtension("ext");
        when(ctx.getContextPath()).thenReturn("context");
        String result = editorTransformer.transform(link);
        assertEquals("context/path/nodeData/fileName.ext", result);
    }

    @After
    public void tearDown(){
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }
}
