/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.module.samples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
    
    private Map paragraphCollections = new HashMap();
    
    public Map getParagraphCollections() {
        return this.paragraphCollections;
    }
    
    public void setParagraphCollections(Map paragraphCollections) {
        this.paragraphCollections = paragraphCollections;
    }
    
    public void addParagraphCollection(String name, ParagraphCollection paragraphCollection) {
        this.paragraphCollections.put(name, paragraphCollection);
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
    
    public static class ParagraphCollection {
        private Collection paragraphs;
        
        
        public ParagraphCollection() {
            this.paragraphs = new HashSet();
        }
        
        public Collection getParagraphs() {
            return paragraphs;
        }
        
        public void setParagraphs(Collection paragraphs) {
            this.paragraphs = paragraphs;
        }
        
        public void addParagraph(ParagraphReference paragraph) {
            this.paragraphs.add(paragraph);
        }
    }
    
    public static class ParagraphReference {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}
