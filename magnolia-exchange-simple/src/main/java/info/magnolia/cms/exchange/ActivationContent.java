/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.exchange;

import java.io.File;
import java.util.Map;
import java.util.HashMap;


/**
 * This class holds all content needed to be activated
 * @author Sameer Charles $Id$
 */
public class ActivationContent {

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
}
