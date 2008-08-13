/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.module.PropertyDefinition;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.model.ModuleDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pbracher
 * @Author fgiust
 */
public class PropertiesInitializer {
    /**
     * The properties file containing the bean default implementations
     */
    private static final String MGNL_BEANS_PROPERTIES = "/mgnl-beans.properties";

    /**
     * Placeholder prefix: "${"
     */
    public static final String PLACEHOLDER_PREFIX = "${";

    /**
     * Placeholder suffix: "}"
     */
    public static final String PLACEHOLDER_SUFFIX = "}";

    public static PropertiesInitializer getInstance() {
        return (PropertiesInitializer) FactoryUtil.getSingleton(PropertiesInitializer.class);
    }

    private static Logger log =  LoggerFactory.getLogger(PropertiesInitializer.class);
    /**
     * Default value for the MAGNOLIA_INITIALIZATION_FILE parameter.
     */
    public static final String DEFAULT_INITIALIZATION_PARAMETER = //
    "WEB-INF/config/${servername}/${webapp}/magnolia.properties," //$NON-NLS-1$
        + "WEB-INF/config/${servername}/magnolia.properties," //$NON-NLS-1$
        + "WEB-INF/config/${webapp}/magnolia.properties," //$NON-NLS-1$
        + "WEB-INF/config/default/magnolia.properties," //$NON-NLS-1$
        + "WEB-INF/config/magnolia.properties"; //$NON-NLS-1$

    public void loadAllProperties(String propertiesFilesString, String rootPath) {
        // load mgnl-beans.properties first
        loadBeanProperties();

        loadAllModuleProperties();

        // complete or override with WEB-INF properties files
        loadPropertiesFiles(propertiesFilesString, rootPath);

        // complete or override with JVM system properties
        overloadWithSystemProperties();

        // resolve nested properties
        resolveNestedProperties();
    }

    private void resolveNestedProperties() {

        Properties sysProps = SystemProperty.getProperties();

        for (Iterator it = sysProps.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            String oldValue = (String) sysProps.get(key);
            String value = parseStringValue(oldValue, new HashSet());
            SystemProperty.getProperties().put(key, value);
        }

    }

    public void loadAllModuleProperties() {
        // complete or override with modules' properties
        final ModuleManager moduleManager = ModuleManager.Factory.getInstance();
        try {
            final List moduleDefinitions = moduleManager.loadDefinitions();
            loadModuleProperties(moduleDefinitions);
        } catch (ModuleManagementException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * Load the properties defined in the module descriptors. They can get overridden later in the properties files in
     * WEB-INF
     */
    protected void loadModuleProperties(List moduleDefinitions) {
        final Iterator it = moduleDefinitions.iterator();
        while (it.hasNext()) {
            final ModuleDefinition module = (ModuleDefinition) it.next();
            final Iterator propsIt = module.getProperties().iterator();
            while (propsIt.hasNext()) {
                final PropertyDefinition property = (PropertyDefinition) propsIt.next();
                SystemProperty.setProperty(property.getName(), property.getValue());
            }
        }
    }

    public void loadPropertiesFiles(String propertiesLocationString, String rootPath) {

        String[] propertiesLocation = StringUtils.split(propertiesLocationString, ',');

        boolean found = false;
        // attempt to load each properties file at the given locations in reverse order: first files in the list override the later ones
        for (int j = propertiesLocation.length - 1; j >= 0; j--) {
            String location = StringUtils.trim(propertiesLocation[j]);

            if (loadPropertiesFile(rootPath, location)) {
                found = true;
            }
        }

        if (!found) {
            String msg = MessageFormat.format("No configuration found using location list {0}. Base path is [{1}]", new Object[]{ArrayUtils.toString(propertiesLocation), rootPath}); //$NON-NLS-1$
            log.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    public void loadBeanProperties() {
        InputStream mgnlbeansStream = getClass().getResourceAsStream(MGNL_BEANS_PROPERTIES);

        if (mgnlbeansStream != null) {
            Properties mgnlbeans = new Properties();
            try {
                mgnlbeans.load(mgnlbeansStream);
            }
            catch (IOException e) {
                log.error("Unable to load {} due to an IOException: {}", MGNL_BEANS_PROPERTIES, e.getMessage());
            }
            finally {
                IOUtils.closeQuietly(mgnlbeansStream);
            }

            for (Iterator iter = mgnlbeans.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                SystemProperty.setProperty(key, mgnlbeans.getProperty(key));
            }

        }
        else {
            log.warn("{} not found in the classpath. Check that all the needed implementation classes are defined in your custom magnolia.properties file.", MGNL_BEANS_PROPERTIES);
        }
    }

    /**
     * Try to load a magnolia.properties file
     * @param rootPath
     * @param location
     * @return
     */
    public boolean loadPropertiesFile(String rootPath, String location) {
        File initFile = new File(rootPath, location);

        if (!initFile.exists() || initFile.isDirectory()) {
            log.debug("Configuration file not found with path [{}]", initFile.getAbsolutePath()); //$NON-NLS-1$
            return false;
        }

        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(initFile);
        }
        catch (FileNotFoundException e1) {
            log.debug("Configuration file not found with path [{}]", initFile.getAbsolutePath());
            return false;
        }

        try {
            SystemProperty.getProperties().load(fileStream);
            log.info("Loading configuration at {}", initFile.getAbsolutePath());//$NON-NLS-1$
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        finally {
            IOUtils.closeQuietly(fileStream);
        }
        return true;
    }

    /**
     * Overload the properties with set system properties
     */
    public void overloadWithSystemProperties() {
        Iterator it = SystemProperty.getProperties().keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (System.getProperties().containsKey(key)) {
                log.info("system property found: {}", key);
                String value = System.getProperty(key);
                SystemProperty.setProperty(key, value);
            }
        }
    }

    public static String processPropertyFilesString(String servername, String webapp, String propertiesFilesString) {
        propertiesFilesString = StringUtils.replace(propertiesFilesString, "${servername}", servername); //$NON-NLS-1$
        propertiesFilesString = StringUtils.replace(propertiesFilesString, "${webapp}", webapp); //$NON-NLS-1$

        return propertiesFilesString;
    }

    /**
     * Parse the given String value recursively, to be able to resolve nested placeholders. Partly borrowed from
     * org.springframework.beans.factory.config.PropertyPlaceholderConfigurer (original author: Juergen Hoeller)
     */
    protected String parseStringValue(String strVal, Set visitedPlaceholders) {

        StringBuffer buf = new StringBuffer(strVal);

        int startIndex = strVal.indexOf(PLACEHOLDER_PREFIX);
        while (startIndex != -1) {
            int endIndex = -1;

            int index = startIndex + PLACEHOLDER_PREFIX.length();
            int withinNestedPlaceholder = 0;
            while (index < buf.length()) {
                if (PLACEHOLDER_SUFFIX.equals(buf.subSequence(index, index + PLACEHOLDER_SUFFIX.length()))) {
                    if (withinNestedPlaceholder > 0) {
                        withinNestedPlaceholder--;
                        index = index + PLACEHOLDER_SUFFIX.length();
                    }
                    else {
                        endIndex = index;
                        break;
                    }
                }
                else if (PLACEHOLDER_PREFIX.equals(buf.subSequence(index, index + PLACEHOLDER_PREFIX.length()))) {
                    withinNestedPlaceholder++;
                    index = index + PLACEHOLDER_PREFIX.length();
                }
                else {
                    index++;
                }
            }

            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                if (!visitedPlaceholders.add(placeholder)) {

                    log.warn("Circular reference detected in properties, \"{}\" is not resolvable", strVal);
                    return strVal;
                }
                // Recursive invocation, parsing placeholders contained in the placeholder key.
                placeholder = parseStringValue(placeholder, visitedPlaceholders);
                // Now obtain the value for the fully resolved key...
                String propVal = SystemProperty.getProperty(placeholder);
                if (propVal != null) {
                    // Recursive invocation, parsing placeholders contained in the
                    // previously resolved placeholder value.
                    propVal = parseStringValue(propVal, visitedPlaceholders);
                    buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
                    startIndex = buf.indexOf(PLACEHOLDER_PREFIX, startIndex + propVal.length());
                }
                else {
                    // Proceed with unprocessed value.
                    startIndex = buf.indexOf(PLACEHOLDER_PREFIX, endIndex + PLACEHOLDER_SUFFIX.length());
                }
                visitedPlaceholders.remove(placeholder);
            }
            else {
                startIndex = -1;
            }
        }

        return buf.toString();
    }

}
