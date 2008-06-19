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
package info.magnolia.cms.util;

import info.magnolia.cms.core.SystemProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Util used to process config files
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public class ConfigUtil {
    private static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    /**
     * EntityResolver using a Map to resources
     * @author Philipp Bracher
     * @version $Id$
     *
     */
    public static final class MapDTDEntityResolver implements EntityResolver {

        private final Map dtds;

        public MapDTDEntityResolver(Map dtds) {
            this.dtds = dtds;
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            String key = StringUtils.substringAfterLast(systemId, "/");
            if (dtds.containsKey(key)) {
                Class clazz = getClass();
                InputStream in = clazz.getResourceAsStream((String)dtds.get(key));
                if (in == null) {
                    log.error("Could not find [" + systemId + "]. Used ["
                        + clazz.getClassLoader()
                        + "] class loader in the search.");
                    return null;
                }
                else {
                    return new InputSource(in);
                }
            }
            else {
                return null;
            }
        }
    }

    /**
     * Try to get the file. Get it first in the file system, then from the resources
     * @param fileName
     * @return the input stream
     */
    public static InputStream getConfigFile(String fileName) {
        File file = new File(SystemProperty.getProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR), fileName);
        InputStream stream;
        // relative to webapp root
        if (file.exists()) {
            URL url;
            try {
                url = new URL("file:" + file.getAbsolutePath()); //$NON-NLS-1$
            }
            catch (MalformedURLException e) {
                log.error("Unable to read config file from [" //$NON-NLS-1$
                    + fileName
                    + "], got a MalformedURLException: " //$NON-NLS-1$
                    , e);
                return null;
            }
            try {
                stream = url.openStream();
            }
            catch (IOException e) {
                log.error("Unable to read config file from [" //$NON-NLS-1$
                    + fileName
                    + "], got a IOException " //$NON-NLS-1$
                    ,e);
                return null;
            }
            return stream;
        }

        // try it directly
        file = new File(fileName);
        if(file.exists()) {
            try {
                stream = new FileInputStream(file);
            }
            catch (FileNotFoundException e) {
                log.error("Unable to read config file from [" //$NON-NLS-1$
                    + fileName
                    + "], got a FileNotFoundException " //$NON-NLS-1$
                    , e);
                return null;
            }
            return stream;
        }
        // try resources
        try {
            return ClasspathResourcesUtil.getStream(fileName);
        } catch (IOException e) {
            log.error("Unable to read config file from the resources [" //$NON-NLS-1$
                    + fileName
                    + "]" //$NON-NLS-1$
                    , e);
        }

        return null;
    }

    /**
     * Read the stream and replace tokens
     * @param stream
     * @return the string with replaced tokens
     * @throws IOException
     */
    public static String replaceTokens(InputStream stream) throws IOException {
        String config;
        config = IOUtils.toString(stream);
        IOUtils.closeQuietly(stream);
        return replaceTokens(config);
    }

    /**
     * Replace tokens in a string
     * @param config
     * @return
     * @throws IOException
     */
    public static String replaceTokens(String config) throws IOException {
        for (Iterator iter = SystemProperty.getProperties().keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            config = StringUtils.replace(config, "${" + key + "}", SystemProperty.getProperty(key, ""));
        }
        return config;
    }

    /**
     * Convert the string to an DOM Document
     * @param xml
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document string2DOM(String xml) throws ParserConfigurationException, SAXException, IOException {
        return string2DOM(xml, Collections.EMPTY_MAP);
    }

    /**
     * Convert the string to a JDOM Document
     * @param xml
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static org.jdom.Document string2JDOM(String xml) throws JDOMException, IOException{
        return string2JDOM(xml, Collections.EMPTY_MAP);
    }

    /**
     * Uses a map to find the dtds in the resources
     * @param xml
     * @param dtds
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static org.jdom.Document string2JDOM(String xml, final Map dtds) throws JDOMException, IOException{
        SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(new MapDTDEntityResolver(dtds));
        return builder.build(IOUtils.toInputStream(xml));
    }

    /**
     * Uses a map to find dtds in the resources
     * @param xml
     * @param dtds
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document string2DOM(String xml, final Map dtds) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        DocumentBuilder builder;
        builder = dbf.newDocumentBuilder();
        builder.setEntityResolver(new MapDTDEntityResolver(dtds));
        return builder.parse(IOUtils.toInputStream(xml));
    }

}
