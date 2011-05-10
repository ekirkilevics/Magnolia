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
package info.magnolia.module.exchangesimple;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for all information about activated (to be) content.
 * @author Sameer Charles $Id$
 */
public class ActivationContent implements Cloneable {

    private static final Logger log = LoggerFactory.getLogger(ActivationContent.class);
    /**
     * Collection of files to be transfered during activation.
     */
    private Map<String, File> fileList = new HashMap<String, File>();

    /**
     * Collection of properties describing activated content (path, repo, etc). Supported property keys are listed in {@link BaseSyndicatorImpl}.
     */
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Adds resource to the list of files for transfer.
     */
    public void addFile(String resourceId, File file) {
        this.fileList.put(resourceId, file);
    }

    public File getFile(String resourceId) {
        return this.fileList.get(resourceId);
    }

    public void removeFile(String resourceId) {
        this.fileList.remove(resourceId);
    }

    /**
     * Cats collection of all files. This collection is not a copy, but a reference to internal collection!
     */
    public Map<String, File> getFiles() {
        return this.fileList;
    }

    /**
     * Adds property to the list of properties. Null values are automatically converted to empty strings. If the key already exists, existing value will be replaced with the one provided to this method.
     */
    public void addProperty(String key, String value) {
        if (value == null) {
            value = "";
        }
        this.properties.put(key, value);
    }

    /**
     * @see #setProperty(String, String)
     */
    public void setProperty(String key, String value) {
        if (value == null) {
            value = "";
        }
        // HashMap replaces existing value on put
        this.properties.put(key, value);
    }

    /**
     * Gets value of property with specified key or null if such property was not set.
     */
    public String getproperty(String key) {
        return this.properties.get(key);
    }

    public void removeProperty(String key) {
        this.properties.remove(key);
    }

    /**
     * Gets collection of all properties. Such collection is not a copy, but the reference to internal collection!
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public Object clone() {
        try {
            ActivationContent clone = (ActivationContent) super.clone();
            // need to clone maps otherwise cloned object would reference the original ones
            clone.properties = new HashMap<String, String>(this.properties);
            clone.fileList = new HashMap<String, File>(this.fileList);
            return clone;
        } catch (CloneNotSupportedException e) {
            // should never be thrown since we support cloning.
            log.error("Failed to clone itself with " + e.getLocalizedMessage(), e);
            return null;
        }
    }
}
