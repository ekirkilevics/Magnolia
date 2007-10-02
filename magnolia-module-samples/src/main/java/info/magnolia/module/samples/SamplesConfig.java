/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.samples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class SamplesConfig {

    private static Logger log = LoggerFactory.getLogger(SamplesConfig.class);
    
    private Collection jsFiles = new ArrayList();
    
    private Collection cssFiles = new ArrayList();
    
    private Map columns = new HashMap();
    
    public Map getColumns() {
        return this.columns;
    }
    
    public void setColumns(Map columns) {
        this.columns = columns;
    }
    
    public void addColumn(String name, Collection paragraphs) {
        this.columns.put(name, paragraphs);
    }

   
    public Collection getCssFiles() {
        return this.cssFiles;
    }

    
    public void setCssFiles(Collection cssFiles) {
        this.cssFiles = cssFiles;
    }

    
    public Collection getJsFiles() {
        return this.jsFiles;
    }

    
    public void setJsFiles(Collection jsFiles) {
        this.jsFiles = jsFiles;
    }
    
}
