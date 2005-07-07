/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.license;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;


/**
 * User: sameercharles Date: Nov 5, 2003 Time: 4:18:59 PM
 * @author Sameer Charles
 * @version 1.1
 */
public final class LicenseFileExtractor {

    public static final String VERSION_NUMBER = "VersionNumber"; //$NON-NLS-1$

    public static final String BUILD_NUMBER = "BuildNumber"; //$NON-NLS-1$

    public static final String PROVIDER = "Provider"; //$NON-NLS-1$

    public static final String PROVIDER_ADDRESS = "ProviderAddress"; //$NON-NLS-1$

    public static final String PRIVIDER_EMAIL = "ProviderEmail"; //$NON-NLS-1$

    public static final String PRODUCT_DOMAIN = "ProductDomain"; //$NON-NLS-1$

    public static final String VERSION_PAGE_HANDLE = "VersionPageHandle"; //$NON-NLS-1$

    private static final String LICENSE_FILE_PATH = "info/magnolia/cms/license/license.xml"; //$NON-NLS-1$

    private static final String ELEMENT_META = "Meta"; //$NON-NLS-1$

    private static final String NOT_DEFINED = "Not Defined"; //$NON-NLS-1$

    private static final String OS_NAME = "OSName"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(LicenseFileExtractor.class);

    private static LicenseFileExtractor license = new LicenseFileExtractor();

    private static Map values;

    public static LicenseFileExtractor getInstance() {
        return license;
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
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
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
    }
}
