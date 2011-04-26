/**
 * This file Copyright (c) 2011 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.cms.core.Content;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockJCRNode;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.junit.Test;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Tests.
 * 
 * $Id$
 */
public class AsJcrNodeMethodTest {

    @Test
    public void testExec() throws RepositoryException {
        final AsJcrNodeMethod method = new AsJcrNodeMethod();
        List<TemplateModel> args = new ArrayList<TemplateModel>();
        AdapterTemplateModel mockTemplateModel = new AdapterTemplateModel() {

            public Object getAdaptedObject(Class unused) {
                return new MockContent("test");
            }
        };
        args.add(mockTemplateModel);


        try {
            Object result = method.exec(args);
            assertTrue(result instanceof MockJCRNode);
            assertEquals("test", ((MockJCRNode) result).getName());
        } catch (TemplateModelException e) {
            fail("Expected TemplateModelException");
        }

    }

    @Test
    public void testExecFailure() {
        final AsJcrNodeMethod method = new AsJcrNodeMethod();
        List<String> args = new ArrayList<String>();
        try {
            // empty args
            method.exec(args);
            fail("Expected TemplateModelException");
        } catch (TemplateModelException e) {
            assertTrue(true);
        }

        args.add("dummy1");
        args.add("dummy2");
        try {
            // more than one arg
            method.exec(args);
            fail("Expected TemplateModelException");
        } catch (TemplateModelException e) {
            assertTrue(true);
        }

        List<TemplateModel> singleArg = new ArrayList<TemplateModel>();

        final Content c = new MockContent("test");
        final TemplateModel model = new SimpleNumber(1);
        singleArg.add(model);

        try {
            method.exec(singleArg);
            fail("Expected TemplateModelException");
        } catch (TemplateModelException e) {
            assertTrue(true);
        }
    }
}
