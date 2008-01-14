/**
 * This file Copyright (c) 2007-2008 Magnolia International
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
package info.magnolia.module.samples.jsp;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.samples.SamplesConfig;
import info.magnolia.module.samples.SamplesConfig.ParagraphCollection;
import info.magnolia.module.samples.SamplesConfig.ParagraphReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * {@link SamplesModuleConfigHelper} is a convenience class that wraps the {@link SamplesConfig} module configuration object.
 * 
 * 
 * @author vsteller
 * @version $Id$
 *
 */
public class SamplesModuleConfigHelper {

    private final SamplesConfig samplesConfig;
    
    public SamplesModuleConfigHelper() {
        samplesConfig = (SamplesConfig) ModuleRegistry.Factory.getInstance().getModuleInstance("samples");
    }

    public Collection getCssFiles() {
        return samplesConfig.getCssFiles();
    }

    public Collection getJsFiles() {
        return samplesConfig.getJsFiles();
    }

    public Map getParagraphCollections() {
        return samplesConfig.getParagraphCollections();
    }
    
    /**
     * Joins all configured paragraph names for the given paragraphCollectionName into a comma-separated list, 
     * which can be used e.g. for the paragraph attribute in a <cms:newBar/> tag.
     *
     * Example:
     * <pre>
     *  &lt;jsp:useBean id="module" class="info.magnolia.module.samples.jsp.SamplesModuleConfigHelper" scope="request"/&gt;
     *  &lt;cms:newBar contentNodeCollectionName="mainColumnParagraphs" paragraph="${module.paragraphs.mainColumn}" /&gt;
     * </pre>
     * 
     * @param paragraphCollectionName
     * @return
     */
    public String listParagraphs(String paragraphCollectionName) {
        final StringBuffer paragraphList = new StringBuffer();
        final Map paragraphCollections = samplesConfig.getParagraphCollections();
        
        if (paragraphCollections.containsKey(paragraphCollectionName)) {
            final ParagraphCollection paragraphCollection = (ParagraphCollection) paragraphCollections.get(paragraphCollectionName);
            final Collection paragraphs = paragraphCollection.getParagraphs();
            
            for (final Iterator paragraphsIterator = paragraphs.iterator(); paragraphsIterator.hasNext();) {
                final ParagraphReference paragraph = (ParagraphReference) paragraphsIterator.next();
                paragraphList.append(paragraph.getName());
                if (paragraphsIterator.hasNext()) {
                    paragraphList.append(",");
                }
            }
            return paragraphList.toString();
        }
        return StringUtils.EMPTY;
    }
    
    public Map getParagraphs() {
        final HashMap paragraphs = new HashMap();
        final Iterator keyIterator = getParagraphCollections().keySet().iterator();
        
        while (keyIterator.hasNext()) {
            final String paragraphCollectionName = (String) keyIterator.next();
            paragraphs.put(paragraphCollectionName, listParagraphs(paragraphCollectionName));
        }
        
        return paragraphs;
    }
}
