/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.test.MgnlTagTestCase;
import static org.easymock.classextension.EasyMock.*;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PoweredByTagTest extends MgnlTagTestCase {
    protected void setUp() throws Exception {
        super.setUp();

        final LicenseFileExtractor licenseExtractor = createStrictMock(LicenseFileExtractor.class);
        expect(licenseExtractor.get(LicenseFileExtractor.EDITION)).andReturn("Test Edition");
        expect(licenseExtractor.get(LicenseFileExtractor.VERSION_NUMBER)).andReturn("7.5.3");
        expect(licenseExtractor.get(LicenseFileExtractor.BUILD_NUMBER)).andReturn("30. July 2008");
        expect(licenseExtractor.get(LicenseFileExtractor.PRODUCT_DOMAIN)).andReturn("www.magnolia-cms.com");
        expect(licenseExtractor.get(LicenseFileExtractor.PROVIDER)).andReturn("Magnolia International");
        expect(licenseExtractor.get(LicenseFileExtractor.PROVIDER_ADDRESS)).andReturn("Maiengasse 30 - 4054 Basel - Switzerland");
        expect(licenseExtractor.get(LicenseFileExtractor.PROVIDER_EMAIL)).andReturn("info@magnolia-cms.com");
        replay(licenseExtractor);

        FactoryUtil.setInstance(LicenseFileExtractor.class, licenseExtractor);
    }

    public void testShouldBeUseableWithoutAnyAttribute() throws Exception {
        final PoweredByTag tag = new PoweredByTag();
        tag.setJspContext(pageContext);
        tag.doTag();

        assertJspContent("Powered by <a href=\"http://www.magnolia-cms.com\">Magnolia</a> Test Edition 7.5.3.");
    }

    public void testCanUseACustomPatternWithDifferentParameters() throws Exception {
        final PoweredByTag tag = new PoweredByTag();
        tag.setPattern("I''m using version {1} of the {0} of Magnolia which was built on {2} by {4}. These guys have their offices at {5}, and can be reached by email at {6}. Their wonderful website is at http://{3} !");
        tag.setJspContext(pageContext);
        tag.doTag();

        assertJspContent("I'm using version 7.5.3 of the Test Edition of Magnolia which was built on 30. July 2008 by Magnolia International. These guys have their offices at Maiengasse 30 - 4054 Basel - Switzerland, and can be reached by email at info@magnolia-cms.com. Their wonderful website is at http://www.magnolia-cms.com !");
    }

    protected HierarchyManager initWebsiteData() throws IOException, RepositoryException {
        return null;
    }
}
