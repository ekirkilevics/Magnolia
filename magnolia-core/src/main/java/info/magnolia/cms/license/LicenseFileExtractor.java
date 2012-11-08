/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.license;

import info.magnolia.objectfactory.Components;
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
import javax.inject.Singleton;


/**
 * Extracts license information from the info/magnolia/cms/license/license.xml file.
 *
 * @version $Id$
 */
@Singleton
public class LicenseFileExtractor {
    private static final Logger log = LoggerFactory.getLogger(LicenseFileExtractor.class);

    public static final String VERSION_NUMBER = "VersionNumber";

    /**
     * Same as VERSION_NUMBER, but read from the manifest.
     */
    public static final String IMPLEMENTATION_VERSION = "ImplementationVersion";

    public static final String BUILD_NUMBER = "BuildNumber";

    public static final String PROVIDER = "Provider";

    public static final String PROVIDER_ADDRESS = "ProviderAddress";

    public static final String PROVIDER_EMAIL = "ProviderEmail";

    public static final String EDITION = "Edition";

    public static final String PRODUCT_DOMAIN = "ProductDomain";

    private static final String LICENSE_FILE_PATH = "info/magnolia/cms/license/license.xml";

    private static final String ELEMENT_META = "Meta";

    private static final String NOT_DEFINED = "Not Defined";

    private static final String OS_NAME = "OSName";

    private Map<String, String> values = new HashMap<String, String> ();

    public static LicenseFileExtractor getInstance() {
        return Components.getComponent(LicenseFileExtractor.class);
    }

    public String get(String id) {
        if (values.containsKey(id)) {
            return values.get(id);
        }
        return NOT_DEFINED;
    }

    public Map<String, String> getEntries() {
        return values;
    }

    public String getOSName() {
        String osName = System.getProperty("os.name");
        return osName.replaceAll(" ", "-");
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
            log.error("failed to load license information");
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
     * Load meta element.
     */
    private void load(Document document) {
        Element metaElement = document.getRootElement().getChild(ELEMENT_META);

        @SuppressWarnings("unchecked")
        List<Element> elements = metaElement.getChildren();

        values = new HashMap<String,String>(10);
        Iterator<Element> iterator = elements.iterator();
        while (iterator.hasNext()) {
            Element element = iterator.next();
            values.put(element.getName(), element.getText());
        }

        String osName = System.getProperty("os.name");
        values.put(OS_NAME, osName.replaceAll(" ", "-"));

        values.put(IMPLEMENTATION_VERSION, getVersionFromManifest());

    }

    /**
     * Print version info to System.out.
     */
    public void printVersionInfo() {
        System.out.println("---------------------------------------------");
        System.out.println("MAGNOLIA LICENSE");
        System.out.println("---------------------------------------------");
        System.out.println("Version number : " + get(LicenseFileExtractor.VERSION_NUMBER));
        System.out.println("Build          : " + get(LicenseFileExtractor.BUILD_NUMBER));
        System.out.println("Edition        : " + get(LicenseFileExtractor.EDITION));
        System.out.println("Provider       : " + get(LicenseFileExtractor.PROVIDER) + " (" + get(LicenseFileExtractor.PROVIDER_EMAIL) + ")");
    }
}
