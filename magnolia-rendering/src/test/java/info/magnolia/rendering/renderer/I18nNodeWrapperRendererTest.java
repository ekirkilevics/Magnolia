/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.rendering.renderer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

/**
 * Test case for {@link HTMLEscapingPropertyWrapper}.
 */
public class I18nNodeWrapperRendererTest extends RepositoryTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setInstance(I18nContentSupport.class,
                new DefaultI18nContentSupport());
    }

    @Test(expected = PathNotFoundException.class)
    public void throwExceptionWhenPropertyDoesntExists() throws IOException, RepositoryException {
        //GIVEN
        Node content = getTestNode();
        AbstractRenderer renderer = new DummyRenderer();
        content = renderer.wrapNodeForModel(content);
        //WHEN
        content.getProperty("unexisting property").getString();
        //THEN PathNotFoundException is thrown
    }

    @Test
    public void doesntThrowExceptionWhenPropertyExists() throws IOException, RepositoryException {

        //GIVEN
        Node content = getTestNode();
        AbstractRenderer renderer = new DummyRenderer();
        content = renderer.wrapNodeForModel(content);
        content.setProperty("existing property", "value");
        //WHEN
        String getString = content.getProperty("existing property").getString();
        //THEN
        assertEquals(getString, "value");
    }

    private Node getTestNode() throws IOException, RepositoryException {
        String contentProperties = "/mycontent.@type=mgnl:content\n"
                + "/mycontent.nd1=hello";

        Session hm = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(hm.getRootNode(),
                IOUtils.toInputStream(contentProperties));
        hm.save();
        Node content = hm.getNode("/mycontent");
        return content;
    }
}