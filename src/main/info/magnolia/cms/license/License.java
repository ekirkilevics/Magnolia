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
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;


/**
 * User: sameercharles Date: Nov 5, 2003 Time: 4:18:59 PM
 * @author Sameer Charles
 * @version 1.1
 */
public final class License {

    private static Logger log = Logger.getLogger(License.class);

    private static final String LICENSE_FILE_PATH = "info/magnolia/cms/license/license.xml";

    public static final String VERSION_NUMBER = "VersionNumber";

    public static final String BUILD_NUMBER = "BuildNumber";

    public static final String PROVIDER = "Provider";

    public static final String PROVIDER_ADDRESS = "ProviderAddress";

    public static final String PRIVIDER_EMAIL = "ProviderEmail";

    public static final String PRODUCT_DOMAIN = "ProductDomain";

    public static final String VERSION_PAGE_HANDLE = "VersionPageHandle";

    private static final String ELEMENT_META = "Meta";

    private static final String NOT_DEFINED = "Not Defined";

    private static License license = new License();

    private static Element metaElement;

    public static License getInstance() {
        return license;
    }

    public String get(String id) {
        Element e = metaElement.getChild(id);
        if (e != null) {
            return e.getText();
        }
        return NOT_DEFINED;
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
            Document document = this.buildDocument(in);
            this.load(document);
        }
        catch (Exception e) {
            log.error("failed to load license information");
            log.error(e.getMessage(), e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    /**
     * <p>
     * builds JDOM document
     * </p>
     */
    private Document buildDocument(InputStream in) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        return builder.build(in);
    }

    /**
     * load meta element
     */
    private void load(Document document) {
        Element root = document.getRootElement();
        metaElement = root.getChild(ELEMENT_META);
    }
}
