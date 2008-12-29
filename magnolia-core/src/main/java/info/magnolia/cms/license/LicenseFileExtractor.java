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
package info.magnolia.cms.license;

import info.magnolia.cms.util.FactoryUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * User: sameercharles Date: Nov 5, 2003 Time: 4:18:59 PM
 * @author Sameer Charles
 * @version 1.1
 */
public class LicenseFileExtractor {
    private static final Logger log = LoggerFactory.getLogger(LicenseFileExtractor.class);

    public static final String VERSION_NUMBER = "VersionNumber"; //$NON-NLS-1$

    /**
     * Same as VERSION_NUMBER, but read from the manifest.
     */
    public static final String IMPLEMENTATION_VERSION = "ImplementationVersion"; //$NON-NLS-1$

    public static final String BUILD_NUMBER = "BuildNumber"; //$NON-NLS-1$

    public static final String PROVIDER = "Provider"; //$NON-NLS-1$

    public static final String PROVIDER_ADDRESS = "ProviderAddress"; //$NON-NLS-1$

    public static final String PROVIDER_EMAIL = "ProviderEmail"; //$NON-NLS-1$

    public static final String EDITION = "Edition"; //$NON-NLS-1$

    public static final String PRODUCT_DOMAIN = "ProductDomain"; //$NON-NLS-1$

    private static final String LICENSE_FILE_PATH = "info/magnolia/cms/license/license.xml"; //$NON-NLS-1$

    private static final String ELEMENT_META = "Meta"; //$NON-NLS-1$

    private static final String NOT_DEFINED = "Not Defined"; //$NON-NLS-1$

    private static final String OS_NAME = "OSName"; //$NON-NLS-1$

    private Map values;

    public static LicenseFileExtractor getInstance() {
        return (LicenseFileExtractor) FactoryUtil.getSingleton(LicenseFileExtractor.class);
    }

    public String get(String id) {
        if (values.containsKey(id)) {
            return (String) values.get(id);
        }
        return NOT_DEFINED;
    }

    public Map getEntries() {
        return values;
    }

    public String getOSName() {
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        return osName.replaceAll(" ", "-"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void init() {
        InputStream in = getClass().getClassLoader().getResourceAsStream(LICENSE_FILE_PATH);
        this.init(in);
    }

    public void init(InputStream in) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(in);
            this.load(document);
        }
        catch (Exception e) {
            log.error("failed to load license information"); //$NON-NLS-1$
            log.error(e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Returns the current magnolia version, read from the jar Manifest. This will return null if classes are not inside
     * a jar.
     * @return implementation version, read from the manifest file.
     */
    public String getVersionFromManifest() {
        Package p = this.getClass().getPackage();
        if (p != null) {
            return StringUtils.defaultString(p.getImplementationVersion());
        }
        return StringUtils.EMPTY;
    }

    /**
     * load meta element
     */
    private void load(Document document) {
        Element metaElement = document.getRootElement().getChild(ELEMENT_META);

        List elements = metaElement.getChildren();

        values = new HashMap(10);
        Iterator iterator = elements.iterator();
        while (iterator.hasNext()) {
            Element element = (Element) iterator.next();
            values.put(element.getName(), element.getText());
        }

        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        values.put(OS_NAME, osName.replaceAll(" ", "-")); //$NON-NLS-1$ //$NON-NLS-2$

        values.put(IMPLEMENTATION_VERSION, getVersionFromManifest());

    }

    /**
     * Print version info to System.out
     */
    public void printVersionInfo() {
        System.out.println("---------------------------------------------"); //$NON-NLS-1$
        System.out.println("MAGNOLIA LICENSE"); //$NON-NLS-1$
        System.out.println("---------------------------------------------"); //$NON-NLS-1$
        System.out.println("Version number : " + get(LicenseFileExtractor.VERSION_NUMBER)); //$NON-NLS-1$
        System.out.println("Build          : " + get(LicenseFileExtractor.BUILD_NUMBER)); //$NON-NLS-1$
        System.out.println("Edition        : " + get(LicenseFileExtractor.EDITION)); //$NON-NLS-1$
        System.out.println("Provider       : " + get(LicenseFileExtractor.PROVIDER) + " (" + get(LicenseFileExtractor.PROVIDER_EMAIL) + ")"); //$NON-NLS-1$ $NON-NLS-3$ $NON-NLS-5$
    }
}
