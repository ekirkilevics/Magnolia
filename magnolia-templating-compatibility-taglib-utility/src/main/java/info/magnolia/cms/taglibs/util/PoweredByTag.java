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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.license.LicenseFileExtractor;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

/**
 * A simple tag which can display the version and edition of the running Magnolia instance.
 * @jsp.tag name="poweredBy" body-content="empty"
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PoweredByTag extends SimpleTagSupport {
    private String pattern = "Powered by <a href=\"http://{3}\">Magnolia</a> {0} {1}.";

    /**
     * Sets a different message pattern than the default. This uses the java.text.MessageFormat syntax.
     * Available parameters are:
     * {0} Magnolia edition
     * {1} Magnolia version
     * {2} Magnolia build number or date
     * {3} Magnolia product url
     * {4} Magnolia provider name
     * {5} Magnolia provider address
     * {6} Magnolia provider email
     *
     * <strong>Warning: since this is using java.text.MessageFormat, you need to escape single quotes, for example:
     * "I''m showing my Magnolia love by using the {0} version {1}."</strong>
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void doTag() throws JspException, IOException {
        final Writer out = getJspContext().getOut();
        final LicenseFileExtractor license = LicenseFileExtractor.getInstance();
        final String[] licenseValues = new String[] {
                license.get(LicenseFileExtractor.EDITION),
                license.get(LicenseFileExtractor.VERSION_NUMBER),
                license.get(LicenseFileExtractor.BUILD_NUMBER),
                license.get(LicenseFileExtractor.PRODUCT_DOMAIN),
                license.get(LicenseFileExtractor.PROVIDER),
                license.get(LicenseFileExtractor.PROVIDER_ADDRESS),
                license.get(LicenseFileExtractor.PROVIDER_EMAIL),
        };

        final String message = MessageFormat.format(pattern, licenseValues);
        out.write(message);
    }
}
