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
package info.magnolia.test;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.PropertiesInitializer;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public abstract class MgnlTestCase extends TestCase {

    private static Logger log = LoggerFactory.getLogger(MgnlTestCase.class);

    protected void setUp() throws Exception {
        super.setUp();
        // ignore mapping warnings
        org.apache.log4j.Logger.getLogger(ContentRepository.class).setLevel(Level.ERROR);

        FactoryUtil.clear();
        initDefaultImplementations();
        setMagnoliaProperties();
        initContext();
    }

    protected void initContext() {
        MockUtil.initMockContext();
    }

    protected void tearDown() throws Exception {
        FactoryUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    protected void setMagnoliaProperties() throws Exception {
        setMagnoliaProperties(getMagnoliaPropertiesStream());
    }

    protected void setMagnoliaProperties(InputStream propertiesStream) throws IOException {
        try {
            SystemProperty.getProperties().load(propertiesStream);
        } finally {
            IOUtils.closeQuietly(propertiesStream);
        }
    }

    protected InputStream getMagnoliaPropertiesStream() throws IOException {
        return this.getClass().getResourceAsStream(getMagnoliaPropertiesFileName());
    }

    protected String getMagnoliaPropertiesFileName() {
        return "/test-magnolia.properties";
    }

    protected void initDefaultImplementations() throws IOException {
        PropertiesInitializer.getInstance().loadBeanProperties();
        PropertiesInitializer.getInstance().loadAllModuleProperties();
    }

    protected MockHierarchyManager initMockConfigRepository(String properties) throws IOException, RepositoryException, UnsupportedRepositoryOperationException {

        MockHierarchyManager hm = MockUtil.createAndSetHierarchyManager(ContentRepository.CONFIG, properties);

        return hm;
    }

    /**
     * Utility assertion that will match a String against a regex,
     * <strong>with the DOTALL flag enabled, which means the . character will also matche new lines</strong>.
     */
    public static void assertMatches(String message, String s, String regex) {
        assertMatches(message, s, regex, Pattern.DOTALL);
    }

    /**
     * Utility assertion that will match a String against a regex.
     */
    public static void assertMatches(String message, String s, String regex, int flags) {
        final StringBuffer completeMessage = new StringBuffer();
        if (message!=null) {
            completeMessage.append(message).append(":\n");
        }
        completeMessage.append("Input:\n    ");
        completeMessage.append(s);
        completeMessage.append("did not match regex:\n    ");
        completeMessage.append(regex);
        assertTrue(completeMessage.toString(), Pattern.compile(regex, flags).matcher(s).matches());
    }
}
