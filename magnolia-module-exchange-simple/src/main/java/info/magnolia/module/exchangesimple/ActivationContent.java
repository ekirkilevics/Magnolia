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
package info.magnolia.module.exchangesimple;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class holds all content needed to be activated
 * @author Sameer Charles $Id$
 */
public class ActivationContent implements Cloneable {

    private static final Logger log = LoggerFactory.getLogger(ActivationContent.class);
    /**
     * File list
     */
    private Map fileList = new HashMap();

    /**
     * properties
     */
    private Map properties = new HashMap();

    /**
     * add file
     * @param resourceId
     * @param file
     */
    public void addFile(String resourceId, File file) {
        this.fileList.put(resourceId, file);
    }

    /**
     * get file
     * @param resourceId
     * @return file
     */
    public File getFile(String resourceId) {
        return (File) this.fileList.get(resourceId);
    }

    /**
     * remove file
     * @param resourceId
     */
    public void removeFile(String resourceId) {
        this.fileList.remove(resourceId);
    }

    /**
     * get all files
     * @return file list
     */
    public Map getFiles() {
        return this.fileList;
    }

    /**
     * add property
     * @param key
     * @param value
     */
    public void addProperty(String key, String value) {
        if (value == null) {
            value = "";
        }
        this.properties.put(key, value);
    }

    /**
     * set property
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        if (value == null) {
            value = "";
        }
        // HashMap replaces existing value on put
        this.properties.put(key, value);
    }

    /**
     * get property
     * @param key
     * @return property value
     */
    public String getproperty(String key) {
        return (String) this.properties.get(key);
    }

    /**
     * remove property
     * @param key
     */
    public void removeProperty(String key) {
        this.properties.remove(key);
    }

    /**
     * get property list
     * @return all properties
     */
    public Map getProperties() {
        return this.properties;
    }

    public Object clone() {
        try {
            ActivationContent clone = (ActivationContent) super.clone();
            // need to clone maps otherwise cloned object would reference the original ones
            clone.properties = new HashMap(this.properties);
            clone.fileList = new HashMap(this.fileList);
            return clone;
        } catch (CloneNotSupportedException e) {
            // should never be thrown since we support cloning.
            log.error("Failed to clone itself with " + e.getLocalizedMessage(), e);
            return null;
        }
    }
}
